package com.monad.talep.repository;

import com.monad.talep.entity.AppUser;
import com.monad.talep.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    List<AppUser> findByRole_RoleName(RoleName roleName);
}
