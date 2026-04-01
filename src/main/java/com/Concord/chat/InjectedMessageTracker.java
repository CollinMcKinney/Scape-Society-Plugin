package com.concord.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.ChatMessageType;

public final class InjectedMessageTracker
{
	private final Map<String, Long> fingerprints = new ConcurrentHashMap<>();
	private final long ttlMs;

	public InjectedMessageTracker(long ttlMs)
	{
		this.ttlMs = ttlMs;
	}

	public void remember(ChatMessageType type, String name, String message, String sender)
	{
		fingerprints.put(
				buildMessageFingerprint(type, name, message, sender),
				System.currentTimeMillis()
		);
	}

	public boolean consumeIfInjected(ChatMessageType type, String name, String message, String sender)
	{
		pruneExpired();
		return fingerprints.remove(buildMessageFingerprint(type, name, message, sender)) != null;
	}

	public void clear()
	{
		fingerprints.clear();
	}

	private void pruneExpired()
	{
		long cutoff = System.currentTimeMillis() - ttlMs;
		fingerprints.entrySet().removeIf(entry -> entry.getValue() < cutoff);
	}

	private String buildMessageFingerprint(ChatMessageType type, String name, String message, String sender)
	{
		return String.join("|",
				type.name(),
				normalizeFingerprintPart(name),
				normalizeFingerprintPart(message),
				normalizeFingerprintPart(sender)
		);
	}

	private String normalizeFingerprintPart(String value)
	{
		return value == null ? "" : value.trim();
	}
}
