

package com.gradle.swing;

import java.awt.*;
import javax.swing.*;

public abstract class AppGUI {

    private final int DEBUG = 1;

    protected JFrame abortFrame;
    protected JFrame mainFrame;

    protected static void launchAbortScreen() {
        AbortGUI abortScreen = new AbortGUI();
        abortScreen.buildGUI();
    }

    protected static void launchMainGUI() {
        MainGUI mainGUI = new MainGUI();
        mainGUI.buildGUI();
    }

    protected static JPanel getBorderPanel(JPanel inside, String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(inside);
        return panel;
    }

    protected static JPanel getBorderPanel(JPanel inside, String title, Insets insets) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = insets;
        panel.add(inside, constraints);
        return panel;
    }

    protected static void setConstraints(GridBagConstraints constraints,
                                        int gridx,
                                        int gridy,
                                        int anchor) {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.anchor = anchor;
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                launchAbortScreen();
            }
        });
    }
}
