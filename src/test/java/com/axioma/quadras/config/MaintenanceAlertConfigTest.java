package com.axioma.quadras.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"costanorte.security.bootstrap-supervisor.enabled=false",
		"costanorte.security.demo-user.enabled=false"
})
class MaintenanceAlertConfigTest {

	@Autowired
	private MaintenanceAlertProperties properties;

	@Autowired
	private FirebasePushProperties firebaseProperties;

	@Autowired
	@Qualifier("maintenanceOperationalClock")
	private Clock operationalClock;

	@Test
	void shouldUseSafeOperationalDefaults() {
		assertThat(properties.zoneId()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
		assertThat(properties.detectorInterval()).isEqualTo(Duration.ofMinutes(1));
		assertThat(properties.overdueReminderInterval()).isEqualTo(Duration.ofMinutes(60));
		assertThat(properties.outboxDispatcherInterval()).isEqualTo(Duration.ofSeconds(10));
		assertThat(properties.outboxClaimTimeout()).isEqualTo(Duration.ofMinutes(2));
		assertThat(properties.outboxRetryInitialDelay()).isEqualTo(Duration.ofSeconds(30));
		assertThat(properties.outboxRetryMaxDelay()).isEqualTo(Duration.ofMinutes(30));
		assertThat(properties.operationalStart()).isEqualTo(LocalTime.of(6, 0));
		assertThat(properties.operationalEnd()).isEqualTo(LocalTime.of(23, 0));
		assertThat(properties.detectorBatchSize()).isEqualTo(100);
		assertThat(properties.outboxBatchSize()).isEqualTo(50);
		assertThat(properties.outboxMaxAttempts()).isEqualTo(8);
		assertThat(properties.inAppEnabled()).isFalse();
		assertThat(properties.detectorEnabled()).isFalse();
		assertThat(properties.sseEnabled()).isFalse();
		assertThat(properties.pushEnabled()).isFalse();
		assertThat(firebaseProperties.enabled()).isFalse();
		assertThat(firebaseProperties.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
		assertThat(firebaseProperties.requestTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(operationalClock.getZone()).isEqualTo(properties.zoneId());
	}
}
