package com.github.gentoopng.tducenvmirror;

import java.io.File;
import java.util.ArrayList;

import com.github.gentoopng.tducenvmirror.WebSocket.WebSocketClient;
import com.github.gentoopng.tducenvmirror.WebSocket.WebSocketClientForEnv;
import com.github.gentoopng.tducenvmirror.WebSocket.WebSocketClientForPose;
import com.github.gentoopng.tducenvmirror.area.AreaManager;
import com.github.gentoopng.tducenvmirror.db.DBAccessor;
import com.github.gentoopng.tducenvmirror.entity.EntityManager;
import com.github.gentoopng.tducenvmirror.periodic.TaskManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public final class TDUCEnvMirror extends JavaPlugin {

    public static final int DEFAULT_SPAWN_QUANTITY = 5;
    public static final int DEFAULT_REMOVE_QUANTITY = 1;

    public static final String DATA_KEY = "ENVBRIDGE";

    public AreaManager areaManager;
    public EntityManager entityManager;
    public DBAccessor dbAccessor;
    public TaskManager taskManager;

    public WebSocketClient webSocketClientForEnv;
    public WebSocketClient webSocketClientForPose;

    public int counter;

    ArrayList<Villager> villagers;

    FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting TDUCraft Environment Mirror...");

        File configYml = new File(getDataFolder(), "config.yml"); //プラグインの設定ファイルを取得
        if (!configYml.exists()) {
            saveDefaultConfig(); //config.ymlの内容を保存する。
        }
        config = getConfig(); //config.ymlの内容を取得

        String dbAccess = config.getString("db.access");
        String dbName = config.getString("db.name");
        String dbCollection = config.getString("db.collection");
        dbAccessor = new DBAccessor(this, dbAccess, dbName, dbCollection);

        getCommand("envmirror").setExecutor(new CommandHandler(this)); //コマンドを設定
        villagers = new ArrayList<>();
        areaManager = new AreaManager();
        entityManager = new EntityManager(this);
        taskManager = new TaskManager(this);
        webSocketClientForEnv = new WebSocketClientForEnv(this);
        webSocketClientForPose = new WebSocketClientForPose(this);

        getLogger().info("Done!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabling TDUCraft Environment Mirror...");
        entityManager.removeAllEntity();
        dbAccessor.closeConnection();
        getLogger().info("Done.");
    }

    public void spawnEntity(Location location, EntityType entityType, int quantity) {
        entityManager.spawnEntity(location, entityType, quantity);
    }

    public boolean removeEntity(int quantity) {
        return entityManager.removeEntity(quantity);
    }

    public boolean startUpdateAreaTask(String areaID) {
        final long UPDATE_DELAY = 1L;
        // final long UPDATE_PERIOD = 1200L; // about 60sec
        final long UPDATE_PERIOD = 100L;    // about 10sec for exhibit
        boolean result1 = taskManager.startUpdateAreaTask(areaID, UPDATE_DELAY, UPDATE_PERIOD);

        final long GENERATE_DELAY = 1L;
        final long GENERATE_PERIOD = 10L; // about 0.5sec
        boolean result2 = taskManager.startGenerateParticlesTask(areaID, GENERATE_DELAY, GENERATE_PERIOD);

        return result1 || result2;
    }

    public boolean startHeatEffectTask(String areaID) {
        final long UPDATE_DELAY = 1L;
        final long UPDATE_PERIOD = 1000L;
        boolean result1 = taskManager.startUpdateAreaTask(areaID, UPDATE_DELAY, UPDATE_PERIOD);
        boolean result2 = taskManager.startHeatEffectTask(areaID, UPDATE_DELAY, UPDATE_PERIOD);
        return result1 || result2;
    }

    public void cancelUpdateAreaTask(String areaID) {
        taskManager.stopUpdateAreaTask(areaID);
        taskManager.stopGenerateParticlesTask(areaID);
    }

    public void cancelHeatEffectTask(String areaID) {
        taskManager.stopHeatEffectTask(areaID);
    }

    // CURRENTLY NOT IN USE
    public void spawnVillager(Location location, int quantity) {
        World world = location.getWorld();
        for (int i = 0; i < quantity; i++) {
            assert world != null : "Provided world is null!";
            Villager spawnedVillager = (Villager) world.spawnEntity(location, EntityType.VILLAGER, true);
            spawnedVillager.setMetadata(
                    DATA_KEY,
                    new FixedMetadataValue(this, location)
            );
            villagers.add(spawnedVillager);
        }
        getLogger().info("Current quantity of villagers is: " + villagers.size());
    }

    // CURRENTLY NOT IN USE
    public boolean removeVillager(int quantity) {
        if (quantity < villagers.size()) { // 入力された値が小さすぎる場合
            getLogger().info("No entities to remove!");
            return false;
        }
        for (int i = 0; i < quantity; i++) {
            Entity removingEntity = villagers.get(0);
            removingEntity.remove();
            villagers.remove(removingEntity);
        }
        getLogger().info("Current quantity of villagers is: " + villagers.size());
        return true;
    }

    public void countUp() {
        this.counter++;
    }

    public int getCounter() {
        return counter;
    }

    public void resetCounter() {
        this.counter = 0;
    }
}
