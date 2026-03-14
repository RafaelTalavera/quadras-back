package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByUsername(String username);
}
