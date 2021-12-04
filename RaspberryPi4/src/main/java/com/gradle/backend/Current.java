package com.gradle.backend;

import com.gradle.swing.MainGUI;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Current {
    private double currentReading;
    private JLabel currentLabel;
    public int currentNumber;
    private long previousTime;
    public static final int currentSize = 60;
    public JLabel precautionLabel;

    private static int currentCount = 0;
    public double currentWarning;
    public double currentCritical;
    private double fps;
    private double avgFps;
    public static Map<String, Current> currentMap = new HashMap<String, Current>();
    public static ArrayList<XYSeries> currentList = new ArrayList<>();
    public static final int historySize = 60;
    public XYSeries currentHistory;
    public int currentSample = 0;

    public Current(double currentWarning, double currentCritical) {
        this.currentWarning = currentWarning;
        this.currentCritical = currentCritical;
        precautionLabel = new JLabel();
        currentNumber = ++currentCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        currentReading = 0;
        currentLabel = new JLabel(String.format("Current Reading %d (%.01fFPS): %.01f", currentNumber,
                fps, currentReading));
        precautionLabel.setText("[" + setPrecaution() + "]");
        currentMap.put("I" + Integer.toString(currentNumber), this);
        currentHistory = new XYSeries("I" + Integer.toString(currentNumber));
        currentList.add(currentHistory);

    }

    public void nullifyFps() {
        avgFps = 0;
    }

    private void updateConsole(StringBuilder message) {
        MainGUI.mainGUI.updateConsole(message);
    }

    private void updateConsole(String message) {
        MainGUI.mainGUI.updateConsole(new StringBuilder(message));
    }

    public String setPrecaution() {
        if (avgFps < 0.1) {
            precautionLabel.setForeground(Color.LIGHT_GRAY);
            currentLabel.setForeground(Color.LIGHT_GRAY);
            return " OFFLINE ";
        } else if (currentReading > currentCritical) {
            if (precautionLabel.getForeground() == Color.RED) {
                precautionLabel.setForeground(Color.ORANGE);
                currentLabel.setForeground(Color.ORANGE);
            } else {
                precautionLabel.setForeground(Color.RED);
                currentLabel.setForeground(Color.RED);
            }
            return " CRITICAL ";
        } else if (currentReading > currentWarning) {
            precautionLabel.setForeground(Color.red);
            currentLabel.setForeground(Color.red);
            return "WARNING";
        } else {
            precautionLabel.setForeground(Color.BLACK);
            currentLabel.setForeground(Color.BLACK);
            return " NORMAL ";
        }
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

    public void updateAvgFps() {
        updateFPS();
        avgFps = (9 * avgFps + fps) / 10;
    }

    public JLabel getCurrentLabel() {
        return currentLabel;
    }

    public void updateJLabel() {
        currentLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.01f", currentNumber,
                fps, currentReading));
    }

    public void addCurrentHistory() {
        if (currentSample > currentSize) {
            currentHistory.remove(0);
        }
        currentSample++;
        currentHistory.add(MainGUI.epochDifference / 1000.0, currentReading, true);
    }

    public void setCurrentReading(double currentReading) {
        this.currentReading = currentReading;
    }

    public void updatePrecautionLabel() {
        precautionLabel.setText("[" + setPrecaution() + "]");
    }

    public JLabel getPrecautionLabel() {
        return precautionLabel;
    }

}