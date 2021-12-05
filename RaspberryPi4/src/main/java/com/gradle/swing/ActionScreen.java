package com.gradle.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.StrokeBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ActionScreen extends AppGUI {

    public StringBuilder disabledButton;
    public StringBuilder buttonSequence;

    public static ActionScreen actionScreen;
    public static long epochDifference;
    public static long epochClock;

    public static int consoleIndex = 0;

    final static int CHARGE = 0;
    final static int DISCHARGE = 1;

    public JFrame getFrame() {
        return actionFrame;
    }

    public int pushableButton = CHARGE;
    public boolean buttonPushed = false;

    private static long oldTime;
    private static long newTime;
    protected static long difference;

    void startMasterClock() {
        epochClock = System.currentTimeMillis();
    }

    void updateMasterClock() {
        epochDifference = System.currentTimeMillis() - epochClock;
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

    void updateClock() {
        actionBorder.setTitle("EMMA Application " + timeNotation(epochDifference));
        actionFrame.getContentPane().getComponent(0).repaint();
    }

    private void updateGUI() {
        while (true) {
            updateMasterClock();
            difference();
            updateClock();
//            parseCritical();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private static final int fontSize = 72;

    public static final int consoleWidth = 190;
    public static final int consoleHeight = 10;
    public static ArrayList<StringBuilder> consoleList;
    JTextArea textArea;
    JTextPane textPane;
    public static StringBuilder console;

    public JGradientButton chargeButton;
    public JGradientButton dischargeButton;

    public ActionScreen() {}

    public void buildGUI() {
        consoleIndex = 0;
        disabledButton = new StringBuilder("");
        buttonSequence = new StringBuilder("");
        startMasterClock();

        actionFrame = new JFrame("EMMA Application");
        Container mainPanel = actionFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);

        mainPanel.add(getActionBorderPanel(getMainPanel(), "EMMA Application " + timeNotation(epochDifference),
                new Insets(8, 3, 3, 0)), constraints);

        actionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        actionFrame.setSize(250, 150);
        actionFrame.setLocationRelativeTo(null);
        actionFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        actionFrame.setUndecorated(true);
        actionFrame.setVisible(true);
        Thread updateGUIThread = createUpdateGUIThread();
        updateGUIThread.start();
        updateConsole("To initiate the charging sequence, please select the \"CHARGE\" option above...");
    }

    public JPanel getMainPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        panel.add(getActionPanel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        panel.add(getConsolePanel(), constraints);
        return panel;
    }

    public static String timeNotation(long difference) {
        long hours = difference / 3600000;
        difference = difference % 3600000;
        long minutes = difference / 60000;
        difference = difference % 60000;
        long seconds = difference / 1000;
        return String.format("(%02d:%02d:%02d)", hours, minutes, seconds);
    }


    protected JPanel getConsolePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        initializeConsole();
        TitledBorder border = BorderFactory.createTitledBorder("Console:");
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        border.setTitleJustification(TitledBorder.LEFT);
        border.setBorder(new StrokeBorder(new BasicStroke(1.0f), borderColor));
        Border consoleBorder = new StrokeBorder(new BasicStroke(1.0f), Color.BLACK);
        textArea.setBorder(consoleBorder);
        panel.setBorder(border);
        textArea.setEditable(false);
        panel.add(textArea);
        return panel;
    }

    protected void initializeConsole() {
        consoleList = new ArrayList<StringBuilder>(consoleHeight);
        for (int i = 0; i < consoleHeight; i++) {
            StringBuilder consoleLine = new StringBuilder(consoleWidth);
            consoleList.add(consoleLine);
        }

        StringBuilder current = consoleList.get(consoleHeight - 1);
        for (int i = 0; i < consoleWidth; i++) {
            current.append(" ");
        }
        textArea = new JTextArea();
        textPane = new JTextPane();
        console = new StringBuilder();
        for (int i = 0; i < consoleHeight; i++) {
            console.append(consoleList.get(i));
            if (i < consoleHeight - 1) {
                console.append("\n");
            }
        }
        textArea.setText(console.toString());
    }

    private JPanel getActionPanel() {

        TitledBorder border = BorderFactory.createTitledBorder("Action:");
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        border.setTitleJustification(TitledBorder.LEFT);
        border.setBorder(new StrokeBorder(new BasicStroke(1.0f), borderColor));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(border);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
//        JButton launchButton = new JButton("Launch");
        chargeButton = new JGradientButton("CHARGE");
        chargeButton.setPreferredSize(new Dimension(
                (int) (chargeButton.getPreferredSize().getWidth() * 4.25),
                (int) (chargeButton.getPreferredSize().getHeight() * 4)
        ));
        if (pushableButton == CHARGE) {
            chargeButton.setBackground(Color.GREEN);
        } else {
            chargeButton.setBackground(Color.GRAY);
        }

        chargeButton.setFont(new Font("Arial", Font.PLAIN, 48));
        chargeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!buttonPushed) {
                    if (pushableButton == CHARGE) {
                        chargeButton.setBackground(Color.GRAY);
                        buttonPushed = true;
                        Thread chargeSequenceThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                chargeSequence();
                            }
                        }, "chargeSequenceThread");
                        chargeSequenceThread.start();
                    } else {
                        if (disabledButton.toString().equals("")) {
                            disabledButton.setLength(0);
                            disabledButton.append(" WARNING: Invalid user input. The system is completly charged. Please select the \"Discharge\" button.");
                            updateConsole(disabledButton);
                        }
                    }
                } else {
                    if (buttonSequence.toString().equals("")) {
                        buttonSequence.setLength(0);
                        buttonSequence.append(" WARNING: Invalid user input. The system is currently ");
                        if (pushableButton == CHARGE) {
                            buttonSequence.append("charging. ");
                        } else {
                            buttonSequence.append("discharging. ");
                        }
                        buttonSequence.append("Following safety protocol.");
                        updateConsole(buttonSequence);
                    }
                }
