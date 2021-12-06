

package com.gradle.swing;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.StrokeBorder;
import javax.swing.border.TitledBorder;

final class JGradientButton extends JButton{
    public JGradientButton(String text){
        super(text);
        setContentAreaFilled(false);
    }

    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(new GradientPaint(
                new Point(0, 0),
                getBackground(),
                new Point(0, getHeight()/3),
                Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight()/3);
        g2.setPaint(new GradientPaint(
                new Point(0, getHeight()/3),
                Color.WHITE,
                new Point(0, getHeight()),
                getBackground()));
        g2.fillRect(0, getHeight()/3, getWidth(), getHeight());
        g2.dispose();

        super.paintComponent(g);
    }
}

public abstract class AppGUI {

    private final int DEBUG = 1;

    final static int CHARGE = 0;
    final static int DISCHARGE = 1;

    public static AtomicBoolean MainApp = new AtomicBoolean(false);

    protected JFrame abortFrame;
    protected JFrame mainFrame;
    protected JFrame actionFrame;
    protected static boolean actionScreenCreated = false;
    protected static boolean mainScreenCreated = false;


    public static Color borderColor = new Color(95, 123, 184);

    protected static TitledBorder mainBorder;
    protected static TitledBorder actionBorder;

    protected static void launchAbortScreen() {
        AbortGUI abortScreen = new AbortGUI();
        abortScreen.buildGUI();
    }

    protected static void launchActionScreen(String string) {
        if (actionScreenCreated == false) {
            ActionScreen.actionScreen = new ActionScreen();
            if (string.equals("CHARGE")) {
                ActionScreen.actionScreen.pushableButton = CHARGE;
            } else if (string.equals("DISCHARGE")) {
                ActionScreen.actionScreen.pushableButton = DISCHARGE;
            }
            ActionScreen.actionScreen.buildGUI();
            actionScreenCreated = true;
        } else {
            if (string.equals("CHARGE")) {
                ActionScreen.actionScreen.pushableButton = CHARGE;
            } else if (string.equals("DISCHARGE")) {
                ActionScreen.actionScreen.pushableButton = DISCHARGE;
                
            }
            ActionScreen.actionScreen.startMasterClock();
            ActionScreen.actionScreen.dischargeButton.setBackground(Color.GREEN);
            ActionScreen.actionScreen.refreshConsole();
            ActionScreen.actionScreen.getFrame().setVisible(true);
            ActionScreen.actionScreen.buttonPushed = false;
            ActionScreen.actionScreen.updateConsole("To initiate the discharging sequence, please select the \"DISCHARGE\" option above...");
        }
    }

    protected static void launchMainGUI() {
        if (!mainScreenCreated) {
            mainScreenCreated = true;
            MainGUI.mainGUI = new MainGUI();
            MainGUI.mainGUI.buildGUI();
        } else {
            MainGUI.mainGUI.mainFrame.setVisible(true);
        }
    }

    protected static JPanel getBorderPanel(JPanel inside, String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(inside);
        return panel;
    }

    protected static JPanel getBorderPanel(JPanel inside) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(""));
        panel.add(inside);
        return panel;
    }


    protected static JPanel getBorderPanel(JPanel inside, String title, Insets insets) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = insets;
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("Arial", Font.BOLD, 12));
        border.setTitleJustification(TitledBorder.LEFT);
        border.setBorder(new StrokeBorder(new BasicStroke(1.0f), borderColor));
        panel.setBorder(border);
        panel.add(inside, constraints);
        return panel;
    }

    protected static JPanel getMainBorderPanel(JPanel inside, String title, Insets insets) {
        JPanel panel = new JPanel(new GridBagLayout());
        mainBorder = BorderFactory.createTitledBorder(title);
        mainBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        mainBorder.setTitleJustification(TitledBorder.CENTER);
        mainBorder.setBorder(new StrokeBorder(new BasicStroke(1.0f)));
        panel.setBorder(mainBorder);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = insets;
        panel.add(inside, constraints);
        return panel;
    }

    protected static JPanel getActionBorderPanel(JPanel inside, String title, Insets insets) {
        JPanel panel = new JPanel(new GridBagLayout());
        actionBorder = BorderFactory.createTitledBorder(title);
        actionBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        actionBorder.setTitleJustification(TitledBorder.CENTER);
        actionBorder.setBorder(new StrokeBorder(new BasicStroke(1.0f)));
        panel.setBorder(actionBorder);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = insets;
        panel.add(inside, constraints);
        return panel;
    }

    protected static JPanel getBorderPanel(JPanel inside, String title, Insets insets,
                                       Border border, int titleJusitification, int titlePosition,
                                       Font titleFont, Color titleColor) {
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
