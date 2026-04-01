package com.concord.connectivity;

import com.concord.chat.ChatRelay;
import com.concord.ui.UiManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PacketRouter
{
	private final ConnectionCoordinator connectionCoordinator;
	private final ChatRelay chatRelay;
	private final UiManager uiManager;
	private final SessionManager sessionManager;

	public PacketRouter(
			ConnectionCoordinator connectionCoordinator,
			ChatRelay chatRelay,
			UiManager uiManager,
			SessionManager sessionManager)
	{
		this.connectionCoordinator = connectionCoordinator;
		this.chatRelay = chatRelay;
		this.uiManager = uiManager;
		this.sessionManager = sessionManager;
	}

	public void handleIncomingPacket(ConcordPacket packet)
	{
		if (packet == null)
		{
			return;
		}

		String packetType = packet.getType();
		if ("auth.guestIssued".equalsIgnoreCase(packetType))
		{
			if (connectionCoordinator != null)
			{
				connectionCoordinator.updateFromGuestIssuedPacket(packet);
			}
			if (chatRelay != null)
			{
				chatRelay.updateSuppressedPrefixes(packet.getSuppressedPrefixes());
			}
			if (uiManager != null)
			{
				uiManager.setDiscordInviteUrl(packet.getDiscordInviteUrl());
			}
			if (sessionManager != null)
			{
				log.info("Authenticated guest session for Concord user {}", sessionManager.getAuthenticatedUserId());
			}
			if (connectionCoordinator != null)
			{
				connectionCoordinator.sendGuestProfileSync();
			}
			return;
		}

		if ("config.discordInviteUrl".equalsIgnoreCase(packetType))
		{
			if (uiManager != null)
			{
				uiManager.setDiscordInviteUrl(packet.getDiscordInviteUrl());
			}
			return;
		}

		if ("config.suppressedPrefixes".equalsIgnoreCase(packetType))
		{
			if (chatRelay != null)
			{
				chatRelay.updateSuppressedPrefixes(packet.getSuppressedPrefixes());
				log.info("Updated Concord suppressed prefix list ({} entries)", chatRelay.suppressionRuleCount());
			}
			return;
		}

		if (chatRelay != null)
		{
			chatRelay.handleIncomingChatPacket(packet);
		}
	}
}
