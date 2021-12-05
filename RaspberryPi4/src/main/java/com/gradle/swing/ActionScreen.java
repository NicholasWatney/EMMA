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

    public static ActionScreen actionScreen;
    public static long epochDifference;
    public static long epochClock;

    public static int consoleIndex = 0;

    final static int CHARGE = 0;
    final static int DISCHARGE = 1;

    public int pushableButton = CHARGE;

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
//        chargeButton.setPreferredSize(new Dimension(
//                (int) (chargeButton.getPreferredSize().getWidth() * 0.9),
//                (int) (chargeButton.getPreferredSize().getHeight())
//        ));
        chargeButton.setBackground(Color.GREEN);
        chargeButton.setFont(new Font("Arial", Font.PLAIN, 60));
        chargeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (pushableButton == CHARGE) {
                    updateConsole("CHARGE");
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
//        dischargeButton.setPreferredSize(new Dimension(
//                (int) (dischargeButton.getPreferredSize().getWidth() * 0.9),
//                (int) (dischargeButton.getPreferredSize().getHeight())
//        ));
        dischargeButton.setBackground(Color.GRAY);
        dischargeButton.setFont(new Font("Arial", Font.PLAIN, 60));
        dischargeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (pushableButton == DISCHARGE) {
                    updateConsole("DISCHARGE");
                }
//                AppGUI.launchMainGUI();
//                closeThreadWindow();
            }
        });
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        constraints.insets = new Insets(0, 2, 4, 2);
        panel.add(dischargeButton, constraints);
        return panel;
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
