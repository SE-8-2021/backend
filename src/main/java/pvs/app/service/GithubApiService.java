package pvs.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GithubIssueDTO;
import pvs.app.dto.GithubPullRequestDTO;
import pvs.app.service.thread.GithubCommitLoaderThread;
import pvs.app.service.thread.GithubIssueLoaderThread;
import pvs.app.service.thread.GithubPullRequestLoaderThread;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@SuppressWarnings("squid:S1192")
public class GithubApiService {

    private final WebClient webClient;
    private final GithubCommitService githubCommitService;
    private Map<String, Object> graphQlQuery;

    public GithubApiService(WebClient.Builder webClientBuilder, @Value("${webClient.baseUrl.github}") String baseUrl, GithubCommitService githubCommitService) {
        String token = System.getenv("PVS_GITHUB_TOKEN");
        this.githubCommitService = githubCommitService;
        this.webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    private String dateToISO8601(Date date) {
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sdf.format(date);
    }

    private void setGraphQlGetCommitsTotalCountAndCursorQuery(String owner, String name, Date lastUpdate) {
        String since = dateToISO8601(lastUpdate);
        Map<String, Object> graphQl = new HashMap<>();
        graphQl.put("query", "{repository(owner: \"" + owner + "\", name:\"" + name + "\") {" +
                "defaultBranchRef {" +
                "target {" +
                "... on Commit {" +
                "history (since: \"" + since + "\") {" +
                "totalCount\n" +
                "pageInfo {" +
                "startCursor" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}}");
        this.graphQlQuery = graphQl;
    }

    private void setGraphQlGetIssuesTotalCountQuery(String owner, String name) {
        Map<String, Object> graphQl = new HashMap<>();
        graphQl.put("query", "{repository(owner: \"" + owner + "\", name:\"" + name + "\") {" +
                "issues (first: 100) {" +
                "totalCount" +
                "}" +
                "}}");
        this.graphQlQuery = graphQl;
    }

    private void setGraphQlGetPullRequestQuery(String owner, String name) {
        Map<String, Object> graphQl = new HashMap<>();
        graphQl.put("query", "{repository(owner: \"" + owner + "\", name:\"" + name + "\") {" +
                "pullRequests (first: 100) {" +
                "totalCount\n" +
                "edges {" +
                "node {" +
                "author {" +
                "login\n" +
                "}" +
                "createdAt\n" +
                "closedAt\n" +
                "mergedAt\n" +
                "}" +
                "}" +
                "}" +
                "}}");
        this.graphQlQuery = graphQl;
    }

    private void setGraphQlGetAvatarQuery(String owner) {
        Map<String, Object> graphQl = new HashMap<>();
        graphQl.put("query", "{search(type: USER, query: \"in:username " + owner + "\", first: 1) {" +
                "edges {" +
                "node {" +
                "... on User {" +
                "avatarUrl" +
                "}" +
                "... on Organization {" +
                "avatarUrl" +
                "}" +
                "}" +
                "}}}");

        this.graphQlQuery = graphQl;
    }

