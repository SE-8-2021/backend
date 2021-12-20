package pvs.app.service;

import okhttp3.Headers;
import okhttp3.Response;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;
import pvs.app.service.http.HttpService;
import pvs.app.service.http.HttpServiceImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

@Service
public class SonarApiProxyService {
    private final HttpService httpService = new HttpServiceImpl();

    public Response getMetrics(Headers originHeaders, String metricKeys, String componentKey)
            throws URISyntaxException, ExecutionException, InterruptedException {
        final String baseUrl = "https://sonarcloud.io/api";
        final String apiPath = "/measures/component";

        final URI requestUri = new URIBuilder(baseUrl + apiPath)
                .addParameter("metricKeys", metricKeys)
                .addParameter("component", componentKey)
                .build();

        return this.httpService.get(originHeaders, requestUri);
    }
}
