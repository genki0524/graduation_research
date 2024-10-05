package com.github.gentoopng.tducenvmirror.entity;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;

public class DisplayManager {
    TDUCEnvMirror plugin;
    ArrayList<BlockDisplay> blockDisplays;
    ArrayList<ItemDisplay> itemDisplays;
    ArrayList<TextDisplay> textDisplays;

    public void createTextDisplay(String text, Location location) {
        TextDisplay newOne = createTextDisplay(location);
        newOne.setAlignment(TextDisplay.TextAlignment.CENTER);
        newOne.setText(text);
    }

    public TextDisplay createTextDisplay(Location location) {
        World world = location.getWorld();

        assert world != null : "Provided world is null!";
        TextDisplay newOne = (TextDisplay)world.spawnEntity(location, EntityType.TEXT_DISPLAY);

        textDisplays.add(newOne);
        return newOne;
    }

    public void removeTextDisplay(TextDisplay toRemove) {
        toRemove.remove();
        textDisplays.remove(toRemove);
    }
}
