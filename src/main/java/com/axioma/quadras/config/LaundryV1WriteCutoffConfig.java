package com.axioma.quadras.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LaundryV1WriteCutoffConfig implements WebMvcConfigurer {
	private final LaundryV1WriteCutoffInterceptor interceptor;

	public LaundryV1WriteCutoffConfig(LaundryV1WriteCutoffInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor).addPathPatterns("/api/v1/laundry/**");
	}
}
