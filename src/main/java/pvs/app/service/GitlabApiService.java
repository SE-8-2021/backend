package pvs.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GithubIssueDTO;
import pvs.app.dto.GitlabIssueDTO;
import pvs.app.service.thread.GithubCommitLoaderThread;
import pvs.app.service.thread.GithubIssueLoaderThread;
import pvs.app.service.thread.GitlabCommitLoaderThread;
import pvs.app.service.thread.GitlabIssueLoaderThread;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GitlabApiService {

    private final WebClient webClient;
    private final GitlabCommitService gitlabCommitService;
    private GitLabApi gitLabApi;
    private String username = null;
    private List<Commit> commits;
    private boolean useToken = true;

    public GitlabApiService(WebClient.Builder webClientBuilder, GitlabCommitService gitlabCommitService) {
        //        String token = System.getenv("PVS_GITLAB_TOKEN");
        String token = "glpat-SAwLzPB3SsPxzhPU92PZ";
        if (useToken){
            this.gitLabApi = new GitLabApi("https://gitlab.com", Constants.TokenType.ACCESS, token);
        }

        this.gitlabCommitService = gitlabCommitService;
        this.webClient = webClientBuilder.baseUrl("https://api.gitlab.com/")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    private String dateToISO8601(Date date) {
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sdf.format(date);
    }

    public void oauth2login(String username, String password) throws GitLabApiException {
        if (this.gitLabApi == null) {
            this.gitLabApi = GitLabApi.oauth2Login("https://gitlab.com/", username, password);
        }
        System.out.println("oauth2login has been executed");
        // two problems need to be dealt with:
        // 1. password security
        // 2. whether we need to log in whenever the service start
    }

    public Boolean getCommitsFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException {
        CommitsApi commitsApi = this.gitLabApi.getCommitsApi();
        ProjectApi projectApi = this.gitLabApi.getProjectApi();
        Project project = projectApi.getProject(owner, name);
        commits = commitsApi.getCommits(project.getId());
        if (commits.size() != 0){
            List<CommitStats> commitStats = new ArrayList<>();
            for(Commit commit: commits){
                commitStats.add(commitsApi.getCommit(project.getId(),commit.getId()).getStats());
            }

            List<GitlabCommitLoaderThread> gitlabCommitLoaderThreadList = new ArrayList<>();

            for (int i = 0; i < commits.size(); i++) {
                GitlabCommitLoaderThread gitlabCommitLoaderThread =
                        new GitlabCommitLoaderThread(
                                this.gitlabCommitService,
                                this,
                                owner, // repoOwner
                                name,  // repoName
                                String.valueOf(commits.get(i)), // commit info
                                commitStats.get(i),  // commit stats
                                commitsApi.getDiff(project.getId(), commits.get(i).getId()).size() // changeFileCount
                        );
                gitlabCommitLoaderThreadList.add(gitlabCommitLoaderThread);
                gitlabCommitLoaderThread.start();
            }

            for (GitlabCommitLoaderThread thread : gitlabCommitLoaderThreadList) {
                thread.join();
            }
            return true;
        } else {
            return false;
        }
    }

    public List<GitlabIssueDTO> getIssuesFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException{
        List<GitlabIssueDTO> gitlabIssueDTOList = new ArrayList<>();
        IssuesApi issuesApi = this.gitLabApi.getIssuesApi();
        ProjectApi projectApi = this.gitLabApi.getProjectApi();
        Project project = projectApi.getProject(owner, name);
        List<Issue> issues = new ArrayList<>();
        issues = issuesApi.getIssues(project.getId());

        if (issues.size() != 0) {
            List<GitlabIssueLoaderThread> gitlabIssueLoaderThreadList = new ArrayList<>();

            for (int i = 0; i < issues.size(); i++) {
                GitlabIssueLoaderThread gitlabIssueLoaderThread =
                        new GitlabIssueLoaderThread(
                                gitlabIssueDTOList,
                                owner,
                                name,
                                issues.get(i));
                gitlabIssueLoaderThreadList.add(gitlabIssueLoaderThread);
                gitlabIssueLoaderThread.start();
            }

            for (GitlabIssueLoaderThread thread : gitlabIssueLoaderThreadList) {
                thread.join();
            }
        } else {
            return null;
        }
        return gitlabIssueDTOList;
    }

    public String getAvatarURL(String owner, String projectName) throws GitLabApiException {
        if (this.gitLabApi != null){
            return this.gitLabApi.getProjectApi().getProject(owner, projectName).getOwner().getAvatarUrl();
        }
        return null;
    }
}
