

package com.gradle.backend;

import com.gradle.swing.MainGUI;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Voltage {
    private double voltageReading;
    private JLabel voltageLabel;
    private int voltageNumber;
    private long previousTime;
    public double voltageWarning;
    public double voltageCritical;
    public int currentSample = 0;
    public static final int currentSize = 60;
    public JLabel precautionLabel;

    private static int voltageCount = 0;
    private double fps;
    public double avgFps;
    public static Map<String, Voltage> voltageMap = new HashMap<String, Voltage>();
    public static ArrayList<XYSeries> voltageList = new ArrayList<>();
    public static final int historySize = 60;
    public XYSeries voltageHistory;
    public int voltageSample = 0;

    public void updateAvgFps() {
        updateFPS();
        avgFps = (9 * avgFps + fps) / 10;
    }

    public JLabel getPrecautionLabel() {
        return precautionLabel;
    }

    public void nullifyFps() {
        avgFps = 0;
    }

    public Voltage(double voltageWarning, double voltageCritical) {
        precautionLabel = new JLabel();
        this.voltageWarning = voltageWarning;
        this.voltageCritical = voltageCritical;
        voltageNumber = ++voltageCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        voltageReading = 0;
        voltageLabel = new JLabel(String.format("Voltage Reading %d (%.01fFPS): %.01f", voltageNumber,
                fps, voltageReading));
        precautionLabel.setText("[" + setPrecaution() + "]");
        voltageMap.put("V" + Integer.toString(voltageNumber), this);
        voltageHistory = new XYSeries("V" + Integer.toString(voltageNumber));
        voltageList.add(voltageHistory);
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

    private void updateConsole(StringBuilder message) {
        MainGUI.mainGUI.updateConsole(message);
    }

    private void updateConsole(String message) {
        MainGUI.mainGUI.updateConsole(new StringBuilder(message));
    }

    public String setPrecaution() {
        if (avgFps < 0.1) {
            precautionLabel.setForeground(Color.LIGHT_GRAY);
            voltageLabel.setForeground(Color.LIGHT_GRAY);
            return " OFFLINE ";
        } else if (voltageReading > voltageCritical) {
            if (precautionLabel.getForeground() == Color.RED) {
                precautionLabel.setForeground(Color.ORANGE);
                voltageLabel.setForeground(Color.ORANGE);
            } else {
                precautionLabel.setForeground(Color.RED);
                voltageLabel.setForeground(Color.RED);
            }
            return " CRITICAL ";
        } else if (voltageReading > voltageWarning) {
            precautionLabel.setForeground(Color.red);
            voltageLabel.setForeground(Color.red);
            return "WARNING";
        } else {
            precautionLabel.setForeground(Color.BLACK);
            voltageLabel.setForeground(Color.BLACK);
            return " NORMAL ";
        }
    }

    public void addVoltageHistory() {
        if (currentSample > currentSize) {
            voltageHistory.remove(0);
        }
        currentSample++;
        voltageHistory.add(MainGUI.epochDifference / 1000.0, voltageReading, true);
    }

    public void updatePrecautionLabel() {
        precautionLabel.setText("[" + setPrecaution() + "]");
    }

    public void setVoltageReading(double voltageReading) {
        this.voltageReading = voltageReading;
    }

}