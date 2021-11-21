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
    private final GitlabApiService gitlabApiService;

    public ProjectService(ProjectDAO projectDAO, GithubApiService githubApiService, GitlabApiService gitlabApiService) {
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

        if (!projectDTO.getGithubRepositoryURL().equals("")) {
            AddGithubRepositoryDTO addGithubRepositoryDTO = new AddGithubRepositoryDTO();
            addGithubRepositoryDTO.setProjectId(savedProject.getProjectId());
            addGithubRepositoryDTO.setRepositoryURL(projectDTO.getGithubRepositoryURL());
            addGithubRepo(addGithubRepositoryDTO);
        }

        if (!projectDTO.getGitlabRepositoryURL().equals("")) {
            AddGitlabRepositoryDTO addGitlabRepositoryDTO = new AddGitlabRepositoryDTO();
            addGitlabRepositoryDTO.setProjectId(savedProject.getProjectId());
            addGitlabRepositoryDTO.setRepositoryURL(projectDTO.getGitlabRepositoryURL());
            addGitlabRepo(addGitlabRepositoryDTO);
        }

        if (!projectDTO.getSonarRepositoryURL().equals("")) {
            AddSonarRepositoryDTO addSonarRepositoryDTO = new AddSonarRepositoryDTO();
            addSonarRepositoryDTO.setProjectId(savedProject.getProjectId());
            addSonarRepositoryDTO.setRepositoryURL(projectDTO.getSonarRepositoryURL());
            addSonarRepo(addSonarRepositoryDTO);
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
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            Repository repository = new Repository();
            repository.setUrl(addSonarRepositoryDTO.getRepositoryURL());
            repository.setType("sonar");
            project.getRepositorySet().add(repository);
            projectDAO.save(project);
            return true;
        } else {
            return false;
        }
    }

    public boolean addGithubRepo(AddGithubRepositoryDTO addGithubRepositoryDTO) throws IOException {
        Optional<Project> projectOptional = projectDAO.findById(addGithubRepositoryDTO.getProjectId());
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            String url = addGithubRepositoryDTO.getRepositoryURL();
            Repository repository = new Repository();
            repository.setUrl(url);
            repository.setType("github");
            project.getRepositorySet().add(repository);
            String owner = url.split("/")[3]; // Get gitHub project owner name by split project url
            JsonNode responseJson = githubApiService.getAvatarURL(owner);
            if (null != responseJson) {
                String json = responseJson.textValue();
                project.setAvatarURL(json);
            }
            projectDAO.save(project);
            return true;
        } else {
            return false;
        }
    }

    public boolean addGitlabRepo(AddGitlabRepositoryDTO addGitlabRepositoryDTO) throws GitLabApiException {
        Optional<Project> projectOptional = projectDAO.findById(addGitlabRepositoryDTO.getProjectId());
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            String url = addGitlabRepositoryDTO.getRepositoryURL();
            Repository repository = new Repository();
            repository.setUrl(url);
            repository.setType("gitlab");
            project.getRepositorySet().add(repository);
            String owner = url.split("/")[2];
            String projectName = url.split("/")[3];
            String responseURL = gitlabApiService.getAvatarURL(owner, projectName);
            if (null != responseURL) {
                project.setAvatarURL(responseURL);
            }
            projectDAO.save(project);
            return true;
        } else {
            return false;
        }
    }
}
