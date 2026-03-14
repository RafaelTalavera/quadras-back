package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ApplicationUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public ApplicationUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final AppUser appUser = appUserRepository.findByUsername(AppUser.normalizeUsername(username))
				.orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
		return new AuthenticatedUserPrincipal(appUser);
	}
}
