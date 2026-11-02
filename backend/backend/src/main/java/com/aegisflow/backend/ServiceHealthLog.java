
package com.aegisflow.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_health_logs")
public class ServiceHealthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;
    private String status;
    private LocalDateTime timestamp;

    public ServiceHealthLog() {
    }

    public ServiceHealthLog(String serviceName, String status) {
        this.serviceName = serviceName;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
