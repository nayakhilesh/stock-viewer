package stockviewer.controller;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.stock.StockData;
import stockviewer.stock.StockDataException;
import stockviewer.stock.StockDataSource;
import stockviewer.stock.StockInfo;
import stockviewer.ui.View;

public class StockViewerController implements Controller {

	private static final Logger LOG = LoggerFactory
			.getLogger(StockViewerController.class);

	private StockDataSource ds;
	private List<View> views;

	public StockViewerController(StockDataSource ds) {
		this.ds = ds;
		views = new LinkedList<View>();
	}

	@Override
	public void addView(View view) {
		views.add(view);
	}

	@Override
	public void removeView(View view) {
		views.remove(view);
	}

	@Override
	public void onCreateChart(Date from, Date to, String tickerSymbol1,
			String tickerSymbol2) throws StockDataException {

		LOG.info("Retrieving stock data");

		List<StockData> data1 = ds.getStockData(tickerSymbol1, from, to);
		List<StockData> data2 = ds.getStockData(tickerSymbol2, from, to);

		LOG.info("Updating registered Views");

		for (View view : views) {
			view.onReceivingNewStockInfo(from, to, new StockInfo(tickerSymbol1,
					data1), new StockInfo(tickerSymbol2, data2));
		}

		LOG.info("Done updating registered Views");
	}

}
