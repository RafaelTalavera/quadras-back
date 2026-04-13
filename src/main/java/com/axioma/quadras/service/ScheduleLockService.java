package com.axioma.quadras.service;

import com.axioma.quadras.domain.model.ScheduleLock;
import com.axioma.quadras.domain.model.ScheduleLockType;
import com.axioma.quadras.repository.ScheduleLockRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleLockService {

	private static final String GLOBAL_SCOPE_KEY = "GLOBAL";
	private static final int MAX_ACQUIRE_ATTEMPTS = 4;

	private final ScheduleLockRepository scheduleLockRepository;

	public ScheduleLockService(ScheduleLockRepository scheduleLockRepository) {
		this.scheduleLockRepository = scheduleLockRepository;
	}

	public void acquireReservationDates(Collection<LocalDate> reservationDates) {
		acquireLocks(reservationDates.stream()
				.map(date -> new ScheduleLockKey(ScheduleLockType.RESERVATION, GLOBAL_SCOPE_KEY, date))
				.toList());
	}

	public void acquireCourtBookingDates(Collection<LocalDate> bookingDates) {
		acquireLocks(bookingDates.stream()
				.map(date -> new ScheduleLockKey(ScheduleLockType.COURT_BOOKING, GLOBAL_SCOPE_KEY, date))
				.toList());
	}

	public void acquireMassageTherapistDates(Collection<MassageTherapistScheduleKey> bookingKeys) {
		acquireLocks(bookingKeys.stream()
				.map(key -> new ScheduleLockKey(
						ScheduleLockType.MASSAGE_THERAPIST,
						String.valueOf(key.therapistId()),
						key.bookingDate()
				))
				.toList());
	}

	private void acquireLocks(Collection<ScheduleLockKey> keys) {
		if (keys == null || keys.isEmpty()) {
			return;
		}
		keys.stream()
				.filter(key -> key.bookingDate() != null)
				.distinct()
				.sorted(Comparator
						.comparing(ScheduleLockKey::lockType)
						.thenComparing(ScheduleLockKey::scopeKey)
						.thenComparing(ScheduleLockKey::bookingDate))
				.forEach(this::ensureAndLock);
	}

	private void ensureAndLock(ScheduleLockKey key) {
		for (int attempt = 0; attempt < MAX_ACQUIRE_ATTEMPTS; attempt++) {
			if (scheduleLockRepository.findByKeyForUpdate(
					key.lockType(),
					key.scopeKey(),
					key.bookingDate()
			).isPresent()) {
				return;
			}
			try {
				scheduleLockRepository.saveAndFlush(
						ScheduleLock.create(key.lockType(), key.scopeKey(), key.bookingDate())
				);
			} catch (DataIntegrityViolationException ignored) {
				// Another concurrent transaction inserted the same lock row.
			}
		}
		scheduleLockRepository.findByKeyForUpdate(key.lockType(), key.scopeKey(), key.bookingDate())
				.orElseThrow(() -> new IllegalStateException("Unable to acquire schedule lock"));
	}

	public record MassageTherapistScheduleKey(Long therapistId, LocalDate bookingDate) {}

	private record ScheduleLockKey(
			ScheduleLockType lockType,
			String scopeKey,
			LocalDate bookingDate
	) {}
}
