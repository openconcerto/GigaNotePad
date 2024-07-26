package cleanlaf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public class CleanCheckBoxMenuItemUI extends BasicCheckBoxMenuItemUI {

    public static ComponentUI createUI(JComponent c) {
        return new CleanCheckBoxMenuItemUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        super.paint(g, c);
    }
}
