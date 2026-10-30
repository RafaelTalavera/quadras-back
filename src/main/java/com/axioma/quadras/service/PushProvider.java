package com.axioma.quadras.service;

public interface PushProvider {
	void deliver(PushMessage message) throws Exception;
}
