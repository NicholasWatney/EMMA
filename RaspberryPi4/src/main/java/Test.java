import com.gradle.backend.Temperature;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Queue;

import static java.lang.Math.random;

public class Test {

    public static void main(String[] args) {
        JFrame mainFrame = new JFrame("EMMA Application");
        Container mainPanel = mainFrame.getContentPane();

        JPanel panel = new JPanel();
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.setNotify(true);
        for (int i = 0; i < 2; i++) {
            XYSeries series = new XYSeries("T" + i + 1);
            collection.addSeries(series);
        }

        JFreeChart lineChart = ChartFactory.createXYLineChart("",
                "", "", collection,
                PlotOrientation.VERTICAL,
                true, false, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        lineChart.setBorderVisible(false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
//        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setPreferredSize(new Dimension(800, 800));
        panel.add(chartPanel);
        panel.validate();


        mainPanel.add(panel);
        mainPanel.setLayout(new GridBagLayout());

        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 800);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
        mainFrame.setUndecorated(true);
        mainFrame.setVisible(true);

        int x = 1;

        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            XYSeries series = collection.getSeries(0);
            series.add(x++, random());
            XYSeries series1 = collection.getSeries(1);
            series1.add(x++, random());

            if (series.getItems().size() > 10) {
                series.remove(0);
                series1.remove(0);
            }
        }

    }

}