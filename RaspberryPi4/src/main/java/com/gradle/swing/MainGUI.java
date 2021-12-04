package com.gradle.swing;

import com.gradle.backend.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import static java.lang.Math.abs;



public class MainGUI extends AppGUI {

    public final int SPEED = 50; // FPS = 20

    private static Timer timer;
    private static long oldTime;
    private static long newTime;
    protected static long difference;
    public static long epochClock;
    public static long epochDifference;
    public static JTabbedPane tabbedPane;
    public static int currentTabPane = 0;
    public static int tabbedPaneCount = 0;
    public static StringBuilder console;
    public static ArrayList<StringBuilder> consoleList;
    public static final int consoleWidth = 190;
    public static final int consoleHeight = 6;
    public static final int graphWidth = 500;
    public static final int graphHeight = 285;
    public static int consoleIndex = 0;
    JTextArea textArea;
    JTextPane textPane;
    public static MainGUI mainGUI;

    public final int TEMPERATURE = 0;
    public final int CURRENT = 1;
    public final int VOLTAGE = 2;

    public JGradientButton startButton;
    public JGradientButton stopButton;
    public JGradientButton start_stopButton;

    public MainGUI() {
        oldTime = -1;
    }

    public static void main(String[] args) {
        launchMainGUI();
    }

    public void buildGUI() {
        startMasterClock();
        mainFrame = new JFrame("EMMA Application");
        Container mainPanel = mainFrame.getContentPane();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        mainPanel.add(getMainBorderPanel(getMainPanel(), "EMMA Application " + timeNotation(epochDifference),
                new Insets(8, 3, 3, 0)));
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

    public static ArrayList<JLabel> criticalList = new ArrayList<>();

    public void parseCritical() {
        for (JLabel label : criticalList) {
            Color color = label.getForeground();
            if (color == Color.RED) {
                label.setForeground(Color.WHITE);
            } else if (color == Color.WHITE) {
                label.setForeground(Color.RED);
            } else if (color == Color.BLACK) {
                label.setForeground(Color.RED);
            }
        }
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

    void startMasterClock() {
        epochClock = System.currentTimeMillis();
    }

    public static String timeNotation(long difference) {
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

    protected JTabbedPane getTabbedPane() {
        tabbedPane = new JTabbedPane();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 0, 0);
        tabbedPane.addTab("Overview", getReadingPanel());
        tabbedPaneCount++;
        JPanel panel1 = new JPanel();

        panel1.add(getGraphPanel(TEMPERATURE), constraints);
        tabbedPane.addTab("Temperature", panel1);
        tabbedPaneCount++;
        JPanel panel2 = new JPanel();
        panel2.add(getGraphPanel(CURRENT), constraints);
        tabbedPane.addTab("Current", panel2);
        tabbedPaneCount++;
        JPanel panel3 = new JPanel();
        panel3.add(getGraphPanel(VOLTAGE), constraints);
        tabbedPane.addTab("Voltage", panel3);
        tabbedPaneCount++;
        return tabbedPane;
    }

    protected JPanel getOuterControllerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        setConstraints(constraints, 0, 0, GridBagConstraints.SOUTH); //
        JLabel copyright = new JLabel("Georgia Tech EMMA ©");
        JPanel localPanel = new JPanel();
        localPanel.add(copyright);
//        panel.add(getBorderPanel(localPanel, "License:"), constraints);
        setConstraints(constraints, 0, 0, GridBagConstraints.SOUTH); //
        constraints.gridheight = 5;
        panel.add(getBorderPanel(getControllerPanel(), "Controller:", new Insets(0, 1, 0, 1)), constraints);
        return panel;
    }

    public static ArrayList<JLabel> temperatureList = new ArrayList<>();
    public static ArrayList<JLabel> currentList = new ArrayList<>();
    public static ArrayList<JLabel> voltageList = new ArrayList<>();

    protected JPanel getReadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
//        setConstraints(constraints, 0, 0, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getTemperatureGraph(), "Temperature Graph:"), constraints);
//        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getCurrentGraph(),"Current Graph:"), constraints);
//        setConstraints(constraints, 0, 2, GridBagConstraints.CENTER);
//        panel.add(getBorderPanel(getVoltageGraph(),"Voltage Graph:"), constraints);
//        constraints.gridheight = 2;
        setConstraints(constraints, 0, 0, GridBagConstraints.LAST_LINE_END);
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(getBorderPanel(getTemperaturePanel(), "Temperature:"), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.FIRST_LINE_START);
        constraints.insets = new Insets(7, 6, 2, 0);
        panel.add(getBorderPanel(getTemperaturePrecaution()), constraints);
        setConstraints(constraints, 0, 1, GridBagConstraints.CENTER);
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(getBorderPanel(getCurrentPanel(), "Current:"), constraints);
        setConstraints(constraints, 1, 1, GridBagConstraints.FIRST_LINE_START);
        constraints.insets = new Insets(7, 6, 2, 0);
        panel.add(getBorderPanel(getCurrentPrecaution()), constraints);
        setConstraints(constraints, 0, 2, GridBagConstraints.CENTER);
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(getBorderPanel(getVoltagePanel(), "Voltage:"), constraints);
        setConstraints(constraints, 1, 2, GridBagConstraints.FIRST_LINE_START);
        constraints.insets = new Insets(7, 6, 2, 0);
        panel.add(getBorderPanel(getVoltagePrecaution()), constraints);

//        panel.add(getBorderPanel(getControllerPanel(), "Controller:"), constraints);
        return panel;
    }

