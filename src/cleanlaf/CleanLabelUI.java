package cleanlaf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicLabelUI;

public class CleanLabelUI extends BasicLabelUI implements PropertyChangeListener {

    private final JLabel lbl;

    public static ComponentUI createUI(JComponent comp) {
        return new CleanLabelUI((JLabel) comp);
    }

    private CleanLabelUI(JLabel lbl) {
        this.lbl = lbl;
    }

    @Override
    protected void uninstallDefaults(JLabel c) {
        if (this.lbl.getBorder() != null)
            this.lbl.setBorder(null);

        if ((this.lbl.getForeground() instanceof UIResource))
            this.lbl.setForeground(null);

        if ((this.lbl.getFont() instanceof UIResource))
            this.lbl.setFont(null);

        if ((this.lbl.getBackground() instanceof UIResource))
            this.lbl.setBackground(null);
    }

    @Override
    protected void installDefaults(JLabel b) {
        //
    }

    @Override
    protected void installListeners(JLabel c) {
        c.addPropertyChangeListener(this);
    }

    @Override
    protected void uninstallListeners(JLabel c) {
        c.removePropertyChangeListener(this);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        super.paint(g, c);

    }

}
