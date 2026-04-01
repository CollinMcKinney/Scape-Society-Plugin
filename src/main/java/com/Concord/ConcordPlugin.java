package com.concord;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.concord.chat.ChatRelay;
import com.concord.connectivity.ConnectionCoordinator;
import com.concord.connectivity.ConcordPacket;
import com.concord.connectivity.PacketFactory;
import com.concord.connectivity.PacketRouter;
import com.concord.connectivity.WssClient;
import com.concord.servers.ServerController;
import com.concord.servers.ServerPreferences;
import com.concord.connectivity.SessionManager;
import com.concord.ui.UiManager;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;

@PluginDescriptor(
	name = "Concord",
	description = "Discord ↔ RuneLite chat bridge"
)
public class ConcordPlugin extends Plugin
{
	private static final long INJECTED_MESSAGE_TTL_MS = 5000L;

	@Inject
	private ConcordConfig config;

	@Provides
	ConcordConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ConcordConfig.class);
	}

	@Inject
	private Client client;

	@Inject
	private net.runelite.client.callback.ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	private WssClient webSocket;
	private SessionManager sessionManager;
	private final PacketFactory packetFactory = new PacketFactory();
	private ChatRelay chatRelay;
	private ConnectionCoordinator connectionCoordinator;
	private UiManager uiManager;
	private PacketRouter packetRouter;
	private ServerController serverController;

	@Override
	protected void startUp()
	{
		sessionManager = new SessionManager(configManager, client);
		sessionManager.loadPersistedGuestSession();
		webSocket = new WssClient(this, configManager);
		connectionCoordinator = new ConnectionCoordinator(sessionManager, packetFactory, webSocket);
		chatRelay = new ChatRelay(
				INJECTED_MESSAGE_TTL_MS,
				sessionManager,
				packetFactory,
				connectionCoordinator,
				client,
				clientThread
		);
		uiManager = new UiManager();
		uiManager.start(clientToolbar);
		packetRouter = new PacketRouter(connectionCoordinator, chatRelay, uiManager, sessionManager);
		serverController = new ServerController(
				new ServerPreferences(configManager),
				connectionCoordinator,
				uiManager,
				client
		);
		serverController.start();
		connectionCoordinator.ensureConnectedIfReady();
		//eventBus.register(this);
	}

	@Override
	protected void shutDown()
	{
		if (uiManager != null)
		{
			uiManager.shutdown(clientToolbar);
			uiManager = null;
		}

		if (connectionCoordinator != null)
		{
			connectionCoordinator.shutdown();
			connectionCoordinator = null;
		}

		webSocket = null;

		if (chatRelay != null)
		{
			chatRelay.clear();
			chatRelay = null;
		}

		if (sessionManager != null)
		{
			sessionManager.clearProfileSyncState();
		}
		if (serverController != null)
		{
			serverController.shutdown();
			serverController = null;
		}
		packetRouter = null;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (chatRelay != null)
		{
			chatRelay.onChatMessage(event);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (connectionCoordinator != null)
		{
			connectionCoordinator.handleGameStateChanged(event.getGameState());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (connectionCoordinator != null)
		{
			connectionCoordinator.handleGameTick(client.getGameState());
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"Concord".equals(event.getGroup()))
		{
			return;
		}

		// Reconnect when server settings change
		if ("selectedServer".equals(event.getKey()) || "manualServerUrl".equals(event.getKey()))
		{
			if (serverController != null)
			{
				serverController.applyServerConfig();
			}
		}
	}

	public void handleIncomingPacket(ConcordPacket packet)
	{
		if (packetRouter != null)
		{
			packetRouter.handleIncomingPacket(packet);
		}
	}

	public void onSocketDisconnected()
	{
		if (chatRelay != null)
		{
			chatRelay.clear();
		}
		if (uiManager != null)
		{
			uiManager.setConnectionStatusDisconnected();
		}
	}

	public boolean isConnectionReady()
	{
		return connectionCoordinator != null && connectionCoordinator.isConnectionReady();
	}

	public void onSocketConnected()
	{
		if (connectionCoordinator != null)
		{
			connectionCoordinator.onSocketConnected();
		}
		if (uiManager != null)
		{
			uiManager.setConnectionStatusConnected();
		}
	}
}
