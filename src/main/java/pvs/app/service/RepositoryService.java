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
        AtomicBoolean result = new AtomicBoolean(false);

        this.webClient
                .get()
                .uri(targetURL)
                .exchange()
                .doOnSuccess(clientResponse ->
//                        result.set(clientResponse.statusCode().equals(HttpStatus.OK))
                        result.set(true)
                )
                .block();
        return result.get();
    }

    public boolean checkGitlabURL(String url) {
        return url.contains("gitlab.com");
    }

    public boolean checkSonarURL(String url) {
        if (!url.contains("localhost")) {
            return false;
        }

        String targetURL = url.replace("dashboard?id", "api/components/show?component");
        AtomicBoolean result = new AtomicBoolean(false);

        this.webClient
                .get()
                .uri(targetURL)
                .exchange()
                .doOnSuccess(clientResponse ->
                        result.set(clientResponse.statusCode().equals(HttpStatus.OK))
                )
                .block();
        return result.get();
    }
}
