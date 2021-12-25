package pvs.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.stereotype.Service;
import pvs.app.dao.ProjectDAO;
import pvs.app.dto.*;
import pvs.app.entity.Project;
import pvs.app.entity.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectDAO projectDAO;
    private final GithubApiService githubApiService;
    private final GitLabApiService gitlabApiService;

    public ProjectService(ProjectDAO projectDAO, GithubApiService githubApiService, GitLabApiService gitlabApiService) {
        this.projectDAO = projectDAO;
        this.githubApiService = githubApiService;
        this.gitlabApiService = gitlabApiService;
    }

    public void create(CreateProjectDTO projectDTO) throws IOException, GitLabApiException {
        Project savedProject;
        Project project = new Project();
        project.setMemberId(1L);
        project.setName(projectDTO.getProjectName());
        savedProject = projectDAO.save(project);

        if (!projectDTO.getGithubRepositoryURL().trim().equals("")) {
            AddGithubRepositoryDTO addGithubRepositoryDTO = new AddGithubRepositoryDTO();
            addGithubRepositoryDTO.setProjectId(savedProject.getProjectId());
            addGithubRepositoryDTO.setRepositoryURL(projectDTO.getGithubRepositoryURL());
            addGithubRepo(addGithubRepositoryDTO);
        }

        if (!projectDTO.getGitlabRepositoryURL().trim().equals("")) {
            AddGitLabRepositoryDTO addGitlabRepositoryDTO = new AddGitLabRepositoryDTO();
            addGitlabRepositoryDTO.setProjectId(savedProject.getProjectId());
            addGitlabRepositoryDTO.setRepositoryURL(projectDTO.getGitlabRepositoryURL());
            addGitlabRepo(addGitlabRepositoryDTO);
        }

        if (!projectDTO.getSonarRepositoryURL().trim().equals("")) {
            AddSonarRepositoryDTO addSonarRepositoryDTO = new AddSonarRepositoryDTO();
            addSonarRepositoryDTO.setProjectId(savedProject.getProjectId());
            addSonarRepositoryDTO.setRepositoryURL(projectDTO.getSonarRepositoryURL());
            addSonarRepo(addSonarRepositoryDTO);
        }

        if (!projectDTO.getTrelloBoardURL().trim().equals("")) {
            AddTrelloBoardDTO addTrelloBoardDTO = new AddTrelloBoardDTO();
            addTrelloBoardDTO.setProjectId(savedProject.getProjectId());
            addTrelloBoardDTO.setRepositoryURL(projectDTO.getSonarRepositoryURL());
            addTrelloBoard(addTrelloBoardDTO);
        }
    }

    public List<ResponseProjectDTO> getMemberProjects(Long memberId) {
        List<Project> projectList = projectDAO.findByMemberId(memberId);
        List<ResponseProjectDTO> projectDTOList = new ArrayList<>();

        for (Project project : projectList) {
            ResponseProjectDTO projectDTO = new ResponseProjectDTO();
            projectDTO.setProjectId(project.getProjectId());
            projectDTO.setProjectName(project.getName());
            projectDTO.setAvatarURL(project.getAvatarURL());
            for (Repository repository : project.getRepositorySet()) {
                RepositoryDTO repositoryDTO = new RepositoryDTO();
                repositoryDTO.setUrl(repository.getUrl());
                repositoryDTO.setType(repository.getType());
                projectDTO.getRepositoryDTOList().add(repositoryDTO);
            }
            projectDTOList.add(projectDTO);
        }
        return projectDTOList;
    }

    public boolean addSonarRepo(AddSonarRepositoryDTO addSonarRepositoryDTO) {
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

    public boolean addGithubRepo(AddGithubRepositoryDTO addGithubRepositoryDTO) throws IOException {
        Optional<Project> projectOptional = projectDAO.findById(addGithubRepositoryDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        String url = addGithubRepositoryDTO.getRepositoryURL();
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

    public boolean addGitlabRepo(AddGitLabRepositoryDTO addGitlabRepositoryDTO) throws GitLabApiException {
        Optional<Project> projectOptional = projectDAO.findById(addGitlabRepositoryDTO.getProjectId());
        if (projectOptional.isEmpty()) return false;

        Project project = projectOptional.get();
        String url = addGitlabRepositoryDTO.getRepositoryURL();
        Repository repository = new Repository();
        repository.setUrl(url);
        repository.setType("gitlab");
        project.getRepositorySet().add(repository);
        String owner = url.split("/")[3];
        String projectName = url.split("/")[4];
        String responseURL = gitlabApiService.getAvatarURL(owner, projectName);
        if (responseURL != null) {
            project.setAvatarURL(responseURL);
        }
        projectDAO.save(project);
        return true;
    }
  
    public boolean addTrelloBoard(AddTrelloBoardDTO addTrelloBoardDTO) {
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
                ResponseProjectDTO projectDTO = new ResponseProjectDTO();
                projectDTO.setProjectId(project.getProjectId());
                projectDTO.setProjectName(project.getName());
                projectDTO.setAvatarURL(project.getAvatarURL());
                for (Repository repository : project.getRepositorySet()) {
                    RepositoryDTO repositoryDTO = new RepositoryDTO();
                    repositoryDTO.setUrl(repository.getUrl());
                    repositoryDTO.setType(repository.getType());
                    projectDTO.getRepositoryDTOList().add(repositoryDTO);
                }
                projectDTOList.add(projectDTO);
            }
        }
        return projectDTOList;
    }
}
