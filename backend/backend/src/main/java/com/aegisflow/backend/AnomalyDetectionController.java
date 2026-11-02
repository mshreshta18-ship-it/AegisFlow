
package com.aegisflow.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
public class AnomalyDetectionController {

    private final ServiceHealthLogRepository repository;

    public AnomalyDetectionController(
            ServiceHealthLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/anomalies")
    public List<Map<String, Object>> detectAnomalies() {

        return repository.findAll().stream()
            .collect(Collectors.groupingBy(
                ServiceHealthLog::getServiceName
            ))
            .entrySet().stream()
            .map(entry -> {
                List<ServiceHealthLog> logs = entry.getValue();

                long failures = logs.stream()
                    .filter(log -> "DOWN".equals(log.getStatus()))
                    .count();

                double failureRate =
                    (failures * 100.0) / logs.size();

                String riskLevel =
                    failureRate >= 30 ? "HIGH" :
                    failureRate >= 10 ? "MEDIUM" : "LOW";

                Map<String, Object> result =
                    new LinkedHashMap<>();

                result.put("serviceName", entry.getKey());
                result.put("totalChecks", logs.size());
                result.put("failures", failures);
                result.put("failureRate",
                    Math.round(failureRate * 100.0) / 100.0);
                result.put("riskLevel", riskLevel);

                return result;
            })
            .toList();
    }
}
