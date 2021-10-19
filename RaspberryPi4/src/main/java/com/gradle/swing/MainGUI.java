package com.gradle.swing;

import com.gradle.backend.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainGUI extends AppGUI {

    public final int SPEED = 50; // FPS = 20

    private static Timer timer;
    private static long oldTime;
    private static long newTime;
    protected static long difference;
    public static long epochClock;
    public static long epochDifference;

    public MainGUI() {
        oldTime = -1;
    }

    public void buildGUI() {
        startMasterClock();
        mainFrame = new JFrame("EMMA Application");
        Container mainPanel = mainFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
        mainPanel.add(getMainBorderPanel(getMainPanel(), "EMMA Application " + timeNotation(epochDifference),
                new Insets(8, 21, 21, 21), TitledBorder.TOP));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(true);
        mainFrame.setVisible(true);
        UART uart = new UART();
        Thread connectToUARTThread = uart.createConnectToUARTThread();
        Thread updateGUIThread = createUpdateGUIThread();
        updateGUIThread.start();
        connectToUARTThread.start();
    }

    public Thread createUpdateGUIThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                updateGUI();
            }
        }, "UpdateGUI Thread");
    }

    private void updateGUI() {
        while (true) {
            updateMasterClock();
            difference();
            updateClock();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    void startMasterClock() {
        epochClock = System.currentTimeMillis();
    }

    String timeNotation(long difference) {
        long hours = difference / 3600000;
        difference = difference % 3600000;
        long minutes = difference / 60000;
        difference = difference % 60000;
        long seconds = difference / 1000;
        return String.format("(%02d:%02d:%02d)", hours, minutes, seconds);
    }

    void updateMasterClock() {
        epochDifference = System.currentTimeMillis() - epochClock;
    }

    void updateClock() {
        mainBorder.setTitle("EMMA Application " + timeNotation(epochDifference));
        mainFrame.getContentPane().getComponent(0).repaint();
    }

    protected JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getTemperatureGraph(), "Temperature Graph:"), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getCurrentGraph(),"Current Graph:"), constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getVoltageGraph(),"Voltage Graph:"), constraints);
//        constraints.gridheight = 2;
        setConstraints(constraints, 1, 0, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getTemperaturePanel(), "Temperature Panel:"), constraints);
        setConstraints(constraints, 1, 1, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getCurrentPanel(), "Current Panel:"), constraints);
        setConstraints(constraints, 1, 2, GridBagConstraints.CENTER);
        panel.add(getBorderPanel(getVoltagePanel(), "Voltage Panel:"), constraints);
        return panel;
    }


    private JPanel getTemperatureGraph() {
        JPanel panel = new JPanel();
        XYSeries series = new XYSeries("2021");
        series.add(1, 1);
        series.add(2, 4);
        series.add(3, 9);
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);

        JFreeChart lineChart = ChartFactory.createXYLineChart("",
                "Years", "Number", collection,
                PlotOrientation.VERTICAL,
                true, false, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.setBorderVisible(false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setPreferredSize(new Dimension(100, 100));
        panel.add(chartPanel);
        panel.validate();
        return panel;
    }

    private JPanel getCurrentGraph() {
        JPanel panel = new JPanel();
        XYSeries series = new XYSeries("2021");
        series.add(1, 1);
        series.add(2, 4);
        series.add(3, 9);
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);

        JFreeChart lineChart = ChartFactory.createXYLineChart("",
                "Years", "Number", collection,
                PlotOrientation.VERTICAL,
                true, false, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.setBorderVisible(false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setPreferredSize(new Dimension(100, 100));
        panel.add(chartPanel);
        panel.validate();
        return panel;
    }

    private JPanel getVoltageGraph() {
        JPanel panel = new JPanel();
        XYSeries series = new XYSeries("2021");
        series.add(1, 1);
        series.add(2, 4);
        series.add(3, 9);
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);

        JFreeChart lineChart = ChartFactory.createXYLineChart("",
                "Years", "Number", collection,
                PlotOrientation.VERTICAL,
                true, false, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.setBorderVisible(false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setPreferredSize(new Dimension(100, 100));
        panel.add(chartPanel);
        panel.validate();
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
        setConstraints(constraints, 0, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Temperature().getTemperatureLabel(), constraints);
        return panel;
    }

    private JPanel getCurrentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 40, 0, 40);
        setConstraints(constraints, 0, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Current().getCurrentLabel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Current().getCurrentLabel(), constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Current().getCurrentLabel(), constraints);
        setConstraints(constraints, 0, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Current().getCurrentLabel(), constraints);
        setConstraints(constraints, 0, 4, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Current().getCurrentLabel(), constraints);
        return panel;
    }

    private JPanel getVoltagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 40, 0, 40);
        setConstraints(constraints, 0, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Voltage().getVoltageLabel(), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(new Voltage().getVoltageLabel(), constraints);
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
                updateMasterClock();
                difference();
                updateClock();
//                Concurrent.updateConcurrent();
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