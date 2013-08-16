package stockviewer.ui.custom;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;

/**
 * 
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 *         (http://www.jroller.com/santhosh/entry/file_path_autocompletion)
 * @author modified by Akhilesh Nayak - nayakhilesh@gmail.com
 * 
 */
public abstract class AutoCompleter {

	protected JList list = new JList();
	protected JTextField textField;
	private JPopupMenu popup = new JPopupMenu();
	private static final String AUTOCOMPLETER = "AUTOCOMPLETER";
	private static final String NEXT_ELEMENT = "NEXT_ELEMENT";
	private static final String PREVIOUS_ELEMENT = "PREVIOUS_ELEMENT";
	private static final String HIDE_POPUP = "HIDE_POPUP";
	private static final String SELECT_ELEMENT = "SELECT_ELEMENT";

	private volatile boolean currentlyActive = false;
	// the completer is activated when you
	// hit the down arrow in the text field

	public AutoCompleter(JTextField field) {

		textField = field;
		textField.putClientProperty(AUTOCOMPLETER, this);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(null);

		list.setFocusable(false);
		scroll.getVerticalScrollBar().setFocusable(false);
		scroll.getHorizontalScrollBar().setFocusable(false);

		popup.setBorder(BorderFactory.createLineBorder(Color.black));
		popup.add(scroll);
		popup.setFocusable(false);

		textField.getDocument().addDocumentListener(documentListener);

		textField.getActionMap().put(NEXT_ELEMENT, downAction);
		textField.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), NEXT_ELEMENT);

		textField.getActionMap().put(PREVIOUS_ELEMENT, upAction);
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				PREVIOUS_ELEMENT);

		textField.getActionMap().put(HIDE_POPUP, hidePopupAction);
		textField.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), HIDE_POPUP);

		textField.getActionMap().put(SELECT_ELEMENT, selectAction);

		textField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				JTextField tf = (JTextField) e.getSource();
				AutoCompleter completer = (AutoCompleter) tf
						.getClientProperty(AUTOCOMPLETER);
				if (tf.isEnabled())
					completer.popup.setVisible(false);
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				currentlyActive = false;
				textField.getInputMap().remove(
						KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

	}

	private DocumentListener documentListener = new DocumentListener() {

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (currentlyActive) {
				showPopup();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (currentlyActive) {
				showPopup();
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	};

	private void showPopup() {
		if (textField.isEnabled() && updateListData()
				&& list.getModel().getSize() != 0) {
			textField.getInputMap().put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
					SELECT_ELEMENT);
			int size = list.getModel().getSize();
			list.setVisibleRowCount(size < 10 ? size : 10);

			int x = 0;
			try {
				int pos = Math.min(textField.getCaret().getDot(), textField
						.getCaret().getMark());
				x = textField.getUI().modelToView(textField, pos).x;
			} catch (BadLocationException e) {
				// this should never happen!!!
				e.printStackTrace();
			}
			popup.show(textField, x, textField.getHeight());
		} else {
			popup.setVisible(false);
		}
	}

	private static Action selectAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField tf = (JTextField) e.getSource();
			AutoCompleter completer = (AutoCompleter) tf
					.getClientProperty(AUTOCOMPLETER);
			completer.selectedListItem(completer.list.getSelectedValue());
			completer.popup.setVisible(false);
		}
	};

	private static Action downAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField tf = (JTextField) e.getSource();
			AutoCompleter completer = (AutoCompleter) tf
					.getClientProperty(AUTOCOMPLETER);
			if (tf.isEnabled()) {
				completer.currentlyActive = true;
				if (completer.popup.isVisible())
					completer.selectNextPossibleValue();
				else
					completer.showPopup();
			}
		}
	};

	private static Action upAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField tf = (JTextField) e.getSource();
			AutoCompleter completer = (AutoCompleter) tf
					.getClientProperty(AUTOCOMPLETER);
			if (tf.isEnabled() && completer.popup.isVisible()) {
				completer.selectPreviousPossibleValue();
			}
		}
	};

	private static Action hidePopupAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			JTextField tf = (JTextField) e.getSource();
			AutoCompleter completer = (AutoCompleter) tf
					.getClientProperty(AUTOCOMPLETER);
			if (tf.isEnabled())
				completer.popup.setVisible(false);
		}
	};

	/**
	 * Selects the next item in the list. It won't change the selection if the
	 * currently selected item is already the last item.
	 */
	protected void selectNextPossibleValue() {
		int si = list.getSelectedIndex();

		if (si < list.getModel().getSize() - 1) {
			list.setSelectedIndex(si + 1);
			list.ensureIndexIsVisible(si + 1);
		}
	}

	/**
	 * Selects the previous item in the list. It won't change the selection if
	 * the currently selected item is already the first item.
	 */
	protected void selectPreviousPossibleValue() {
		int si = list.getSelectedIndex();

		if (si > 0) {
			list.setSelectedIndex(si - 1);
			list.ensureIndexIsVisible(si - 1);
		}
	}

	// update list model depending on the data in textfield
	protected abstract boolean updateListData();

	// user has selected some item in the list. update textfield accordingly...
	protected abstract void selectedListItem(Object selected);
}