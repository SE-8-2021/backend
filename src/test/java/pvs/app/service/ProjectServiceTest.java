package pvs.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import pvs.app.Application;
import pvs.app.dao.ProjectDAO;
import pvs.app.dto.CreateProjectDTO;
import pvs.app.dto.ResponseProjectDTO;
import pvs.app.entity.Project;
import pvs.app.entity.Repository;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProjectServiceTest {
    final String responseJson = "{\"avatarUrl\":\"https://avatars3.githubusercontent.com/u/17744001?u=038d9e068c4205d94c670d7d89fb921ec5b29782&v=4\"}";
    final Long memberID = 1L;
    CreateProjectDTO projectDTO;
    Project project;
    Repository githubRepository;
    Set<Repository> repositorySet;
    Optional<JsonNode> mockAvatar;
    @Autowired
    private ProjectService projectService;
    @MockBean
    private GithubApiService githubApiService;
    @MockBean
    private ProjectDAO projectDAO;

    @Before
    public void setup() throws IOException {
        projectDTO = new CreateProjectDTO();
        projectDTO.setProjectName("react");

        project = new Project();
        project.setProjectId(1L);
        project.setMemberId(memberID);
        project.setName(projectDTO.getProjectName());

        githubRepository = new Repository();
        githubRepository.setType("github");
        githubRepository.setUrl("https://github.com/facebook/react");
        githubRepository.setRepositoryId(1L);

        repositorySet = new HashSet<>();
        repositorySet.add(githubRepository);
        project.setRepositorySet(repositorySet);

        ObjectMapper mapper = new ObjectMapper();
        mockAvatar = Optional.ofNullable(mapper.readTree(responseJson));
    }

    @Test
    public void getMemberProjects() {
        //given
        project.setAvatarURL("https://avatars3.githubusercontent.com/u/17744001?u=038d9e068c4205d94c670d7d89fb921ec5b29782&v=4");

        List<ResponseProjectDTO> projectDTOList = new ArrayList<>();
        ResponseProjectDTO projectDTO = new ResponseProjectDTO();
        projectDTO.setProjectId(project.getProjectId());
        projectDTO.setProjectName(project.getName());
        projectDTO.setAvatarURL(project.getAvatarURL());

        projectDTOList.add(projectDTO);

        //when
        when(projectDAO.findByMemberId(1L))
                .thenReturn(List.of(project));
        //then
        assertEquals(1, projectService.getMemberProjects(1L).size());
//        assertTrue(projectDTOList.equals(projectService.getMemberProjects(1L)));
    }

    @Test
    public void removeProjectsAndGetTheActiveProjects() {
        // when
        when(projectDAO.findByMemberId(memberID))
                .thenReturn(List.of(project));
        when(projectDAO.findById(project.getProjectId()))
                .thenReturn(Optional.of(project));

        // then
        assertEquals(1, projectService.getMemberActiveProjects(memberID).size());
        assertTrue(projectService.removeProjectById(project.getProjectId()));
        assertEquals(0, projectService.getMemberActiveProjects(memberID).size());
    }
}
