package com.axioma.quadras.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "costanorte.maintenance.alerts")
public record MaintenanceAlertProperties(
		@NotNull ZoneId zoneId,
		@NotNull Duration detectorInterval,
		@NotNull Duration overdueReminderInterval,
		@NotNull Duration outboxDispatcherInterval,
		@NotNull Duration outboxClaimTimeout,
		@NotNull Duration outboxRetryInitialDelay,
		@NotNull Duration outboxRetryMaxDelay,
		@NotNull LocalTime operationalStart,
		@NotNull LocalTime operationalEnd,
		int detectorBatchSize,
		int outboxBatchSize,
		int outboxMaxAttempts,
		boolean inAppEnabled,
		boolean detectorEnabled,
		boolean sseEnabled,
		boolean pushEnabled
) {
	public MaintenanceAlertProperties {
		if (detectorInterval != null && detectorInterval.compareTo(Duration.ofSeconds(1)) < 0) {
			throw new IllegalArgumentException("detectorInterval must be >= 1 second");
		}
		if (overdueReminderInterval != null && overdueReminderInterval.compareTo(Duration.ofMinutes(1)) < 0) {
			throw new IllegalArgumentException("overdueReminderInterval must be >= 1 minute");
		}
		if (outboxDispatcherInterval != null && outboxDispatcherInterval.compareTo(Duration.ofSeconds(1)) < 0) {
			throw new IllegalArgumentException("outboxDispatcherInterval must be >= 1 second");
		}
		if (outboxClaimTimeout != null && outboxClaimTimeout.compareTo(Duration.ofSeconds(10)) < 0) {
			throw new IllegalArgumentException("outboxClaimTimeout must be >= 10 seconds");
		}
		if (outboxRetryInitialDelay != null && outboxRetryInitialDelay.compareTo(Duration.ofSeconds(1)) < 0) {
			throw new IllegalArgumentException("outboxRetryInitialDelay must be >= 1 second");
		}
		if (outboxRetryInitialDelay != null && outboxRetryMaxDelay != null
				&& outboxRetryMaxDelay.compareTo(outboxRetryInitialDelay) < 0) {
			throw new IllegalArgumentException("outboxRetryMaxDelay must be >= outboxRetryInitialDelay");
		}
		if (operationalStart != null && operationalEnd != null && !operationalEnd.isAfter(operationalStart)) {
			throw new IllegalArgumentException("operationalEnd must be after operationalStart");
		}
		if (detectorBatchSize < 1 || detectorBatchSize > 500) {
			throw new IllegalArgumentException("detectorBatchSize must be between 1 and 500");
		}
		if (outboxBatchSize < 1 || outboxBatchSize > 500) {
			throw new IllegalArgumentException("outboxBatchSize must be between 1 and 500");
		}
		if (outboxMaxAttempts < 1 || outboxMaxAttempts > 20) {
			throw new IllegalArgumentException("outboxMaxAttempts must be between 1 and 20");
		}
	}
}
