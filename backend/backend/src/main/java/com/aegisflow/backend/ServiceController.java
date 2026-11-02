
package com.aegisflow.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
public class ServiceController {

    private final ServiceHealthLogRepository repository;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    public ServiceController(ServiceHealthLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/services")
    public List<Map<String, String>> getServices() {
        return List.of(
            checkService("AegisFlow Backend",
                "http://localhost:8080/api/health"),
            checkService("Payment Service",
                "http://localhost:8081/actuator/health"),
            checkService("Order Service",
                "http://localhost:8082/actuator/health"),
            checkService("Inventory Service",
                "http://localhost:8083/actuator/health")
        );
    }

    private Map<String, String> checkService(String name, String url) {
        String status = "DOWN";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                status = "UP";
            }

        } catch (Exception e) {
            status = "DOWN";
        }

        // Save health status to database
        ServiceHealthLog log = new ServiceHealthLog(name, status);
        repository.save(log);

        return Map.of(
                "name", name,
                "status", status
        );
    }
}

