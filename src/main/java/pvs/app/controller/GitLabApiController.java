package pvs.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.GitLabCommitDTO;
import pvs.app.dto.GitLabIssueDTO;
import pvs.app.service.GitLabApiService;
import pvs.app.service.GitLabCommitService;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GitLabApiController {
    private final GitLabApiService gitLabApiService;
    private final GitLabCommitService gitLabCommitService;
    @Value("${message.exception}")
    private String exceptionMessage;

    public GitLabApiController(GitLabApiService gitLabApiService, GitLabCommitService gitLabCommitService) {
        this.gitLabApiService = gitLabApiService;
        this.gitLabCommitService = gitLabCommitService;
    }

    @SneakyThrows
    @GetMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommits(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) throws GitLabApiException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitLabCommitDTO> gitLabCommitDTOS = gitLabCommitService.getAllCommits(repoOwner, repoName);

        try {
            String gitlabCommitDTOsJson = objectMapper.writeValueAsString(gitLabCommitDTOS);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(gitlabCommitDTOsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @SneakyThrows
    @PostMapping("/gitlab/{username}/{password}")
    public ResponseEntity<String> oauth2login(@PathVariable("username") String username, @PathVariable("password") String password) {
        System.out.println("going to execute gitlab api..");
        this.gitLabApiService.oauth2login(username, password);
        return ResponseEntity.status(HttpStatus.OK).body("log in succeed");
    }

    @PostMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommitsFromGitLab(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        System.out.println("going to get commit from gitlab...");
        try {
            if (this.gitLabApiService.getCommitsFromGitLab(repoOwner, repoName)) {
                return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab succeed");
            }
            return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab failed");
        } catch (InterruptedException | GitLabApiException | ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

    }

    @GetMapping("/gitlab/issues/{repoOwner}/{repoName}")
    public ResponseEntity<String> getIssuesFromGitLab(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        System.out.println("going to get issue from gitlab...");
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitLabIssueDTO> gitLabIssueDTOS;

        try {
            gitLabIssueDTOS = gitLabApiService.getIssuesFromGitLab(repoOwner, repoName);
        } catch (InterruptedException | GitLabApiException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

        try {
            if (null != gitLabIssueDTOS) {
                String gitlabIssueDTOsJson = objectMapper.writeValueAsString(gitLabIssueDTOS);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(gitlabIssueDTOsJson);
            }
            return ResponseEntity.status(HttpStatus.OK).body("no issue data");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @GetMapping("/gitlab/branchList/{repoOwner}/{repoName}")
    public ResponseEntity<List<String>> getBranchList(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        try {
            List<String> branchNameList = this.gitLabApiService.getBranchNameList(repoOwner, repoName);
            if (branchNameList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.OK).body(branchNameList);
        } catch (GitLabApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/gitlab/commits")
    public ResponseEntity<String> getCommitsOfBranch(@RequestParam("repoOwner") String repoOwner, @RequestParam("repoName") String repoName, @RequestParam("branchName") String branchName) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitLabCommitDTO> gitLabCommitDTOS = this.gitLabCommitService.getCommitsOfSpecificBranch(repoOwner, repoName, branchName);
        try {
            String gitLabCommitDTOsJson = objectMapper.writeValueAsString(gitLabCommitDTOS);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(gitLabCommitDTOsJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Get commits from branches failed");
        }
    }
}
