

package com.gradle.swing;

import java.awt.*;
import javax.swing.*;

public class AppGUI {

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
