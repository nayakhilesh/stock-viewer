package stockviewer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.stock.StockData;
import stockviewer.stock.StockInfo;

public class DBManager {

	private static final Logger LOG = LoggerFactory.getLogger(DBManager.class);

	private Connection conn;

	private static final String CONNECTION_STRING = "jdbc:hsqldb:file:db/appdb";
	
	private static final String SHUTDOWN = "SHUTDOWN";

	private static final String CREATE_STOCK_DATA_TABLE = "CREATE TABLE STOCK_DATA ( "
			+ "TICKER_SYMBOL VARCHAR(15), "
			+ "DATE DATE, "
			+ "OPEN NUMERIC(13,3), "
			+ "HIGH NUMERIC(13,3), "
			+ "LOW NUMERIC(13,3), "
			+ "CLOSE NUMERIC(13,3), "
			+ "VOLUME NUMERIC(15,0), "
			+ "ADJ_CLOSE NUMERIC(13,3) )";

	private static final String STOCK_DATA_QUERY = "SELECT DATE, OPEN, HIGH, LOW, CLOSE, VOLUME, ADJ_CLOSE "
			+ "FROM STOCK_DATA "
			+ "WHERE TICKER_SYMBOL = ? "
			+ "AND DATE BETWEEN ? AND ? ";
	
	private static final String STOCK_DATA_INSERT = "INSERT INTO STOCK_DATA ( "
			+ "TICKER_SYMBOL, "
			+ "DATE, "
			+ "OPEN, "
			+ "HIGH, "
			+ "LOW, "
			+ "CLOSE, "
			+ "VOLUME, "
			+ "ADJ_CLOSE ) values (?, ?, ?, ?, ?, ?, ?, ?)";

	public DBManager() throws Exception {
		conn = DriverManager.getConnection(CONNECTION_STRING);
		conn.setAutoCommit(false);
	}

	public void shutDown() throws SQLException {

		Statement st = conn.createStatement();
		st.execute(SHUTDOWN);
		conn.commit();
		conn.close();

	}

	public void createTable() {

		try {
			Statement st = conn.createStatement();
			st.executeUpdate(CREATE_STOCK_DATA_TABLE);
			st.close();
			conn.commit();
		} catch (Exception e) {
			LOG.error("Error creating table (possibly already exists)", e);
		}

	}

	public List<StockData> getStockData(String tickerSymbol, Date from, Date to) {
return null;
	}
	
	public void insertStockInfo(StockInfo info) {
		
		PreparedStatement pstmt = conn.prepareStatement(STOCK_DATA_INSERT);
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		int i = 0;
		
		try {
			String tickerSymbol = info.getTickerSymbol();
			for (StockData data : info.getStockData()) {
				
				java.sql.Date sqlDate = new java.sql.Date(data.getDate().getTime());
				pstmt.setString(1, tickerSymbol);
				pstmt.setDate(2, sqlDate, cal);
				
				pstmt.addBatch();
				
				if (i >= 100) {
					pstmt.executeBatch();
					conn.commit();
					i = 0;
				}
			}
			
			
			
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public static void main(String[] args) {
		//new DBManager().createTable();
	}

}
