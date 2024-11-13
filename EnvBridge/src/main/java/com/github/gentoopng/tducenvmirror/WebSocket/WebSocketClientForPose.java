package com.github.gentoopng.tducenvmirror.WebSocket;
import com.github.gentoopng.tducenvmirror.model.GroveGestureData;
import com.github.gentoopng.tducenvmirror.model.Pose;
import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import com.github.gentoopng.tducenvmirror.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WebSocketClientForPose extends WebSocketClient{
    Player player;
    Gson gson = new Gson();
    int backWardCnt = 0;
    public WebSocketClientForPose(TDUCEnvMirror mainClass){
        super(mainClass);
    }
    private Map<String,Player> playerChache = new HashMap<>();

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
                    GroveGestureData gestureData = gson.fromJson(msg,GroveGestureData.class);
                    Player movePlayer = getPlayerByName(gestureData.user_name);
                    if(movePlayer!=null) {
                        if (gestureData.pose != null) {
                            if (gestureData.pose.equals("FORWARD") || gestureData.pose.equals("LEFT") || gestureData.pose.equals("RIGHT")) {
                                Bukkit.getServer().getScheduler().runTask(mainClass, () -> {
                                    Utils.walkPlayer(movePlayer, 0.2, gestureData.pose);
                                });
                            } else if (gestureData.pose.equals("UP")) {
                                Utils.PlayerJump(movePlayer);
                            }
                        }
                        if (gestureData.grove_gesture != null) {
                            switch (gestureData.grove_gesture) {
                                case "Up":
                                    runRotationTask(0, 30,movePlayer);
                                    break;
                                case "Down":
                                    runRotationTask(0, -30,movePlayer);
                                    break;
                                case "Left":
                                    runRotationTask(30, 0,movePlayer);
                                    break;
                                case "Right":
                                    runRotationTask(-30, 0,movePlayer);
                                    break;
                                case "Clockwise":
                                    Utils.selectNextItem(movePlayer);
                                    break;
                                case "Anti-Clockwise":
                                    Utils.selectPreItem(movePlayer);
                                    break;
                                case "Forward":
                                    runUseItem(movePlayer);
                                    break;
                                case "Backward":
                                    backWardCnt += 1;
                                    if (backWardCnt % 2 == 1) {
                                        Utils.giveItem(movePlayer, Material.DIAMOND_SWORD);
                                    } else {
                                        Utils.giveItem(movePlayer, Material.DIAMOND_AXE);
                                    }
                                    break;
                                case "Wave":

                            }
                        }
                        if (gestureData.gesture_R != null) {
                            switch (gestureData.gesture_R) {
                                case "Up":
                                    runRotationTask(0, -30,movePlayer);
                                    break;
                                case "Down":
                                    runRotationTask(0, 30,movePlayer);
                                    break;
                                case "Right":
                                    runMoveTask(-2.0,movePlayer);
                                    break;
                                case "Left":
                                    runMoveTask(1.0,movePlayer);
                                    break;
                                case "Forward":
                                    runRotationTask(30, 0,movePlayer);
                                    break;
                            }
                        }
                        if (gestureData.gesture_L != null) {
                            switch (gestureData.gesture_L) {
                                case "Up":
                                    runRotationTask(0, -30,movePlayer);
                                    break;
                                case "Down":
                                    runRotationTask(0, 30,movePlayer);
                                    break;
                                case "Right":
                                    runMoveTask(1.0,movePlayer);
                                    break;
                                case "Left":
                                    runMoveTask(-2.0,movePlayer);
                                    break;
                                case "Forward":
                                    runRotationTask(-30, 0,movePlayer);
                                    break;
                            }
                        }
                        if (gestureData.smart_ir_gesture != null) {
                            System.out.println(gestureData.smart_ir_gesture);
                            Utils.getGroveGesture(gestureData.smart_ir_gesture, player);
                        }
                    }
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

    private void runRotationTask(float deltaYaw, float deltaPitch,Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(TDUCEnvMirror.getPlugin(TDUCEnvMirror.class), () -> {
            Utils.PlayerRotation(player, deltaYaw, deltaPitch);
        });
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    private void runMoveTask(double speed,Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(TDUCEnvMirror.getPlugin(TDUCEnvMirror.class), () -> {
            Vector direction = player.getLocation().getDirection().normalize();
            if (speed < 0) direction.multiply(-1);
            Vector velocity = direction.multiply(Math.abs(speed));
            player.setVelocity(velocity);
        });
    }

    private void runUseItem(Player player){
        Bukkit.getScheduler().runTask(TDUCEnvMirror.getPlugin(TDUCEnvMirror.class),() -> {
            Utils.useItem(player);
        });
    }

    private Player getPlayerByName(String playerName){
        if(playerChache.containsKey(playerName)){
            return playerChache.get(playerName);
        }
        Player player = Bukkit.getPlayer(playerName);
        if(player != null && player.isOnline()){
            playerChache.put(playerName,player);
            return player;
        }else{
            return null;
        }
    }

}


