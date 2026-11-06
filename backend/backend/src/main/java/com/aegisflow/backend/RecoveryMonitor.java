package com.aegisflow.backend;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecoveryMonitor {

    private final SelfHealingService selfHealingService;

    private final Map<String, Integer> failureCounts =
            new ConcurrentHashMap<>();

    private final Map<String, String> healthUrls = Map.of(
            "Payment Service", "http://localhost:8081/actuator/health",
            "Order Service", "http://localhost:8082/actuator/health",
            "Inventory Service", "http://localhost:8083/actuator/health"
    );

    public RecoveryMonitor(SelfHealingService selfHealingService) {
        this.selfHealingService = selfHealingService;
    }

    @Scheduled(fixedRate = 15000)
    public void monitorServices() {
        healthUrls.forEach((service, url) -> {
            try {
                var connection =
                        new java.net.URL(url).openConnection();

                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                int status =
                        ((java.net.HttpURLConnection) connection)
                                .getResponseCode();

                if (status == 200) {
                    failureCounts.put(service, 0);
                } else {
                    handleFailure(service);
                }

            } catch (Exception e) {
                handleFailure(service);
            }
        });
    }

    private void handleFailure(String service) {
        int failures =
                failureCounts.merge(service, 1, Integer::sum);

        if (failures >= 2) {
            selfHealingService.restartService(service);
            failureCounts.put(service, 0);
        }
    }
}