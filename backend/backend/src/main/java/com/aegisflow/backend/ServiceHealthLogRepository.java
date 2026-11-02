
package com.aegisflow.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceHealthLogRepository
        extends JpaRepository<ServiceHealthLog, Long> {

    List<ServiceHealthLog> findByServiceNameOrderByTimestampDesc(
            String serviceName
    );
}
