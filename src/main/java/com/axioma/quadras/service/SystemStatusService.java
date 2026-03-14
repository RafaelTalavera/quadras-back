package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.SystemStatus;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SystemStatusService {

	private static final String SERVICE_NAME = "COSTANORTE-BACKEND";

	private final Environment environment;

	public SystemStatusService(Environment environment) {
		this.environment = environment;
	}

	public SystemStatus getCurrentStatus() {
		return new SystemStatus(
				SERVICE_NAME,
				"UP",
				resolveEnvironment(),
				OffsetDateTime.now()
		);
	}

	private String resolveEnvironment() {
		String[] activeProfiles = environment.getActiveProfiles();
		if (activeProfiles.length == 0) {
			return "default";
		}
		return String.join(",", activeProfiles);
	}
}
