package com.github.gentoopng.tducenvmirror.entity;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityManager {
    TDUCEnvMirror instance;

    ArrayList<Entity> entities;

    public EntityManager(TDUCEnvMirror instance) {
        this.instance = instance;
        this.entities = new ArrayList<>();
    }

    public void spawnEntity(Location location, EntityType entityType, int quantity) {
        instance.getLogger().info("Spawning " + quantity + " entities");
        World world = location.getWorld();
        for (int i = 0; i < quantity; i++) {
            assert world != null : "Provided world is null!";
            Entity spawnedEntity = world.spawnEntity(location, entityType, true);
            spawnedEntity.setMetadata(
                    TDUCEnvMirror.DATA_KEY,
                    new FixedMetadataValue(instance, location)
            );
            entities.add(spawnedEntity);
        }
    }

    public boolean removeEntity(int quantity) {
        if (quantity > entities.size()) {
            instance.getLogger().info("Provided number is bigger than the number of entities. Set to max of it.");
            quantity = entities.size();
        }
        if (quantity < 0) { // 入力された値が小さすぎる場合
            instance.getLogger().info("There was no entities to remove!");
            return false;
        }
        instance.getLogger().info("Removing " + quantity + " entities...");

        for (int i = 0; i < quantity; i++) {
            Entity removingEntity = entities.get(0);
            removingEntity.remove();
            entities.remove(removingEntity);
        }
        return true;
    }

    public boolean removeAllEntity() {
        instance.getLogger().info("Removing ALL entities spawned by this plugin...");
        return removeEntity(entities.size());
    }

    // gets all entities spawned by this plugin
    public List<Entity> getAllEntities() {
        return entities;
    }

}
