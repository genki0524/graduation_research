package com.github.gentoopng.tducenvmirror.model;

public class GroveGestureData {
    public String smart_ir_gesture;
    public String grove_gesture;
    public String pose;
    public String gesture_L;
    public String gesture_R;
    public String user_name;

    public GroveGestureData(String grove_gesture, String smart_ir_gesture,String pose) {
        this.smart_ir_gesture = smart_ir_gesture;
        this.grove_gesture = grove_gesture;
        this.pose = pose;
    }

}