    protected JPanel getMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        setConstraints(constraints, 0, 0, GridBagConstraints.SOUTH);
        constraints.insets = new Insets(0, 1, 0, 0);
        panel.add(getTabbedPane(), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.SOUTH);
        constraints.insets = new Insets(13, 0, 0, 0);
        panel.add(getOuterControllerPanel(), constraints);
        setConstraints(constraints, 0, 0, GridBagConstraints.PAGE_END);
        constraints.insets = new Insets(0,0,0,0);
        setConstraints(constraints, 0, 1, GridBagConstraints.PAGE_END);
        constraints.gridwidth = 2;
        panel.add(getConsolePanel(), constraints);
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

    public void updateConsole(String message) {
        StringBuilder updatedLine = new StringBuilder();
        String timeString = timeNotation(epochDifference);
        timeString = timeString.substring(1, timeString.length() - 1) + ": ";
        updatedLine.append(timeString);
        updatedLine.append(message);
        updateConsoleHelper(updatedLine);
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
                console.append(consoleList.get((consoleIndex + i - consoleHeight + 1) % (consoleHeight - 1)) + "\n");
            }
            console.append(consoleList.get(consoleHeight - 1));
        }
        textArea.setText(console.toString());
        System.out.println(updatedLine);
        consoleIndex++;
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
        System.out.println(console.toString());
//        consoleIndex++;
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

    private JPanel getGraphPanel(int graphType) {
        JPanel panel = new JPanel();
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.setNotify(true);
        ArrayList<XYSeries> list = null;
        switch (graphType) {
            case (TEMPERATURE): {
                list = Temperature.temperatureList;
                break;
            }
            case (VOLTAGE): {
                list = Voltage.voltageList;
                break;
            }
            case (CURRENT): {
                list = Current.currentList;
                break;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            collection.addSeries(list.get(i));
        }

        JFreeChart lineChart = ChartFactory.createXYLineChart("", "", "",
                collection, PlotOrientation.VERTICAL, true, false, false);
        final XYPlot xyPlot = lineChart.getXYPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        shapeRenderer(xyPlot, list.size());

        final NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);
        lineChart.setBorderVisible(false);
        ChartPanel chartPanel = new ChartPanel(lineChart);

        chartPanel.setBackground(Color.lightGray);

        chartPanel.setPreferredSize(new Dimension(graphWidth, graphHeight));
        chartPanel.getInsets().set(0, 0, 0, 0);
        panel.add(chartPanel);
        panel.validate();
        return panel;
    }

