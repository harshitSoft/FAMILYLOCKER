package com.familyvault.repository;

import com.familyvault.entity.Folder;
import com.familyvault.entity.Locker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByLockerOrderByCreatedAtAsc(Locker locker);
    List<Folder> findByLockerAndHiddenFalseOrderByCreatedAtAsc(Locker locker);
    Optional<Folder> findByIdAndLocker(Long id, Locker locker);
}
