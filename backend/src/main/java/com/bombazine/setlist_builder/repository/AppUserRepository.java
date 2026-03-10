package com.bombazine.setlist_builder.repository;

import com.bombazine.setlist_builder.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsernameAndDeletedAtIsNull(String username);

    Optional<AppUser> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByUsernameAndDeletedAtIsNull(String username);

    List<AppUser> findByDeletedAtIsNull();
}
