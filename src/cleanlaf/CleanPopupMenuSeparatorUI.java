package cleanlaf;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;

public class CleanPopupMenuSeparatorUI extends BasicPopupMenuSeparatorUI {

    public static ComponentUI createUI(JComponent c) {
        return new BasicPopupMenuSeparatorUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Dimension s = c.getSize();
        g.setColor(c.getForeground());
        g.drawLine(0, 0, s.width, 0);
    }
}
