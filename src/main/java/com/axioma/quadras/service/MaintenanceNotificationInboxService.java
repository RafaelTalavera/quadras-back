package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.MaintenanceNotificationDto;
import com.axioma.quadras.domain.dto.MaintenanceNotificationPageDto;
import com.axioma.quadras.domain.dto.UnreadNotificationCountDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.repository.MaintenanceNotificationRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MaintenanceNotificationInboxService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final MaintenanceNotificationRepository notificationRepository;
	private final Clock operationalClock;
	private final MaintenanceNotificationEventPublisher eventPublisher;

	public MaintenanceNotificationInboxService(
			MaintenanceNotificationRepository notificationRepository,
			@Qualifier("maintenanceOperationalClock") Clock operationalClock,
			MaintenanceNotificationEventPublisher eventPublisher
	) {
		this.notificationRepository = notificationRepository;
		this.operationalClock = operationalClock;
		this.eventPublisher = eventPublisher;
	}

	public MaintenanceNotificationPageDto list(
			String username,
			String module,
			MaintenanceNotificationStatus status,
			Integer page,
			Integer size
	) {
		validateModule(module);
		final int safePage = page == null ? 0 : page;
		final int safeSize = size == null ? DEFAULT_PAGE_SIZE : size;
		if (safePage < 0) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "page must be >= 0");
		}
		if (safeSize < 1 || safeSize > MAX_PAGE_SIZE) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100");
		}
		final var notifications = notificationRepository.findInbox(
				username,
				status,
				PageRequest.of(safePage, safeSize)
		);
		return new MaintenanceNotificationPageDto(
				safePage,
				safeSize,
				notifications.hasNext(),
				notifications.getContent().stream().map(MaintenanceNotificationDto::from).toList()
		);
	}

	public UnreadNotificationCountDto unreadCount(String username, String module) {
		validateModule(module);
		return new UnreadNotificationCountDto(notificationRepository.countByRecipientUsernameAndStatus(
				username,
				MaintenanceNotificationStatus.UNREAD
		));
	}

	@Transactional
	public void markRead(Long notificationId, String username) {
		final int updated = notificationRepository.markRead(notificationId, username, nowUtc());
		ensureOwned(notificationId, username, updated);
		if (updated > 0) {
			eventPublisher.publishChanged();
		}
	}

	@Transactional
	public void dismiss(Long notificationId, String username) {
		final int updated = notificationRepository.dismiss(notificationId, username, nowUtc());
		ensureOwned(notificationId, username, updated);
		if (updated > 0) {
			eventPublisher.publishChanged();
		}
	}

	private void ensureOwned(Long notificationId, String username, int updated) {
		if (updated == 0 && !notificationRepository.existsByIdAndRecipientUsername(notificationId, username)) {
			throw new ApplicationException(HttpStatus.NOT_FOUND, "Notification not found.");
		}
	}

	private OffsetDateTime nowUtc() {
		return OffsetDateTime.ofInstant(operationalClock.instant(), ZoneOffset.UTC);
	}

	private void validateModule(String module) {
		if (module != null && !module.equalsIgnoreCase("maintenance")) {
			throw new ApplicationException(HttpStatus.BAD_REQUEST, "Unsupported notification module.");
		}
	}
}
