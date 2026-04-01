package com.concord;

import com.concord.servers.ClanServer;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Concord")
public interface ConcordConfig extends Config
{
	@ConfigSection(
		name = "Server Settings",
		description = "Configure which Concord server to connect to",
		position = 0
	)
	String SERVER_SECTION = "serverSection";

	@ConfigSection(
		name = "Advanced",
		description = "Advanced settings for development",
		position = 100
	)
	String ADVANCED_SECTION = "advancedSection";

	@ConfigItem(
		keyName = "selectedServer",
		name = "Concord Server",
		description = "Select the Concord server to connect to",
		position = 0,
		section = SERVER_SECTION
	)
	default ClanServer selectedServer()
	{
		return ClanServer.NONE;
	}

	@ConfigItem(
		keyName = "manualServerUrl",
		name = "Custom Server URL",
		description = "Or enter a custom server URL (e.g., localhost)",
		position = 1,
		section = SERVER_SECTION
	)
	default String manualServerUrl()
	{
		return "";
	}

	@ConfigItem(
			keyName = "greeting",
			name = "Welcome Greeting",
			description = "The message to show to the user when they login",
			position = 50,
			section = ADVANCED_SECTION
	)
	default String greeting()
	{
		return "Hello";
	}

	@ConfigItem(
			keyName = "trustSelfSigned",
			name = "Trust Self-Signed Certs (DEV ONLY)",
			description = "Allow connections to localhost with self-signed certificates. DO NOT enable for production!",
			position = 51,
			section = ADVANCED_SECTION
	)
	default boolean trustSelfSigned()
	{
		return false;
	}
}
