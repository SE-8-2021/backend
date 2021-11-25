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
import org.gitlab4j.api.GitLabApiException;
import pvs.app.dto.GitlabCommitDTO;
import pvs.app.dto.GitlabIssueDTO;
import pvs.app.service.GitlabApiService;
import pvs.app.service.GitlabCommitService;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class GitlabApiController {

    static final Logger logger = LogManager.getLogger(GithubApiController.class.getName());
    private final GitlabApiService gitlabApiService;
    private final GitlabCommitService gitlabCommitService;
    @Value("${message.exception}")
    private String exceptionMessage;

    public GitlabApiController(GitlabApiService gitlabApiService, GitlabCommitService gitlabCommitService){
        this.gitlabApiService = gitlabApiService;
        this.gitlabCommitService = gitlabCommitService;
    }

    @SneakyThrows
    @GetMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommits(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) throws GitLabApiException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitlabCommitDTO> gitlabCommitDTOs = gitlabCommitService.getAllCommits(repoOwner, repoName);
        String gitlabCommitDTOsJson;

        try {
            gitlabCommitDTOsJson = objectMapper.writeValueAsString(gitlabCommitDTOs);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(gitlabCommitDTOsJson);
        } catch (JsonProcessingException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }
    }

    @PostMapping("/gitlab/{username}/{password}")
    public ResponseEntity<String> oauth2login(@PathVariable("username") String username, @PathVariable("password") String password) throws GitLabApiException {
        System.out.println("going to execute gitlab api..");
        this.gitlabApiService.oauth2login(username, password);
        return ResponseEntity.status(HttpStatus.OK).body("log in succeed");
    }

    @PostMapping("/gitlab/commits/{repoOwner}/{repoName}")
    public ResponseEntity<String> getCommitsFromGitlab(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        System.out.println("going to get commit from gitlab...");
        try{
            if(this.gitlabApiService.getCommitsFromGitlab(repoOwner, repoName)){
                return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab succeed");
            }else{
                return ResponseEntity.status(HttpStatus.OK).body("get commit from gitlab failed");
            }

        }catch (InterruptedException | GitLabApiException e){
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

    }

    @GetMapping("/gitlab/issues/{repoOwner}/{repoName}")
    public ResponseEntity<String> getIssuesFromGitlab(@PathVariable("repoOwner") String repoOwner, @PathVariable("repoName") String repoName) {
        System.out.println("going to get issue from gitlab...");
        ObjectMapper objectMapper = new ObjectMapper();
        List<GitlabIssueDTO> gitlabIssueDTOs;

        try {
            gitlabIssueDTOs = gitlabApiService.getIssuesFromGitlab(repoOwner, repoName);
            if (null == gitlabIssueDTOs) {
                System.out.println("test1");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("cannot get issue data");
            }
        } catch (InterruptedException | GitLabApiException e) {
            System.out.println("test2");
            logger.debug(e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

        try {
            String gitlabIssueDTOsJson = objectMapper.writeValueAsString(gitlabIssueDTOs);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(gitlabIssueDTOsJson);
        } catch (IOException e) {
            System.out.println("test3");
            logger.debug(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(exceptionMessage);
        }

    }
}
