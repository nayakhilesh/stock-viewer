import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class ChartUtility {

	private Calendar cal;
	private final DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
	private static final String SPACE_DOLLAR_SYMBOL = " ($)";

	public ChartUtility() {
		cal = Calendar.getInstance();
	}

	// TODO add cursors / tooltips
	// TODO make pretty

	@SuppressWarnings("deprecation")
	public JPanel createChart(Date from, Date to, StockInfo stock1,
			StockInfo stock2, StockPriceType priceType) {

		String chartTitle = df.format(from) + " to " + df.format(to) + " " + priceType;
		XYDataset dataset1 = createDataset(stock1, priceType);
		XYDataset dataset2 = createDataset(stock2, priceType);

		JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle,
				"Date", stock1.getTickerSymbol() + SPACE_DOLLAR_SYMBOL, dataset1, true, true, false);

		XYPlot plot = chart.getXYPlot();
		NumberAxis axis2 = new NumberAxis(stock2.getTickerSymbol() + SPACE_DOLLAR_SYMBOL);
		Font tickLabelFont = axis2.getTickLabelFont().deriveFont(11.0F);
		Font labelFont = axis2.getLabelFont().deriveFont(15.0F).deriveFont(Font.BOLD);
		axis2.setTickLabelFont(tickLabelFont);
		axis2.setLabelFont(labelFont);
		axis2.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, axis2);
		plot.setDataset(1, dataset2);
		plot.mapDatasetToRangeAxis(1, 1);
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setToolTipGenerator(StandardXYToolTipGenerator
				.getTimeSeriesInstance());
		if (renderer instanceof StandardXYItemRenderer) {
			StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
			// rr.setPlotShapes(true);
			rr.setPlotImages(true);
			rr.setShapesFilled(true);
		}

		StandardXYItemRenderer renderer2 = new StandardXYItemRenderer();
		renderer2.setSeriesPaint(0, Color.black);
		// renderer2.setPlotShapes(true);
		renderer2.setPlotImages(true);
		renderer.setToolTipGenerator(StandardXYToolTipGenerator
				.getTimeSeriesInstance());
		plot.setRenderer(1, renderer2);

		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(df);

		ChartPanel chartPanel = new ChartPanel(chart);

		return chartPanel;
	}

	private XYDataset createDataset(StockInfo stock, StockPriceType priceType) {

		@SuppressWarnings("deprecation")
		TimeSeries timeSeries = new TimeSeries(stock.getTickerSymbol(),
				Day.class);
		for (StockData sd : stock.getStockData()) {
			cal.setTime(sd.getDate());
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);
			timeSeries.add(new Day(day, month + 1, year), priceType.get(sd));
			// + 1 since calendar month starts at 0
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(timeSeries);

		return dataset;
	}

}
