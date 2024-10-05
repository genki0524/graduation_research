package com.github.gentoopng.tducenvmirror.area;

import java.util.ArrayList;

public class AreaManager {
    ArrayList<Area> areaArrayList;

    public AreaManager() {
        areaArrayList = new ArrayList<>();
    }

    public ArrayList<Area> getArrayList() {
        return areaArrayList;
    }

    public Area getByID(String areaID) {
        if (areaID == null) { return null; }

        areaID = areaID.toLowerCase();

        for (Area area : areaArrayList) {
            if (area.getAreaID().equalsIgnoreCase(areaID)) {
                return area;
            }
        }
        // リストになかった場合
        return null;
    }

    public Area getByName(String areaName) {
        if (areaName == null) { return null; }

        areaName = areaName.toLowerCase();

        for (Area area : areaArrayList) {
            if (area.getAreaName().equalsIgnoreCase(areaName)) {
                return area;
            }
        }
        // リストになかった場合
        return null;
    }

    public Area getArea(String areaIDorName) {
        Area result;
        result = getByID(areaIDorName);
        if (result == null) {
            result = getByName(areaIDorName);
        }
        // ID, Name のどちらでも見つからなかった場合
        return result;
    }

    public boolean add(Area areaToAdd) {
        if (areaToAdd != null && getByID(areaToAdd.areaID) == null) {
            areaArrayList.add(areaToAdd);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeByID(String areaID) {
        Area area = getByID(areaID); // リストになければ，null が返ってくる
        if (area != null) {
            areaArrayList.remove(area);
            return true;
        } else {
            return false;
        }
    }

    public boolean setEntryPoint(String areaID, double x, double y, double z) {
        Area area = getByID(areaID);
        return area.setEntryPoint(x, y, z);
    }

    public void setTextLocation(String areaID) {
        setTextLocation(getByID(areaID));
    }

    public void setTextLocation(Area area) {
        area.setTextLocation(area.getCenterLocation());
    }

    public void setTextLocation(String areaID, double x, double y, double z) {
        Area area = getByID(areaID);
        setTextLocation(area, x, y, z);
    }
    public void setTextLocation(Area area, double x, double y, double z) {
        area.setTextLocation(x, y, z);
    }
}
