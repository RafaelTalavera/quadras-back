package com.axioma.quadras.repository;

import com.axioma.quadras.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	@EntityGraph(attributePaths = {
			"courtPartnerCoach", "massageTherapist", "maintenanceProvider", "laundryProvider"
	})
	Optional<AppUser> findByUsername(String username);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select user from AppUser user where user.username = :username")
	Optional<AppUser> findByUsernameForUpdate(@Param("username") String username);

	boolean existsByUsername(String username);

	@EntityGraph(attributePaths = {
			"courtPartnerCoach", "massageTherapist", "maintenanceProvider", "laundryProvider"
	})
	List<AppUser> findAllByOrderByUsernameAsc();

	List<AppUser> findAllByEnabledTrueOrderByUsernameAsc();

	long countByRoleAndEnabledTrue(com.axioma.quadras.domain.model.AppUserRole role);

	boolean existsByCourtPartnerCoachId(Long providerResourceId);

	boolean existsByCourtPartnerCoachIdAndIdNot(Long providerResourceId, Long userId);

	boolean existsByMassageTherapistId(Long providerResourceId);

	boolean existsByMassageTherapistIdAndIdNot(Long providerResourceId, Long userId);

	boolean existsByMaintenanceProviderId(Long providerResourceId);

	boolean existsByMaintenanceProviderIdAndIdNot(Long providerResourceId, Long userId);

	boolean existsByLaundryProviderId(Long providerResourceId);

	boolean existsByLaundryProviderIdAndIdNot(Long providerResourceId, Long userId);
}
