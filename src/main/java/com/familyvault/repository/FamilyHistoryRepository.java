package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyHistoryRepository extends JpaRepository<FamilyHistory, Long> {
    Optional<FamilyHistory> findByFamily(Family family);
}
