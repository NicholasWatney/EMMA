package com.gradle.swing;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AbortGUI extends AppGUI {

    private JLabel abortText;
    private JGradientButton abortButton;
    private int countDown;
    private Timer timer;
    private static final int fontSize = 72;

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
        abortFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        abortFrame.setUndecorated(true);
        abortFrame.setVisible(true);
        initializeAndStartTimer();
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        TitledBorder border = BorderFactory.createTitledBorder("EMMA Application");
        border.setTitleFont(new Font("Arial", Font.PLAIN, fontSize));
        border.setTitleJustification(TitledBorder.CENTER);
        border.setBorder(new StrokeBorder(new BasicStroke(5.0f)));
        panel.setBorder(border);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 0, 0, 0);
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
        setConstraints(constraints, 0, 0, GridBagConstraints.WEST);
        countDown = 10;
        abortText = new JLabel(String.format("Launching in: %d", countDown));
        abortText.setFont(new Font("Arial", Font.PLAIN, fontSize));
        panel.add(abortText, constraints);
        return panel;
    }

    private JPanel getActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
//        JButton launchButton = new JButton("Launch");
        JGradientButton launchButton = new JGradientButton("LAUNCH");
        launchButton.setBackground(Color.GREEN);
        launchButton.setFont(new Font("Arial", Font.PLAIN, fontSize));
        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AppGUI.launchMainGUI();
                closeWindow();
            }
        });
        constraints.insets = new Insets(10, 30, 10, 10);
        panel.add(launchButton, constraints);
//        abortButton = new JButton("Abort");
        abortButton = new JGradientButton("ABORT");
        abortButton.setBackground(Color.RED);
        abortButton.setFont(new Font("Arial", Font.PLAIN, fontSize));
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                closeWindow();
                System.exit(0);
            }
        });
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        constraints.insets = new Insets(10, 10, 10, 30);
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