//    private JPanel initializeTemperatureGraph() {
//        temperaturePanel = new JPanel();
//        XYSeriesCollection collection = new XYSeriesCollection();
//        collection.setNotify(true);
//        for (int i = 0; i < Temperature.temperatureList.size(); i++) {
//            collection.addSeries(Temperature.temperatureList.get(i));
//        }
//        JFreeChart lineChart = ChartFactory.createXYLineChart("",
//                "", "", collection,
//                PlotOrientation.VERTICAL,
//                true, false, false);
//        lineChart.setBackgroundPaint(Color.WHITE);
//        final XYPlot xyPlot = lineChart.getXYPlot();
//        xyPlot.setBackgroundPaint(Color.WHITE);
//        xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
//        xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
//        shapeRenderer(xyPlot, Temperature.temperatureList.size());
//
//        final NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
//        rangeAxis.setAutoRangeIncludesZero(false);
//        lineChart.setBorderVisible(false);
//        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setPreferredSize(new Dimension(graphWidth, graphHeight));
//        temperaturePanel.add(chartPanel);
//        temperaturePanel.validate();
//        return temperaturePanel;
//    }

    public void shapeRenderer(XYPlot xyPlot, int size) {

        XYLineAndShapeRenderer lasr = (XYLineAndShapeRenderer) xyPlot.getRenderer();
        for (int i = 0; i < size; i++) {
            XYLineAndShapeRenderer xylasr = (XYLineAndShapeRenderer) xyPlot.getRenderer();
            Shape shape = null;
            Color color = null;

            switch(i) {

                case (0): {
                    shape = ShapeUtils.createDiagonalCross(0.6f, 0.6f);
                    color = Color.RED;
                    break;
                }

                case (1): {
                    shape = ShapeUtils.createDiagonalCross(0.8f, 0.8f);
                    color = Color.GREEN;
                    break;
                }

                case (2): {
                    shape = ShapeUtils.createDiagonalCross(1.0f, 1.0f);
                    color = Color.BLUE;
                    break;
                }

                case (3): {
                    shape = ShapeUtils.createDiagonalCross(1.2f, 1.2f);
                    color = Color.MAGENTA;
                    break;
                }

                default: {
                    shape = ShapeUtils.createDiagonalCross(1.4f, 1.4f);
                    color = Color.ORANGE;
                    break;
                }
//            switch (1) {
//                case (0): {
//                    shape = ShapeUtils.createUpTriangle(4.0f);
//                    color = Color.RED;
//                    break;
//                }
//
//                case (1): {
//                    shape = ShapeUtils.createDiagonalCross(2.0f, 2.0f);
//                    color = Color.BLUE;
//                    break;
//                }
//
//                case (2): {
//                    shape = ShapeUtils.createDownTriangle(4.0f);
//                    color = Color.GREEN;
//                    break;
//                }
//
//                case (3): {
//                    shape = ShapeUtils.createDiamond(6.0f);
//                    color = Color.ORANGE;
//                    break;
//                }
//
//                default: {
//                    shape = ShapeUtils.createDiagonalCross(2.0f, 2.0f);
//                    color = Color.CYAN;
//                    break;
//                }
//
            }
            xylasr.setSeriesShape(i, shape);
            xylasr.setSeriesPaint(i, color);
            xylasr.setSeriesStroke(i, new BasicStroke(0.6f + 0.2f * i));
            xylasr.setSeriesShapesVisible(i, true);
        }
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
                "", "", collection,
                PlotOrientation.VERTICAL,
                true, false, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.setBorderVisible(false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setPreferredSize(new Dimension(graphWidth, graphHeight));
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
        chartPanel.setPreferredSize(new Dimension(graphWidth, graphHeight));
        panel.add(chartPanel);
        panel.validate();
        return panel;
    }

    final public static int BUFFER = 8;

    private JPanel getVoltagePrecaution() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.set(8, BUFFER, 0, BUFFER);
        for (int i = 0; i < voltageList.size(); i++) {
            if (i == voltageList.size() - 1) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            } else if (i > 0) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            }
            setConstraints(constraints, 0, i, GridBagConstraints.CENTER);
            panel.add(voltageList.get(i), constraints);
        }

        return panel;
    }

    private JPanel getCurrentPrecaution() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.set(8, BUFFER, 0, BUFFER);
        for (int i = 0; i < currentList.size(); i++) {

            if (i == currentList.size() - 1) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            } else if (i > 0) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            }
            setConstraints(constraints, 0, i, GridBagConstraints.CENTER);
            panel.add(currentList.get(i), constraints);

        }

        return panel;
    }

    private JPanel getTemperaturePrecaution() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets.set(8, BUFFER, 0, BUFFER);
        for (int i = 0; i < temperatureList.size(); i++) {
            if (i == temperatureList.size() - 1) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            } else if (i > 0) {
                constraints.insets.set(0, BUFFER, 0, BUFFER);
            }
            setConstraints(constraints, 0, i, GridBagConstraints.CENTER);
            panel.add(temperatureList.get(i), constraints);
        }

        return panel;
    }

    private JPanel getTemperaturePanel() {
        Temperature temperature;
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 10, 0, 10);
        setConstraints(constraints, 0, 0, GridBagConstraints.LAST_LINE_END);
        temperature = new Temperature(24.0, 28.0);
        temperatureList.add(temperature.getPrecautionLabel());
        panel.add(temperature.getTemperatureLabel(), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(temperature.getPrecautionLabel(), constraints);


        setConstraints(constraints, 0, 1, GridBagConstraints.LAST_LINE_END);
        temperature = new Temperature(24.0, 28.0);
        temperatureList.add(temperature.getPrecautionLabel());
        panel.add(temperature.getTemperatureLabel(), constraints);
        setConstraints(constraints, 1, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(temperature.getPrecautionLabel(), constraints);

        setConstraints(constraints, 0, 2, GridBagConstraints.LAST_LINE_END);
        temperature = new Temperature(24.0, 28.0);
        temperatureList.add(temperature.getPrecautionLabel());
        panel.add(temperature.getTemperatureLabel(), constraints);
        setConstraints(constraints, 1, 2, GridBagConstraints.FIRST_LINE_START);
        panel.add(temperature.getPrecautionLabel(), constraints);

        setConstraints(constraints, 0, 3, GridBagConstraints.LAST_LINE_END);
        temperature = new Temperature(24.0, 28.0);
        temperatureList.add(temperature.getPrecautionLabel());
        panel.add(temperature.getTemperatureLabel(), constraints);
        setConstraints(constraints, 1, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(temperature.getPrecautionLabel(), constraints);

        return panel;
    }

    private static final class JGradientButton extends JButton{
        private JGradientButton(String text){
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g){
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

    private JPanel getControllerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 2, 0);
        constraints.fill = GridBagConstraints.BOTH;
        start_stopButton = new JGradientButton("START");
        start_stopButton.setBackground(Color.GREEN);
        start_stopButton.setFont(new Font("Arial", Font.PLAIN, 40));
        start_stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                switch (start_stopButton.getText()) {
                    case ("START") : {
                        start_stopButton.setText("STOP");
                        start_stopButton.setBackground(Color.RED);
                        start_stopButton.repaint();
                        break;
                    }

                    case ("STOP") : {
                        start_stopButton.setText("START");
                        start_stopButton.setBackground(Color.GREEN);
                        start_stopButton.repaint();
                        break;
                    }
                }
            }
        });


