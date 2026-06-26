package com.familyvault.repository;

import com.familyvault.entity.Folder;
import com.familyvault.entity.Locker;
import com.familyvault.entity.VaultFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VaultFileRepository extends JpaRepository<VaultFile, Long> {
    List<VaultFile> findByFolderOrderByCreatedAtDesc(Folder folder);
    @Query("select vf from VaultFile vf where vf.folder.locker = :locker order by vf.createdAt desc")
    List<VaultFile> findByLocker(@Param("locker") Locker locker);
    @Query("select vf from VaultFile vf where vf.folder.locker = :locker and vf.hidden = false and vf.folder.hidden = false order by vf.createdAt desc")
    List<VaultFile> findEmergencyVisibleByLocker(@Param("locker") Locker locker);
    @Query("select vf from VaultFile vf where vf.id = :id and vf.folder.locker = :locker")
    Optional<VaultFile> findByIdAndLocker(@Param("id") Long id, @Param("locker") Locker locker);
}
