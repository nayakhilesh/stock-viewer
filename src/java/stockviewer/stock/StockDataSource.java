package stockviewer.stock;

import java.util.Date;
import java.util.List;

public interface StockDataSource {

	List<StockData> getStockData(String tickerSymbol, Date from, Date to)
			throws StockDataException;

	List<StockTicker> searchTickers(String query) throws StockDataException;

}
