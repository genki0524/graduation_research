package com.github.gentoopng.tducenvmirror.WebSocket;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;

import javax.websocket.*;
import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.WebSocket;

import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import com.github.gentoopng.tducenvmirror.area.Area;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WebSocketClient {
    protected TDUCEnvMirror mainClass;
    protected WebSocket webSocket;
    protected HttpClient client;

    public WebSocketClient(TDUCEnvMirror mainClass) {
        this.mainClass = mainClass;
        this.client = HttpClient.newHttpClient();
        mainClass.getLogger().info("WebSocket client created");
    }

    public void open(String uri) throws ExecutionException, InterruptedException {
        mainClass.getLogger().info("Trying " + uri + " ...");

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                WebSocketClient.this.webSocket = webSocket;
                mainClass.getLogger().info("WebSocket connection opened");
                webSocket.request(Long.MAX_VALUE); // Requesting the first message
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                mainClass.getLogger().info("WebSocket connection closed with status " + statusCode + ": " + reason);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                mainClass.getLogger().warning("WebSocket Error: " + error.getMessage());
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
//                mainClass.getLogger().info("Message received");
//                mainClass.getLogger().info("Showing data\n" + data.toString());
                String msg = data.toString();
                System.out.println(msg);
                try {
                    JsonObject jsonObjectMsg = JsonParser.parseString(msg).getAsJsonObject();
                    JsonObject jsonObjectPayload = jsonObjectMsg.get("payload").getAsJsonObject();

                    String areaID = jsonObjectPayload.get("areaid").getAsString();
//                  mainClass.getLogger().info("Received message of areaID \"" + areaID + "\"");
                    mainClass.getLogger().info("Received: " + jsonObjectPayload);
                    Area area = mainClass.areaManager.getArea(areaID);
                    area.setFromJSON(jsonObjectPayload.toString());
                    System.out.println(area);
                    mainClass.taskManager.generateParticlesTask(areaID).run();
                    mainClass.countUp();
                } catch (Exception e) {
                    mainClass.getLogger().info(msg);
                    e.printStackTrace();
                }

                webSocket.request(Long.MAX_VALUE); // Requesting the next message
                return null;
            }

            // Implement other necessary methods like onClose, onError
        };

        this.client.newWebSocketBuilder()
                .buildAsync(URI.create(uri), listener)
                .join();
    }

    public void close() {
        if (this.webSocket == null) {
            mainClass.getLogger().info("There is no WebSocket session to close");
        } else {
            this.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Closing")
                    .thenAccept(stage -> mainClass.getLogger().info("WebSocket closed"));
        }
    }

    public boolean send(String payload) {
        if (this.webSocket != null && !this.webSocket.isOutputClosed()) {
//            var msg = new JsonObject();
//            msg.add("payload", JsonParser.parseString(payload));
//            mainClass.getLogger().info(msg.getAsString());
//            this.webSocket.sendText(msg.toString(), true);
//            return true;
            try {
                JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
                JsonObject msg = new JsonObject();
                msg.add("payload", payloadJson);

                String jsonString = msg.toString();
                mainClass.getLogger().info(jsonString);
                this.webSocket.sendText(jsonString, true);
                return true;
            } catch (Exception e) {
                mainClass.getLogger().info("Error parsing JSON: " + e.getMessage());
                return false;
            }
        } else {
            mainClass.getLogger().info("WebSocket is not open or already closed.");
            return false;
        }
    }
}


/*
@ClientEndpoint
public class WebSocketClient {
    TDUCEnvMirror mainClass;
    Session session;

//    private WebSocket websocket;

    public WebSocketClient(TDUCEnvMirror mainClass) {
        this.mainClass = mainClass;
        mainClass.getLogger().info("WebSocket client created");
    }

    @OnMessage
    public void onMessage(String msg) {
        JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
        String areaID = jsonObject.get("areaid").getAsString();
        mainClass.getLogger().info("Received message of areaID \"" + "\"");
        Area area = mainClass.areaManager.getArea(areaID);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        mainClass.getLogger().info("WebSocket connection opened");
    }

    public void open(String uri) {
        mainClass.getLogger().info("Trying " + uri + " ...");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, URI.create(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (session == null) {
            mainClass.getLogger().info("There is no WebSocket session to close");
        } else {
            try {
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean send(String string) {
        if (session == null || session.isOpen()) {
            try{
                session.getBasicRemote().sendText(string);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

 */
