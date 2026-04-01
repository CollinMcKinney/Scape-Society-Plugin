package com.concord.connectivity;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;

@Slf4j
public final class ConnectionCoordinator
{
	private final SessionManager sessionManager;
	private final PacketFactory packetFactory;
	private final WssClient webSocket;

	public ConnectionCoordinator(
			SessionManager sessionManager,
			PacketFactory packetFactory,
			WssClient webSocket)
	{
		this.sessionManager = sessionManager;
		this.packetFactory = packetFactory;
		this.webSocket = webSocket;
	}

	public void handleGameStateChanged(GameState newState)
	{
		if (newState == GameState.LOGGED_IN)
		{
			ensureConnectedIfReady();
			sendGuestProfileSync();
			return;
		}

		if (webSocket != null)
		{
			webSocket.disconnect();
		}
	}

	public void handleGameTick(GameState gameState)
	{
		if (gameState != GameState.LOGGED_IN)
		{
			return;
		}

		ensureConnectedIfReady();
		sendGuestProfileSync();
	}

	public void sendPacket(ConcordPacket packet)
	{
		if (packet == null)
		{
			log.warn("Tried to send null packet");
			return;
		}

		if (webSocket == null)
		{
			log.warn("WebSocket not initialized");
			return;
		}

		log.debug("Sending packet: {}", packet.toJson());

		webSocket.send(packet);
	}

	public void sendGuestProfileSync()
	{
		if (!sessionManager.hasSession())
		{
			return;
		}

		String osrsName = sessionManager.getLocalPlayerName();
		if (osrsName == null)
		{
			log.debug("Skipping guest profile sync because local player name is not ready");
			return;
		}

		if (!sessionManager.shouldSendProfileSync(osrsName))
		{
			return;
		}

		ConcordPacket packet = packetFactory.buildProfileSyncPacket(
				sessionManager.getAuthenticatedUserId(),
				sessionManager.getSessionToken(),
				osrsName
		);

		sendPacket(packet);
		sessionManager.markProfileSynced(osrsName);
		log.debug("Sent guest profile sync for {}", osrsName);
	}

	public void onSocketConnected()
	{
		if (!sessionManager.hasSession())
		{
			return;
		}

		String osrsName = sessionManager.getLocalPlayerName();
		ConcordPacket packet = packetFactory.buildResumePacket(
				sessionManager.getAuthenticatedUserId(),
				sessionManager.getSessionToken(),
				osrsName
		);

		sendPacket(packet);
		log.debug("Sent guest session resume for {}", sessionManager.getAuthenticatedUserId());
	}

	public void onSocketDisconnected()
	{
		// Currently only clears suppression rules in chat relay.
	}

	public void updateServerUrl(String serverUrl)
	{
		if (webSocket == null)
		{
			return;
		}

		webSocket.setServerUrl(serverUrl);
		webSocket.disconnect();
		ensureConnectedIfReady();
	}

	public void ensureConnectedIfReady()
	{
		if (webSocket == null)
		{
			return;
		}

		if (!isConnectionReady())
		{
			log.debug("Waiting to connect to Concord until local player name is available");
			return;
		}

		webSocket.connect();
	}

	public boolean isConnectionReady()
	{
		return sessionManager.getLocalPlayerName() != null;
	}

	public void updateFromGuestIssuedPacket(ConcordPacket packet)
	{
		sessionManager.updateFromGuestIssuedPacket(packet);
	}

	public void clearProfileSyncState()
	{
		sessionManager.clearProfileSyncState();
	}

	public void shutdown()
	{
		if (webSocket != null)
		{
			webSocket.shutdown();
		}
	}
}
