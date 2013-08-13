package stockviewer.ui;

import java.util.Date;

import stockviewer.stock.StockInfo;

public interface View {

	void onReceivingNewStockInfo(Date from, Date to, StockInfo stock1,
			StockInfo stock2);

}
