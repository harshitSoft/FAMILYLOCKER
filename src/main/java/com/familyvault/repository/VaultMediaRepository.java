package com.familyvault.repository;

import com.familyvault.entity.Locker;
import com.familyvault.entity.VaultMediaItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultMediaRepository extends JpaRepository<VaultMediaItem, Long> {
    List<VaultMediaItem> findByLockerOrderByCreatedAtDesc(Locker locker);
    List<VaultMediaItem> findByLockerAndPrivateHiddenFalseOrderByCreatedAtDesc(Locker locker);
    Optional<VaultMediaItem> findByIdAndLocker(Long id, Locker locker);
}
