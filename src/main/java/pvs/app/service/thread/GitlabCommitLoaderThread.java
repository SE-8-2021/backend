package pvs.app.service.thread;

import com.fasterxml.jackson.databind.JsonNode;
import org.gitlab4j.api.models.CommitStats;
import org.gitlab4j.api.utils.JacksonJson;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.dto.GitlabCommitDTO;
import pvs.app.service.GitlabApiService;
import pvs.app.service.GitlabCommitService;

import java.io.IOException;
import java.util.*;

public class GitlabCommitLoaderThread extends Thread {

    private static final Object lock = new Object();
    private final GitlabCommitService gitlabCommitService;
    private final GitlabApiService gitlabApiService;
    private final String repoOwner, repoName;
    private final String responseJson;
    private final CommitStats commitStats;
    private final Integer changeFileCount;

    public GitlabCommitLoaderThread(GitlabCommitService githubCommitService, GitlabApiService gitlabApiService, String repoOwner, String repoName, String responseJson, CommitStats commitStats, Integer changeFileCount) {
        this.gitlabCommitService = githubCommitService;
        this.gitlabApiService = gitlabApiService;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.responseJson = responseJson;
        this.commitStats = commitStats;
        this.changeFileCount = changeFileCount;
    }

    @Override
    public void run() {
        JacksonJson jacksonJson = new JacksonJson();

        try {
            JsonNode commitJsonNode = jacksonJson.readTree(responseJson);

            GitlabCommitDTO gitlabCommitDTO = new GitlabCommitDTO();
            gitlabCommitDTO.setRepoOwner(repoOwner);
            gitlabCommitDTO.setRepoName(repoName);
            gitlabCommitDTO.setAuthorName(String.valueOf(commitJsonNode.get("authorName")));
            gitlabCommitDTO.setAuthorEmail(String.valueOf(commitJsonNode.get("authorEmail")));
            gitlabCommitDTO.setAdditions(commitStats.getAdditions());
            gitlabCommitDTO.setDeletions(commitStats.getDeletions());
            gitlabCommitDTO.setChangeFiles(changeFileCount);
            gitlabCommitDTO.setCommittedDate(commitJsonNode.get("committedDate"));
            gitlabCommitDTO.setAuthor(Optional.ofNullable(commitJsonNode.get("authorName")));
            if (this.gitlabCommitService.checkIfExist(gitlabCommitDTO)) {
                Thread.currentThread().interrupt();
            }else {
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
