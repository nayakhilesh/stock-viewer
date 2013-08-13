public enum StockPriceType {

	OPEN, HIGH, LOW, CLOSE, ADJCLOSE;

	public double get(StockData sd) {
		switch (this) {
		case OPEN:
			return sd.getOpen();
		case HIGH:
			return sd.getHigh();
		case LOW:
			return sd.getLow();
		case CLOSE:
			return sd.getClose();
		case ADJCLOSE:
			return sd.getAdjClose();
		}
		return 0;
	}

}
