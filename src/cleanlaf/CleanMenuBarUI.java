package cleanlaf;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

public class CleanMenuBarUI extends BasicMenuBarUI {

    public static ComponentUI createUI(JComponent c) {
        return new CleanMenuBarUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }
}
