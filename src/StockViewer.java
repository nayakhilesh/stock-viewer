import javax.swing.SwingUtilities;

public class StockViewer {

	public StockViewer() {

		StockDataSource ds = new YahooFinanceClient();
		Controller controller = new StockViewerController(ds);
		View view = new StockViewerView(controller, ds);

		controller.addView(view);
		
		// TODO logging
		// TODO splash screen

		// YAHOO FINANCE:
		
		// historical
		// http://ichart.yahoo.com/table.csv?s=AAPL&a=0&b=1&c=2000&d=0&e=31&f=2010&g=d&ignore=.csv

		// real time
		// http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&format=json

		// stock look up
		// http://autoc.finance.yahoo.com/autoc?query=google&callback=YAHOO.Finance.SymbolSuggest.ssCallback

	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new StockViewer();
			}
		});

	}
}