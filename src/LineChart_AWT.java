import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.List;

public class LineChart_AWT extends ApplicationFrame {

    public LineChart_AWT(String applicationTitle, String chartTitle, List<long[]> data) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createLineChart(
                chartTitle,
                "Number of threads", "Time to complete (ms)",
                createDataset(data),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(List<long[]> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (long[] datum : data) {

            // time to complete operations , "Time to complete" , Number of threads used
            dataset.addValue(datum[0], "EBS", Long.toString(datum[1]));

        }
        return dataset;
    }
}