package pvs.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.AddRepositoryDTO;
import pvs.app.service.ProjectService;
import pvs.app.service.RepositoryService;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RepositoryController {
    private final ProjectService projectService;
    private final RepositoryService repositoryService;
    @Value("${message.exception}")
    private String exceptionMessage;
    @Value("${message.invalid.url}")
    private String urlInvalidMessage;
    @Value("${message.success}")
    private String successMessage;
    @Value("${message.fail}")
    private String failMessage;

    public RepositoryController(ProjectService projectService, RepositoryService repositoryService) {
        this.projectService = projectService;
        this.repositoryService = repositoryService;
    }

    @GetMapping("/repository/github/check")
    public ResponseEntity<String> checkGitHubURL(@RequestParam("url") String url) {
        if (repositoryService.checkGithubURL(url)) {
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

    @GetMapping("/repository/sonar/check")
    public ResponseEntity<String> checkSonarURL(@RequestParam("url") String url) {
        if (repositoryService.checkSonarURL(url)) {
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

    @PostMapping("/project/{projectId}/repository/github")
    public ResponseEntity<String> addGitHubRepository(@RequestBody AddRepositoryDTO addRepositoryDTO) {
        try {
            if (repositoryService.checkGithubURL(addRepositoryDTO.getRepositoryURL())) {
                if (projectService.addGithubRepo(addRepositoryDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/gitlab")
    public ResponseEntity<String> addGitLabRepository(@RequestBody AddRepositoryDTO addGitLabRepositoryDTO) {
        try {
            if (repositoryService.checkGitLabURL(addGitLabRepositoryDTO.getRepositoryURL())) {
                if (projectService.addGitLabRepo(addGitLabRepositoryDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/sonar")
    public ResponseEntity<String> addSonarRepository(@RequestBody AddRepositoryDTO addSonarRepositoryDTO) {
        try {
            if (repositoryService.checkSonarURL(addSonarRepositoryDTO.getRepositoryURL())) {
                if (projectService.addSonarRepo(addSonarRepositoryDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/trello")
    public ResponseEntity<String> addTrelloBoard(@RequestBody AddRepositoryDTO addTrelloBoardDTO) {
        try {
            if (repositoryService.checkTrelloURL(addTrelloBoardDTO.getRepositoryURL())) {
                if (projectService.addTrelloBoard(addTrelloBoardDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }
}