//        JButton stopButton = new JButton("Stop");
        setConstraints(constraints, 0, 0, GridBagConstraints.FIRST_LINE_START);
        start_stopButton.setPreferredSize(new Dimension(
                (int) start_stopButton.getPreferredSize().getWidth(),
                (int) start_stopButton.getPreferredSize().getHeight() * 2
        ));

        constraints.gridheight = 2;
        panel.add(start_stopButton, constraints);

        constraints.insets = new Insets(2, 0, 2, 0);
//        setConstraints(constraints, 0, 1, GridBagConstraints.FIRST_LINE_START);
//        panel.add(stopButton, constraints);

//        JButton resetButton = new JButton("RESET");
        JGradientButton resetButton = new JGradientButton("RESET");
        resetButton.setBackground(Color.ORANGE);
        resetButton.setFont(new Font("Arial", Font.PLAIN, 40));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                UART.uart.nullifyEverything();
                if (UART.uart.comPort != null) {
                    UART.resetCommand = true;
                    UART.uart.restartESP32();
                }
            }
        });

        setConstraints(constraints, 0, 2, GridBagConstraints.FIRST_LINE_START);
        panel.add(getDirectionPanel(), constraints);

        setConstraints(constraints, 0, 4, GridBagConstraints.CENTER);
        constraints.insets = new Insets(2, 0, 0, 0);
        constraints.gridheight = 1;
        panel.add(resetButton, constraints);

        return panel;
    }

    private void directionHandler(String direction) {
        currentTabPane = tabbedPane.getSelectedIndex();
        if (direction.equals("<<")) {
            currentTabPane--;
        }

        if (direction.equals(">>")) {
            currentTabPane++;
        }

        if (currentTabPane < 0) {
            currentTabPane = 0;
        }

        if (currentTabPane >= tabbedPaneCount) {
            currentTabPane = tabbedPaneCount - 1;
        }

        tabbedPane.setSelectedIndex(currentTabPane);
    }

    private JPanel getDirectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 0, 2);

