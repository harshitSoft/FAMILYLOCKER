package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyImportantDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyImportantDateRepository extends JpaRepository<FamilyImportantDate, Long> {
    List<FamilyImportantDate> findByFamilyOrderByDateValueAsc(Family family);
    Optional<FamilyImportantDate> findByIdAndFamily(Long id, Family family);
}
