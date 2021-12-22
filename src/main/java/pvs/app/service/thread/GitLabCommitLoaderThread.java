package pvs.app.service.thread;

import com.fasterxml.jackson.databind.JsonNode;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.utils.JacksonJson;
import pvs.app.dto.GitLabCommitDTO;
import pvs.app.service.GitLabCommitService;

import java.io.IOException;
import java.util.Optional;

public class GitLabCommitLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final GitLabCommitService gitlabCommitService;
    private final String repoOwner, repoName, branchName;
    private final String responseJson;
    private final CommitStats commitStats;
    private final Integer changeFileCount;

    public GitLabCommitLoaderThread(GitLabCommitService githubCommitService, String repoOwner, String repoName, String branchName, String responseJson, CommitStats commitStats, Integer changeFileCount) {
        this.gitlabCommitService = githubCommitService;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.branchName = branchName;
        this.responseJson = responseJson;
        this.commitStats = commitStats;
        this.changeFileCount = changeFileCount;
    }

    @Override
    public void run() {
        JacksonJson jacksonJson = new JacksonJson();

        try {
            JsonNode commitJsonNode = jacksonJson.readTree(responseJson);

            GitLabCommitDTO gitlabCommitDTO = new GitLabCommitDTO();
            gitlabCommitDTO.setRepoOwner(repoOwner);
            gitlabCommitDTO.setRepoName(repoName);
            gitlabCommitDTO.setBranchName(branchName);
            gitlabCommitDTO.setAuthorName(String.valueOf(commitJsonNode.get("authorName")));
            gitlabCommitDTO.setAuthorEmail(String.valueOf(commitJsonNode.get("authorEmail")));
            gitlabCommitDTO.setAdditions(commitStats.getAdditions());
            gitlabCommitDTO.setDeletions(commitStats.getDeletions());
            gitlabCommitDTO.setChangeFiles(changeFileCount);
            gitlabCommitDTO.setCommittedDate(commitJsonNode.get("committedDate"));
            gitlabCommitDTO.setAuthor(Optional.ofNullable(commitJsonNode.get("authorName")));
            if (this.gitlabCommitService.checkIfExist(gitlabCommitDTO)) {
                Thread.currentThread().interrupt();
            } else {
                System.out.println("---------------------------inserting");
                synchronized (lock) {
                    gitlabCommitService.save(gitlabCommitDTO);
                }
                System.out.println("---------------------------complete");
            }

        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
