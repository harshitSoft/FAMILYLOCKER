package com.familyvault.repository;

import com.familyvault.entity.EmergencyAccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyAccessRequestRepository extends JpaRepository<EmergencyAccessRequest, Long> {
}
