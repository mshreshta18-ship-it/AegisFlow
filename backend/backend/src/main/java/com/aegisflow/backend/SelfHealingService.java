package com.aegisflow.backend;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class SelfHealingService {

    private static final Duration COOLDOWN =
            Duration.ofSeconds(60);

    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private record ServiceConfig(
            String jarPath,
            String healthUrl) {}

    private final Map<String, ServiceConfig> approvedServices =
            Map.of(
        "Payment Service",
        new ServiceConfig(
            "/workspaces/AegisFlow/payment-service/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar",
            "http://localhost:8081/actuator/health"
        ),

        "Order Service",
        new ServiceConfig(
            "/workspaces/AegisFlow/order-service/order-service/target/order-service-0.0.1-SNAPSHOT.jar",
            "http://localhost:8082/actuator/health"
        ),

        "Inventory Service",
        new ServiceConfig(
            "/workspaces/AegisFlow/inventory-service/inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar",
            "http://localhost:8083/actuator/health"
        )
    );

    private final Map<String, Instant> lastAttempt =
            new HashMap<>();

    private final Map<String, Integer> attempts =
            new HashMap<>();

    public synchronized String restartService(
            String serviceName) {

        ServiceConfig config =
                approvedServices.get(serviceName);

        if (config == null) {
            return "Restart rejected: service is not approved";
        }

        if (isHealthy(config.healthUrl())) {
            return "Restart skipped: service is already healthy";
        }

        int count = attempts.getOrDefault(serviceName, 0);

        if (count >= MAX_ATTEMPTS) {
            return "Restart blocked: maximum attempts reached";
        }

        Instant previous = lastAttempt.get(serviceName);

        if (previous != null &&
                Duration.between(previous, Instant.now())
                        .compareTo(COOLDOWN) < 0) {
            return "Restart blocked: cooldown active";
        }

        File jar = new File(config.jarPath());

        if (!jar.isFile()) {
            return "Restart failed: JAR file not found";
        }

        try {
            Path logPath = Path.of(
                    "/workspaces/AegisFlow",
                    serviceName.toLowerCase()
                            .replace(" ", "-")
                            + "-recovery.log"
            );

            ProcessBuilder builder = new ProcessBuilder(
                    "java", "-jar", config.jarPath()
            );

            builder.redirectErrorStream(true);
            builder.redirectOutput(
                    ProcessBuilder.Redirect.appendTo(
                            logPath.toFile()
                    )
            );

            Process process = builder.start();

            lastAttempt.put(serviceName, Instant.now());
            attempts.put(serviceName, count + 1);

            return "Restart launched for " + serviceName
                    + " (PID: " + process.pid() + ")";

        } catch (IOException e) {
            return "Restart failed: " + e.getMessage();
        }
    }

    private boolean isHealthy(String healthUrl) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}
