package pvs.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@SuppressWarnings("squid:S1192")
public class RepositoryService {
    private final WebClient webClient;

    public RepositoryService(WebClient.Builder webClientBuilder, @Value("${webClient.baseUrl.test}") String baseUrl) {
        String token = System.getenv("PVS_GITHUB_TOKEN");
        this.webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public boolean checkGithubURL(String url) {
        if (!url.contains("github.com")) {
            return false;
        }
        String targetURL = url.replace("github.com", "api.github.com/repos");

        this.webClient
                .get()
                .uri(targetURL)
                .exchange()
                .block();
        // TODO: Check if we can get a successful response from the targetURL. To facilitate development, temporarily force it to be true.
        return true;
    }

    public boolean checkGitLabURL(String url) {
        return url.contains("gitlab.com");
    }

    public boolean checkSonarURL(String url) {
        if (!url.contains("sonarcloud.io")) {
            return false;
        }

        String targetURL = url.replace("project/overview?id", "api/components/show?component");
        AtomicBoolean result = new AtomicBoolean(false);

        this.webClient
                .get()
                .uri(targetURL)
                .exchange()
                .block();
        // TODO: Check if we can get a successful response from the targetURL. To facilitate development, temporarily force it to be true.
        return true;
    }

    public boolean checkTrelloURL(String url) {
        return url.contains("trello.com");
    }
}
