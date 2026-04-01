package com.concord.chat;

import java.util.List;
import com.concord.connectivity.ConnectionCoordinator;
import com.concord.connectivity.ConcordPacket;
import com.concord.connectivity.PacketFactory;
import com.concord.connectivity.SessionManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.events.ChatMessage;

@Slf4j
public final class ChatRelay
{
	private final InjectedMessageTracker injectedMessageTracker;
	private final SuppressionRules suppressionRules = new SuppressionRules();
	private final SessionManager sessionManager;
	private final PacketFactory packetFactory;
	private final ConnectionCoordinator connectionCoordinator;
	private final Client client;
	private final ClientThread clientThread;

	public ChatRelay(
			long injectedMessageTtlMs,
			SessionManager sessionManager,
			PacketFactory packetFactory,
			ConnectionCoordinator connectionCoordinator,
			Client client,
			ClientThread clientThread)
	{
		this.injectedMessageTracker = new InjectedMessageTracker(injectedMessageTtlMs);
		this.sessionManager = sessionManager;
		this.packetFactory = packetFactory;
		this.connectionCoordinator = connectionCoordinator;
		this.client = client;
		this.clientThread = clientThread;
	}

	public void onChatMessage(ChatMessage event)
	{
		// Relay both regular clan chat and system-style clan messages, but ignore
		// recent inbound messages we injected into the client ourselves.
		ChatMessageType messageType = event.getType();
		if (messageType != ChatMessageType.CLAN_CHAT &&
				messageType != ChatMessageType.CLAN_MESSAGE)
		{
			return;
		}

		if (injectedMessageTracker.consumeIfInjected(
				messageType,
				event.getName(),
				event.getMessage(),
				event.getSender()))
		{
			log.debug("Ignoring Concord-injected message: {} -> {}", event.getName(), event.getMessage());
			return;
		}

		String name = event.getName();
		String message = event.getMessage();

		if (message == null || message.isEmpty())
		{
			return;
		}
		if (suppressionRules.isSuppressed(message))
		{
			log.debug("Ignoring suppressed RuneLite message: {}", message);
			return;
		}
		if (!sessionManager.hasSession())
		{
			log.debug("Ignoring outbound chat before Concord guest session is ready");
			return;
		}

		log.debug("Outgoing: {} -> {}", name, message);

		ConcordPacket packet = packetFactory.buildChatMessagePacket(
				sessionManager.getAuthenticatedUserId(),
				sessionManager.getSessionToken(),
				name,
				message
		);

		connectionCoordinator.sendPacket(packet);
	}

	public void handleIncomingChatPacket(ConcordPacket packet)
	{
		if (packet == null) return;
		if (!"chat.message".equalsIgnoreCase(packet.getType())) return;
		if ("runelite".equalsIgnoreCase(packet.getOrigin()))
		{
			log.debug("Ignoring RuneLite-origin packet to prevent relay loop");
			return;
		}

		@NonNull
		String name = packet.getActorName();
		String message = packet.getBody();

		if (message == null) return;

		log.debug("Incoming: {} -> {}", name, message);

		clientThread.invoke(() -> {
			injectedMessageTracker.remember(ChatMessageType.CLAN_CHAT, name, message, "Concord");
			injectedMessageTracker.remember(ChatMessageType.CLAN_CHAT, name, message, "");
			injectedMessageTracker.remember(ChatMessageType.CLAN_CHAT, name, message, null);
			injectedMessageTracker.remember(ChatMessageType.CLAN_MESSAGE, name, message, "Concord");
			injectedMessageTracker.remember(ChatMessageType.CLAN_MESSAGE, name, message, "");
			injectedMessageTracker.remember(ChatMessageType.CLAN_MESSAGE, name, message, null);
			client.addChatMessage(
					ChatMessageType.CLAN_CHAT,
					name,
					message,
					"Concord"
			);
		});
	}

	public void updateSuppressedPrefixes(List<String> suppressedPrefixes)
	{
		suppressionRules.setRules(suppressedPrefixes);
	}

	public int suppressionRuleCount()
	{
		return suppressionRules.size();
	}

	public void clear()
	{
		suppressionRules.clear();
		injectedMessageTracker.clear();
	}
}
