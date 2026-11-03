package com.axioma.quadras.service;

public record IdempotentResult<T>(int status, T body, boolean replay) {
}
