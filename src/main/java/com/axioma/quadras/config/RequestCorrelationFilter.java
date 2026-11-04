package com.axioma.quadras.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Correlation-ID";
	public static final String MDC_KEY = "correlationId";
	private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestCorrelationFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = correlationId(request.getHeader(HEADER));
		long startedAt = System.nanoTime();
		MDC.put(MDC_KEY, correlationId);
		response.setHeader(HEADER, correlationId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			if (isLaundryRequest(request)) {
				long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
				LOGGER.info("Laundry request completed method={} path={} status={} durationMs={}",
						request.getMethod(), request.getRequestURI(), response.getStatus(), elapsedMillis);
			}
			MDC.remove(MDC_KEY);
		}
	}

	private String correlationId(String candidate) {
		if (candidate != null && SAFE_ID.matcher(candidate.trim()).matches()) {
			return candidate.trim();
		}
		return UUID.randomUUID().toString();
	}

	private boolean isLaundryRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/v1/laundry") || path.startsWith("/api/v2/laundry");
	}
}
