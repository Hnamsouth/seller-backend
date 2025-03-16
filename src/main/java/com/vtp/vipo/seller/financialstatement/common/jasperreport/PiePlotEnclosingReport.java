package com.vtp.vipo.seller.financialstatement.common.jasperreport;

import net.sf.jasperreports.charts.JRChart;
import net.sf.jasperreports.charts.JRChartCustomizer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;

public class PiePlotEnclosingReport implements JRChartCustomizer {
    public void customize(JFreeChart chart, JRChart jasperChart) {
        LegendTitle legend = chart.getLegend();
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setShadowXOffset(0.0D);
        plot.setShadowYOffset(0.0D);
        legend.setFrame(BlockBorder.NONE);
    }
}
