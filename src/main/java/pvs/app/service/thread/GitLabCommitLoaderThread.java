package pvs.app.service.thread;

import com.fasterxml.jackson.databind.JsonNode;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.utils.JacksonJson;
import pvs.app.dto.CommitDTO;
import pvs.app.service.GitLabCommitService;

import java.io.IOException;
import java.util.Optional;

public class GitLabCommitLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final GitLabCommitService gitLabCommitService;
    private final String repoOwner, repoName, branchName;
    private final String responseJson;
    private final CommitStats commitStats;
    private final Integer changeFileCount;

    public GitLabCommitLoaderThread(GitLabCommitService gitLabCommitService, String repoOwner, String repoName, String branchName, String responseJson, CommitStats commitStats, Integer changeFileCount) {
        this.gitLabCommitService = gitLabCommitService;
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

            CommitDTO commitDTO = new CommitDTO();
            commitDTO.setRepoOwner(repoOwner);
            commitDTO.setRepoName(repoName);
            commitDTO.setBranchName(branchName);
            commitDTO.setAuthorName(String.valueOf(commitJsonNode.get("authorName")));
            commitDTO.setAuthorEmail(String.valueOf(commitJsonNode.get("authorEmail")));
            commitDTO.setAdditions(commitStats.getAdditions());
            commitDTO.setDeletions(commitStats.getDeletions());
            commitDTO.setChangeFiles(changeFileCount);
            commitDTO.setCommittedDate(commitJsonNode.get("committedDate"));
            commitDTO.setAuthor(Optional.ofNullable(commitJsonNode.get("authorName")));
            if (this.gitLabCommitService.checkIfExist(commitDTO)) {
                Thread.currentThread().interrupt();
            } else {
                System.out.println("---------------------------inserting");
                synchronized (lock) {
                    gitLabCommitService.save(commitDTO);
                }
                System.out.println("---------------------------complete");
            }

        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
