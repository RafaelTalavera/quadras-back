package com.axioma.quadras.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "costanorte.push-subscriptions")
public record PushSubscriptionProperties(String encryptionKey) {
}
