package com.axioma.quadras.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties({
		MaintenanceAlertProperties.class,
		PushSubscriptionProperties.class,
		FirebasePushProperties.class
})
@EnableScheduling
public class MaintenanceAlertConfig {

	@Bean("maintenanceOperationalClock")
	Clock maintenanceOperationalClock(MaintenanceAlertProperties properties) {
		return Clock.system(properties.zoneId());
	}
}
