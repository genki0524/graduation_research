package com.github.gentoopng.tducenvmirror;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Utils {
    public static void walkPlayer(Player player,double distance,String value){
        System.out.println(value);
        System.out.println(value.equals("FORWARD"));
        if(value.equalsIgnoreCase("FORWARD")){
            Location loc = player.getLocation();
            loc.add(loc.getDirection().multiply(distance));
            player.teleport(loc);
        }else if(value.equalsIgnoreCase("LEFT")){
            //左に移動するコード
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            double yawRadians = Math.toRadians(yaw);
            Vector forward = new Vector(-Math.sin(yawRadians),0,Math.cos(yawRadians)); //向いている方向のベクトル
            Vector right = forward.clone().rotateAroundY(Math.PI/2);
            loc.add(right);
            player.teleport(loc);
        }else if(value.equalsIgnoreCase("RIGHT")){
            //右に移動するコード
            Location loc = player.getLocation();
            float yaw = loc.getYaw();
            double yawRadians = Math.toRadians(yaw);
            Vector forward = new Vector(-Math.sin(yawRadians),0,Math.cos(yawRadians));
            Vector left = forward.clone().rotateAroundY(-Math.PI/2);
            loc.add(left);
            player.teleport(loc);
        }
    }
}
