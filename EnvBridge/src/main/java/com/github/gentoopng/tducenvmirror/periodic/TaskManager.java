package com.github.gentoopng.tducenvmirror.periodic;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.area.Area;
import org.bukkit.entity.Panda;

import java.util.ArrayList;

public class TaskManager {
    private final TDUCEnvMirror instance;
    private final ArrayList<UpdateAreaTask> updateAreaTasksList;
    private final ArrayList<GenerateParticlesTask> generateParticlesTaskList;
    private ArrayList<HeatEffectTask> heatEffectTaskList;

    public TaskManager(TDUCEnvMirror instance) {
        this.instance = instance;
        this.updateAreaTasksList = new ArrayList<>();
        this.generateParticlesTaskList = new ArrayList<>();
        this.heatEffectTaskList = new ArrayList<>();
    }

    public boolean startUpdateAreaTask(String areaID, long delay, long period) {
        Area area = instance.areaManager.getArea(areaID);
        System.out.println("area: "+area);
        if (area == null) {
            instance.getLogger().info("Couldn't create a task: No area with ID \"" + areaID + "\" found!");
            return false;
        }
        System.out.println(updateAreaTasksList);
        if (findUpdateAreaTask(areaID) != null) { //すでにそのareaで動いているTaskがあれば止める
            stopUpdateAreaTask(areaID);
        }
        var task = new UpdateAreaTask(instance, area);
        updateAreaTasksList.add(task);
        task.runTaskTimer(instance, delay, period);
        instance.getLogger().info("Created update task for \"" + areaID + "\" (each " + period + " ticks)");
        return true;
    }

    public void stopUpdateAreaTask(String areaID) {
        UpdateAreaTask task = findUpdateAreaTask(areaID);
        if (task != null) {
            instance.getLogger().info("Stopping task for \"" + areaID + "\"");
            task.cancel();
            updateAreaTasksList.remove(task);
        } else {
            instance.getLogger().info("No task to cancel!");
        }
    }

    public UpdateAreaTask findUpdateAreaTask(String areaID) {
        for (var task : updateAreaTasksList) {
            if (task.getArea().getAreaID().equalsIgnoreCase(areaID)) {
                return task;
            }
        }
        return null;
    }

    public boolean startGenerateParticlesTask(String areaID, long delay, long period) {
        Area area = instance.areaManager.getArea(areaID);
        if (area == null) {
            instance.getLogger().info("Couldn't create a task: No area with ID \"" + areaID + "\" found!");
            return false;
        }

        if (findUpdateAreaTask(areaID) != null) {//すでにそのareaで動いているTaskがあれば止める
            stopGenerateParticlesTask(areaID);
        }
        var task = new GenerateParticlesTask(instance, area);
        generateParticlesTaskList.add(task);
        task.runTaskTimer(instance, delay, period);
        instance.getLogger().info("Created particle-generating task for \"" + areaID + "\" (each " + period + " ticks)");
        return true;
    }

    public GenerateParticlesTask generateParticlesTask(String areaID) {
        Area area = instance.areaManager.getArea(areaID);
        if (area == null) {
            instance.getLogger().info("Couldn't get a task: No area with ID \"" + areaID + "\" found!");
            return null;
        } else {
            for (GenerateParticlesTask x: generateParticlesTaskList) {
                if (x.getArea() == area) {
                    return x;
                }
            }
            return null;
        }
    }

    public void stopGenerateParticlesTask(String areaID) {
        GenerateParticlesTask task = findGenerateParticlesTask(areaID);
        if (task != null) {
            task.cancel();
            generateParticlesTaskList.remove(task);
        } else {
            instance.getLogger().info("No tasks to cancel!");
        }
    }

    public GenerateParticlesTask findGenerateParticlesTask(String areaID) {
        for (var task : generateParticlesTaskList) {
            if (task.getArea().getAreaID().equalsIgnoreCase(areaID)) {
                return task;
            }
        }
        return null;
    }

    public boolean startHeatEffectTask(String areaID, long delay, long period) {
        Area area = instance.areaManager.getArea(areaID);
        var task = new HeatEffectTask(instance, area);
        heatEffectTaskList.add(task);
        task.runTaskTimer(instance, delay, period);
        instance.getLogger().info("Created heat-effect task for \"" + areaID + "\" (each " + period + " ticks)");
        return true;
    }

    public void stopHeatEffectTask(String areaID) {
       HeatEffectTask task = findHeatEffectTask(areaID);
       if (task != null) {
           task.cancel();
           heatEffectTaskList.remove(task);
       } else {
           instance.getLogger().info("No tasks to cancel!");
       }
    }

    public HeatEffectTask findHeatEffectTask(String areaID) {
        for (var task: heatEffectTaskList) {
            if (task.getArea().getAreaID().equalsIgnoreCase(areaID)) {
                return task;
            }
        }
        return null;
    }
}
