package pvs.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.GithubCommitDTO;
import pvs.app.dto.GithubIssueDTO;
import pvs.app.dto.GithubPullRequestDTO;
import pvs.app.service.GithubApiService;
import pvs.app.service.GithubCommitService;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GithubApiController {

    static final Logger logger = LogManager.getLogger(GithubApiController.class.getName());
    private final GithubApiService githubApiService;
    private final GithubCommitService githubCommitService;
    @Value("${message.exception}")
    private String exceptionMessage;

    public GithubApiController(GithubApiService githubApiService, GithubCommitService githubCommitService) {
        this.githubApiService = githubApiService;
        this.githubCommitService = githubCommitService;
    }

    @SneakyThrows
    @PostMapping("/github/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> postCommits(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        boolean callAPISuccess;
        GithubCommitDTO githubCommitDTO = githubCommitService.getLastCommit(repoOwner, repoName);
        final Date lastUpdate = githubCommitDTO == null ? Date.from(Instant.ofEpochSecond(0)) : githubCommitDTO.getCommittedDate();

        try {
            callAPISuccess = githubApiService.getCommitsFromGithub(repoOwner, repoName, lastUpdate);
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            logger.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

        if (callAPISuccess) {
            return ResponseEntity.status(HttpStatus.OK).body("success get commit data and save to database");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("cannot get commit data");
        }
    }

    @GetMapping("/github/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommits(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {

        ObjectMapper objectMapper = new ObjectMapper();

        List<GithubCommitDTO> githubCommitDTOs = githubCommitService.getAllCommits(repoOwner, repoName);

        String githubCommitDTOsJson;

        try {
            githubCommitDTOsJson = objectMapper.writeValueAsString(githubCommitDTOs);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(githubCommitDTOsJson);
        } catch (JsonProcessingException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @GetMapping("/github/branchList/{repoOwner}/{repoName}")
    public ResponseEntity<List<String>> getBranchList(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        GithubCommitDTO githubCommitDTO = githubCommitService.getLastCommit(repoOwner, repoName);
        final Date lastUpdate = githubCommitDTO == null ? Date.from(Instant.ofEpochSecond(0)) : githubCommitDTO.getCommittedDate();

        try {
            List<String> branchNameList = this.githubApiService.getBranchNameList(repoOwner, repoName, lastUpdate);
            if (branchNameList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(branchNameList);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/github/commits/{repoOwner}/{repoName}/{branchName}")
    public ResponseEntity<String> getCommitsOfBranch(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName, @PathVariable("branchName") String branchName) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GithubCommitDTO> githubCommitDTOS = this.githubCommitService.getCommitsOfSpecificBranch(repoOwner, repoName, branchName);
        try {
            String githubCommitDTOsJson = objectMapper.writeValueAsString(githubCommitDTOS);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(githubCommitDTOsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Get commits from branches failed");
        }
    }

    @GetMapping("/github/issues/{repoOwner}/{repoName}")
    public ResponseEntity<String> getIssues(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        ObjectMapper objectMapper = new ObjectMapper();

        List<GithubIssueDTO> githubIssueDTOs;

        try {
            githubIssueDTOs = githubApiService.getIssuesFromGithub(repoOwner, repoName);

            // Retry if the githubIssueDTOs is null
            int retryCount = 1;
            while (retryCount <= 5) {
                if (githubIssueDTOs != null) break;
                githubIssueDTOs = githubApiService.getIssuesFromGithub(repoOwner, repoName);
                retryCount++;
            }

            if (githubIssueDTOs == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Get issue data failed from GitHub API");
            }
        } catch (InterruptedException | IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

        try {
            String githubIssueDTOsJson = objectMapper.writeValueAsString(githubIssueDTOs);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(githubIssueDTOsJson);
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @GetMapping("/github/pullRequests/{repoOwner}/{repoName}")
    public ResponseEntity<String> getPullRequests(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        ObjectMapper objectMapper = new ObjectMapper();

        List<GithubPullRequestDTO> githubPullRequestDTOs;

        try {
            githubPullRequestDTOs = githubApiService.getPullRequestMetricsFromGithub(repoOwner, repoName);

            // Retry if the githubPullRequestDTOs is null
            int retryCount = 1;
            while (retryCount <= 5) {
                if (githubPullRequestDTOs != null) break;
                githubPullRequestDTOs = githubApiService.getPullRequestMetricsFromGithub(repoOwner, repoName);
                retryCount++;
            }

            if (githubPullRequestDTOs == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Get pull request data failed from GitHub API");
            }
        } catch (InterruptedException | IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

        try {
            String githubPullRequestDTOsJson = objectMapper.writeValueAsString(githubPullRequestDTOs);
            return ResponseEntity.status(HttpStatus.OK).body(githubPullRequestDTOsJson);
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }
}
