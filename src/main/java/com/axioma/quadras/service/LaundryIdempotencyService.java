package com.axioma.quadras.service;

import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.LaundryIdempotencyRecord;
import com.axioma.quadras.repository.LaundryIdempotencyRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class LaundryIdempotencyService {

	private static final Pattern KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]{1,120}$");
	private static final Duration TTL = Duration.ofHours(24);
	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final LaundryIdempotencyRepository repository;
	private final TransactionTemplate requiresNewTransaction;

	public LaundryIdempotencyService(
			LaundryIdempotencyRepository repository,
			PlatformTransactionManager transactionManager
	) {
		this.repository = repository;
		this.requiresNewTransaction = new TransactionTemplate(transactionManager);
		this.requiresNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	public <T> IdempotentResult<T> execute(
			String actorUsername,
			String idempotencyKey,
			String requestFingerprint,
			Class<T> responseType,
			int successStatus,
			Supplier<T> action
	) {
		validateKey(idempotencyKey);
		final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		for (int attempt = 0; attempt < 2; attempt++) {
			final Optional<LaundryIdempotencyRecord> existing =
					repository.findByActorUsernameAndIdempotencyKey(actorUsername, idempotencyKey);
			if (existing.isPresent()) {
				final LaundryIdempotencyRecord record = existing.get();
				if (record.isExpired(now)) {
					requiresNewTransaction.executeWithoutResult(status -> repository.delete(record));
					continue;
				}
				return existingResult(record, requestFingerprint, responseType);
			}

			try {
				requiresNewTransaction.executeWithoutResult(status -> repository.saveAndFlush(
						LaundryIdempotencyRecord.inProgress(
								actorUsername,
								idempotencyKey,
								requestFingerprint,
								now.plus(TTL)
						)
				));
			} catch (DataIntegrityViolationException ex) {
				continue;
			}

			try {
				final T result = action.get();
				final String body = serialize(result);
				final LaundryIdempotencyRecord record = repository
						.findByActorUsernameAndIdempotencyKey(actorUsername, idempotencyKey)
						.orElseThrow(() -> new IllegalStateException("Idempotency placeholder disappeared"));
				record.complete(successStatus, body);
				repository.save(record);
				return new IdempotentResult<>(successStatus, result, false);
			} catch (RuntimeException ex) {
				release(actorUsername, idempotencyKey);
				throw ex;
			}
		}
		throw new ApplicationException(
				HttpStatus.CONFLICT,
				"A request with this Idempotency-Key is still in progress"
		);
	}

	public String fingerprint(String method, String path, Object body) {
		try {
			final String payload = body == null ? "" : JSON.writeValueAsString(body);
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(
					(method + "\n" + path + "\n" + payload).getBytes(StandardCharsets.UTF_8)
			);
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Unable to fingerprint request", ex);
		} catch (RuntimeException ex) {
			throw new IllegalStateException("Unable to fingerprint request", ex);
		}
	}

	private <T> IdempotentResult<T> existingResult(
			LaundryIdempotencyRecord record,
			String requestFingerprint,
			Class<T> responseType
	) {
		if (!record.getRequestFingerprint().equals(requestFingerprint)) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"Idempotency-Key was already used with a different request"
			);
		}
		if (!record.isCompleted()
				|| record.getResponseStatus() == null
				|| record.getResponseBody() == null) {
			throw new ApplicationException(
					HttpStatus.CONFLICT,
					"A request with this Idempotency-Key is still in progress"
			);
		}
		try {
			final T body = JSON.readValue(record.getResponseBody(), responseType);
			return new IdempotentResult<>(record.getResponseStatus(), body, true);
		} catch (RuntimeException ex) {
			throw new IllegalStateException("Unable to read stored idempotent response", ex);
		}
	}

	private String serialize(Object value) {
		try {
			return JSON.writeValueAsString(value);
		} catch (RuntimeException ex) {
			throw new IllegalStateException("Unable to serialize idempotent response", ex);
		}
	}

	private void release(String actorUsername, String idempotencyKey) {
		try {
			requiresNewTransaction.executeWithoutResult(status ->
					repository.deleteByActorUsernameAndIdempotencyKey(actorUsername, idempotencyKey)
			);
		} catch (RuntimeException ignored) {
			// The placeholder expires on its own if the release cannot be committed.
		}
	}

	private void validateKey(String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Idempotency-Key header is required"
			);
		}
		if (!KEY_PATTERN.matcher(idempotencyKey).matches()) {
			throw new ApplicationException(
					HttpStatus.BAD_REQUEST,
					"Idempotency-Key must contain 1 to 120 characters from [A-Za-z0-9._:-]"
			);
		}
	}
}
