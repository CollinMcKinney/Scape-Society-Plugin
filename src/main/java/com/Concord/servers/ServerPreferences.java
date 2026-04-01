package com.concord.servers;

import net.runelite.client.config.ConfigManager;

/**
 * Handles loading and saving Concord server preferences.
 */
public final class ServerPreferences
{
	private static final String CONFIG_GROUP = "Concord";
	private static final String CONFIG_SELECTED_SERVER = "selectedServer";
	private static final String CONFIG_MANUAL_SERVER_URL = "manualServerUrl";

	private final ConfigManager configManager;

	public ServerPreferences(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	public ClanServer getSelectedServer()
	{
		String stored = configManager.getConfiguration(CONFIG_GROUP, CONFIG_SELECTED_SERVER);
		if (stored == null || stored.isEmpty())
		{
			return ClanServer.NONE;
		}

		try
		{
			return ClanServer.valueOf(stored.trim());
		}
		catch (IllegalArgumentException e)
		{
			return ClanServer.NONE;
		}
	}

	public void setSelectedServer(ClanServer server)
	{
		if (server == null)
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_SELECTED_SERVER, server.name());
	}

	public String getManualServerUrl()
	{
		String stored = configManager.getConfiguration(CONFIG_GROUP, CONFIG_MANUAL_SERVER_URL);
		return stored == null ? "" : stored.trim();
	}

	public void setManualServerUrl(String url)
	{
		if (url == null)
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_MANUAL_SERVER_URL, url.trim());
	}
}
