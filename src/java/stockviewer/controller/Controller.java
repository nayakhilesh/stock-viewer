package stockviewer.controller;

import java.util.Date;

import stockviewer.stock.StockDataException;
import stockviewer.ui.View;

public interface Controller {

	void onCreateChart(Date from, Date to, String tickerSymbol1,
			String tickerSymbol2) throws StockDataException;

	void addView(View view);

	void removeView(View view);

}
