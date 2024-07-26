package cleanlaf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CleanScrollBarUI extends BasicScrollBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new CleanScrollBarUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        super.paint(g, c);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !this.scrollbar.isEnabled()) {
            return;
        }

        int w = thumbBounds.width;
        int h = thumbBounds.height;

        g.translate(thumbBounds.x, thumbBounds.y);

        g.setColor(new Color(205, 205, 205));
        g.fillRect(0, 0, w - 1, h - 1);

        g.translate(-thumbBounds.x, -thumbBounds.y);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton b = super.createDecreaseButton(orientation);
        b.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
        return b;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton b = super.createIncreaseButton(orientation);
        b.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
        return b;
    }
}
