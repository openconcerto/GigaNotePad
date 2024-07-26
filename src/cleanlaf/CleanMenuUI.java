package cleanlaf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuUI;

public class CleanMenuUI extends BasicMenuUI {

    public static ComponentUI createUI(JComponent c) {
        return new CleanMenuUI();
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        super.paint(g, c);
    }

    @Override
    protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        int menuWidth = menuItem.getWidth();
        int menuHeight = menuItem.getHeight();

        if (model.isArmed() || (menuItem instanceof JMenu && model.isSelected())) {
            if (menuItem.getParent() instanceof JMenuBar) {
                g.setColor(new Color(204, 232, 255));
            } else {
                g.setColor(bgColor);
            }
            g.fillRect(0, 0, menuWidth, menuHeight);
        } else {
            if (menuItem.getParent() instanceof JMenuBar) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(menuItem.getBackground());
            }
            g.fillRect(0, 0, menuWidth, menuHeight);
        }
        g.setColor(oldColor);

    }
}
