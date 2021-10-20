package pvs.app.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import pvs.app.Application;
import pvs.app.entity.Project;
import pvs.app.entity.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProjectDAOIntegrationTest {
    Set<Project> projects;
    Set<Repository> repositories;
    @Autowired
    private ProjectDAO projectDAO;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Project project01 = new Project();
        project01.setMemberId(1L);
        project01.setName("react");

        Project project02 = new Project();
        project02.setMemberId(2L);
        project02.setName("angular");

        Repository repository01 = new Repository();
        repository01.setType("github");
        repository01.setUrl("facebook/react");

        Repository repository02 = new Repository();
        repository02.setType("github");
        repository02.setUrl("angular/angular");

        projects = new HashSet<>();
        repositories = new HashSet<>();

        project01.getRepositorySet().add(repository01);
        project01.getRepositorySet().add(repository02);
        projectDAO.save(project01);
        projectDAO.save(project02);
    }

    @Test
    public void whenFindAll_thenReturnProjectList() {
        List<Project> foundEntityList = projectDAO.findAll();
        assertEquals(2, foundEntityList.size());
        assertEquals(2, foundEntityList.get(0).getRepositorySet().size());
    }
}
