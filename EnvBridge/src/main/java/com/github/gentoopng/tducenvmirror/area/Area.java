package com.github.gentoopng.tducenvmirror.area;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Date;
import java.lang.Math;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

public class Area {
    public final String areaID;
    private String areaName;
    private String facilityName;
    private String buildingName;
    private String floor;
    private Location vertex1;
    private Location vertex2;
    private Location entryPoint;
    private Location textLocation;
    private boolean showText;
    private TextDisplay textDisplay;
    private World world;
    private int capacity;
    private float density;
    private double temperature;
    private double humidity;
    private double pressure;
    private int co2;
    private int pm25;
    private double wbgt;
    private Date lastupdate;

    public Area(String areaID, Location vertex1, Location vertex2) throws WorldsMismatchException {
        if (vertex1.getWorld() != vertex2.getWorld()) { // 2つの頂点が参照しているワールドが同一でない場合
            var msg = "The worlds of provided two vertexes were not equal!";
            throw new WorldsMismatchException(msg);
        }
        this.areaID = areaID.toLowerCase();
        this.areaName = null;
        this.facilityName = null;
        this.floor = null;
        this.world = vertex1.getWorld();
        setVertexes(vertex1, vertex2);
        this.entryPoint = null;
        this.density = 0;
        this.co2 = 0;
    }

    public String getAreaID() { return areaID; }
    public String getAreaName() { return areaName; }
    public String getFacilityName() { return facilityName; }
    public String getBuildingName() { return buildingName; }
    public String getFloor() { return floor; }
    public World getWorld() { return world; }
    public Location[] getVertexes() { return new Location[]{vertex1, vertex2}; }
    public Location getEntryPoint() { return entryPoint; }
    public Location getTextLocation() { return textLocation; }
    public boolean getShowText() { return showText; }
    public TextDisplay getTextDisplay() { return textDisplay; }
    public int getCapacity() { return capacity; }
    public float getDensity() { return density; }
    public float getDensityPercent() { return density * 100; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getPressure() { return pressure; }
    public int getCo2() { return co2; }
    public double getWbgt() { return wbgt; }
    public Date getDate() { return lastupdate; }

    public void setParameters(String areaName, String facilityName, String buildingName, String floor) {
        this.areaName = areaName;
        this.facilityName = facilityName;
        this.buildingName = buildingName;
        this.floor = floor;
    }
    public void setVertexes(Location vertex1, Location vertex2) {
        this.world = vertex1.getWorld();
        Location[] v = justifyVertexes(vertex1, vertex2);
        this.vertex1 = v[0];
        this.vertex2 = v[1];
    }
    public boolean setEntryPoint(double x, double y, double z) {
        var loc = new Location(this.world, x, y, z);
        if (isInArea(loc)) {
            this.entryPoint = loc;
            return true;
        } else {
            return false;
        }
    }
    public boolean setTextLocation(double x, double y, double z) {
        var loc = new Location(this.world, x, y, z);
        return setTextLocation(loc);
    }
    public boolean setTextLocation(Location loc) {
        if (isInArea(loc)) {
            this.textLocation = loc;
            return true;
        } else {
            return false;
        }
    }
    public void setShowText(boolean bool) {
        if (textDisplay != null) {
            textDisplay.remove();   // first remove the current entity (if exists)
        }
        this.showText = bool;
        if (bool) { // when set to true, spawn the entity (TextDisplay)
            spawnStatusText();
        }
    }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDensity(float density) { this.density = density; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setPressure(double pressure) { this.pressure = pressure; }
    public void setCo2(int ppm) { this.co2 = ppm; }
    public void setWbgt(double wbgt) { this.wbgt = wbgt; }
    public void setDate(Date date) { this.lastupdate = date; }

    public void setFromJSON(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true); // プライベートフィールドへのアクセスを可能にする
            String fieldName = field.getName();

            if (jsonObject.has(fieldName)) {
                try {
                    Object value = gson.fromJson(jsonObject.get(fieldName), field.getType());
                    field.set(this, value);
                } catch (IllegalAccessException e) {
                    // アクセス違反の例外処理
                }
            }
        }
    }

    public void spawnStatusText() {
        if (textLocation == null) {
            textLocation = getCenterLocation();
        }
        textDisplay = (TextDisplay)world.spawnEntity(textLocation, EntityType.TEXT_DISPLAY);
        textDisplay.setBillboard(Display.Billboard.VERTICAL);
        textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
        textDisplay.setDefaultBackground(true);
        updateStatusText();
    }

