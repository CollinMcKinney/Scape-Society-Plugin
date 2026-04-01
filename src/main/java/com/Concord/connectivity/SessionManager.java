package com.concord.connectivity;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;

public final class SessionManager
{
	private static final String CONFIG_GROUP = "Concord";
	private static final String CONFIG_GUEST_USER_ID = "guestUserId";
	private static final String CONFIG_GUEST_SESSION_TOKEN = "guestSessionToken";

	private final ConfigManager configManager;
	private final Client client;

	private String authenticatedUserId;
	private String sessionToken;
	private String lastProfileSyncUserId;
	private String lastProfileSyncOsrsName;

	public SessionManager(ConfigManager configManager, Client client)
	{
		this.configManager = configManager;
		this.client = client;
	}

	public void loadPersistedGuestSession()
	{
		authenticatedUserId = configManager.getConfiguration(CONFIG_GROUP, CONFIG_GUEST_USER_ID);
		sessionToken = configManager.getConfiguration(CONFIG_GROUP, CONFIG_GUEST_SESSION_TOKEN);
	}

	public void updateFromGuestIssuedPacket(ConcordPacket packet)
	{
		authenticatedUserId = packet.getIssuedUserId();
		sessionToken = packet.getIssuedSessionToken();
		clearProfileSyncState();
		persistGuestSession();
	}

	public boolean hasSession()
	{
		return authenticatedUserId != null && sessionToken != null;
	}

	public String getAuthenticatedUserId()
	{
		return authenticatedUserId;
	}

	public String getLocalPlayerName()
	{
		Player localPlayer = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN || localPlayer == null)
		{
			return null;
		}

		String osrsName = localPlayer.getName();
		if (osrsName == null || osrsName.trim().isEmpty())
		{
			return null;
		}

		return osrsName;
	}

	public boolean shouldSendProfileSync(String osrsName)
	{
		if (!hasSession())
		{
			return false;
		}

		return !(authenticatedUserId.equals(lastProfileSyncUserId)
				&& osrsName.equals(lastProfileSyncOsrsName));
	}

	public void markProfileSynced(String osrsName)
	{
		lastProfileSyncUserId = authenticatedUserId;
		lastProfileSyncOsrsName = osrsName;
	}

	public void clearProfileSyncState()
	{
		lastProfileSyncUserId = null;
		lastProfileSyncOsrsName = null;
	}

	public String getSessionToken()
	{
		return sessionToken;
	}

	private void persistGuestSession()
	{
		if (!hasSession())
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_GUEST_USER_ID, authenticatedUserId);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_GUEST_SESSION_TOKEN, sessionToken);
	}
}
