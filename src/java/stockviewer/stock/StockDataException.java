package stockviewer.stock;

public class StockDataException extends Exception {

	private StockDataExceptionType type;

	public StockDataException(String message, StockDataExceptionType type) {
		super(message);
		this.type = type;
	}

	public StockDataExceptionType getType() {
		return type;
	}

}
