package cleanlaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CleanComboBoxUI extends BasicComboBoxUI {

    public static ComponentUI createUI(JComponent c) {
        c.setBorder(BorderFactory.createLineBorder(new Color(122, 122, 122), 1));
        return new CleanComboBoxUI();
    }

    @Override
    protected JButton createArrowButton() {
        final BasicArrowButton basicArrowButton = new BasicArrowButton(SwingConstants.SOUTH, Color.WHITE, Color.WHITE, Color.DARK_GRAY, Color.WHITE);
        basicArrowButton.setBorderPainted(false);
        return basicArrowButton;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        super.paint(g, c);

    }

    @Override
    protected Dimension getDisplaySize() {
        final Dimension displaySize = super.getDisplaySize();
        return new Dimension(displaySize.width + 10, displaySize.height + 2);
    }
}
