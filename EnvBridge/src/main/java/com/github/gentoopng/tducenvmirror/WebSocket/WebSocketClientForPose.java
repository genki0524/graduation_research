package com.github.gentoopng.tducenvmirror.WebSocket;
import com.github.gentoopng.tducenvmirror.Pose;
import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import com.github.gentoopng.tducenvmirror.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WebSocketClientForPose extends WebSocketClient{
    Player player;
    Gson gson = new Gson();
    public WebSocketClientForPose(TDUCEnvMirror mainClass){
        super(mainClass);
    }

    @Override
    public void open(String uri) throws ExecutionException, InterruptedException {
        mainClass.getLogger().info("Trying " + uri + " ...");
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                WebSocketClientForPose.this.webSocket = webSocket;
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
                try {
                    JsonObject jsonObjectMsg = JsonParser.parseString(msg).getAsJsonObject();
                    Pose pose = gson.fromJson(msg,Pose.class);
                    Bukkit.getServer().getScheduler().runTask(mainClass,() ->{
                        Utils.walkPlayer(player, 0.5,pose.getPose());
                    });
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

    public void setPlayer(Player player){
        this.player = player;
    }
}
