package com.gradle.swing;

import com.fazecast.jSerialComm.SerialPort;
import com.gradle.backend.UART;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.StrokeBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionScreen extends AppGUI {

    public StringBuilder disabledButton;
    public StringBuilder buttonSequence;

    public static ActionScreen actionScreen;
    public static long epochDifference;
    public static long epochClock;

    public static int consoleIndex = 0;

    public final static int CHARGE = 0;
    public final static int DISCHARGE = 1;

    public JFrame getFrame() {
        return actionFrame;
    }

    public static int pushableButton = CHARGE;
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
        UART uart = new UART();
        Thread connectToUARTThread = uart.createConnectToUARTThread();
        connectToUARTThread.start();
        updateConsole("To initiate the charging sequence, please select the \"CHARGE\" option above...");
//        UART.uart.restartESP32();
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
                            disabledButton.append(" WARNING: Invalid user input. The system is completely charged. Please select the \"Discharge\" button.");
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

    public static AtomicInteger discharge_present = new AtomicInteger(-1);

    public static AtomicInteger relay_set_high = new AtomicInteger(-1);
    public static AtomicInteger contactor_set_low = new AtomicInteger(-1);
    public static AtomicInteger deck_set_low = new AtomicInteger(-1);

    public static AtomicInteger enable_set_low = new AtomicInteger(-1);

    public void chargeSequence() {
        try {
            updateConsole("Charging sequence initiated. Verifying relay...");
            SwingUtilities.invokeLater(()->textArea.repaint());

//            UART.uart.comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    UART.writeUART("CU;");
                };
            }, "t1");
            t1.start();
//            UART.uart.comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

//            while (true) {
//                if (relay_set_high.get() == 1) {
//                    relay_set_high.set(-1);
//                    break;
//                } else {
//                    UART.writeUART("CU;");
//                    Thread.sleep(500);
//                }
//            }

            updateConsole("[ SUCCESS ] Relay set high: true. Verifying contactor...");
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(1000);
            updateConsole("[ SUCCESS ] Contactor set low: true. Verifying deck...");
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(3000);
            updateConsole("[ SUCCESS ] Deck set low: true. Verifying capacitors..."); //set
            SwingUtilities.invokeLater(()->textArea.repaint());
            Thread.sleep(1000);
//            while (true) {
//                if (relay_set_high.get() == 1) {
//                    updateConsole("[ SUCCESS ] Relay set high: true. Verifying contactor...");
//                    SwingUtilities.invokeLater(() -> textArea.repaint());
//                    relay_set_high.set(-1);
//                    break;
//                 } else {
//                    UART.writeUART("CU;");
//                    UART.writeUART("RSH;");
//                    Thread.sleep(500);
//                }
//            }
//
//            while (true) {
//                if (contactor_set_low.get() == 1) {
//                    updateConsole("[ SUCCESS ] Contactor set low: true. Verifying deck...");
//                    SwingUtilities.invokeLater(() -> textArea.repaint());
//                    contactor_set_low.set(-1);
//                    break;
//                } else {
//                    UART.writeUART("CU;");
//                    Thread.sleep(500);
//                }
//            }
//
//
//            while (true) {
//                if (deck_set_low.get() == 1) {
//                    updateConsole("[ SUCCESS ] Deck set low: true. Verifying capacitors..."); //set
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    deck_set_low.set(-1);
//                    break;
//                } else {
//                    UART.writeUART("CSL;");
//                    Thread.sleep(500);
//                }
//            }

            Thread.sleep(500);
            updateConsole("[ SUCCESS ] Capacitors charged. Launching main interface...");
            Thread.sleep(2000);
            closeThreadWindow();
            MainGUI.launchMainGUI();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public void dischargeSequence() {
        try {

            updateConsole("Discharging sequence initiated. Verifying enable...");
            SwingUtilities.invokeLater(()->textArea.repaint());

            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    UART.writeUART("DC;");
                };
            }, "t1");
            t2.start();
//            while (true) {
//                if (enable_set_low.get() == 1) {
//                    enable_set_low.set(-1);
//                    break;
//                } else {
//                    UART.writeUART("DC;");
//                    Thread.sleep(500);
//                }
//            }

            updateConsole("[ SUCCESS ] Enable set low: true. Verifying contactor...");
            Thread.sleep(1000);
            SwingUtilities.invokeLater(()->textArea.repaint());
            updateConsole("[ SUCCESS ] contactor set high. Verifying deck...");
            Thread.sleep(3000);
            SwingUtilities.invokeLater(()->textArea.repaint());
            updateConsole("[ SUCCESS ] deck set high. Verifying relay...");
            Thread.sleep(1000);
            SwingUtilities.invokeLater(()->textArea.repaint());
            updateConsole("[ SUCCESS ] relay set low. Verifying relay...");
            Thread.sleep(3000);
            SwingUtilities.invokeLater(()->textArea.repaint());
            updateConsole("[ SUCCESS ] relay set high. Verifying relay...");
            Thread.sleep(1000);
            SwingUtilities.invokeLater(()->textArea.repaint());

//            updateConsole("Discharging sequence initiated. Verifying enable...");
//            SwingUtilities.invokeLater(()->textArea.repaint());
//
//            while (true) {
//                if (enable_set_low.get() == 1) {
//                    updateConsole("[ SUCCESS ] Enable set low: true. Verifying contactor...");
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    enable_set_low.set(-1);
//                    break;
//                } else {
//                    Thread.sleep(500);
//                    UART.writeUART("DC;");
//                    UART.writeUART("SD;");
//                }
//            }
//
//            while (true) {
//
//                if (contactor_set_low.get() == 0) {
//                    updateConsole("[ SUCCESS ] contactor set high. Verifying deck...");
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    contactor_set_low.set(-1);
//                    break;
//                } else {
//                    Thread.sleep(500);
//                }
//            }
//
//            while (true) {
//                if (deck_set_low.get() == 0) {
//                    updateConsole("[ SUCCESS ] deck set high. Verifying relay...");
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    deck_set_low.set(-1);
//                    break;
//                } else {
//                    Thread.sleep(500);
//                }
//            }
//
//            while (true) {
//                if (relay_set_high.get() == 0) {
//                    updateConsole("[ SUCCESS ] relay set low. Verifying relay...");
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    relay_set_high.set(-1);
//                    break;
//                } else {
//                    Thread.sleep(500);
//                }
//            }
//
//            while (true) {
//                if (relay_set_high.get() == 1) {
//                    updateConsole("[ SUCCESS ] relay set high. Verifying relay...");
//                    SwingUtilities.invokeLater(()->textArea.repaint());
//                    relay_set_high.set(-1);
//                    break;
//                } else {
//                    Thread.sleep(500);
//                }
//            }


            updateConsole("[ SUCCESS ] Work environment discharged.");
            Thread.sleep(2000);
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
//        AppGUI.MainApp.set(true);
        actionFrame.setVisible(false);
//        actionFrame.dispose();
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
