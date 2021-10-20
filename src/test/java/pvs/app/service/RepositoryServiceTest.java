package pvs.app.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import pvs.app.Application;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RepositoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Before
    public void setup() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        this.repositoryService = new RepositoryService(WebClient.builder(), mockWebServer.url("/").toString());

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader("Content-Type", "application/json")
        );
    }

    @Test
    public void checkSonarURL_thenReturnFalse() {
        boolean exist = repositoryService.checkSonarURL("pvs-springboot");
        Assert.assertFalse(exist);
    }

    @Test
    public void checkSonarURL_thenReturnTrue() throws InterruptedException {
        boolean exist = repositoryService.checkSonarURL("http://localhost:9000/dashboard");
        Assert.assertTrue(true);
    }

    @Test
    public void checkGithubURL_thenReturnFalse() throws InterruptedException {
        boolean exist = repositoryService.checkGithubURL("pvs-springboot");
        Assert.assertFalse(exist);
    }

    @Test
    public void checkGithubURL_thenReturnTrue() throws InterruptedException {
        boolean exist = repositoryService.checkGithubURL("https://github.com/imper0502/pvs-spring-boot");
        Assert.assertTrue(true);
    }

}
