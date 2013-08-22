package stockviewer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.controller.Controller;
import stockviewer.controller.StockViewerController;
import stockviewer.stock.StockDao;
import stockviewer.stock.StockDataSource;
import stockviewer.stock.YahooFinanceClient;
import stockviewer.ui.StockViewerView;
import stockviewer.ui.View;

public class StockViewer {

	private static final Logger LOG = LoggerFactory
			.getLogger(StockViewer.class);

	private StockDataSource yahooFinanceClient;
	private StockDao dao;
	private Controller controller;
	private View view;
	private Boolean shutDownDbOnExit;

	public StockViewer() {

		LOG.info("Application started");

		initializeDatabase();

		yahooFinanceClient = new YahooFinanceClient();
		controller = new StockViewerController(yahooFinanceClient, dao);
		view = new StockViewerView(controller, yahooFinanceClient, dao,
				shutDownDbOnExit);

		controller.addView(view);

		// YAHOO FINANCE:

		// historical
		// http://ichart.yahoo.com/table.csv?s=AAPL&a=0&b=1&c=2000&d=0&e=31&f=2010&g=d&ignore=.csv

		// real time
		// http://finance.yahoo.com/d/quotes.csv?s=AAPL+GOOG+MSFT&format=json

		// stock look up
		// http://autoc.finance.yahoo.com/autoc?query=google&callback=YAHOO.Finance.SymbolSuggest.ssCallback

	}

	private void initializeDatabase() {

		Properties dbProperties = new Properties();
		try {

			dbProperties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("db.properties"));
			String dbConnectionUrl = dbProperties.getProperty("DB_URL");
			Boolean createTableOnStartup = Boolean.parseBoolean(dbProperties
					.getProperty("CREATE_TABLE_ON_STARTUP"));
			shutDownDbOnExit = Boolean.parseBoolean(dbProperties
					.getProperty("SHUTDOWN_ON_EXIT"));
			dao = new StockDao(dbConnectionUrl);

			if (createTableOnStartup) {
				try {
					dao.createTable();
				} catch (SQLException e) {
					LOG.error(
							"Error creating stock data table (possibly already exists)",
							e);
				}
			}

		} catch (IOException e) {
			LOG.error("Could not find db.properties file", e);
		}

	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new StockViewer();
			}
		});

	}
}