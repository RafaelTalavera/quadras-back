package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.domain.model.ProviderAccess;
import com.axioma.quadras.domain.model.ProviderAccessType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUserPrincipal implements UserDetails {

	private final AppUser appUser;
	private final ProviderAccess providerAccess;

	public AuthenticatedUserPrincipal(AppUser appUser) {
		this.appUser = appUser;
		this.providerAccess = appUser.providerAccess();
	}

	public AppUser getAppUser() {
		return appUser;
	}

	public AppUserRole getRole() {
		return appUser.getRole();
	}

	public long getSecurityVersion() {
		return appUser.getSecurityVersion();
	}

	public Long getLaundryProviderId() {
		return appUser.getLaundryProvider() == null ? null : appUser.getLaundryProvider().getId();
	}

	public ProviderAccess getProviderAccess() {
		return providerAccess;
	}

	public ProviderAccessType getProviderType() {
		return providerAccess == null ? null : providerAccess.type();
	}

	public Long getProviderResourceId() {
		return providerAccess == null ? null : providerAccess.resourceId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (appUser.getRole() == AppUserRole.SUPERVISOR) {
			return List.of(
					new SimpleGrantedAuthority(AppUserRole.SUPERVISOR.authority()),
					new SimpleGrantedAuthority(AppUserRole.OPERATOR.authority())
			);
		}
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
