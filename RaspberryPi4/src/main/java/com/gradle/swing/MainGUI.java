package com.gradle.swing;

import com.gradle.backend.Concurrent;
import com.gradle.backend.Light;
import com.gradle.backend.Temperature;
import com.gradle.backend.UART;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGUI extends AppGUI {

    public final int SPEED = 50; // FPS = 20

    private static Timer timer;
    private static long oldTime;
    private static long newTime;
    protected static long difference;

    public MainGUI() {
        oldTime = -1;
    }

    public void buildGUI() {
        mainFrame = new JFrame("EMMA Application");
        Container mainPanel = mainFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        mainPanel.add(getBorderPanel(getMainPanel(), "Emma Application", new Insets(8, 21, 21, 21)), constraints);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(true);
        mainFrame.setVisible(true);
        UART uart = new UART();
        Thread thread = uart.createReadUARTThread();
        thread.start();
        initializeAndStartTimer();
    }

    protected JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getConcurrentPanel(), "MultiThreading Processes: (Internal Clock: " + 1000 / SPEED + ")" ), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getLightPanel(), "Toggle Lights:"), constraints);
        constraints.gridheight = 2;
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getTemperaturePanel(), "Temperature Panel:"), constraints);
        return panel;
    }

    private JPanel getTemperaturePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 40, 0, 40);
        setConstraints(constraints, 0, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Temperature().getTemperatureLabel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Temperature().getTemperatureLabel(), constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Temperature().getTemperatureLabel(), constraints);
        return panel;
    }

    private JPanel getConcurrentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 0, 0, 0);
        setConstraints(constraints, 0, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Concurrent().getConcurrentLabel(), constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        setConstraints(constraints, 0, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Concurrent().getConcurrentLabel(), constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.FIRST_LINE_START);
        constraints.insets = new Insets(0, 0, 20, 0);
        panel.add(new Concurrent().getConcurrentLabel(), constraints);
        return panel;
    }

    private JPanel getLightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(new Light().getLightLabel(), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        panel.add(new Light().getLightLabel(), constraints);
        setConstraints(constraints, 2, 0, GridBagConstraints.CENTER);
        panel.add(new Light().getLightLabel(), constraints);
        setConstraints(constraints, 3, 0, GridBagConstraints.CENTER);
        panel.add(new Light().getLightLabel(), constraints);
        return panel;
    }

    private void initializeAndStartTimer() {
        int speed = SPEED;
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
                Concurrent.updateConcurrent();
            }
        };
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