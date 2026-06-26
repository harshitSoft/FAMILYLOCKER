package com.familyvault.repository;

import com.familyvault.entity.FamilyMember;
import com.familyvault.entity.Locker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerRepository extends JpaRepository<Locker, Long> {
    Optional<Locker> findByOwner(FamilyMember owner);
}
