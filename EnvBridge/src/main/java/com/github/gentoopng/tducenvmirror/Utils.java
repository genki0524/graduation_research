package com.github.gentoopng.tducenvmirror;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.inventory.ItemStack;
import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static void walkPlayer(Player player,double distance,String value){
        if(value.equalsIgnoreCase("FORWARD")){
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            double yawRadians = Math.toRadians(yaw);
            Vector forward = new Vector(-Math.sin(yawRadians)*distance,0,Math.cos(yawRadians)*distance); //向いている方向のベクトル
            player.setVelocity(forward);
        }else if(value.equalsIgnoreCase("LEFT")){
            //左に移動するコード
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            double yawRadians = Math.toRadians(yaw);
            Vector forward = new Vector(-Math.sin(yawRadians)*distance,0,Math.cos(yawRadians)*distance); //向いている方向のベクトル
            Vector right = forward.clone().rotateAroundY(Math.PI/2);
            player.setVelocity(right);
        }else if(value.equalsIgnoreCase("RIGHT")){
            //右に移動するコード
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            double yawRadians = Math.toRadians(yaw);
            Vector forward = new Vector(-Math.sin(yawRadians)*distance,0,Math.cos(yawRadians)*distance);
            Vector left = forward.clone().rotateAroundY(-Math.PI/2);
            player.setVelocity(left);
        }
    }

    public static void PlayerRotation(Player player, float deltaYaw, float deltaPitch) {
        Location playerLocation = player.getLocation();
        float newYaw = playerLocation.getYaw() + deltaYaw;
        float newPitch = playerLocation.getPitch() + deltaPitch;

        new BukkitRunnable() {
            private float tick = 0;

            @Override
            public void run() { //20tick後に目的の方向を向くようにする
                tick++;
                float totalTicks = 20;
                float ration = tick / totalTicks;
                float currentYaw = playerLocation.getYaw();
                float currentPitch = playerLocation.getPitch();
                float interpolatedYaw = currentYaw + (newYaw - currentYaw) * ration;
                float interpolatedPitch = currentPitch + (newPitch - currentPitch) * ration;

                playerLocation.setYaw(interpolatedYaw);
                playerLocation.setPitch(interpolatedPitch);
                player.teleport(playerLocation);
                System.out.println(tick);
                if (tick >= totalTicks) {
                    cancel();
                }
            }
        }.runTaskTimer(TDUCEnvMirror.getPlugin(TDUCEnvMirror.class), 0, 1);
    }

    public static void MovePlayer(Player player, String finger) {
        double moveDistance = 0;
        switch (finger) {
            case "1-finger":
                moveDistance = 0.2;
                break;
            case "2-finger":
                moveDistance = 0.4;
                break;
            case "3-finger":
                moveDistance = 0.6;
                break;
            case "4-finger":
                moveDistance = 0.8;
                break;
            case "5-finger":
                moveDistance = 1.0;
                break;
            default:
                break;
        }
        float yaw = player.getLocation().getYaw();
        double radianYaw = Math.toRadians(yaw);

        double moveX = -Math.sin(radianYaw) * moveDistance;
        double moveZ = Math.cos(radianYaw) * moveDistance;

        Vector movement = new Vector(moveX, 0, moveZ);
        player.setVelocity(movement);
    }

    public static void MovePlayer(Player player) {
        double moveDistance = 2.0;
        float yaw = player.getLocation().getYaw();
        double radianYaw = Math.toRadians(yaw);

        double moveX = -Math.sin(radianYaw) * moveDistance;
        double moveZ = Math.cos(radianYaw) * moveDistance;

        Vector movement = new Vector(moveX, 0, moveZ);
        player.setVelocity(movement);
    }

    public static void giveItem(Player player, Material material) {
        PlayerInventory inventory = player.getInventory();
        ItemStack itemStack = new ItemStack(material);
        inventory.addItem(itemStack);
    }

    public static void useItem(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        String mainItem = itemStack.getType().toString();
        Location location = player.getLocation();
        Vector vector = location.getDirection();
        int maxDistance = 1;
        Location targetLocation = location.clone().add(vector.multiply(maxDistance));
        if (mainItem.equals("DIAMOND_SWORD")) {
            if (targetLocation.clone().add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
                targetLocation.clone().add(0, 1, 0).getBlock().setType(Material.DIAMOND_BLOCK);
            } else if (targetLocation.clone().add(0, 1, 0).getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                targetLocation.clone().add(0, 1, 0).getBlock().setType(Material.AIR);
            } else if (targetLocation.clone().add(0, 2, 0).getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
                targetLocation.clone().add(0, 2, 0).getBlock().setType(Material.AIR);
            }
        }
    }

    public static void getGroveGesture(String gesture, Player player) {
        if (gesture != null) {
            Pattern patternRight = Pattern.compile("^Rotate Right");
            Pattern patternLeft = Pattern.compile("^Rotate Left");
            Matcher mathcerRight = patternRight.matcher(gesture);
            Matcher matcherLeft = patternLeft.matcher(gesture);
            if(mathcerRight.find()){
                gesture = "Rotate Right";
            }
            if(matcherLeft.find()){
                gesture = "Rotate Left";
            }
            switch (gesture) {
                case "1-finger", "3-finger", "5-finger", "2-finger", "4-finger":
                    MovePlayer(player, gesture);
                    break;
                case "Swipe Right":
                    giveItem(player, Material.DIAMOND_SWORD);
                    break;
                case "Swipe Left":
                    giveItem(player, Material.DIAMOND_AXE);
                    break;
                case "Rotate Right":
                    runRotationTask(player,30,0);
                    break;
                case "Rotate Left":
                    runRotationTask(player,-30,0);
                    break;
                case "Pinch":
                    useItem(player);
                    break;
            }
        }
    }

    public static void PlayerJump(Player player){
        Vector v = player.getLocation().getDirection().multiply(0.5).setX(0).setY(0.5).setZ(0);
        player.setVelocity(v);

    }

    private static void runRotationTask(Player player, float deltaYaw, float deltaPitch) {
        Bukkit.getScheduler().runTaskAsynchronously(TDUCEnvMirror.getPlugin(TDUCEnvMirror.class), () -> {
            PlayerRotation(player, deltaYaw, deltaPitch);
        });
    }

    public static int getHotbarSlot(Player player){
        return player.getInventory().getHeldItemSlot();
    }

    public static void setPlayerItem(Player player,int slot){
        if(slot >= 0 && slot <= 8){
            player.getInventory().setHeldItemSlot(slot);
        }
    }

    public static List<ItemStack> getHotbarItems(Player player){
        List<ItemStack> itemStacks = new ArrayList<>();
        for(int slot = 0; slot < 8; slot++){
            ItemStack item = player.getInventory().getItem(slot);
            itemStacks.add(item);
        }
        return itemStacks;
    }

    public static void selectNextItem(Player player){
        int currentItemSlot = getHotbarSlot(player);
        System.out.println("Nextのcurrentslot:"+currentItemSlot);
        setPlayerItem(player, (currentItemSlot + 1) % 9);
    }

    public static void selectPreItem(Player player){
        int currentItemSlot = getHotbarSlot(player);
        System.out.println("preのcurrentslot:"+currentItemSlot);
        setPlayerItem(player, (currentItemSlot - 1 + 9) % 9);
    }






}
