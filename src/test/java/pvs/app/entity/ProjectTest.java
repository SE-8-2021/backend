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
    Project myProject;
    final String testProjectName = "myProject";
    Integer defaultStatus = 1;
    Integer deletedStatus = 0;

    @Before
    public void setup() {
        myProject = new Project();
        myProject.setMemberId(1L);
        myProject.setName(testProjectName);
    }

    @Test
    public void getNameTest() {
        assertEquals(testProjectName, myProject.getName());
    }

    @Test
    public void defaultOfStatusTest() {
        assertEquals(defaultStatus, myProject.getStatus());
    }

    @Test
    public void deleteMyProjectTest() {
        myProject.setStatus(deletedStatus);
        assertEquals(deletedStatus, myProject.getStatus());
    }
}
