package com.axioma.quadras.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentActorService {

	public String currentUsername() {
		final AuthenticatedUserPrincipal principal = currentPrincipal();
		return principal == null ? null : principal.getUsername();
	}

	public String currentRole() {
		final AuthenticatedUserPrincipal principal = currentPrincipal();
		return principal == null ? null : principal.getRole().name();
	}

	private AuthenticatedUserPrincipal currentPrincipal() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
			return null;
		}
		return principal;
	}
}
