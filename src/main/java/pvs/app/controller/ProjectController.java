package pvs.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.dto.AddRepositoryDTO;
import pvs.app.dto.CreateProjectDTO;
import pvs.app.dto.ResponseProjectDTO;
import pvs.app.service.ProjectService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectController {
    private final ProjectService projectService;
    @Value("${message.exception}")
    private String exceptionMessage;
    @Value("${message.success}")
    private String successMessage;
    @Value("${message.fail}")
    private String failMessage;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/project")
    public ResponseEntity<String> createProject(@RequestBody CreateProjectDTO createProjectDTO) {
        projectService.create(createProjectDTO);
        return ResponseEntity.status(HttpStatus.OK).body(successMessage);
    }

    @PostMapping("/project/{projectId}/repository/github")
    public ResponseEntity<String> addGitHubRepository(@RequestBody AddRepositoryDTO addRepositoryDTO) {
        try {
            if (projectService.addGithubRepo(addRepositoryDTO)) {
                return ResponseEntity.status(HttpStatus.OK).body(successMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/gitlab")
    public ResponseEntity<String> addGitLabRepository(@RequestBody AddRepositoryDTO addGitLabRepositoryDTO) {
        try {
            if (projectService.addGitLabRepo(addGitLabRepositoryDTO)) {
                return ResponseEntity.status(HttpStatus.OK).body(successMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/sonar")
    public ResponseEntity<String> addSonarRepository(@RequestBody AddRepositoryDTO addSonarRepositoryDTO) {
        try {
            if (projectService.addSonarRepo(addSonarRepositoryDTO)) {
                return ResponseEntity.status(HttpStatus.OK).body(successMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionMessage);
        }
    }

    @PostMapping("/project/{projectId}/repository/trello")
    public ResponseEntity<String> addTrelloBoard(@RequestBody AddRepositoryDTO addTrelloBoardDTO) {
        try {
            if (projectService.addTrelloBoard(addTrelloBoardDTO)) {
                return ResponseEntity.status(HttpStatus.OK).body(successMessage);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
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

    @DeleteMapping("/project/remove/{projectId}")
    public ResponseEntity<String> removeProject(@PathVariable Long projectId) {
        if (projectService.removeProjectById(projectId)) {
            return ResponseEntity.status(HttpStatus.OK).body(successMessage);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(failMessage);
        }
    }

    // get the projects that are not removed
    @GetMapping("/project/{memberId}/active")
    public ResponseEntity<List<ResponseProjectDTO>> readMemberActiveProjects(@PathVariable Long memberId) {
        List<ResponseProjectDTO> projectList = projectService.getMemberActiveProjects(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(projectList);
    }
}
