package com.familyvault.repository;

import com.familyvault.entity.EmergencyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyAccessLogRepository extends JpaRepository<EmergencyAccessLog, Long> {
}
