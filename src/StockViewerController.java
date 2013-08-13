import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StockViewerController implements Controller {

	private StockDataSource ds;
	private List<View> views;

	public StockViewerController(StockDataSource ds) {
		this.ds = ds;
		views = new LinkedList<View>();
	}

	public void addView(View view) {
		views.add(view);
	}

	public void removeView(View view) {
		views.remove(view);
	}

	public void onCreateChart(Date from, Date to, String tickerSymbol1,
			String tickerSymbol2) throws StockDataException {

		List<StockData> data1 = ds.getStockData(tickerSymbol1, from, to);
		List<StockData> data2 = ds.getStockData(tickerSymbol2, from, to);

		for (View view : views) {
			view.onReceivingNewStockInfo(from, to, new StockInfo(tickerSymbol1,
					data1), new StockInfo(tickerSymbol2, data2));
		}
	}

}