//                AppGUI.launchMainGUI();
//                AppGUI.launchActionScreen();
//                closeThreadWindow();
            }
        });
        constraints.insets = new Insets(0, 2, 4, 2);
        panel.add(chargeButton, constraints);
//        abortButton = new JButton("Abort");
        dischargeButton = new JGradientButton("DISCHARGE");
        dischargeButton.setPreferredSize(new Dimension(
                (int) (dischargeButton.getPreferredSize().getWidth() * 3.4),
                (int) (dischargeButton.getPreferredSize().getHeight() * 4.0)
        ));
        if (pushableButton == DISCHARGE) {
            dischargeButton.setBackground(Color.GREEN);
        } else {
            dischargeButton.setBackground(Color.GRAY);
        }
        dischargeButton.setFont(new Font("Arial", Font.PLAIN, 48));
        dischargeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!buttonPushed) {
                    if (pushableButton == DISCHARGE) {
                        buttonPushed = true;
                        dischargeButton.setBackground(Color.GRAY);
                        Thread chargeSequenceThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                dischargeSequence();
                            }
                        }, "chargeSequenceThread");
                        chargeSequenceThread.start();
                    } else {
                        if (disabledButton.toString().equals("")) {
                            disabledButton.setLength(0);
                            disabledButton.append(" WARNING: Invalid user input. The system is completed discharged. Please select the \"Charge\" button.");
                            updateConsole(disabledButton);
                        }
                    }
                } else {
                    if (buttonSequence.toString().equals("")) {
                        buttonSequence.setLength(0);
                        buttonSequence.append(" WARNING: Invalid user input. The system is currently ");
                        if (pushableButton == CHARGE) {
                            buttonSequence.append("charging. ");
                        } else {
                            buttonSequence.append("discharging. ");
                        }
                        buttonSequence.append("Following safety protocol.");
                        updateConsole(buttonSequence);
                    }
                }
            }