    public void updateStatusText() {
        if (showText) {
            String text =
                    "温度: " + (int)this.temperature + "°C\n" +
                            "湿度: " + (int)this.humidity + "%\n" +
                            "CO2: " + this.co2 + "ppm"
                    ;
            textDisplay.setText(text);
        }
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("----- Current status of \"").append(areaID).append("\"");
        if (areaName != null) {
            builder.append(" (").append(areaName).append(")");
        }
        builder.append(" in world \"").append(world.getName()).append("\"");
        builder.append(" -----");
        builder.append("\n").append("Temperature:  ").append(temperature).append(" degree Celsius");
        builder.append("\n").append("Humidity:     ").append(humidity).append(" %");
        builder.append("\n").append("Air pressure: ").append(pressure).append(" hPa");
        builder.append("\n").append("CO2:          ").append(co2).append(" ppm");
//        builder.append("\n").append("PM2.5: ").append(temperature).append("ppm");
        builder.append("\n").append("Density:      ").append(density * 100).append(" %");
        builder.append("\n").append("WBGT:         ").append(wbgt).append(" degree Celsius");
        builder.append("\n--------------------");

        return builder.toString();
    }

    public Location getCenterLocation() {
        double x = getCenterOf(vertex1.getBlockX(), vertex2.getBlockX()) + 0.5;
        double y = getCenterOf(vertex1.getBlockY(), vertex2.getBlockY()) + 0.5;
        double z = getCenterOf(vertex1.getBlockZ(), vertex2.getBlockZ()) + 0.5;

        return new Location(world, x, y, z);
    }

    public double getCenterOf(double a, double b) {
        return (a + b) / 2;
    }

    public List<Block> getAllBlocks() {
        List<Block> result = null;
        for (int x = vertex1.getBlockX(); x < vertex2.getBlockX(); x++) {
            for (int y = vertex1.getBlockY(); y < vertex2.getBlockY(); y++) {
                for (int z = vertex1.getBlockY(); z < vertex2.getBlockZ(); z++) {
                        result.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return result;
    }

    public List<Block> getAirBlockForFire() {
        List<Block> result = null;
        for (int x = vertex1.getBlockX(); x < vertex2.getBlockX(); x++) {
            for (int y = vertex1.getBlockY(); y < vertex2.getBlockY(); y++) {
                for (int z = vertex1.getBlockY(); z < vertex2.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    boolean isLowerBlockAir = world.getBlockAt(x, y - 1, z).getType().isAir();
                    if (block.getType().isAir() && !isLowerBlockAir) {
                        result.add(block);
                    }
                }
            }
        }
        return result;
    }

    public List<Block> getFireBlocks() {
        List<Block> result = null;
        List<Block> blocks = getAllBlocks();
        for (Block b: blocks) {
            if (b.getType() == Material.FIRE) {
                result.add(b);
            }
        }
        return result;
    }

    public void setFireBlocks() {
        List<Block> blocks = getAirBlockForFire();
        for (Block b: blocks) {
            b.setType(Material.FIRE);
        }
    }

    public void removeFireBlocks() {
        List<Block> blocks = getFireBlocks();
        for (Block b: blocks) {
            b.setType(Material.AIR);
        }
    }


    public Location randomLocation() {
        double x = randomBetween(vertex1.getBlockX(), vertex2.getBlockX()) + 0.5; // ブロックの中心にするため +0.5
        double y = randomBetween(vertex1.getBlockY(), vertex2.getBlockY()) + 0.5;
        System.out.println(y);
        double z = randomBetween(vertex1.getBlockZ(), vertex2.getBlockZ()) + 0.5;
        return new Location(world, x, y, z);
    }

    private int randomBetween(int a, int b) {
        var randomMax = Math.abs(b - a);
        if(randomMax==0){
            return a;
        }
        if (b < a) {
            return new Random().nextInt(randomMax) + b;
        }
        return new Random().nextInt(randomMax) + a;
    }

    public boolean isInArea(Location location) {
        Block block = location.getBlock();
        Block v1 = vertex1.getBlock();
        Block v2 = vertex2.getBlock();

        return  isBetween(block.getX(), v1.getX(), v2.getX()) &&
                isBetween(block.getY(), v1.getY(), v2.getY()) &&
                isBetween(block.getZ(), v1.getZ(), v2.getZ())
                ;
    }

    private boolean isBetween(double i, double a, double b) {
        return (a <= i && i <= b) || (b <= i && i <= a);
    }

    private Location[] justifyVertexes(Location vertex1, Location vertex2) {
        double[] x = sortTwo(vertex1.getBlockX(), vertex2.getBlockX());
        double[] y = sortTwo(vertex1.getBlockY(), vertex2.getBlockY());
        double[] z = sortTwo(vertex1.getBlockZ(), vertex2.getBlockZ());

        return new Location[]{new Location(world, x[0], y[1], z[0]), new Location(world, x[1], y[1], z[1])};
    }

    private double[] sortTwo(double a, double b) {
        var result = new double[2];

        int i = Double.compare(a, b);
        if (i < 0) {
            result[0] = b;
            result[1] = a;
        } else {
            result[0] = a;
            result[1] = b;
        }
        return result;
    }
}
