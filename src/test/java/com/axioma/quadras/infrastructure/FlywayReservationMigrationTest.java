package com.axioma.quadras.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:flyway_migration_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.flyway.enabled=true",
		"spring.jpa.hibernate.ddl-auto=validate"
})
class FlywayReservationMigrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldCreateReservationsTableViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_NAME = 'RESERVATIONS'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateUsersTableViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_NAME = 'APP_USERS'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateMassageProvidersTableViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_NAME = 'MASSAGE_PROVIDERS'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateMassageBookingsTableViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_NAME = 'MASSAGE_BOOKINGS'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateMassageTherapistsTableViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_NAME = 'MASSAGE_THERAPISTS'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateMaintenanceProviderSpecialtyColumnViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MAINTENANCE_PROVIDERS'
				  AND COLUMN_NAME = 'SPECIALTY'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldCreateMaintenanceWorkflowColumnsViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MAINTENANCE_ORDERS'
				  AND COLUMN_NAME IN (
				    'REQUEST_ORIGIN',
				    'REQUESTED_FOR_GUEST',
				    'GUEST_NAME',
				    'GUEST_REFERENCE',
				    'REQUESTED_BY_USERNAME',
				    'REQUESTED_BY_ROLE',
				    'BUSINESS_PRIORITY',
				    'ESTIMATED_EXECUTION_MINUTES',
				    'ASSIGNED_USERNAME',
				    'ASSIGNED_AT'
				  )
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(10);
	}

	@Test
	void shouldAllowNullableMaintenanceProviderSnapshotsViaFlyway() {
		final List<String> nullableFlags = jdbcTemplate.queryForList(
				"""
				SELECT IS_NULLABLE
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MAINTENANCE_ORDERS'
				  AND COLUMN_NAME IN (
				    'PROVIDER_ID',
				    'PROVIDER_TYPE_SNAPSHOT',
				    'PROVIDER_NAME_SNAPSHOT',
				    'SERVICE_LABEL_SNAPSHOT'
				  )
				ORDER BY COLUMN_NAME
				""",
				String.class
		);

		assertThat(nullableFlags).hasSize(4);
		assertThat(nullableFlags).allMatch("YES"::equals);
	}

	@Test
	void shouldCreateMassagePaymentColumnsViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MASSAGE_BOOKINGS'
				  AND COLUMN_NAME IN ('PAYMENT_METHOD', 'PAYMENT_DATE', 'PAYMENT_NOTES')
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(3);
	}

	@Test
	void shouldCreateMassageStatusAndAuditColumnsViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MASSAGE_BOOKINGS'
				  AND COLUMN_NAME IN (
				    'STATUS',
				    'CANCELLATION_NOTES',
				    'CANCELLED_AT',
				    'CREATED_BY',
				    'UPDATED_BY',
				    'CANCELLED_BY'
				  )
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(6);
	}

	@Test
	void shouldCreateMassageTherapistColumnInBookingsViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM INFORMATION_SCHEMA.COLUMNS
				WHERE TABLE_NAME = 'MASSAGE_BOOKINGS'
				  AND COLUMN_NAME = 'THERAPIST_ID'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(1);
	}

	@Test
	void shouldSeedCanonicalMassageProvidersViaFlyway() {
		final List<String> providers = jdbcTemplate.queryForList(
				"""
				SELECT name
				FROM massage_providers
				ORDER BY name
				""",
				String.class
		);

		assertThat(providers).containsExactly("Danuska", "David");
	}

	@Test
	void shouldSeedCanonicalMassageTherapistsViaFlyway() {
		final List<String> therapists = jdbcTemplate.queryForList(
				"""
				SELECT CONCAT(mp.name, ':', mt.name)
				FROM massage_therapists mt
				JOIN massage_providers mp ON mp.id = mt.provider_id
				ORDER BY mp.name, mt.name
				""",
				String.class
		);

		assertThat(therapists).containsExactly(
				"Danuska:Danuska",
				"David:David",
				"David:Isabelita",
				"David:Maria"
		);
	}

	@Test
	void shouldSeedMassageBookingsForAprilAndMayViaFlyway() {
		final Integer count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM massage_bookings
				WHERE booking_date BETWEEN DATE '2026-04-01' AND DATE '2026-05-31'
				""",
				Integer.class
		);

		assertThat(count).isEqualTo(123);
	}

	@Test
	void shouldKeepDailyMassageSeedBetweenOneAndThreeBookings() {
		final List<Integer> dailyCounts = jdbcTemplate.queryForList(
				"""
				SELECT COUNT(*)
				FROM massage_bookings
				WHERE booking_date BETWEEN DATE '2026-04-01' AND DATE '2026-05-31'
				GROUP BY booking_date
				ORDER BY booking_date
				""",
				Integer.class
		);

		assertThat(dailyCounts).hasSize(61);
		assertThat(dailyCounts).allMatch(count -> count >= 1 && count <= 3);
		assertThat(dailyCounts).contains(1, 2, 3);
	}
}