    public boolean getCommitsFromGithub(String owner, String name, Date lastUpdate) throws InterruptedException, IOException {
        this.setGraphQlGetCommitsTotalCountAndCursorQuery(owner, name, lastUpdate);

        String responseJson = Objects.requireNonNull(this.webClient.post()
                        .body(BodyInserters.fromObject(this.graphQlQuery))
                        .exchange()
                        .block())
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();

        Optional<JsonNode> paginationInfo = Optional.ofNullable(mapper.readTree(responseJson))
                .map(resp -> resp.get("data"))
                .map(data -> data.get("repository"))
                .map(repo -> repo.get("defaultBranchRef"))
                .map(branch -> branch.get("target"))
                .map(tag -> tag.get("history"));

        if (paginationInfo.isPresent()) {
            double totalCount = paginationInfo.get().get("totalCount").asInt();
            List<GithubCommitLoaderThread> githubCommitLoaderThreadList = new ArrayList<>();

            if (totalCount != 0) {
                String cursor = paginationInfo.get().get("pageInfo").get("startCursor").textValue()
                        .split(" ")[0];
                for (int i = 1; i <= Math.ceil(totalCount / 100); i++) {
                    GithubCommitLoaderThread githubCommitLoaderThread =
                            new GithubCommitLoaderThread(
                                    this.webClient,
                                    this.githubCommitService,
                                    owner,
                                    name,
                                    cursor + " " + (i * 100));
                    githubCommitLoaderThreadList.add(githubCommitLoaderThread);
                    githubCommitLoaderThread.start();
                }

                for (GithubCommitLoaderThread thread : githubCommitLoaderThreadList) {
                    thread.join();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public List<GithubIssueDTO> getIssuesFromGithub(String owner, String name) throws IOException, InterruptedException {
        List<GithubIssueDTO> githubIssueDTOList = new ArrayList<>();
        this.setGraphQlGetIssuesTotalCountQuery(owner, name);

        String responseJson = Objects.requireNonNull(this.webClient.post()
                        .body(BodyInserters.fromObject(this.graphQlQuery))
                        .exchange()
                        .block())
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();

        Optional<JsonNode> paginationInfo = Optional.ofNullable(mapper.readTree(responseJson))
                .map(resp -> resp.get("data"))
                .map(data -> data.get("repository"))
                .map(repo -> repo.get("issues"));

        if (paginationInfo.isPresent()) {
            double totalCount = paginationInfo.get().get("totalCount").asInt();
            List<GithubIssueLoaderThread> githubIssueLoaderThreadList = new ArrayList<>();

            if (0 != totalCount) {
                for (int i = 1; i <= Math.ceil(totalCount / 100); i++) {
                    GithubIssueLoaderThread githubIssueLoaderThread =
                            new GithubIssueLoaderThread(
                                    githubIssueDTOList,
                                    owner,
                                    name,
                                    i);
                    githubIssueLoaderThreadList.add(githubIssueLoaderThread);
                    githubIssueLoaderThread.start();
                }

                for (GithubIssueLoaderThread thread : githubIssueLoaderThreadList) {
                    thread.join();
                }
            }
        } else {
            return null;
        }
        return githubIssueDTOList;
    }

    public List<GithubPullRequestDTO> getPullRequestMetricsFromGithub(String owner, String name) throws IOException, InterruptedException {
        List<GithubPullRequestDTO> githubPullRequestDTOs = new ArrayList<>();
        this.setGraphQlGetPullRequestQuery(owner, name);

        String responseJson = Objects.requireNonNull(this.webClient.post()
                        .body(BodyInserters.fromObject(this.graphQlQuery))
                        .exchange()
                        .block())
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();

        Optional<JsonNode> paginationInfo = Optional.ofNullable(mapper.readTree(responseJson))
                .map(resp -> resp.get("data"))
                .map(data -> data.get("repository"))
                .map(repo -> repo.get("pullRequests"));

        if (paginationInfo.isPresent()) {
            int totalCount = paginationInfo.get().get("totalCount").asInt();
            List<GithubPullRequestLoaderThread> githubPullRequestLoaderThreadList = new ArrayList<>();

            if (0 != totalCount) {
                Optional<JsonNode> requestNode = paginationInfo.map(request -> request.get("edges"));
                if (requestNode.isPresent()) {
                    for (JsonNode objNode: requestNode.get()) {
                        GithubPullRequestLoaderThread githubPullRequestLoaderThread =
                                new GithubPullRequestLoaderThread(
                                        githubPullRequestDTOs,
                                        owner,
                                        name,
                                        objNode);
                        githubPullRequestLoaderThreadList.add(githubPullRequestLoaderThread);
                        githubPullRequestLoaderThread.start();
                    }

                    for (GithubPullRequestLoaderThread thread : githubPullRequestLoaderThreadList) {
                        thread.join();
                    }
                }
            }
        } else {
            return null;
        }
        return githubPullRequestDTOs;
    }

    public JsonNode getAvatarURL(String owner) throws IOException {
        this.setGraphQlGetAvatarQuery(owner);
        String responseJson = Objects.requireNonNull(this.webClient.post()
                        .body(BodyInserters.fromObject(this.graphQlQuery))
                        .exchange()
                        .block())
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        Optional<JsonNode> avatar = Optional.ofNullable(mapper.readTree(responseJson))
                .map(resp -> resp.get("data"))
                .map(data -> data.get("search"))
                .map(search -> search.get("edges").get(0))
                .map(edges -> edges.get("node"))
                .map(node -> node.get("avatarUrl"));

        return avatar.orElse(null);
    }
}

