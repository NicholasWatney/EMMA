package com.gradle.swing;

import org.junit.runner.OrderWith;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AbortGUI extends AppGUI {

    private JLabel abortText;
    private JButton abortButton;
    private int countDown;
    private Timer timer;

    public AbortGUI() {}

    public void buildGUI() {
        abortFrame = new JFrame("EMMA Application");
        Container mainPanel = abortFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        mainPanel.add(getMainPanel(), constraints);

        abortFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        abortFrame.setSize(250, 150);
        abortFrame.setLocationRelativeTo(null);
        abortFrame.setVisible(true);
        initializeAndStartTimer();
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("EMMA Project"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getAbortPanel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        panel.add(getActionPanel(), constraints);
        return panel;
    }

    private JPanel getAbortPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        countDown = 10;
        abortText = new JLabel(String.format("Launching in: %d", countDown));
        panel.add(abortText, constraints);
        return panel;
    }

    private JPanel getActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        JButton launchButton = new JButton("Launch");
        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeWindow();
                AppGUI.launchMainGUI();
            }
        });
        panel.add(launchButton, constraints);
        abortButton = new JButton("Abort");
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeWindow();
                System.exit(0);
            }
        });
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        panel.add(abortButton, constraints);
        return panel;
    }

    private void closeWindow() {
        abortFrame.dispose();
        timer.stop();
    }

    private void initializeAndStartTimer() {
        int speed = 1000; // FPS: 1
        int delay = 1000;
        timer = new Timer(speed, updateAbortTextField());
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
    }

    private ActionListener updateAbortTextField() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                abortText.setText(String.format("Launching in: %d", --countDown));
                if (countDown == 0) {
                    closeWindow();
                    AppGUI.launchMainGUI();
                }
            }
        };
    }
}
