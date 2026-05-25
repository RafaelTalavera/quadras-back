package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByUsername(String username);

	boolean existsByUsername(String username);

	List<AppUser> findAllByOrderByUsernameAsc();

	long countByRoleAndEnabledTrue(com.axioma.quadras.domain.model.AppUserRole role);
}
