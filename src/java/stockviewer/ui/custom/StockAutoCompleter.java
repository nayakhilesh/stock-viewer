package stockviewer.ui.custom;

import java.util.List;

import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.stock.StockDataSource;
import stockviewer.stock.StockTicker;

public class StockAutoCompleter extends AutoCompleter {

	private static final Logger LOG = LoggerFactory
			.getLogger(StockAutoCompleter.class);

	private StockDataSource[] dataSources;

	public StockAutoCompleter(JTextField field, StockDataSource... dataSources) {
		super(field);
		this.dataSources = dataSources;
	}

	@Override
	protected boolean updateListData() {
		String value = textField.getText();
		if (value.isEmpty())
			return false;

		long totalStart, totalEnd;
		List<StockTicker> tickers = null;

		totalStart = System.nanoTime();

		for (StockDataSource ds : dataSources) {
			if (ds != null) {
				try {
					long start = System.nanoTime();
					tickers = ds.searchTickers(value);
					long end = System.nanoTime();
					LOG.debug("Source:" + ds + " ticker search time="
							+ ((end - start) / 1000000.) + "ms");
					LOG.info("Tickers successfully found from source:" + ds);
					break;
				} catch (Exception e) {
					LOG.error("Error searching for tickers in source:" + ds
							+ " with query:" + value);
				}
			}
		}

		totalEnd = System.nanoTime();
		LOG.debug("Total Ticker search time="
				+ ((totalEnd - totalStart) / 1000000.) + "ms");

		if (tickers == null || tickers.isEmpty())
			return false;

		StockTicker[] arr = new StockTicker[tickers.size()];

		int i = 0;
		for (StockTicker ticker : tickers) {
			arr[i] = ticker;
			i++;
		}

		list.setListData(arr);
		return true;
	}

	@Override
	protected void selectedListItem(Object selected) {
		if (selected != null) {
			StockTicker selectedTicker = (StockTicker) selected;
			textField.setText(selectedTicker.getSymbol());
		}
	}
}