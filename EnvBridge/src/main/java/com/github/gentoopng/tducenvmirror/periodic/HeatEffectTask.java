package com.github.gentoopng.tducenvmirror.periodic;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.area.Area;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class HeatEffectTask extends BukkitRunnable {
    TDUCEnvMirror mainClass;
    ArrayList<Entity> entities;
    Area area;

    public Area getArea() {
        return this.area;
    }

    public HeatEffectTask(TDUCEnvMirror mainClass, Area area) {
        this.mainClass = mainClass;
        this.area = area;
        this.entities = new ArrayList<>();
    }

    @Override
    public void run() {
        mainClass.getLogger().info("HeatEffectTask");
        ArrayList<Entity> allEntities = new ArrayList<>(area.getWorld().getEntities());
        ArrayList<Entity> entitiesToAdd = new ArrayList<>();
        for (Entity e: allEntities) {
            if (area.isInArea(e.getLocation())) {
                entitiesToAdd.add(e);
            }
        }
        entities = entitiesToAdd;

        int wbgtInt = (int)area.getWbgt();
        if (31 <= wbgtInt) {
            for (Entity e: entities) {
                e.setVisualFire(true);
            }
        } else if (28 <= wbgtInt) {
            for (Entity e: entities) {
                e.setVisualFire(true);
            }
        } else if (25 <= wbgtInt) {
            for (Entity e: entities) {
                if (e.getType() == EntityType.VILLAGER) {
                    // sweat effect
                }
            }
        } else {
            for (Entity e: entities) {
                clearAllEffects(e);
            }
        }
    }

    public void clearAllEffects(Entity e) {
        e.setVisualFire(false);
    }

}
