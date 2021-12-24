package pvs.app.service.thread;

import com.fasterxml.jackson.databind.JsonNode;
import pvs.app.dto.GithubPullRequestDTO;

import java.util.List;

public class GithubPullRequestLoaderThread extends Thread{

    private static final Object lock = new Object();
    private final List<GithubPullRequestDTO> githubPullRequestDTOList;
    private final String repoOwner;
    private final String repoName;
    private final JsonNode requestNode;

    public GithubPullRequestLoaderThread(List<GithubPullRequestDTO> githubPullRequestDTOList, String repoOwner, String repoName, JsonNode requestNode) {
        this.githubPullRequestDTOList = githubPullRequestDTOList;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.requestNode = requestNode;
    }

    @Override
    public void run() {
        JsonNode createdAt = requestNode.get("node").get("createdAt");
        JsonNode closedAt = requestNode.get("node").get("closedAt");
        JsonNode mergedAt = requestNode.get("node").get("mergedAt");

        GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
        githubPullRequestDTO.setRepoOwner(repoOwner);
        githubPullRequestDTO.setRepoName(repoName);
        if (createdAt != null) { githubPullRequestDTO.setCreatedAt(createdAt); }
        if (closedAt != null) { githubPullRequestDTO.setClosedAt(closedAt); }
        if (mergedAt != null) { githubPullRequestDTO.setMergedAt(mergedAt); }
        synchronized (lock) {
            githubPullRequestDTOList.add(githubPullRequestDTO);
        }
    }
}
