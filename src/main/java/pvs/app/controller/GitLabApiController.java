package pvs.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GitLabApiController {

    static final Logger logger = LogManager.getLogger(GitLabApiController.class.getName());
    private final GitLabApiService gitlabApiService;
    private final GitLabCommitService gitlabCommitService;
    @Value("${message.exception}")
    private String exceptionMessage;

    public GitLabApiController(GitLabApiService gitlabApiService, GitLabCommitService gitlabCommitService) {
        this.gitlabApiService = gitlabApiService;
        this.gitlabCommitService = gitlabCommitService;
    }

    @SneakyThrows
    @GetMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommits(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) throws GitLabApiException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitLabCommitDTO> gitLabCommitDTOS = gitlabCommitService.getAllCommits(repoOwner, repoName);

        try {
            String gitlabCommitDTOsJson = objectMapper.writeValueAsString(gitLabCommitDTOS);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(gitlabCommitDTOsJson);
        } catch (JsonProcessingException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @SneakyThrows
    @PostMapping("/gitlab/{username}/{password}")
    public ResponseEntity<String> oauth2login(@PathVariable("username") String username, @PathVariable("password") String password) {
        System.out.println("going to execute gitlab api..");
        this.gitlabApiService.oauth2login(username, password);
        return ResponseEntity.status(HttpStatus.OK).body("log in succeed");
    }

    @PostMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommitsFromGitLab(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        System.out.println("going to get commit from gitlab...");
        try {
            if (this.gitlabApiService.getCommitsFromGitlab(repoOwner, repoName)) {
                return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab succeed");
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab failed");
            }

        } catch (InterruptedException | GitLabApiException e) {
            logger.debug(e.getMessage());
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
            gitLabIssueDTOS = gitlabApiService.getIssuesFromGitlab(repoOwner, repoName);
        } catch (InterruptedException | GitLabApiException e) {
            logger.debug(e.getMessage());
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
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }
}
