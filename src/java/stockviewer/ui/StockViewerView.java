package stockviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.ws.rs.ProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stockviewer.controller.Controller;
import stockviewer.stock.StockDataException;
import stockviewer.stock.StockDataSource;
import stockviewer.stock.StockInfo;
import stockviewer.stock.StockPriceType;
import stockviewer.ui.custom.ColorChooser;
import stockviewer.ui.custom.InfiniteProgressPanel;
import stockviewer.ui.custom.StockAutoCompleter;
import stockviewer.ui.custom.TextPrompt;
import stockviewer.util.ChartUtility;
import stockviewer.util.DateUtil;

import com.toedter.calendar.JDateChooser;

public class StockViewerView implements View {

	private static final Logger LOG = LoggerFactory
			.getLogger(StockViewerView.class);

	private static final String JDATECHOOSER_DATE_PATTERN = "MM/dd/yyyy";
	private static final String JDATECHOOSER_MASK_PATTERN = "##/##/####";
	private static final char JDATECHOOSER_PLACEHOLDER = '_';
	private static final String ERROR = "ERROR";
	private static final int STOCK_FIELD_WIDTH = 10;
	private ChartUtility chartUtility;

	private ExecutorService threadPool;

	private Controller controller;
	private JDateChooser fromDateChooser;
	private JDateChooser toDateChooser;
	private JTextField stock1Field;
	private JTextField stock2Field;
	private JComboBox stockPriceTypeBox;
	private JComboBox colorChooser1;
	private JComboBox colorChooser2;
	private final JButton createButton;
	private final InfiniteProgressPanel glassPane;

	public StockViewerView(final Controller controller, StockDataSource ds) {

		LOG.info("Initializing GUI");

		this.controller = controller;
		this.threadPool = Executors.newFixedThreadPool(1);
		chartUtility = new ChartUtility();

		glassPane = new InfiniteProgressPanel();

		final JFrame frame = new JFrame("Stock Viewer");
		frame.setLocation(310, 130);
		frame.setGlassPane(glassPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				LOG.info("Application exiting...");
			}
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel dateRangeSelectionPanel = new JPanel();

		JLabel from = new JLabel("From:");
		JLabel to = new JLabel("To:");

		fromDateChooser = new JDateChooser(JDATECHOOSER_DATE_PATTERN,
				JDATECHOOSER_MASK_PATTERN, JDATECHOOSER_PLACEHOLDER);
		toDateChooser = new JDateChooser(JDATECHOOSER_DATE_PATTERN,
				JDATECHOOSER_MASK_PATTERN, JDATECHOOSER_PLACEHOLDER);

		dateRangeSelectionPanel.add(from);
		dateRangeSelectionPanel.add(fromDateChooser);
		dateRangeSelectionPanel.add(to);
		dateRangeSelectionPanel.add(toDateChooser);

		JPanel stockPickerPanel = new JPanel();

		stock1Field = new JTextField(STOCK_FIELD_WIDTH);
		new StockAutoCompleter(stock1Field, ds);
		decorateWithPrompt(stock1Field);
		stockPickerPanel.add(stock1Field);

		colorChooser1 = new ColorChooser();
		colorChooser1.setSelectedItem(Color.BLUE);
		stockPickerPanel.add(colorChooser1);

		stock2Field = new JTextField(STOCK_FIELD_WIDTH);
		new StockAutoCompleter(stock2Field, ds);
		decorateWithPrompt(stock2Field);
		stockPickerPanel.add(stock2Field);

		colorChooser2 = new ColorChooser();
		colorChooser2.setSelectedItem(Color.RED);
		stockPickerPanel.add(colorChooser2);

		JPanel controls = new JPanel();
		createButton = new JButton("Create Plot");
		stockPriceTypeBox = new JComboBox(StockPriceType.values());
		createButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				createButton.setEnabled(false);
				glassPane.start();
				threadPool.execute(new Runnable() {
					@Override
					public void run() {
						generateChart();
					}
				});

			}

		});

		controls.add(stockPriceTypeBox);
		controls.add(createButton);

		mainPanel.add(dateRangeSelectionPanel, BorderLayout.NORTH);
		mainPanel.add(stockPickerPanel, BorderLayout.CENTER);
		mainPanel.add(controls, BorderLayout.SOUTH);

		frame.add(mainPanel, BorderLayout.CENTER);

		// Put the frame on the screen
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);

		LOG.info("GUI initialization complete");

	}

	private void decorateWithPrompt(JTextField textField) {

		TextPrompt tp = new TextPrompt("Enter Ticker", textField);
		tp.changeAlpha(128);
		tp.changeStyle(Font.ITALIC);

	}

	private void generateChart() {

		Date fromDate = DateUtil.truncate(fromDateChooser.getDate());
		Date toDate = DateUtil.truncate(toDateChooser.getDate());

		String tickerSymbol1 = stock1Field.getText();
		String tickerSymbol2 = stock2Field.getText();

		try {
			if (isValid(fromDate, toDate, tickerSymbol1, tickerSymbol2)) {
				controller.onCreateChart(fromDate, toDate, tickerSymbol1,
						tickerSymbol2);
			}
		} catch (StockDataException e) {
			final String message = e.getLocalizedMessage()
					+ ", check ticker validity";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.ERROR_MESSAGE);
				}
			});
		} catch (ProcessingException e) {
			final String message = "Error, check network connectivity";
			LOG.error(message, e);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.ERROR_MESSAGE);
				}
			});
		} catch (Exception e) {
			final String message = "Error creating chart";
			LOG.error(message, e);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.ERROR_MESSAGE);
				}
			});
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					glassPane.stop();
					createButton.setEnabled(true);
				}
			});
		}

	}

	private boolean isValid(Date fromDate, Date toDate, String tickerSymbol1,
			String tickerSymbol2) {

		if (fromDate == null) {
			final String message = "Invalid From Date";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.WARNING_MESSAGE);
					fromDateChooser.grabFocus();
				}
			});
			return false;
		}

		if (toDate == null) {
			final String message = "Invalid To Date";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.WARNING_MESSAGE);
					toDateChooser.grabFocus();
				}
			});
			return false;
		}

		if (!toDate.after(fromDate)) {
			final String message = "To Date must be after From Date";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.WARNING_MESSAGE);
					toDateChooser.grabFocus();
				}
			});
			return false;
		}

		if (tickerSymbol1 == null || tickerSymbol1.isEmpty()) {
			final String message = "Stock ticker not filled in";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.WARNING_MESSAGE);
					stock1Field.grabFocus();
				}
			});
			return false;
		}

		if (tickerSymbol2 == null || tickerSymbol2.isEmpty()) {
			final String message = "Stock ticker not filled in";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.WARNING_MESSAGE);
					stock2Field.grabFocus();
				}
			});
			return false;
		}

		return true;

	}

	@Override
	public void onReceivingNewStockInfo(Date from, Date to, StockInfo stock1,
			StockInfo stock2) {

		LOG.info("Received stock info for tickers:" + stock1.getTickerSymbol()
				+ " & " + stock2.getTickerSymbol());

		final JPanel chartPanel = chartUtility.createChart(from, to, stock1,
				stock2, (Color) colorChooser1.getSelectedItem(),
				(Color) colorChooser2.getSelectedItem(),
				(StockPriceType) stockPriceTypeBox.getSelectedItem());

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame chartFrame = new JFrame();
				chartFrame.add(chartPanel);
				chartFrame.pack();
				chartFrame.setVisible(true);
			}
		});

	}

}
