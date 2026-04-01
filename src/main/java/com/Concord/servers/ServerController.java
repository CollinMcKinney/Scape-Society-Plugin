package com.concord.servers;

import com.concord.connectivity.ConnectionCoordinator;
import com.concord.ui.UiManager;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.clan.ClanChannel;

@Slf4j
public final class ServerController
{
	private final ServerPreferences preferences;
	private final ConnectionCoordinator connectionCoordinator;
	private final UiManager uiManager;
	private final Client client;

	public ServerController(
			ServerPreferences preferences,
			ConnectionCoordinator connectionCoordinator,
			UiManager uiManager,
			Client client)
	{
		this.preferences = preferences;
		this.connectionCoordinator = connectionCoordinator;
		this.uiManager = uiManager;
		this.client = client;
	}

	public void start()
	{
		applyServerConfig();
		uiManager.setConnectionStatusNoServerSelected();
	}

	/**
	 * Applies the current server configuration and attempts to connect.
	 */
	public void applyServerConfig()
	{
		ClanServer selectedServer = preferences.getSelectedServer();
		String manualUrl = preferences.getManualServerUrl();

		// Use manual URL if entered, otherwise use selected server
		String urlToUse = manualUrl != null && !manualUrl.isEmpty() ? manualUrl : selectedServer.getUrl();

		String normalized = normalizeServerUrl(urlToUse);
		if (normalized == null || normalized.isEmpty() || selectedServer == ClanServer.NONE)
		{
			uiManager.setConnectionStatusNoServerSelected();
			return;
		}

		// Check if the player is in the required clan for this server
		if (!isPlayerInClan(selectedServer.getClanName()))
		{
			uiManager.showMessage("You must be a member of the '" + selectedServer.getClanName() + "' clan to connect to this server.");
			uiManager.setConnectionStatusNoServerSelected();
			return;
		}

		connectionCoordinator.updateServerUrl(normalized);
		uiManager.setConnectionStatusConnecting();
	}

	public void shutdown()
	{
		// Nothing to dispose yet.
	}

	/**
	 * Checks if the player is currently in a clan with the specified name.
	 * @param clanName The clan name to check for.
	 * @return true if the player is in the clan, false otherwise.
	 */
	private boolean isPlayerInClan(String clanName)
	{
		// "(None)" and "LocalHost" don't require clan membership
		if ("(None)".equals(clanName) || "LocalHost".equalsIgnoreCase(clanName))
		{
			return true;
		}

		ClanChannel clanChannel = client.getClanChannel();
		if (clanChannel == null)
		{
			return false;
		}

		String currentClanName = clanChannel.getName();
		if (currentClanName == null)
		{
			return false;
		}

		return currentClanName.equalsIgnoreCase(clanName);
	}

	private String normalizeServerUrl(String rawUrl)
	{
		if (rawUrl == null || rawUrl.trim().isEmpty())
		{
			// Empty URL - represents "(None)" option
			return "";
		}

		String trimmed = rawUrl.trim();
		if (!trimmed.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*"))
		{
			// Default to wss:// for localhost, ws:// for everything else
			String hostPart = trimmed.split("[/:]")[0];
			if ("localhost".equalsIgnoreCase(hostPart) || "127.0.0.1".equals(hostPart))
			{
				trimmed = "wss://" + trimmed;
			}
			else
			{
				trimmed = "ws://" + trimmed;
			}
		}

		try
		{
			URI uri = new URI(trimmed);
			String scheme = uri.getScheme();
			String host = uri.getHost();
			int port = uri.getPort();
			String path = uri.getPath();
			String query = uri.getQuery();
			String fragment = uri.getFragment();

			if (host == null || host.trim().isEmpty())
			{
				return null;
			}

			if ("http".equalsIgnoreCase(scheme))
			{
				scheme = "ws";
			}
			else if ("https".equalsIgnoreCase(scheme))
			{
				scheme = "wss";
			}

			// Only set port if not the default for the scheme
			if (port <= 0)
			{
				port = "wss".equalsIgnoreCase(scheme) ? -1 : 80;
			}
			else if ("wss".equalsIgnoreCase(scheme) && port == 443)
			{
				port = -1;  // Omit default port
			}
			else if ("ws".equalsIgnoreCase(scheme) && port == 80)
			{
				port = -1;  // Omit default port
			}

			URI normalized = new URI(
					scheme,
					uri.getUserInfo(),
					host,
					port,
					path == null || path.isEmpty() ? "/" : path,
					query,
					fragment
			);
			return normalized.toString();
		}
		catch (URISyntaxException ex)
		{
			log.warn("Invalid server URL '{}': {}", rawUrl, ex.getMessage());
			return null;
		}
	}
}
