package pvs.app.service.thread;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.CommitDTO;
import pvs.app.service.GithubCommitService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GithubCommitLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final GithubCommitService githubCommitService;
    private final String repoOwner;
    private final String repoName;
    private final String branchName;
    private final String cursor;
    private final WebClient webClient;

    public GithubCommitLoaderThread(WebClient webClient, GithubCommitService githubCommitService, String repoOwner, String repoName, String branchName, String cursor) {
        this.githubCommitService = githubCommitService;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.branchName = branchName;
        this.cursor = cursor;
        this.webClient = webClient;
    }

    @Override
    public void run() {
        Map<String, Object> graphQlQuery = new HashMap<>();
        graphQlQuery.put("query", "{repository(owner: \"" + this.repoOwner + "\", name:\"" + this.repoName + "\") {" +
                "ref(qualifiedName: \"" + this.branchName + "\") {" +
                "target {" +
                "... on Commit {" +
                "history (last:30, before: \"" + this.cursor + "\") {" +
                "nodes {" +
                "committedDate\n" +
                "additions\n" +
                "deletions\n" +
                "changedFiles\n" +
                "author {" +
                "email\n" +
                "name\n" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}" +
                "}}");

        String responseJson = Objects.requireNonNull(this.webClient.post()
                        .body(BodyInserters.fromObject(graphQlQuery))
                        .exchange()
                        .block())
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();

        try {
            Optional<JsonNode> commits = Optional.ofNullable(mapper.readTree(responseJson))
                    .map(resp -> resp.get("data"))
                    .map(data -> data.get("repository"))
                    .map(repo -> repo.get("ref"))
                    .map(branch -> branch.get("target"))
                    .map(tag -> tag.get("history"))
                    .map(hist -> hist.get("nodes"));

            commits.ifPresent(jsonNode -> jsonNode.forEach(entity -> {
                CommitDTO githubCommitDTO = new CommitDTO();
                githubCommitDTO.setRepoOwner(repoOwner);
                githubCommitDTO.setRepoName(repoName);
                githubCommitDTO.setBranchName(branchName);
                githubCommitDTO.setAdditions(Integer.parseInt(entity.get("additions").toString()));
                githubCommitDTO.setDeletions(Integer.parseInt(entity.get("deletions").toString()));
                githubCommitDTO.setChangeFiles(Integer.parseInt(entity.get("changedFiles").toString()));
                githubCommitDTO.setCommittedDate(entity.get("committedDate"));
                githubCommitDTO.setAuthor(Optional.ofNullable(entity.get("author")));

                synchronized (lock) {
                    githubCommitService.save(githubCommitDTO);
                }
            }));
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
