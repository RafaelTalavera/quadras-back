package com.axioma.quadras.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
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
}
