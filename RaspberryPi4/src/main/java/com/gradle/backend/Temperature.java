package com.gradle.backend;

import com.gradle.swing.MainGUI;
import com.sun.tools.javac.Main;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Temperature {

    private double temperatureReading;
    public double temperatureWarning;
    public double temperatureCritical;
    private JLabel temperatureLabel;
    private JLabel precautionLabel;
    private int temperatureNumber;
    private long previousTime;
    public StringBuilder criticalMessage;

    private static int temperatureCount = 0;
    private double fps;
    private double avgFps;
    public static Map<String, Temperature> temperatureMap = new HashMap<>();
    public static ArrayList<XYSeries> temperatureList = new ArrayList<>();
    public static final int historySize = 60;
    public XYSeries temperatureHistory;
    public int temperatureSample = 0;

    public void updateAvgFps() {
        updateFPS();
        avgFps = (9 * avgFps + fps) / 10;
    }

    public void nullifyFps() {
        avgFps = 0;
    }

    public void nullifyTemp() {
        temperatureReading = 0;
    }

    public Temperature(double temperatureWarning, double temperatureCritical) {
        criticalMessage = new StringBuilder();
        precautionLabel = new JLabel();
        this.temperatureWarning = temperatureWarning;
        this.temperatureCritical = temperatureCritical;
        temperatureNumber = ++temperatureCount;
        previousTime = System.currentTimeMillis();
        fps = 0;
        avgFps = 0;
        temperatureReading = 0;
        temperatureLabel = new JLabel(String.format("Temperature Sensor %d (%.01fFPS): %.03f", temperatureNumber,
                avgFps, temperatureReading));
        precautionLabel.setText("[" + setPrecaution() + "]" );

        temperatureMap.put("T" + Integer.toString(temperatureNumber), this);
        temperatureHistory = new XYSeries("T" + Integer.toString(temperatureNumber));
        temperatureList.add(temperatureHistory);
    }

    private void updateConsole(StringBuilder message) {
        MainGUI.mainGUI.updateConsole(message);
    }

    private void updateConsole(String message) {
        MainGUI.mainGUI.updateConsole(new StringBuilder(message));
    }

    public boolean inConsole = false;
    public String setPrecaution() {
        if (criticalMessage.length() == 0) {
            inConsole = false;
        }

        if (avgFps < 0.1) {
            if (criticalMessage.length() != 0) {
                criticalMessage.setLength(0);
                String timeString = MainGUI.timeNotation(MainGUI.epochDifference);
                timeString = " " + timeString.substring(1, timeString.length() - 1) + ": ";
                criticalMessage.append(timeString);
                criticalMessage.append("[ OFFLINE ] " + getTemperatureLabel().getText());
                MainGUI.mainGUI.refreshConsole();
            } else {
                inConsole = false;
            }
            precautionLabel.setForeground(Color.LIGHT_GRAY);
            temperatureLabel.setForeground(Color.LIGHT_GRAY);
            return " OFFLINE ";
        } else if (temperatureReading > temperatureCritical) {
            criticalMessage.setLength(0);
            String timeString = MainGUI.timeNotation(MainGUI.epochDifference);
            timeString = " " + timeString.substring(1, timeString.length() - 1) + ": ";
            criticalMessage.append(timeString);
            criticalMessage.append("[ CRITICAL ] " + getTemperatureLabel().getText());
            if (!inConsole) {
                updateConsole(criticalMessage);
                inConsole = true;
            } else {
                MainGUI.mainGUI.refreshConsole();
            }

            if (precautionLabel.getForeground() == Color.RED) {
                precautionLabel.setForeground(Color.ORANGE);
                temperatureLabel.setForeground(Color.ORANGE);
            } else {
                precautionLabel.setForeground(Color.RED);
                temperatureLabel.setForeground(Color.RED);
            }
            return " CRITICAL ";
        } else if (temperatureReading > temperatureWarning) {
            if (criticalMessage.length() != 0) {
                criticalMessage.setLength(0);
                String timeString = MainGUI.timeNotation(MainGUI.epochDifference);
                timeString = " " + timeString.substring(1, timeString.length() - 1) + ": ";
                criticalMessage.append(timeString);
                criticalMessage.append("[WARNING] " + getTemperatureLabel().getText());
                MainGUI.mainGUI.refreshConsole();
            } else {
                inConsole = false;
            }
            precautionLabel.setForeground(Color.red);
            temperatureLabel.setForeground(Color.red);
            return "WARNING";
        } else {
            if (criticalMessage.length() != 0) {
                criticalMessage.setLength(0);
                String timeString = MainGUI.timeNotation(MainGUI.epochDifference);
                timeString = " " + timeString.substring(1, timeString.length() - 1) + ": ";
                criticalMessage.append(timeString);
                criticalMessage.append("[ NORMAL ] " + getTemperatureLabel().getText());
                MainGUI.mainGUI.refreshConsole();
            } else {
                inConsole = false;
            }
            precautionLabel.setForeground(Color.BLACK);
            temperatureLabel.setForeground(Color.BLACK);
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

    public JLabel getTemperatureLabel() {
        return temperatureLabel;
    }

    public JLabel getPrecautionLabel() {
        return precautionLabel;
    }

    public void updateJLabel() {
//        temperatureLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.03f", temperatureNumber,
//                avgFps, temperatureReading));
        temperatureLabel.setText(String.format("Temperature Sensor %d (%.01fFPS): %.03f", temperatureNumber,
                avgFps, temperatureReading));
    }

    public void updatePrecautionLabel() {
        precautionLabel.setText("[" + setPrecaution() + "]");
    }

    public void addTemperatureHistory() {
        if (temperatureSample > historySize) {
            temperatureHistory.remove(0);
        }
        temperatureSample++;
        temperatureHistory.add(MainGUI.epochDifference / 1000.0, temperatureReading, true);
    }

    public void setTemperatureReading(double temperatureReading) {
        this.temperatureReading = temperatureReading;
    }

    
}
