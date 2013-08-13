import java.util.List;

import javax.swing.text.JTextComponent;

public class StockAutoCompleter extends AutoCompleter {

	private StockDataSource ds;

	public StockAutoCompleter(JTextComponent comp, StockDataSource ds) {
		super(comp);
		this.ds = ds;
	}

	protected boolean updateListData() {
		String value = textComp.getText();
		if (value.isEmpty())
			return false;

		List<StockTicker> tickers;
		try {
			tickers = ds.searchTickers(value);
		} catch (Exception e) {
			return false;
		}
		if (tickers == null || tickers.isEmpty())
			return false;

		String[] arr = new String[tickers.size()];

		int i = 0;
		for (StockTicker ticker : tickers) {
			arr[i] = ticker.toString();
			i++;
		}

		list.setListData(arr);
		return true;
	}

	protected void acceptedListItem(String selected) {
		if (selected != null) {
			String[] arr = selected.split(",");
			if (arr != null && arr.length > 0) {
				textComp.setText(arr[0]);
			} else {
				textComp.setText(selected);
			}
		}
	}
}