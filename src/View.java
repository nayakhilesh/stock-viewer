import java.util.Date;

public interface View {

	void onReceivingNewStockInfo(Date from, Date to, StockInfo stock1, StockInfo stock2);

}
