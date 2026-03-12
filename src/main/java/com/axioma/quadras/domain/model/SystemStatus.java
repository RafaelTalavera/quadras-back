package com.axioma.quadras.domain.model;

import java.time.OffsetDateTime;

public record SystemStatus(
		String service,
		String status,
		String environment,
		OffsetDateTime timestamp
) {
}
