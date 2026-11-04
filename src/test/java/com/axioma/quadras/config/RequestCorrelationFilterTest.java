package com.axioma.quadras.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestCorrelationFilterTest {

	private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

	@Test
	void preservesSafeIncomingCorrelationIdAndClearsMdcAfterRequest() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v2/laundry/dashboard");
		request.addHeader(RequestCorrelationFilter.HEADER, "mobile-request_42");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> valueDuringRequest = new AtomicReference<>();
		FilterChain chain = (req, res) -> valueDuringRequest.set(MDC.get(RequestCorrelationFilter.MDC_KEY));

		filter.doFilter(request, response, chain);

		assertThat(valueDuringRequest.get()).isEqualTo("mobile-request_42");
		assertThat(response.getHeader(RequestCorrelationFilter.HEADER)).isEqualTo("mobile-request_42");
		assertThat(MDC.get(RequestCorrelationFilter.MDC_KEY)).isNull();
	}

	@Test
	void replacesUnsafeCorrelationId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/laundry/shipments");
		request.addHeader(RequestCorrelationFilter.HEADER, "invalid correlation with spaces");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) -> { });

		String generated = response.getHeader(RequestCorrelationFilter.HEADER);
		assertThat(generated).matches("[0-9a-f-]{36}");
		assertThat(generated).isNotEqualTo("invalid correlation with spaces");
	}
}
