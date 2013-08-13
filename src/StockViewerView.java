import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

public class StockViewerView implements View {

	private static final String JDATECHOOSER_DATE_PATTERN = "MM/dd/yyyy";
	private static final String JDATECHOOSER_MASK_PATTERN = "##/##/####";
	private static final char JDATECHOOSER_PLACEHOLDER = '_';
	private static final String ERROR = "ERROR";
	private ChartUtility chartUtility;

	private ExecutorService executor;

	// TODO don't hang the UI
	// TODO add color picker
	// TODO improve layout
	// TODO bad case handling

	private final JDateChooser fromDateChooser;
	private final JDateChooser toDateChooser;

	private final JTextField stock1Field;
	private final JTextField stock2Field;
	
	private final JComboBox stockPriceTypeBox;

	public StockViewerView(final Controller controller, StockDataSource ds) {

		chartUtility = new ChartUtility();

		final JFrame frame = new JFrame("Stock Viewer");
		frame.setLocation(310, 130);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create panel to hold all of above
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel dateRangeSelectionPanel = new JPanel();
		// dateRangeSelectionPanel.setLayout(new GridLayout(1, 4, 4, 4));

		JLabel from = new JLabel("From:");
		JLabel to = new JLabel("To:");

		// add objects to panel
		fromDateChooser = new JDateChooser(JDATECHOOSER_DATE_PATTERN,
				JDATECHOOSER_MASK_PATTERN, JDATECHOOSER_PLACEHOLDER);
		toDateChooser = new JDateChooser(JDATECHOOSER_DATE_PATTERN,
				JDATECHOOSER_MASK_PATTERN, JDATECHOOSER_PLACEHOLDER);

		dateRangeSelectionPanel.add(from);
		dateRangeSelectionPanel.add(fromDateChooser);
		dateRangeSelectionPanel.add(to);
		dateRangeSelectionPanel.add(toDateChooser);

		JPanel stockPickerPanel = new JPanel();

		stock1Field = new JTextField(15);
		new StockAutoCompleter(stock1Field, ds);
		stockPickerPanel.add(stock1Field);

		stock2Field = new JTextField(15);
		new StockAutoCompleter(stock2Field, ds);
		stockPickerPanel.add(stock2Field);

		JPanel controls = new JPanel();
		JButton createButton = new JButton("Create Plot");
		stockPriceTypeBox = new JComboBox(StockPriceType.values());
		createButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				if (!isValid())
					return;

				String tickerSymbol1 = stock1Field.getText();
				String tickerSymbol2 = stock2Field.getText();
				try {
					controller.onCreateChart(fromDateChooser.getDate(),
							toDateChooser.getDate(), tickerSymbol1,
							tickerSymbol2);
				} catch (StockDataException e) {
					String message = e.getLocalizedMessage()
							+ ", check ticker validity";
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception e) {
					String message = "Error retrieving stock data";
					JOptionPane.showMessageDialog(null, message, ERROR,
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		controls.add(stockPriceTypeBox);
		controls.add(createButton);

		mainPanel.add(dateRangeSelectionPanel, BorderLayout.NORTH);
		mainPanel.add(stockPickerPanel, BorderLayout.CENTER);
		mainPanel.add(controls, BorderLayout.SOUTH);

		frame.add(mainPanel, BorderLayout.CENTER);

		// Put the frame on the screen
		frame.pack();
		frame.setVisible(true);

	}

	private boolean isValid() {

		if (fromDateChooser.getDate() == null
				|| toDateChooser.getDate() == null) {
			String message = "Date fields not filled in";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (stock1Field.getText() == null || stock1Field.getText().isEmpty()
				|| stock2Field.getText() == null
				|| stock2Field.getText().isEmpty()) {
			String message = "Stock tickers not filled in";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (!toDateChooser.getDate().after(fromDateChooser.getDate())) {
			String message = "To date must be after From date";
			JOptionPane.showMessageDialog(null, message, ERROR,
					JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;

	}

	public void onReceivingNewStockInfo(Date from, Date to, StockInfo stock1,
			StockInfo stock2) {

		JPanel chartPanel = chartUtility.createChart(from, to, stock1, stock2,
				(StockPriceType) stockPriceTypeBox.getSelectedItem());

		JFrame chartFrame = new JFrame();
		chartFrame.add(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);

	}

}
