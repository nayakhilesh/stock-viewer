import java.util.List;

public class StockInfo {

	private String tickerSymbol;
	private List<StockData> stockData;

	public StockInfo(String tickerSymbol, List<StockData> stockData) {
		this.tickerSymbol = tickerSymbol;
		this.stockData = stockData;
	}

	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	public List<StockData> getStockData() {
		return stockData;
	}

	public void setStockData(List<StockData> stockData) {
		this.stockData = stockData;
	}

}
