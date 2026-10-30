package com.axioma.quadras.controller;

import com.axioma.quadras.domain.dto.MaintenanceNotificationPageDto;
import com.axioma.quadras.domain.dto.UnreadNotificationCountDto;
import com.axioma.quadras.domain.model.MaintenanceNotificationStatus;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.MaintenanceNotificationInboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final MaintenanceNotificationInboxService inboxService;

	public NotificationController(MaintenanceNotificationInboxService inboxService) {
		this.inboxService = inboxService;
	}

	@GetMapping
	public ResponseEntity<MaintenanceNotificationPageDto> list(
			@RequestParam(required = false) String module,
			@RequestParam(required = false) MaintenanceNotificationStatus status,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(inboxService.list(principal.getUsername(), module, status, page, size));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<UnreadNotificationCountDto> unreadCount(
			@RequestParam(required = false) String module,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		return ResponseEntity.ok(inboxService.unreadCount(principal.getUsername(), module));
	}

	@PatchMapping("/{notificationId}/read")
	public ResponseEntity<Void> markRead(
			@PathVariable Long notificationId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		inboxService.markRead(notificationId, principal.getUsername());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{notificationId}/dismiss")
	public ResponseEntity<Void> dismiss(
			@PathVariable Long notificationId,
			@AuthenticationPrincipal AuthenticatedUserPrincipal principal
	) {
		inboxService.dismiss(notificationId, principal.getUsername());
		return ResponseEntity.noContent().build();
	}
}
