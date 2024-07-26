package cleanlaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

public class CleanButtonUI extends BasicButtonUI implements PropertyChangeListener {

    public static ComponentUI createUI(JComponent comp) {
        return new CleanButtonUI();
    }

    @Override
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        b.setBorder(null);
        b.setOpaque(true);
        b.setFont(null);
        b.setBackground(null);
        b.setForeground(null);
    }

    @Override
    protected void installListeners(AbstractButton b) {
        super.installListeners(b);
        b.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(AbstractButton b) {
        b.removePropertyChangeListener(this);
        super.uninstallListeners(b);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        AbstractButton button = (AbstractButton) c;
        if (button.isContentAreaFilled()) {
            g.setColor(new Color(225, 225, 225));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
        super.paint(g, c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        final Dimension preferredSize = super.getPreferredSize(c);
        return new Dimension(preferredSize.width + 20, preferredSize.height + 4);

    }

    @Override
    protected void paintIcon(Graphics g, JComponent c, Rectangle iconRect) {
        Graphics2D graphics = (Graphics2D) g.create();
        AbstractButton b = (AbstractButton) c;
        Icon icon = b.getIcon();
        icon.paintIcon(b, graphics, iconRect.x, iconRect.y);
        graphics.dispose();
    }

    @Override
    public void update(Graphics g, JComponent c) {
        this.paint(g, c);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //
    }

}
