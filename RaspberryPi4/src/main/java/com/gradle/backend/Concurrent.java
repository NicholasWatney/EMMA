package com.gradle.backend;

import com.gradle.swing.MainGUI;

import javax.swing.*;
import java.util.ArrayList;

public class Concurrent extends MainGUI {

    private int concurrentReading;
    private JLabel concurrentLabel;
    private long concurrentTime;
    private static int concurrentCount = 0;
    private double fps;
    private static ArrayList<Concurrent> concurrentList = new ArrayList<>();

    public Concurrent() {
        concurrentCount++;
        fps = (1000 / SPEED) / Math.pow(2, concurrentCount - 1);
        concurrentReading = 0;

        concurrentLabel = new JLabel(String.format("Random Process %d (%.1fFPS): %d",
                concurrentCount, fps, concurrentReading));
        concurrentTime = 0;
        concurrentList.add(this);
    }

    public void updateJLabel() {
        concurrentLabel.setText(String.format("Random Process %d (%.0fFPS): %d",
                concurrentCount, fps, concurrentReading));
    }

    public JLabel getConcurrentLabel() {
        return concurrentLabel;
    }

    public Thread createConcurrentThread(int number) {
        double period = 1000 / fps;
        return new Thread(new Runnable() {
            @Override
            public void run() {
                concurrentTime += difference;
                if (concurrentTime > period) {
                    concurrentReading++;
                    updateJLabel();
                    concurrentTime %= period;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, String.valueOf(number));
    }
    public static void updateConcurrent() {
        for (int i = 0; i < concurrentList.size(); i++) {
            concurrentList.get(i).createConcurrentThread(i).start();
        }
    }
}

