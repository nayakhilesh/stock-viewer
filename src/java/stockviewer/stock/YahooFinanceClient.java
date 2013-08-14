package stockviewer.stock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class YahooFinanceClient implements StockDataSource {

	private WebTarget stockDataTarget;
	private DateFormat df;
	private Calendar cal;
	private static final String STOCK_DATA_BASE_URI = "http://ichart.yahoo.com";
	private static final String STOCK_DATA_PATH = "table.csv";

	// http://ichart.yahoo.com/table.csv?s=AAPL&a=0&b=1&c=2000&d=0&e=31&f=2010&g=d&ignore=.csv

	private WebTarget tickerSearchTarget;
	private static final String TICKER_SEARCH_BASE_URI = "http://autoc.finance.yahoo.com";
	private static final String TICKER_SEARCH_PATH = "autoc";

	// http://autoc.finance.yahoo.com/autoc?query=apple&callback=YAHOO.Finance.SymbolSuggest.ssCallback

	public YahooFinanceClient() {
		Client client = ClientBuilder.newClient();
		stockDataTarget = client.target(STOCK_DATA_BASE_URI).path(
				STOCK_DATA_PATH);
		tickerSearchTarget = client.target(TICKER_SEARCH_BASE_URI).path(
				TICKER_SEARCH_PATH);
		df = new SimpleDateFormat("yyyy-MM-dd");
		cal = Calendar.getInstance();
	}

	@Override
	public List<StockData> getStockData(String tickerSymbol, Date from, Date to)
			throws StockDataException {

		cal.setTime(from);
		int fromMonth = cal.get(Calendar.MONTH);
		int fromDay = cal.get(Calendar.DAY_OF_MONTH);
		int fromYear = cal.get(Calendar.YEAR);

		cal.setTime(to);
		int toMonth = cal.get(Calendar.MONTH);
		int toDay = cal.get(Calendar.DAY_OF_MONTH);
		int toYear = cal.get(Calendar.YEAR);

		Response response = stockDataTarget.queryParam("s", tickerSymbol)
				.queryParam("a", fromMonth).queryParam("b", fromDay)
				.queryParam("c", fromYear).queryParam("d", toMonth)
				.queryParam("e", toDay).queryParam("f", toYear)
				.queryParam("g", "d").queryParam("ignore", ".csv")
				.request(MediaType.TEXT_PLAIN_TYPE).get();

		if (Response.Status.NOT_FOUND.equals(Response.Status
				.fromStatusCode(response.getStatus())))
			throw new StockDataException("404: Stock " + tickerSymbol
					+ " data not found");

		String responseCsv = response.readEntity(String.class);

		try {
			return parseCsv(responseCsv);
		} catch (Exception e) {
			throw new StockDataException(
					"Exception parsing retrieved data for " + tickerSymbol);
		}
	}

	private List<StockData> parseCsv(String csv) throws ParseException {

		String data = csv.replace("\r", "");
		String[] rows = data.split("\n");

		List<StockData> list = new LinkedList<StockData>();

		boolean isFirst = true;

		for (String row : rows) {

			// first row is headers so ignore it
			if (isFirst) {
				isFirst = false;
				continue;
			}

			if (row.isEmpty())
				continue;

			String[] cols = row.split(",");

			StockData stockData = new StockData();
			stockData.setDate(df.parse(cols[0]));
			stockData.setOpen(Double.valueOf(cols[1]));
			stockData.setHigh(Double.valueOf(cols[2]));
			stockData.setLow(Double.valueOf(cols[3]));
			stockData.setClose(Double.valueOf(cols[4]));
			stockData.setVolume(Double.valueOf(cols[5]));
			stockData.setAdjClose(Double.valueOf(cols[6]));
			list.add(stockData);

		}

		return list;
	}

	@Override
	public synchronized List<StockTicker> searchTickers(String query)
			throws StockDataException {

		Response response = tickerSearchTarget
				.queryParam("query", query)
				.queryParam("callback",
						"YAHOO.Finance.SymbolSuggest.ssCallback")
				.request(MediaType.APPLICATION_JSON).get();

		if (Response.Status.NOT_FOUND.equals(Response.Status
				.fromStatusCode(response.getStatus())))
			throw new StockDataException("404: Tickers not found for query:"
					+ query);

		String responseString = response.readEntity(String.class);

		Pattern pattern = Pattern
				.compile("YAHOO.Finance.SymbolSuggest.ssCallback\\((.*)\\)");
		Matcher matcher = pattern.matcher(responseString);
		if (matcher.find()) {
			String responseJson = matcher.group(1);
			return parseJson(responseJson);
		}
		return null;
	}

	private List<StockTicker> parseJson(String json) {

		List<StockTicker> tickers = new ArrayList<StockTicker>();
		JSONObject responseObject = (JSONObject) JSONValue.parse(json);
		JSONObject resultSet = (JSONObject) responseObject.get("ResultSet");
		JSONArray results = (JSONArray) resultSet.get("Result");

		for (Object tickerInfo : results) {
			JSONObject tickerInfoObj = (JSONObject) tickerInfo;
			StockTicker stockTicker = new StockTicker();
			stockTicker.setSymbol((String) tickerInfoObj.get("symbol"));
			stockTicker.setName((String) tickerInfoObj.get("name"));
			stockTicker.setExchange((String) tickerInfoObj.get("exchDisp"));
			stockTicker.setType((String) tickerInfoObj.get("typeDisp"));
			tickers.add(stockTicker);
		}

		return tickers;
	}

}
