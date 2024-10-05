package com.github.gentoopng.tducenvmirror.db;

import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.area.Area;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;

import org.bson.Document;

public class DBAccessor {
    TDUCEnvMirror instance;

    MongoClient client;
    MongoDatabase db;
    MongoCollection<Document> collection;

    String access;
    String dbName;
    String collectionName;

    public DBAccessor(TDUCEnvMirror instance, String access, String dbName, String collectionName) {
        this.instance = instance;
        this.access = access;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    public void createConnection(String access, String dbName, String collectionName) {
        instance.getLogger().info("Connecting to the DB:");
        instance.getLogger().info(access);
        // Create MongoDB client
        client = MongoClients.create(access);
        // Get DB Object
        db = client.getDatabase(dbName);
        // Get collection
        collection = db.getCollection(collectionName);
    }

    // timestamp, areaID, temperature, humidity, pressure, co2
    public Record read(Area area) {
        return read(area.getAreaID());
    }

    public Record read(String areaID) {
        instance.getLogger().info("Getting data from the DB");
        if (client == null) {
            createConnection(access, dbName, collectionName);
        }
        Document doc = collection.find().sort(new BasicDBObject("timestamp", -1)).first();

        try {
            if (doc != null) {
                var record = new Record(
                        doc.getObjectId("_id").toString(),
                        doc.getDate("timestamp"),
                        doc.getString("areaid"),
                        doc.get("temperature").toString(),
                        doc.get("humidity").toString(),
                        doc.get("pressure").toString(),
                        doc.getInteger("co2"),
                        doc.getString("wbgt")
                );
                //instance.getLogger().info(record.toString());
                return record;
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean readAndSet(String areaID) {
        Area area = instance.areaManager.getArea(areaID);
        if (area == null) {
            return false;
        }
        return readAndSet(area);
    }
    public boolean readAndSet(Area area) {
        Record record = read(area);
        if (record == null) {
            return false;
        }
        setRecord(area, record);
        return true;
    }

    public void closeConnection() {
        instance.getLogger().info("Closing the connection to DB...");
        client.close();
    }

    public void setRecord(Area area, Record record) {
        area.setTemperature(record.getTemperature());
        area.setHumidity(record.getHumidity());
        area.setPressure(record.getPressure());
        area.setCo2(record.getCo2());
        area.setDate(record.getTimestamp());
        area.setWbgt(record.getWbgt());
    }
}
