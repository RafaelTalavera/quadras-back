package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUserPrincipal implements UserDetails {

	private final AppUser appUser;

	public AuthenticatedUserPrincipal(AppUser appUser) {
		this.appUser = appUser;
	}

	public AppUser getAppUser() {
		return appUser;
	}

	public AppUserRole getRole() {
		return appUser.getRole();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(appUser.getRole().authority()));
	}

	@Override
	public String getPassword() {
		return appUser.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return appUser.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return appUser.isEnabled();
	}
}
