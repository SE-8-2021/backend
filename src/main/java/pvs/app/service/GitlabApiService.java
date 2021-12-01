package pvs.app.service;

import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GitlabIssueDTO;
import pvs.app.service.thread.GitlabCommitLoaderThread;
import pvs.app.service.thread.GitlabIssueLoaderThread;
import reactor.util.annotation.Nullable;

import java.util.*;

@Service
public class GitlabApiService {

    private final GitlabCommitService gitlabCommitService;
    private GitLabApi gitLabApi;

    public GitlabApiService(WebClient.Builder webClientBuilder, GitlabCommitService gitlabCommitService) {
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

        List<GitlabCommitLoaderThread> gitlabCommitLoaderThreadList = new ArrayList<>();

        for (int i = 0; i < commits.size(); i++) {
            GitlabCommitLoaderThread gitlabCommitLoaderThread =
                    new GitlabCommitLoaderThread(
                            this.gitlabCommitService,
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
    }

    @Nullable
    public List<GitlabIssueDTO> getIssuesFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException{
        List<GitlabIssueDTO> gitlabIssueDTOList = new ArrayList<>();
        IssuesApi issuesApi = this.gitLabApi.getIssuesApi();
        ProjectApi projectApi = this.gitLabApi.getProjectApi();
        Project project = projectApi.getProject(owner, name);
        List<Issue> issues = issuesApi.getIssues(project.getId());

        if (issues.size() == 0) return null;

        final List<GitlabIssueLoaderThread> gitlabIssueLoaderThreadList = new ArrayList<>();

        for (final Issue issue : issues) {
            final GitlabIssueLoaderThread gitlabIssueLoaderThread =
                    new GitlabIssueLoaderThread(
                            gitlabIssueDTOList,
                            owner,
                            name,
                            issue);
            gitlabIssueLoaderThreadList.add(gitlabIssueLoaderThread);
            gitlabIssueLoaderThread.start();
        }

        for (final GitlabIssueLoaderThread thread : gitlabIssueLoaderThreadList) {
            thread.join();
        }

        return gitlabIssueDTOList;
    }

    @Nullable
    public String getAvatarURL(String owner, String projectName) throws GitLabApiException {
        if (this.gitLabApi == null) return null;
        return this.gitLabApi.getProjectApi().getProject(owner, projectName).getOwner().getAvatarUrl();
    }
}
