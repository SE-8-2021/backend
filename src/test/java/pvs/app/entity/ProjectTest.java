package pvs.app.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pvs.app.Application;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProjectTest {
    Project project;
    Repository githubRepository;
    Set<Repository> repositorySet;

    @Before
    public void setup() {
        project = new Project();
        project.setMemberId(1L);
        project.setName("myProject");

        githubRepository = new Repository();
        githubRepository.setType("github");
        githubRepository.setUrl("https://github.com/facebook/react");
        githubRepository.setRepositoryId(1L);

        repositorySet = new HashSet<>();
        repositorySet.add(githubRepository);
        project.setRepositorySet(repositorySet);
    }

    @Test
    public void getNameTest() {
        assertEquals("myProject", project.getName());
    }
}