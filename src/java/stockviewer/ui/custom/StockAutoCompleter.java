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

	private StockDataSource ds;

	public StockAutoCompleter(JTextField field, StockDataSource ds) {
		super(field);
		this.ds = ds;
	}

	@Override
	protected boolean updateListData() {
		String value = textField.getText();
		if (value.isEmpty())
			return false;

		long start, end;
		List<StockTicker> tickers;
		try {
			start = System.nanoTime();
			tickers = ds.searchTickers(value);
			end = System.nanoTime();
			LOG.debug("Ticker search time=" + ((end - start) / 1000000.) + "ms");
		} catch (Exception e) {
			LOG.error("Exception searching for tickers with query:" + value, e);
			return false;
		}
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