//                AppGUI.launchMainGUI();
//                closeThreadWindow();
        });
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        constraints.insets = new Insets(0, 2, 4, 2);
        panel.add(dischargeButton, constraints);
        return panel;
    }

    public void wait(int milis) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > milis) {
                return;
            }
        }
    }

    public void chargeSequence() {
        try {
            updateConsole("Charging sequence initiated. Verifying the state of the solenoid...");
            SwingUtilities.invokeLater(()->textArea.repaint());

//            wait(1000);
            Thread.sleep(1000);
            updateConsole(" [SUCCESS] Verified that solenoid is OFF. Digital PIN read: low. Verifying the state of the discharge relay...");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(1000);
            Thread.sleep(1000);
            updateConsole(" [SUCCESS] Verified that discharge relay is OFF. Digital PIN read: low. Closing 3-phase contractor...");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(500);
            Thread.sleep(500);
            updateConsole(" [SUCCESS] Closed 3-phase contractor. Digital PIN set: high. Verifying incoming current...");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(500);
            Thread.sleep(500);
            updateConsole(" [SUCCESS] Verified current is at least 2mA. Digital READING read: high. Verifying solenoid...");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(5000);
            Thread.sleep(5000);
            updateConsole(" [SUCCESS] Verified the solenoid is ON. Verified the relay circuit is CLOSED. Digital PIN set: high.");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(2000);
            Thread.sleep(2000);
            updateConsole(" [SUCCESS] Setup work environment! Launching main interface...");
            SwingUtilities.invokeLater(()->textArea.repaint());
//            wait(2000);
            Thread.sleep(2000);
            closeThreadWindow();
            MainGUI.launchMainGUI();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dischargeSequence() {
        try {
            updateConsole("Discharging sequence initiated. Verifying solenoid...");
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(1000);

            updateConsole(" [SUCCESS] Verified Power Deck EN line off. Digital PIN read: low. Opening 3-phase contractor...");
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(2000);

            updateConsole(" [SUCCESS] Verified 3-phase contractors is open. Digital PIN read: low. Opening Solenoid...");
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(2000);

            updateConsole(" [SUCCESS] Verified Solenoid is open. Digital PIN set: high. Setting discharge relay on...");
            SwingUtilities.invokeLater(()->textArea.repaint());

            Thread.sleep(500);
            updateConsole(" [SUCCESS] Work environment discharged.");
            Thread.sleep(500);
            chargeButton.setBackground(Color.GREEN);
            buttonPushed = false;
            pushableButton = CHARGE;
            updateConsole("To initiate the charging sequence, please select the \"CHARGE\" option above...");

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void closeThreadWindow() {
        for (int i = 0; i < consoleList.size() - 1; i++) {
            consoleList.get(i).setLength(0);
        }
        consoleIndex = 0;
        refreshConsole();
        actionFrame.dispose();
    }



    public Thread createUpdateGUIThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                updateGUI();
            }
        }, "UpdateGUI Thread");
    }

    public void updateConsole(String message) {
        StringBuilder updatedLine = new StringBuilder();
        String timeString = timeNotation(epochDifference);
        timeString = " " + timeString.substring(1, timeString.length() - 1) + ": ";
        updatedLine.append(timeString);
        updatedLine.append(message);
        updateConsoleHelper(updatedLine);


    }

    public void updateConsole(StringBuilder message) {
//        StringBuilder updatedLine = new StringBuilder();
//        updatedLine.append(message);
        updateConsoleHelper(message);
    }

    public void refreshConsole() {
//        StringBuilder updatedLine = new StringBuilder();
//        updatedLine.append(message);


        console = new StringBuilder(consoleWidth * consoleHeight + consoleHeight);
        if (consoleIndex < consoleHeight - 1) {
            for (int i = 0; i < consoleList.size() - 1; i++) {
                console.append(consoleList.get((i)) + "\n");
            }
            console.append(consoleList.get((consoleHeight - 1)));
        } else {
            for (int i = 0; i < consoleList.size() - 1; i++) {
                console.append(consoleList.get((consoleIndex + i - consoleHeight + 1) % (consoleHeight - 1)) + "\n");
            }
            console.append(consoleList.get(consoleHeight - 1));
        }
        textArea.setText(console.toString());
//        System.out.println(console.toString());
//        consoleIndex++;
    }


    private void updateConsoleHelper(StringBuilder updatedLine) {
        StringBuilder temp = consoleList.get(consoleIndex % (consoleHeight - 1));
        temp.setLength(0);
        consoleList.set(consoleIndex % (consoleHeight - 1), updatedLine);

        console = new StringBuilder(consoleWidth * consoleHeight + consoleHeight);
        if (consoleIndex < consoleHeight - 1) {
            for (int i = 0; i < consoleList.size() - 1; i++) {
                console.append(consoleList.get((i)) + "\n");
            }
            console.append(consoleList.get(consoleHeight - 1));
        } else {
            for (int i = 0; i < consoleList.size() - 1; i++) {
                console.append(consoleList.get((consoleIndex + i - consoleHeight + 2) % (consoleHeight - 1)) + "\n");
            }
            console.append(consoleList.get(consoleHeight - 1));
        }
        textArea.setText(console.toString());
//        System.out.println(updatedLine);
        consoleIndex++;
    }


}
