package com.github.gentoopng.tducenvmirror.db;

import java.util.Date;

public class Record {
    private final String id;
    private final Date timestamp;
    private final String areaID;
    private final double temperature; // Celsius
    private final double humidity; // percent
    private final double pressure; // Pa
    private final int co2; // ppm
    private final double wbgt; // WBGT

    Record(String id, Date timestamp, String areaID, double temperature, double humidity, double pressure, int co2, double wbgt) {
        this.id = id;
        this.areaID = areaID;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.co2 = co2;
        this.wbgt = wbgt;
    }

    Record(String id, Date timestamp, String areaID, String temperature, String humidity, String pressure, int co2, String wbgt) {
        this.id = id;
        this.timestamp = timestamp;
        this.areaID = areaID;
        this.temperature = Double.parseDouble(temperature);
        this.humidity = Double.parseDouble(humidity);
        this.pressure = Double.parseDouble(pressure);
        this.co2 = co2;
        this.wbgt = Double.parseDouble(wbgt);
    }

    public String getId() { return id; }
    public Date getTimestamp() { return timestamp; }
    public String getAreaID() { return areaID; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getPressure() { return pressure; }
    public int getCo2() { return co2; }
    public double getWbgt() { return wbgt; }

    @Override
    public String toString() {
        return "_id: " + id +
                "\ntimestamp:   " + timestamp.toString() +
                "\nareaID:      " + areaID +
                "\ntemperature: " + temperature + "°C" +
                "\nhumidity:    " + humidity + "%" +
                "\npressure:    " + pressure + "Pa" +
                "\nCO2:         " + co2 + "ppm" +
                "\nWBGT:        " + wbgt + "°C";
    }
}
