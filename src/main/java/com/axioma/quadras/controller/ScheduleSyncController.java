package com.axioma.quadras.controller;

import com.axioma.quadras.service.ScheduleSyncService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sync")
public class ScheduleSyncController {

	private final ScheduleSyncService scheduleSyncService;

	public ScheduleSyncController(ScheduleSyncService scheduleSyncService) {
		this.scheduleSyncService = scheduleSyncService;
	}

	@GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter events() {
		return scheduleSyncService.subscribe();
	}
}
