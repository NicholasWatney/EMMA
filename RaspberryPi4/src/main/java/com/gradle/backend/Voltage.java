

package com.gradle.backend;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Voltage {
    private double voltageReading;
    private JLabel voltageLabel;
    private int voltageNumber;
    private long previousTime;

    private static int voltageCount = 0;
    private double fps;
    public static Map<String, Voltage> voltageMap = new HashMap<String, Voltage>();

    public Voltage() {
        voltageNumber = ++voltageCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        voltageReading = 0;
        voltageLabel = new JLabel(String.format("Voltage Reading %d (%.01fFPS): %.01f", voltageNumber,
                fps, voltageReading));
        voltageMap.put("I" + Integer.toString(voltageNumber), this);
    }

    public long updateAndGetTimeDifference() {
        long voltageTime = System.currentTimeMillis();
        long difference = voltageTime - previousTime;
        previousTime = voltageTime;
        return difference;
    }

    public void updateFPS() {
        long timeDifference = updateAndGetTimeDifference();
        if (timeDifference != 0) {
            fps = 1000.0 / timeDifference;
        }
    }

    public JLabel getVoltageLabel() {
        return voltageLabel;
    }

    public void updateJLabel() {
        voltageLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.01f", voltageNumber,
                fps, voltageReading));
    }

    public void setVoltageReading(double voltageReading) {
        this.voltageReading = voltageReading;
    }

}