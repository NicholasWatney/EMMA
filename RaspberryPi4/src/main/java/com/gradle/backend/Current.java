package com.gradle.backend;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Current {
    private double currentReading;
    private JLabel currentLabel;
    private int currentNumber;
    private long previousTime;

    private static int currentCount = 0;
    private double fps;
    public static Map<String, Current> currentMap = new HashMap<String, Current>();

    public Current() {
        currentNumber = ++currentCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        currentReading = 0;
        currentLabel = new JLabel(String.format("Current Reading %d (%.01fFPS): %.01f", currentNumber,
                fps, currentReading));
        currentMap.put("I" + Integer.toString(currentNumber), this);
    }

    public long updateAndGetTimeDifference() {
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - previousTime;
        previousTime = currentTime;
        return difference;
    }

        public void updateFPS() {
        long timeDifference = updateAndGetTimeDifference();
        if (timeDifference != 0) {
            fps = 1000.0 / timeDifference;
        }
    }

    public JLabel getCurrentLabel() {
        return currentLabel;
    }

    public void updateJLabel() {
        currentLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.01f", currentNumber,
                fps, currentReading));
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

}