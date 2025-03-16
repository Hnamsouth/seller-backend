package com.vtp.vipo.seller.financialstatement.common.jasperreport;

import net.sf.jasperreports.charts.JRChart;
import net.sf.jasperreports.charts.JRChartCustomizer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RectangleLegendCustomizer implements JRChartCustomizer {

    @Override
    public void customize(JFreeChart chart, JRChart jasperChart) {
        if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();

            // Create rectangle shape for legend
            Shape rectangleShape = new Rectangle2D.Double(-4, -4, 16, 8);

            // Set the modified legend items
            plot.setLegendItemShape(rectangleShape);
        }
    }
}