package stockviewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import stockviewer.controller.Controller;
import stockviewer.stock.StockDataException;
import stockviewer.stock.StockDataSource;
import stockviewer.stock.StockInfo;
import stockviewer.stock.StockPriceType;
import stockviewer.ui.custom.ColorChooser;
import stockviewer.ui.custom.InfiniteProgressPanel;
import stockviewer.ui.custom.StockAutoCompleter;
import stockviewer.util.ChartUtility;
import stockviewer.util.DateUtil;

import com.toedter.calendar.JDateChooser;

public class StockViewerView implements View {

	private static final String JDATECHOOSER_DATE_PATTERN = "MM/dd/yyyy";
	private static final String JDATECHOOSER_MASK_PATTERN = "##/##/####";
	private static final char JDATECHOOSER_PLACEHOLDER = '_';
	private static final String ERROR = "ERROR";
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

		this.controller = controller;
		this.threadPool = Executors.newFixedThreadPool(1);
		chartUtility = new ChartUtility();

		glassPane = new InfiniteProgressPanel();

		final JFrame frame = new JFrame("Stock Viewer");
		frame.setLocation(310, 130);
		frame.setGlassPane(glassPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel dateRangeSelectionPanel = new JPanel();
		// dateRangeSelectionPanel.setLayout(new GridLayout(1, 4, 4, 4));

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

		stock1Field = new JTextField(10);
		new StockAutoCompleter(stock1Field, ds);
		stockPickerPanel.add(stock1Field);

		colorChooser1 = new ColorChooser();
		colorChooser1.setSelectedItem(Color.BLUE);
		stockPickerPanel.add(colorChooser1);

		stock2Field = new JTextField(10);
		new StockAutoCompleter(stock2Field, ds);
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

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
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

	}

	private void generateChart() {

		Date fromDate = DateUtil.truncate(fromDateChooser.getDate());
		Date toDate = DateUtil.truncate(toDateChooser.getDate());

		String tickerSymbol1 = stock1Field.getText();
		String tickerSymbol2 = stock2Field.getText();

		try {
			if (isValid(fromDate, toDate, tickerSymbol1, tickerSymbol2)) {
				controller.onCreateChart(fromDateChooser.getDate(),
						toDateChooser.getDate(), tickerSymbol1, tickerSymbol2);
			}
		} catch (StockDataException e) {
			String message = e.getLocalizedMessage()
					+ ", check ticker validity";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.ERROR_MESSAGE);
		} catch (ProcessingException e) {
			String message = "Error, check network connectivity";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			String message = "Error creating chart";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.ERROR_MESSAGE);
		} finally {
			glassPane.stop();
			createButton.setEnabled(true);
		}

	}

	private boolean isValid(Date fromDate, Date toDate, String tickerSymbol1,
			String tickerSymbol2) {

		if (fromDate == null || toDate == null) {
			String message = "Date fields not filled in";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (tickerSymbol1 == null || tickerSymbol1.isEmpty()
				|| tickerSymbol2 == null || tickerSymbol2.isEmpty()) {
			String message = "Stock tickers not filled in";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (!toDate.after(fromDate)) {
			String message = "To date must be after From date";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;

	}

	@Override
	public void onReceivingNewStockInfo(Date from, Date to, StockInfo stock1,
			StockInfo stock2) {

		JPanel chartPanel = chartUtility.createChart(from, to, stock1, stock2,
				(Color) colorChooser1.getSelectedItem(),
				(Color) colorChooser2.getSelectedItem(),
				(StockPriceType) stockPriceTypeBox.getSelectedItem());

		JFrame chartFrame = new JFrame();
		chartFrame.add(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);

	}

}
