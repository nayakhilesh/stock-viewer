package stockviewer.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.stock.StockDao;
import stockviewer.stock.StockData;
import stockviewer.stock.StockDataException;
import stockviewer.stock.StockDataSource;
import stockviewer.stock.StockInfo;
import stockviewer.ui.View;

public class StockViewerController implements Controller {

	private static final Logger LOG = LoggerFactory
			.getLogger(StockViewerController.class);

	private StockDataSource ds;
	private StockDao dao;
	private List<View> views;

	public StockViewerController(StockDataSource ds, StockDao dao) {
		this.ds = ds;
		this.dao = dao;
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

		List<StockData> data1 = null;
		List<StockData> data2 = null;

		try {

			data1 = ds.getStockData(tickerSymbol1, from, to);
			data2 = ds.getStockData(tickerSymbol2, from, to);

			if (dao != null) {
				dao.insertStockInfo(new StockInfo(tickerSymbol1, data1));
				dao.insertStockInfo(new StockInfo(tickerSymbol2, data2));
			}

		} catch (StockDataException sde) {

			if (dao == null)
				throw sde;

			LOG.info("Error getting stock data from:" + ds);
			try {
				LOG.info("Trying to get from DB");
				data1 = dao.getStockData(tickerSymbol1, from, to);
				data2 = dao.getStockData(tickerSymbol2, from, to);
			} catch (Exception e) {
				LOG.error("Error retrieving stock info from DB", e);
			}

			if (data1 == null || data2 == null || data1.isEmpty()
					|| data2.isEmpty())
				throw sde;

		} catch (SQLException e) {
			LOG.error("Error storing stock info in DB", e);
		}

		LOG.info("Updating registered Views");

		for (View view : views) {
			view.onReceivingNewStockInfo(from, to, new StockInfo(tickerSymbol1,
					data1), new StockInfo(tickerSymbol2, data2));
		}

	}

}
