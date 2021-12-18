package pvs.app.controller;

import okhttp3.Headers;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pvs.app.service.SonarApiProxyService;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProxyApiController {
    private final Logger Log = LoggerFactory.getLogger(ProxyApiController.class);
    private final SonarApiProxyService sonarApiProxyService;

    public ProxyApiController(SonarApiProxyService service) {
        this.sonarApiProxyService = service;
    }

    @GetMapping("/proxy/sonar/metrics")
    public ResponseEntity<String> getSonarMetrics(
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false) String metricKeys,
            @RequestParam(required = false) String component
    ) {
        try {
            final Response proxyResponse = sonarApiProxyService.getMetrics(Headers.of(headers), metricKeys, component);
            return ResponseEntity
                    .status(proxyResponse.code())
                    .body(Objects.requireNonNull(proxyResponse.body()).string());
        } catch (final Exception e) {
            Log.warn(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
