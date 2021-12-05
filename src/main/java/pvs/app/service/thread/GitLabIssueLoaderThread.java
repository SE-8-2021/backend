package pvs.app.service.thread;

import org.gitlab4j.api.models.Issue;
import pvs.app.dto.GitLabIssueDTO;

import java.util.List;

public class GitLabIssueLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final List<GitLabIssueDTO> gitLabIssueDTOList;
    private final String repoOwner;
    private final String repoName;
    private final Issue issue;


    public GitLabIssueLoaderThread(List<GitLabIssueDTO> gitLabIssueDTOList, String repoOwner, String repoName, Issue issue) {
        this.gitLabIssueDTOList = gitLabIssueDTOList;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.issue = issue;
    }

    @Override
    public void run() {
        GitLabIssueDTO gitlabIssueDTO = new GitLabIssueDTO();
        gitlabIssueDTO.setRepoOwner(repoOwner);
        gitlabIssueDTO.setRepoName(repoName);
        gitlabIssueDTO.setCreatedAt(issue.getCreatedAt());
        gitlabIssueDTO.setClosedAt(issue.getClosedAt());

        synchronized (lock) {
            gitLabIssueDTOList.add(gitlabIssueDTO);
        }
    }
}