//        JButton leftButton = new JButton(" <<");
        JGradientButton leftButton = new JGradientButton(" <<");
        leftButton.setBackground(Color.BLUE);
        leftButton.setFont(new Font("Arial", Font.PLAIN, 40));

        leftButton.setPreferredSize(new Dimension(
                (int) leftButton.getPreferredSize().getWidth(),
                (int) leftButton.getPreferredSize().getHeight() * 2
        ));
        leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                directionHandler("<<");
            }
        });

        setConstraints(constraints, 0, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(leftButton, constraints);

        constraints.insets = new Insets(0, 2, 0, 0);
//        JButton rightButton = new JButton(">> ");
        JGradientButton rightButton = new JGradientButton(">> ");
        rightButton.setBackground(Color.BLUE);
        rightButton.setFont(new Font("Arial", Font.PLAIN, 40));
        rightButton.setPreferredSize(new Dimension(
                (int) rightButton.getPreferredSize().getWidth(),
                (int) rightButton.getPreferredSize().getHeight() * 2
        ));
        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                directionHandler(">>");
            }
        });

        setConstraints(constraints, 1, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(rightButton, constraints);
        return panel;
    }

    private JPanel getCurrentPanel() {
        Current current;
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 10, 0, 10);
        setConstraints(constraints, 0, 0, GridBagConstraints.LAST_LINE_END);
        current = new Current(30.0, 32.0);
        currentList.add(current.getPrecautionLabel());
        panel.add(current.getCurrentLabel(), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(current.getPrecautionLabel(), constraints);

        setConstraints(constraints, 0, 1, GridBagConstraints.LAST_LINE_END);
        current = new Current(30.0, 32.0);
        currentList.add(current.getPrecautionLabel());
        panel.add(current.getCurrentLabel(), constraints);
        setConstraints(constraints, 1, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(current.getPrecautionLabel(), constraints);

        setConstraints(constraints, 0, 2, GridBagConstraints.LAST_LINE_END);
        current = new Current(30.0, 32.0);
        currentList.add(current.getPrecautionLabel());
        panel.add(current.getCurrentLabel(), constraints);
        setConstraints(constraints, 1, 2, GridBagConstraints.FIRST_LINE_START);
        panel.add(current.getPrecautionLabel(), constraints);

        setConstraints(constraints, 0, 3, GridBagConstraints.LAST_LINE_END);
        current = new Current(30.0, 32.0);
        currentList.add(current.getPrecautionLabel());
        panel.add(current.getCurrentLabel(), constraints);
        setConstraints(constraints, 1, 3, GridBagConstraints.FIRST_LINE_START);
        panel.add(current.getPrecautionLabel(), constraints);

        return panel;
    }

    private JPanel getVoltagePanel() {
        Voltage voltage;
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 10, 0, 10);

        setConstraints(constraints, 0, 0, GridBagConstraints.LAST_LINE_END);
        voltage = new Voltage(30.0, 40.0);
        voltageList.add(voltage.getPrecautionLabel());
        panel.add(voltage.getVoltageLabel(), constraints);
        setConstraints(constraints, 1, 0, GridBagConstraints.FIRST_LINE_START);
        panel.add(voltage.getPrecautionLabel(), constraints);


        setConstraints(constraints, 0, 1, GridBagConstraints.LAST_LINE_END);
        voltage = new Voltage(30.0, 40.0);
        voltageList.add(voltage.getPrecautionLabel());
        panel.add(voltage.getVoltageLabel(), constraints);
        setConstraints(constraints, 1, 1, GridBagConstraints.FIRST_LINE_START);
        panel.add(voltage.getPrecautionLabel(), constraints);

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


    private ActionListener sampler() { // deprecated
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