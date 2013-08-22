package stockviewer.stock;

public class StockTicker {

	private String symbol;
	private String name;
	private String exchange;
	private String type;

	public StockTicker() {
	}

	public StockTicker(String symbol, String name, String exchange, String type) {
		this.symbol = symbol;
		this.name = name;
		this.exchange = exchange;
		this.type = type;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (symbol != null)
			sb.append(symbol);
		if (name != null) {
			sb.append("\t");
			sb.append(name);
		}
		if (exchange != null) {
			sb.append("\t");
			sb.append(exchange);
		}
		if (type != null) {
			sb.append("\t");
			sb.append(type);
		}
		return sb.toString();
	}

}
