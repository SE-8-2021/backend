package pvs.app.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pvs.app.Application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProjectTest {
    final String testProjectName = "myProject";
    Project givenProject;

    @Before
    public void setup() {
        givenProject = new Project();
        givenProject.setName(testProjectName);
    }

    @Test
    public void getNameTest() {
        assertEquals(testProjectName, givenProject.getName());
    }

    @Test
    public void defaultOfRemovedStatusTest() {
        assertFalse(givenProject.isRemoved());
    }

    @Test
    public void removedProjectTest() {
        givenProject.setRemoved(true);
        assertTrue(givenProject.isRemoved());
    }
}
