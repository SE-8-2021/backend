package pvs.app.service;

import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GitLabIssueDTO;
import pvs.app.service.thread.GitLabCommitLoaderThread;
import pvs.app.service.thread.GitLabIssueLoaderThread;
import reactor.util.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GitLabApiService {

    private final GitLabCommitService gitlabCommitService;
    private GitLabApi gitLabApi;
    private final CommitsApi commitsApi;
    private final IssuesApi issuesApi;
    private Object projectID = null;

    public GitLabApiService(WebClient.Builder webClientBuilder, GitLabCommitService gitlabCommitService) {
        String token = System.getenv("PVS_GITLAB_TOKEN");
        this.gitLabApi = new GitLabApi("https://gitlab.com", Constants.TokenType.ACCESS, token);
        this.commitsApi = this.gitLabApi.getCommitsApi();
        this.issuesApi = this.gitLabApi.getIssuesApi();
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

    private void getProjectID(String owner, String name) throws GitLabApiException {
        this.projectID = this.gitLabApi.getProjectApi().getProject(owner, name).getId();
    }

    private List<Branch> getBranches(String owner, String name) throws GitLabApiException {
        getProjectID(owner, name);
        return this.gitLabApi.getRepositoryApi().getBranches(projectID);
    }

    public List<String> getBranchesName(String owner, String name) throws GitLabApiException {
        List<Branch> branches = getBranches(owner, name);
        List<String> branchesName = new ArrayList<>();
        for (Branch branch: branches) {
            branchesName.add(branch.getName());
        }
        return branchesName;
    }

    private List<Commit> getCommitsWithBranchFromGitlab(Object projectIdOrPath, String branchName) throws ParseException, GitLabApiException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString_01 = "2019-01-01 00:00:00";
        String dateString_02 = "2025-01-01 00:00:00";
        Date since = sdf.parse(dateString_01);
        Date until = sdf.parse(dateString_02);
        return this.commitsApi.getCommits(projectIdOrPath, branchName, since, until);
    }

    public Boolean getCommitsFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException, ParseException {
        getProjectID(owner, name);
        List<Branch> branches = getBranches(owner, name);
        for (Branch branch : branches) {
            List<Commit> commits = getCommitsWithBranchFromGitlab(projectID, branch.getName());

            if (commits.size() == 0) return false;

            List<CommitStats> commitStats = new ArrayList<>();
            for (Commit commit : commits) {
                commitStats.add(this.commitsApi.getCommit(projectID, commit.getId()).getStats());
            }

            List<GitLabCommitLoaderThread> gitLabCommitLoaderThreadList = new ArrayList<>();

            for (int j = 0; j < commits.size(); j++) {
                GitLabCommitLoaderThread gitlabCommitLoaderThread =
                        new GitLabCommitLoaderThread(
                                this.gitlabCommitService,
                                owner, // repoOwner
                                name,  // repoName
                                branch.getName(), //branchName
                                String.valueOf(commits.get(j)), // commit info
                                commitStats.get(j),  // commit stats
                                commitsApi.getDiff(projectID, commits.get(j).getId()).size() // changeFileCount
                        );
                gitLabCommitLoaderThreadList.add(gitlabCommitLoaderThread);
                gitlabCommitLoaderThread.start();
            }

            for (GitLabCommitLoaderThread thread : gitLabCommitLoaderThreadList) {
                thread.join();
            }
        }
        return true;
    }

    @Nullable
    public List<GitLabIssueDTO> getIssuesFromGitlab(String owner, String name) throws GitLabApiException, InterruptedException{
        getProjectID(owner, name);
        List<GitLabIssueDTO> gitLabIssueDTOList = new ArrayList<>();
        List<Issue> issues = this.issuesApi.getIssues(projectID);

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
