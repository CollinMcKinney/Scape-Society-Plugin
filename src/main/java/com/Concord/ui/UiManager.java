package com.concord.ui;

import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

public final class UiManager
{
	private ConcordPanel panel;
	private NavigationButton navigationButton;

	public void start(ClientToolbar clientToolbar)
	{
		panel = new ConcordPanel();
		panel.init();

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/concord.png");
		navigationButton = NavigationButton.builder()
				.tooltip("Concord")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navigationButton);
	}

	public void shutdown(ClientToolbar clientToolbar)
	{
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationButton = null;
		}

		panel = null;
	}

	public void setDiscordInviteUrl(String discordInviteUrl)
	{
		if (panel != null)
		{
			panel.setDiscordInviteUrl(discordInviteUrl);
		}
	}

	public void setConnectionStatusConnecting()
	{
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
					panel.setConnectionStatus("Status: Connecting...", new java.awt.Color(230, 180, 70)));
		}
	}

	public void setConnectionStatusConnected()
	{
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
					panel.setConnectionStatus("Status: Connected", new java.awt.Color(90, 200, 120)));
		}
	}

	public void setConnectionStatusDisconnected()
	{
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
					panel.setConnectionStatus("Status: Select a server from the settings", new java.awt.Color(200, 90, 90)));
		}
	}

	public void setConnectionStatusNoServerSelected()
	{
		if (panel != null)
		{
			SwingUtilities.invokeLater(() ->
					panel.setConnectionStatus("Pick your clan from the settings, or ask for the server URL", new java.awt.Color(200, 90, 90)));
		}
	}

	public void showMessage(String message)
	{
		if (panel != null)
		{
			SwingUtilities.invokeLater(() -> panel.showMessage(message));
		}
	}
}
