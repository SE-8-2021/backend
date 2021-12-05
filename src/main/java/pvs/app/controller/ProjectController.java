package pvs.app.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.*;
import pvs.app.service.ProjectService;
import pvs.app.service.RepositoryService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {

    static final Logger logger = LogManager.getLogger(ProjectController.class.getName());
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

    public ProjectController(ProjectService projectService, RepositoryService repositoryService) {
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

    @GetMapping("/repository/sonar/check")
    public ResponseEntity<String> checkSonarURL(@RequestParam("url") String url) {
        if (repositoryService.checkSonarURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }

    @GetMapping("/repository/gitlab/check")
    public ResponseEntity<String> checkGitLabURL(@RequestParam("url") String url) {
        if (repositoryService.checkGitlabURL(url)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
    }

    @PostMapping("/project")
    public ResponseEntity<String> createProject(@RequestBody CreateProjectDTO projectDTO) {
        try {
            projectService.create(projectDTO);
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        } catch (IOException | GitLabApiException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/sonar")
    public ResponseEntity<String> addSonarRepository(@RequestBody AddSonarRepositoryDTO addSonarRepositoryDTO) {
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
            logger.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/github")
    public ResponseEntity<String> addGitHubRepository(@RequestBody AddGithubRepositoryDTO addGithubRepositoryDTO) {
        try {
            if (repositoryService.checkGithubURL(addGithubRepositoryDTO.getRepositoryURL())) {
                if (projectService.addGithubRepo(addGithubRepositoryDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/gitlab")
    public ResponseEntity<String> addGitLabRepository(@RequestBody AddGitLabRepositoryDTO addGitlabRepositoryDTO) {
        try {
            if (repositoryService.checkGitlabURL(addGitlabRepositoryDTO.getRepositoryURL())) {
                if (projectService.addGitlabRepo(addGitlabRepositoryDTO)) {
                    return ResponseEntity.status(HttpStatus.OK).body(successMessage);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlInvalidMessage);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }


    @GetMapping("/project/{memberId}")
    public ResponseEntity<List<ResponseProjectDTO>> readMemberAllProjects(@PathVariable Long memberId) {
        List<ResponseProjectDTO> projectList = projectService.getMemberProjects(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(projectList);
        //-/-/-/-/-/-/-/-/-/-/
        //    0        0    //
        //         3        //
        //////////\\\\\\\\\\\\
    }

    @GetMapping("/project/{memberId}/{projectId}")
    public ResponseEntity<ResponseProjectDTO> readSelectedProject
            (@PathVariable Long memberId, @PathVariable Long projectId) {
        List<ResponseProjectDTO> projectList = projectService.getMemberProjects(memberId);
        Optional<ResponseProjectDTO> selectedProject =
                projectList.stream()
                        .filter(project -> project.getProjectId().equals(projectId))
                        .findFirst();

        return selectedProject.map(responseProjectDTO -> ResponseEntity.status(HttpStatus.OK).body(responseProjectDTO))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));

        //-/-/-/-/-/-/-/-/-/-/
        //    0        0    //
        //         3        //
        //////////\\\\\\\\\\\\
    }
}
