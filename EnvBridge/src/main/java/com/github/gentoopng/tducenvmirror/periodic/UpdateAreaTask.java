package com.github.gentoopng.tducenvmirror.periodic;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.area.Area;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateAreaTask extends BukkitRunnable {
    private final TDUCEnvMirror instance;
    private final Area area;

    public UpdateAreaTask(TDUCEnvMirror instance, Area area) {
        this.instance = instance;
        this.area = area;
    }

    public Area getArea() {
        return area;
    }

    @Override
    public void run() {
        instance.getLogger().info("Running UpdateAreaTask (scheduled)");
        //instance.dbAccessor.readAndSet(area);
        if (area.getShowText()) {
            area.updateStatusText();
        }
        String request = "{\"request\": true, \"areaid\": \"" + area.getAreaID() + "\"}";
        if (!instance.webSocketClientForEnv.send(request)) {
            instance.getLogger().warning("Tried to send request using WebSocket but not succeeded");
        }
    }
}
