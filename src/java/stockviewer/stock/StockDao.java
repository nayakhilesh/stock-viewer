package stockviewer.stock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.util.DBUtil;
import stockviewer.util.DateUtil;

public class StockDao implements StockDataSource {

	private static final Logger LOG = LoggerFactory.getLogger(StockDao.class);

	private final String connectionUrl;
	private Calendar cal;

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
	
	private static final String CREATE_STOCK_DATA_INDEX = "CREATE INDEX STOCK_DATA_INDEX ON "
			+ "STOCK_DATA (TICKER_SYMBOL, DATE)";

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
	
	private static final String STOCK_DATA_CHECK = "SELECT COUNT(*) AS COUNT FROM STOCK_DATA "
			+ "WHERE TICKER_SYMBOL = ? "
			+ "AND DATE = ? ";
	
	private static final String SEARCH_TICKERS_QUERY = "SELECT DISTINCT TICKER_SYMBOL FROM STOCK_DATA "
			+ "WHERE TICKER_SYMBOL LIKE ?";

	public StockDao(String connectionUrl) {
		this.connectionUrl = connectionUrl;
		this.cal = Calendar.getInstance();
		this.cal.setTimeZone(DateUtil.GMT);
	}

	private Connection getConnection() throws SQLException {
		return DriverManager.getConnection(connectionUrl);
	}

	public void shutDown() throws SQLException {

		Connection conn = getConnection();
		Statement st = conn.createStatement();
		st.execute(SHUTDOWN);
		DBUtil.close(conn);

	}

	public void createTable() throws SQLException {

		Connection conn = null;
		Statement st = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.executeUpdate(CREATE_STOCK_DATA_TABLE);
			st.executeUpdate(CREATE_STOCK_DATA_INDEX);
			conn.commit();
		} catch (SQLException e) {
			DBUtil.rollback(conn);
			throw e;
		} finally {
			DBUtil.close(st);
			DBUtil.close(conn);
		}

	}

	public List<StockData> getStockData(String tickerSymbol, Date from, Date to)
			throws StockDataException {

		List<StockData> list = new LinkedList<StockData>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(STOCK_DATA_QUERY);
			pstmt.setString(1, tickerSymbol.toUpperCase());
			java.sql.Date sqlFromDate = new java.sql.Date(from.getTime());
			java.sql.Date sqlToDate = new java.sql.Date(to.getTime());
			pstmt.setDate(2, sqlFromDate, cal);
			pstmt.setDate(3, sqlToDate, cal);

			rset = pstmt.executeQuery();
			while (rset.next()) {

				StockData sd = new StockData();
				sd.setDate(rset.getDate("DATE", cal));
				sd.setOpen(rset.getDouble("OPEN"));
				sd.setHigh(rset.getDouble("HIGH"));
				sd.setLow(rset.getDouble("LOW"));

				sd.setClose(rset.getDouble("CLOSE"));
				sd.setVolume(rset.getDouble("VOLUME"));
				sd.setAdjClose(rset.getDouble("ADJ_CLOSE"));
				list.add(sd);

			}

		} catch (SQLException e) {
			throw new StockDataException(e.getLocalizedMessage(), StockDataExceptionType.OTHER);
		} finally {
			DBUtil.close(rset);
			DBUtil.close(pstmt);
			DBUtil.close(conn);
		}

		return list;

	}

	public void insertStockInfo(StockInfo info) throws SQLException {

		Connection conn = null;
		PreparedStatement pstmtCheck = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		int count = 0;

		// upsert not used since it isn't standardized across db's
		
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(STOCK_DATA_INSERT);
			pstmtCheck = conn.prepareStatement(STOCK_DATA_CHECK);
			String tickerSymbol = info.getTickerSymbol().toUpperCase();
			for (StockData data : info.getStockData()) {

				java.sql.Date sqlDate = new java.sql.Date(data.getDate()
						.getTime());
				
				pstmtCheck.setString(1, tickerSymbol);
				pstmtCheck.setDate(2, sqlDate, cal);
				rset = pstmtCheck.executeQuery();
				
				if (rset.next() && (rset.getInt("COUNT") > 0))
					continue;
				
				pstmt.setString(1, tickerSymbol);
				pstmt.setDate(2, sqlDate, cal);
				pstmt.setDouble(3, data.getOpen());
				pstmt.setDouble(4, data.getHigh());
				pstmt.setDouble(5, data.getLow());
				pstmt.setDouble(6, data.getClose());
				pstmt.setDouble(7, data.getVolume());
				pstmt.setDouble(8, data.getAdjClose());

				pstmt.addBatch();
				count++;
				if (count == 100) {
					pstmt.executeBatch();
					conn.commit();
					count = 0;
				}

			}

			if (count != 0 && pstmt != null) {
				pstmt.executeBatch();
				conn.commit();
			}

			LOG.info("Finished Committing to DB");
		} catch (SQLException e) {
			DBUtil.rollback(conn);
			throw e;
		} finally {
			DBUtil.close(rset);
			DBUtil.close(pstmtCheck);
			DBUtil.close(pstmt);
			DBUtil.close(conn);
		}

	}

	@Override
	public List<StockTicker> searchTickers(String query)
			throws StockDataException {

		List<StockTicker> tickers = new ArrayList<StockTicker>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;

		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SEARCH_TICKERS_QUERY);
			pstmt.setString(1, query.toUpperCase() + "%");

			rset = pstmt.executeQuery();
			while (rset.next()) {

				StockTicker ticker = new StockTicker();
				ticker.setSymbol(rset.getString("TICKER_SYMBOL"));
				tickers.add(ticker);

			}

		} catch (SQLException e) {
			throw new StockDataException(e.getLocalizedMessage(), StockDataExceptionType.OTHER);
		} finally {
			DBUtil.close(rset);
			DBUtil.close(pstmt);
			DBUtil.close(conn);
		}

		return tickers;
	}

}
