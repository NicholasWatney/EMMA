package com.gradle.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGUI extends AppGUI {

    public final int SPEED = 20;

    private int concurrentReading1;
    private JLabel concurrentLabel1;
    private long concurrentTime1;
    private int concurrentReading2;
    private JLabel concurrentLabel2;
    private long concurrentTime2;
    private int concurrentReading3;
    private JLabel concurrentLabel3;
    private long concurrentTime3;

    private JLabel temperatureSensor1;
    private int temperatureReading1;
    private JLabel temperatureSensor2;
    private int temperatureReading2;
    private JLabel temperatureSensor3;
    private int temperatureReading3;

    private JButton lightButton1;
    private JButton lightButton2;
    private JButton lightButton3;
    private JButton lightButton4;

    private Timer timer;
    private long oldTime;
    private long newTime;
    private long difference;

    public MainGUI() {
        oldTime = -1;
        concurrentTime1 = 0;
        concurrentTime2 = 0;
        concurrentTime3 = 0;
    }

    public void buildGUI() {
        mainFrame = new JFrame("EMMA Application");
        Container mainPanel = mainFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        mainPanel.add(getMainPanel(), constraints);

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(true);
        mainFrame.setVisible(true);
        initializeAndStartTimer();
    }

    protected JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getExternalConcurrentPanel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        panel.add(getLightPanel(), constraints);
        constraints.gridheight = 2;
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        panel.add(getExternalTemperaturePanel(), constraints);
        return panel;
    }

    private JPanel getExternalTemperaturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Temperature Sensors:"));
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getTemperaturePanel(), constraints);
        return panel;
    }

    private JPanel getTemperaturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 40, 0, 40);
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        temperatureSensor1 = new JLabel(String.format("Temperature Sensor 1 (20FPS): %d", temperatureReading1));
        panel.add(temperatureSensor1, constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        temperatureSensor2 = new JLabel(String.format("Temperature Sensor 2 (20FPS): %d", temperatureReading2));
        panel.add(temperatureSensor2, constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.CENTER);
        temperatureSensor3 = new JLabel(String.format("Temperature Sensor 3 (20FPS): %d", temperatureReading3));
        panel.add(temperatureSensor3, constraints);
        return panel;
    }

    private JPanel getExternalConcurrentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("MultiThreading Processes: (Internal Clock: 50FPS)"));
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getConcurrentPanel(), constraints);
        return panel;
    }

    private JPanel getConcurrentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 0, 0, 0);
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        concurrentReading1 = 0;
        concurrentLabel1 = new JLabel(String.format("Random Process 1 (20FPS): %d", concurrentReading1));
        panel.add(concurrentLabel1, constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        concurrentReading2 = 0;
        concurrentLabel2 = new JLabel(String.format("Random Process 2 (10FPS): %d", concurrentReading2));
        panel.add(concurrentLabel2, constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.CENTER);
        concurrentReading3 = 0;
        concurrentLabel3 = new JLabel(String.format("Random Process 3 (5FPS): %d", concurrentReading3));
        constraints.insets = new Insets(0, 0, 20, 0);
        panel.add(concurrentLabel3, constraints);
        return panel;
    }

    private JPanel getLightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Toggle Lights:"));
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        lightButton1 = new JButton("Light1");
        panel.add(lightButton1, constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        lightButton2 = new JButton("Light2");
        panel.add(lightButton2, constraints);
        setConstraints(constraints, 2, 0, GridBagConstraints.CENTER);
        lightButton3 = new JButton("Light3");
        panel.add(lightButton3, constraints);
        setConstraints(constraints, 3, 0, GridBagConstraints.CENTER);
        lightButton4 = new JButton("Light4");
        panel.add(lightButton4, constraints);
        return panel;
    }

    private void initializeAndStartTimer() {
        int speed = SPEED; // FPS 1000/50 = 20FPS
        int delay = 0;
        timer = new Timer(speed, sampler());
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
    }

    private ActionListener sampler() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                difference();
                updateConcurrentProcesses();
            }
        };
    }

    private void updateConcurrentProcesses() {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                concurrentTime1 += difference;
                if (concurrentTime1  > 50) {
                    concurrentReading1++;
                    concurrentLabel1.setText(String.format("Random Process 1 (20FPS): %d", concurrentReading1));
                    concurrentTime1 = concurrentTime1 % 50;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "concurrentProcessThread1");

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                concurrentTime2 += difference;
                if (concurrentTime2  > 100) {
                    concurrentReading2++;
                    concurrentLabel2.setText(String.format("Random Process 2 (10FPS): %d", concurrentReading2));
                    concurrentTime2 = concurrentTime2 % 100;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "concurrentProcessThread2");

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                concurrentTime3 += difference;
                if (concurrentTime3  > 200) {
                    concurrentReading3++;
                    concurrentLabel3.setText(String.format("Random Process 3 (5FPS): %d", concurrentReading3));
                    concurrentTime3 = concurrentTime3 % 200;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "concurrentProcessThread3");

        thread1.start();
        thread2.start();
        thread3.start();
    }

    private void difference() {
        if (oldTime == -1) {
            difference = 0;
            oldTime = System.currentTimeMillis();
        } else {
            newTime = System.currentTimeMillis();
            difference = newTime - oldTime;
            oldTime = newTime;
        }
    }
}