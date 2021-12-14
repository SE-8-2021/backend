package pvs.app.service;

import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GitLabIssueDTO;
import pvs.app.service.thread.GitLabCommitLoaderThread;
import pvs.app.service.thread.GitLabIssueLoaderThread;
import reactor.util.annotation.Nullable;

import java.util.*;

@Service
public class GitLabApiService {

    private final GitLabCommitService gitlabCommitService;
    private GitLabApi gitLabApi;

    public GitLabApiService(WebClient.Builder webClientBuilder, GitLabCommitService gitlabCommitService) {
        String token = System.getenv("PVS_GITLAB_TOKEN");
        this.gitLabApi = new GitLabApi("https://gitlab.com", Constants.TokenType.ACCESS, token);
        this.gitlabCommitService = gitlabCommitService;
        webClientBuilder.baseUrl("https://api.gitlab.com/")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
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
        System.out.println(owner + " " + name);
        Project project = projectApi.getProject(owner, name);
        List<Commit> commits = commitsApi.getCommits(project.getId());

        if (commits.size() == 0) return false;

        List<CommitStats> commitStats = new ArrayList<>();
        for(Commit commit: commits){
            commitStats.add(commitsApi.getCommit(project.getId(),commit.getId()).getStats());
        }

        List<GitLabCommitLoaderThread> gitLabCommitLoaderThreadList = new ArrayList<>();

        for (int i = 0; i < commits.size(); i++) {
            GitLabCommitLoaderThread gitlabCommitLoaderThread =
                    new GitLabCommitLoaderThread(
                            this.gitlabCommitService,
                            owner, // repoOwner
                            name,  // repoName
                            String.valueOf(commits.get(i)), // commit info
                            commitStats.get(i),  // commit stats
                            commitsApi.getDiff(project.getId(), commits.get(i).getId()).size() // changeFileCount
                    );
            gitLabCommitLoaderThreadList.add(gitlabCommitLoaderThread);
            gitlabCommitLoaderThread.start();
        }

        for (GitLabCommitLoaderThread thread : gitLabCommitLoaderThreadList) {
            thread.join();
        }
        return true;
    }

    @Nullable
    public List<GitLabIssueDTO> getIssuesFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException{
        List<GitLabIssueDTO> gitLabIssueDTOList = new ArrayList<>();
        IssuesApi issuesApi = this.gitLabApi.getIssuesApi();
        ProjectApi projectApi = this.gitLabApi.getProjectApi();
        Project project = projectApi.getProject(owner, name);
        List<Issue> issues = issuesApi.getIssues(project.getId());

        if (issues.size() == 0) return null;

        final List<GitLabIssueLoaderThread> gitLabIssueLoaderThreadList = new ArrayList<>();

        for (final Issue issue : issues) {
            final GitLabIssueLoaderThread gitlabIssueLoaderThread =
                    new GitLabIssueLoaderThread(
                            gitLabIssueDTOList,
                            owner,
                            name,
                            issue);
            gitLabIssueLoaderThreadList.add(gitlabIssueLoaderThread);
            gitlabIssueLoaderThread.start();
        }

        for (final GitLabIssueLoaderThread thread : gitLabIssueLoaderThreadList) {
            thread.join();
        }

        return gitLabIssueDTOList;
    }

    @Nullable
    public String getAvatarURL(String owner, String projectName) throws GitLabApiException {
        if (this.gitLabApi == null) return null;
        return this.gitLabApi.getProjectApi().getProject(owner, projectName).getOwner().getAvatarUrl();
    }
}
