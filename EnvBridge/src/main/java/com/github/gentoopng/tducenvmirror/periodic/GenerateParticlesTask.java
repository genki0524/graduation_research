package com.github.gentoopng.tducenvmirror.periodic;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.area.Area;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class GenerateParticlesTask extends BukkitRunnable {
    TDUCEnvMirror instance;
    Area area;

    public GenerateParticlesTask(TDUCEnvMirror instance, Area area) {
        this.instance = instance;
        this.area = area;
    }

    public Area getArea() {
        return this.area;
    }

    // level0: 0 - 799ppm, level1: 800 - 999ppm, level2: 1000 - 1499 ppm, level3: 1500+ ppm
    public int co2level(int co2ppm) {
        if (co2ppm <= 799) {
            return 0;
        }
        else if (co2ppm <= 999) {
            return 1;
        }
        else if (co2ppm <= 1499) {
            return 2;
        }
        else if (co2ppm <= 1999) {
            return 3;
        }
        else {
            return 4;
        }
    }

    @Override
    public void run() {
        int co2ppm = area.getCo2();

        switch(co2level(co2ppm)) {
            case 0 -> {
                area.getWorld().spawnParticle(
                        Particle.VILLAGER_HAPPY,
                        area.getCenterLocation(),
                        15,
                        8,
                        2,
                        8,
                        0.01
                );
            }
            case 1 -> {
                area.getWorld().spawnParticle(
                        Particle.CLOUD,
                        area.getCenterLocation(),
                        20,
                        8,
                        2,
                        8,
                        0.01
                );
            }
            case 2 -> {
                area.getWorld().spawnParticle(
                        Particle.CLOUD,
                        area.getCenterLocation(),
                        20,
                        8,
                        2,
                        8,
                        0.01
                );
                area.getWorld().spawnParticle(
                        Particle.SMOKE_NORMAL,
                        area.getCenterLocation(),
                        25,
                        8,
                        2,
                        8,
                        0.05
                );
            }
            case 3 -> {
                area.getWorld().spawnParticle(
                        Particle.CLOUD,
                        area.getCenterLocation(),
                        40,
                        8,
                        2,
                        8,
                        0.01
                );
                area.getWorld().spawnParticle(
                        Particle.SMOKE_LARGE,
                        area.getCenterLocation(),
                        75,
                        8,
                        2,
                        8,
                        0.05
                );
            }
            case 4 -> {
                area.getWorld().spawnParticle(
                        Particle.CLOUD,
                        area.getCenterLocation(),
                        20,
                        8,
                        2,
                        8,
                        0.01
                );
                area.getWorld().spawnParticle(
                        Particle.SMOKE_NORMAL,
                        area.getCenterLocation(),
                        50,
                        8,
                        2,
                        8,
                        0.05
                );
                area.getWorld().spawnParticle(
                        Particle.SMOKE_LARGE,
                        area.getCenterLocation(),
                        150,
                        8,
                        2,
                        8,
                        0.1
                );
            }
        }
    }
}
