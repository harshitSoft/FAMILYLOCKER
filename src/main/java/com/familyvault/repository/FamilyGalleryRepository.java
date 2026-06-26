package com.familyvault.repository;

import com.familyvault.entity.Family;
import com.familyvault.entity.FamilyGalleryItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyGalleryRepository extends JpaRepository<FamilyGalleryItem, Long> {
    List<FamilyGalleryItem> findByFamilyOrderByCreatedAtDesc(Family family);
    Optional<FamilyGalleryItem> findByIdAndFamily(Long id, Family family);
}
