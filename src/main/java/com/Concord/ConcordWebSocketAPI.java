package com.Concord;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.util.Map;
import java.lang.reflect.Type;

public class ConcordWebSocketAPI {

    private WebSocketClient client;
    private final Gson gson = new Gson();

    public ConcordWebSocketAPI(String wsUrl) {
        try {
            URI uri = new URI(wsUrl);
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("Connected to WS API at " + wsUrl);
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket closed: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        if (client != null) {
            client.connect();
        }
    }

    public void disconnect() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }

    private void handleMessage(String message) {
        // Use TypeToken to avoid unchecked cast warning
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> response = gson.fromJson(message, type);

        if (response.containsKey("result")) {
            System.out.println("API result: " + response.get("result"));
        } else if (response.containsKey("error")) {
            System.err.println("API error: " + response.get("error"));
        }
    }

    public void callApiFunction(String functionName, Object... args) {
        if (client != null && client.isOpen()) {
            Map<String, Object> payload = Map.of(
                    "functionName", functionName,
                    "args", args
            );
            String json = gson.toJson(payload);
            client.send(json);
        }
    }
}