package com.ScapeSociety;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Scape Society"
)
public class ScapeSocietyPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ScapeSocietyConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ScapeSocietyPanel panel;

	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Scape Society started!");
		log.info("Scape Society started!");

		panel.init();
		panel.setPlugin(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Scape Society")
				.priority(1)
				.icon(icon)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		log.debug("Scape Society stopped!");
	}

	@Provides
	ScapeSocietyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ScapeSocietyConfig.class);
	}
}
