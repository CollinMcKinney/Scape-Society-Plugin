package com.concord.connectivity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.concord.ConcordConfig;
import com.concord.ConcordPlugin;
import net.runelite.client.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WssClient implements WebSocket.Listener
{
    private static final long RECONNECT_DELAY_SECONDS = 5L;

    private final ConcordPlugin plugin;
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final HttpClient insecureHttpClient;
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private volatile URI serverUri;
    private volatile WebSocket webSocket;
    private volatile boolean reconnectScheduled;
    private volatile boolean shuttingDown;

    public WssClient(ConcordPlugin plugin, ConfigManager configManager)
    {
        this.plugin = plugin;
        this.configManager = configManager;
        this.httpClient = HttpClient.newHttpClient();
        this.insecureHttpClient = createInsecureHttpClient();
    }

    /**
     * Creates an HttpClient that trusts ALL certificates.
     * WARNING: This is ONLY for development with self-signed certificates on localhost.
     * NEVER use this in production or with real user data.
     */
    private static HttpClient createInsecureHttpClient()
    {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager()
                {
                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                        // Trust all
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                        // Trust all
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an SSL context that uses our trust manager
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        }
        catch (Exception e)
        {
            log.warn("Failed to create insecure HTTP client, falling back to default", e);
            return HttpClient.newHttpClient();
        }
    }

    public void connect()
    {
        if (shuttingDown || isConnected())
        {
            return;
        }

        if (serverUri == null)
        {
            log.warn("Skipping Concord connection attempt because server URL is not configured");
            plugin.onSocketDisconnected();
            return;
        }

        if (!plugin.isConnectionReady())
        {
            log.debug("Skipping Concord connection attempt until RuneLite player name is available");
            return;
        }

        // Use insecure client only for localhost connections when trustSelfSigned is enabled
        boolean isLocalhost = serverUri.getHost().equalsIgnoreCase("localhost") ||
                              serverUri.getHost().equals("127.0.0.1") ||
                              serverUri.getHost().equals("[::1]");
        ConcordConfig config = configManager.getConfig(ConcordConfig.class);
        boolean useInsecure = isLocalhost && config.trustSelfSigned();

        HttpClient clientToUse = useInsecure ? insecureHttpClient : httpClient;

        if (useInsecure)
        {
            log.warn("Using INSECURE SSL context for localhost connection. DO NOT enable trustSelfSigned for production!");
        }

        clientToUse.newWebSocketBuilder()
                .buildAsync(serverUri, this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    this.reconnectScheduled = false;
                    log.info("Connected to Concord server");
                    plugin.onSocketConnected();
                })
                .exceptionally(ex -> {
                    log.warn("Failed to connect to Concord server: {}", ex.getMessage());
                    plugin.onSocketDisconnected();
                    scheduleReconnect();
                    return null;
                });
    }

    public void send(ConcordPacket packet)
    {
        if (!isConnected())
        {
            log.warn("WebSocket not connected");
            return;
        }

        webSocket.sendText(packet.toJson(), true);
        log.debug("Packet was sent!");
    }

    public void setServerUrl(String serverUrl)
    {
        if (serverUrl == null || serverUrl.trim().isEmpty())
        {
            this.serverUri = null;
            return;
        }

        try
        {
            this.serverUri = URI.create(serverUrl.trim());
        }
        catch (IllegalArgumentException ex)
        {
            log.warn("Invalid Concord server URL '{}': {}", serverUrl, ex.getMessage());
            this.serverUri = null;
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
        try
        {
            ConcordPacket packet = ConcordPacket.fromJson(data.toString());
            plugin.handleIncomingPacket(packet);
        }
        catch (Exception e)
        {
            System.out.println("Failed to parse packet: " + e.getMessage());
        }

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
    {
        log.warn("Concord WebSocket closed: code={} reason={}", statusCode, reason);
        this.webSocket = null;
        plugin.onSocketDisconnected();
        scheduleReconnect();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error)
    {
        log.warn("Concord WebSocket error: {}", error.getMessage());
        this.webSocket = null;
        plugin.onSocketDisconnected();
        scheduleReconnect();
        WebSocket.Listener.super.onError(webSocket, error);
    }

    public void shutdown()
    {
        shuttingDown = true;
        reconnectScheduled = false;
        closeSocket("Plugin shutdown");
        reconnectExecutor.shutdownNow();
    }

    public void disconnect()
    {
        reconnectScheduled = false;
        closeSocket("RuneLite logged out");
    }

    private void closeSocket(String reason)
    {
        if (webSocket != null)
        {
            try
            {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, reason);
            }
            catch (Exception e)
            {
                log.debug("Ignoring WebSocket shutdown exception: {}", e.getMessage());
            }
            finally
            {
                webSocket = null;
            }
        }
    }

    private boolean isConnected()
    {
        return webSocket != null && !webSocket.isOutputClosed() && !webSocket.isInputClosed();
    }

    private void scheduleReconnect()
    {
        if (shuttingDown || reconnectScheduled)
        {
            return;
        }

        reconnectScheduled = true;
        reconnectExecutor.schedule(() -> {
            reconnectScheduled = false;
            connect();
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }
}
