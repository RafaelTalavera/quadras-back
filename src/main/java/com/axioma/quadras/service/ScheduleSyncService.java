package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.ScheduleSyncEventDto;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ScheduleSyncService {

	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter subscribe() {
		final SseEmitter emitter = new SseEmitter(0L);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> {
			emitters.remove(emitter);
			emitter.complete();
		});
		emitter.onError(error -> emitters.remove(emitter));
		sendConnectedEvent(emitter);
		return emitter;
	}

	public void broadcast(ScheduleSyncEventDto event) {
		for (final SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
						.name("schedule-changed")
						.data(event, MediaType.APPLICATION_JSON));
			} catch (IOException | IllegalStateException exception) {
				emitters.remove(emitter);
				try {
					emitter.completeWithError(exception);
				} catch (IllegalStateException ignored) {
					emitter.complete();
				}
			}
		}
	}

	private void sendConnectedEvent(SseEmitter emitter) {
		try {
			emitter.send(SseEmitter.event()
					.name("connected")
					.data(Map.of("status", "connected"), MediaType.APPLICATION_JSON));
		} catch (IOException | IllegalStateException exception) {
			emitters.remove(emitter);
			emitter.complete();
		}
	}
}
