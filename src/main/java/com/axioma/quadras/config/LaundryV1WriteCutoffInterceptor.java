package com.axioma.quadras.config;

import com.axioma.quadras.domain.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LaundryV1WriteCutoffInterceptor implements HandlerInterceptor {
	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
	private final boolean writeEnabled;

	public LaundryV1WriteCutoffInterceptor(
			@Value("${costanorte.laundry.v1.write-enabled:false}") boolean writeEnabled) {
		this.writeEnabled = writeEnabled;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!writeEnabled && !SAFE_METHODS.contains(request.getMethod())) {
			throw new ApplicationException(HttpStatus.GONE,
					"Laundry V1 writes are disabled; use /api/v2/laundry");
		}
		return true;
	}
}
