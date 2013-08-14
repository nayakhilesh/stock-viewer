package stockviewer.ui.custom;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ColorChooser extends JComboBox {

	private final static Color[] COLORS = { Color.BLACK, Color.BLUE,
			Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN,
			Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK,
			Color.RED, Color.WHITE, Color.YELLOW };

	public ColorChooser() {
		super(COLORS);

		setEditable(false);
		setRenderer(new ColorPanelRenderer());
		setSelectedItem(COLORS[0]);

	}

	private class ColorPanelRenderer implements ListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			return new ColorPanel((Color) value);
		}
	}

	private class ColorPanel extends JPanel {

		private Color color;

		public ColorPanel(Color color) {
			this.color = color;
			this.setOpaque(true);
		}

		@Override
		public void paint(Graphics g) {
			Rectangle r = this.getBounds();
			g.setColor(color);
			g.fillRect(0, 0, r.width, r.height);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(30, 16);
		}
	}

}