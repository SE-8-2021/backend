package pvs.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pvs.app.service.RepositoryService;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RepositoryController {
    private final RepositoryService repositoryService;
    @Value("${message.invalid.url}")
    private String urlInvalidMessage;
    @Value("${message.success}")
    private String successMessage;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping("/repository/github/check")
    public ResponseEntity<String> checkGitHubURL(@RequestParam("url") String url) {
        if (repositoryService.checkGithubURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }

    @GetMapping("/repository/sonar/check")
    public ResponseEntity<String> checkSonarURL(@RequestParam("url") String url) {
        if (repositoryService.checkSonarURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }

    @GetMapping("/repository/gitlab/check")
    public ResponseEntity<String> checkGitLabURL(@RequestParam("url") String url) {
        if (repositoryService.checkGitLabURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }

    @GetMapping("/repository/trello/check")
    public ResponseEntity<String> checkTrelloURL(@RequestParam("url") String url) {
        if (repositoryService.checkTrelloURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }
}
