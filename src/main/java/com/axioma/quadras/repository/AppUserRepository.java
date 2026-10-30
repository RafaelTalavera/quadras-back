package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByUsername(String username);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select user from AppUser user where user.username = :username")
	Optional<AppUser> findByUsernameForUpdate(@Param("username") String username);

	boolean existsByUsername(String username);

	List<AppUser> findAllByOrderByUsernameAsc();

	List<AppUser> findAllByEnabledTrueOrderByUsernameAsc();

	long countByRoleAndEnabledTrue(com.axioma.quadras.domain.model.AppUserRole role);
}
