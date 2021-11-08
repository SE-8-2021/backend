package pvs.app.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pvs.app.Application;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProjectTest {
    Project project;
    final String testProjectName = "myProject";

    @Before
    public void setup() {
        project = new Project();
        project.setMemberId(1L);
        project.setName(testProjectName);
    }

    @Test
    public void getNameTest() {
        assertEquals(testProjectName, project.getName());
    }
}
