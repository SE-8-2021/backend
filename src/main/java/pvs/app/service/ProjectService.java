package pvs.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.gitlab4j.api.GitLabApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import pvs.app.dao.ProjectDAO;
import pvs.app.dto.*;
import pvs.app.entity.Project;
import pvs.app.entity.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectDAO projectDAO;
    private final GithubApiService githubApiService;
    private final GitLabApiService gitLabApiService;

    public ProjectService(ProjectDAO projectDAO, GithubApiService githubApiService, GitLabApiService gitLabApiService) {
        this.projectDAO = projectDAO;
        this.githubApiService = githubApiService;
        this.gitLabApiService = gitLabApiService;
    }

    public void create(CreateProjectDTO createProjectDTO) {
        Project project = new Project();
        project.setMemberId(createProjectDTO.getMemberId());
        project.setName(createProjectDTO.getProjectName());
        projectDAO.save(project);
    }

    public List<ResponseProjectDTO> getMemberProjects(Long memberId) {
        List<Project> projectList = projectDAO.findByMemberId(memberId);
        List<ResponseProjectDTO> projectDTOList = new ArrayList<>();

        for (Project project : projectList) {
            ResponseProjectDTO responseProjectDTO = new ResponseProjectDTO();
            responseProjectDTO.setProjectId(project.getProjectId());
            responseProjectDTO.setProjectName(project.getName());
            responseProjectDTO.setAvatarURL(project.getAvatarURL());
            for (Repository repository : project.getRepositorySet()) {
                RepositoryDTO repositoryDTO = new RepositoryDTO();
                repositoryDTO.setUrl(repository.getUrl());
                repositoryDTO.setType(repository.getType());
                responseProjectDTO.getRepositoryDTOList().add(repositoryDTO);
            }
            projectDTOList.add(responseProjectDTO);
        }
        return projectDTOList;
    }

    public boolean addSonarRepo(AddRepositoryDTO addSonarRepositoryDTO) {
        Optional<Project> projectOptional = projectDAO.findById(addSonarRepositoryDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        Repository repository = new Repository();
        repository.setUrl(addSonarRepositoryDTO.getRepositoryURL());
        repository.setType("sonar");
        project.getRepositorySet().add(repository);
        projectDAO.save(project);
        return true;
    }

    public boolean addGithubRepo(AddRepositoryDTO addRepositoryDTO) throws IOException {
        Optional<Project> projectOptional = projectDAO.findById(addRepositoryDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        String url = addRepositoryDTO.getRepositoryURL();
        Repository repository = new Repository();
        repository.setUrl(url);
        repository.setType("github");
        project.getRepositorySet().add(repository);
        String owner = url.split("/")[3]; // Get gitHub project owner name by split project url
        JsonNode responseURL = githubApiService.getAvatarURL(owner);
        if (responseURL != null) {
            String avatarUrl = responseURL.textValue();
            project.setAvatarURL(avatarUrl);
        }
        projectDAO.save(project);
        return true;
    }

    public boolean addGitLabRepo(@NotNull AddRepositoryDTO addGitLabRepositoryDTO) {
        Optional<Project> projectOptional = projectDAO.findById(addGitLabRepositoryDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        String url = addGitLabRepositoryDTO.getRepositoryURL();
        Repository repository = new Repository();
        repository.setUrl(url);
        repository.setType("gitlab");
        project.getRepositorySet().add(repository);
        String owner = url.split("/")[3];
        String projectName = url.split("/")[4];
        String responseURL = null;
        try {
            responseURL = gitLabApiService.getAvatarURL(owner, projectName);
        } catch (GitLabApiException | NullPointerException e) {
            e.printStackTrace();
        }
        project.setAvatarURL(Objects.requireNonNullElse(responseURL, "https://i.imgur.com/HTdJRkN.webp"));
        projectDAO.save(project);
        return true;
    }

    public boolean addTrelloBoard(AddRepositoryDTO addTrelloBoardDTO) {
        Optional<Project> projectOptional = projectDAO.findById(addTrelloBoardDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        String url = addTrelloBoardDTO.getRepositoryURL();
        Repository repository = new Repository();
        repository.setUrl(url);
        repository.setType("trello");
        project.getRepositorySet().add(repository);
        projectDAO.save(project);
        return true;
    }

    // toggle removed attribute to true
    public boolean removeProjectById(Long projectId) {
        final Optional<Project> project = projectDAO.findById(projectId);
        if (project.isPresent()) {
            Project projectToBeRemoved = project.get();
            projectToBeRemoved.setRemoved(true);
            projectDAO.save(projectToBeRemoved);
            return true;
        } else {
            return false;
        }
    }

    // get the projects that are not removed
    public List<ResponseProjectDTO> getMemberActiveProjects(Long memberId) {
        final List<Project> projectList = projectDAO.findByMemberId(memberId);
        final List<ResponseProjectDTO> projectDTOList = new ArrayList<>();

        for (Project project : projectList) {
            if (!project.isRemoved()) {
                ResponseProjectDTO responseProjectDTO = new ResponseProjectDTO();
                responseProjectDTO.setProjectId(project.getProjectId());
                responseProjectDTO.setProjectName(project.getName());
                responseProjectDTO.setAvatarURL(project.getAvatarURL());
                for (Repository repository : project.getRepositorySet()) {
                    RepositoryDTO repositoryDTO = new RepositoryDTO();
                    repositoryDTO.setUrl(repository.getUrl());
                    repositoryDTO.setType(repository.getType());
                    responseProjectDTO.getRepositoryDTOList().add(repositoryDTO);
                }
                projectDTOList.add(responseProjectDTO);
            }
        }
        return projectDTOList;
    }
}
