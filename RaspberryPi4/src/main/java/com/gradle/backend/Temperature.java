package com.gradle.backend;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Temperature {

    private double temperatureReading;
    private JLabel temperatureLabel;
    private int temperatureNumber;
    private long previousTime;

    private static int temperatureCount = 0;
    private double fps;
    public static Map<String, Temperature> temperatureMap = new HashMap<String, Temperature>();

    public Temperature() {
        temperatureNumber = ++temperatureCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        temperatureReading = 0;
        temperatureLabel = new JLabel(String.format("Temperature Sensor %d (%.01fFPS): %.01f", temperatureNumber,
                fps, temperatureReading));
        temperatureMap.put("T" + Integer.toString(temperatureNumber), this);
    }

    public long updateAndGetTimeDifference() {
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - previousTime;
        previousTime = currentTime;
        return difference;
    }

    public void updateFPS() {
        fps = 1000.0 / updateAndGetTimeDifference();
    }

    public JLabel getTemperatureLabel() {
        return temperatureLabel;
    }

    public void updateJLabel() {
        temperatureLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.01f", temperatureNumber,
                fps, temperatureReading));
    }

    public void setTemperatureReading(double temperatureReading) {
        this.temperatureReading = temperatureReading;
    }

    
}
