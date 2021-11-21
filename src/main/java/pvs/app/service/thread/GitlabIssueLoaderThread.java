package pvs.app.service.thread;

import org.gitlab4j.api.models.Issue;
import pvs.app.dto.GitlabIssueDTO;

import java.util.List;

public class GitlabIssueLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final List<GitlabIssueDTO> gitlabIssueDTOList;
    private final String repoOwner;
    private final String repoName;
    private final Issue issue;


    public GitlabIssueLoaderThread(List<GitlabIssueDTO> gitlabIssueDTOList, String repoOwner, String repoName, Issue issue) {
        this.gitlabIssueDTOList = gitlabIssueDTOList;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.issue = issue;
    }

    @Override
    public void run() {
        GitlabIssueDTO gitlabIssueDTO = new GitlabIssueDTO();
        gitlabIssueDTO.setRepoOwner(repoOwner);
        gitlabIssueDTO.setRepoName(repoName);
        gitlabIssueDTO.setCreatedAt(issue.getCreatedAt());
        gitlabIssueDTO.setClosedAt(issue.getClosedAt());

        synchronized (lock) {
            gitlabIssueDTOList.add(gitlabIssueDTO);
        }
    }
}
