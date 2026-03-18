package com.Concord;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Concord"
)
public class ConcordPlugin extends Plugin
{

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConcordPanel panel;

	private NavigationButton navButton;

	@Override
	protected void startUp()
	{
		log.debug("Concord started!");
		log.info("Concord started!");

		panel = injector.getInstance(ConcordPanel.class);
		panel.init();
		panel.setPlugin(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Concord")
				.priority(1)
				.icon(icon)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		log.debug("Concord stopped!");
	}
}
