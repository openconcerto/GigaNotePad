package cleanlaf;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;

public class CleanTextFieldUI extends BasicTextFieldUI implements FocusListener {
    private final JTextField txt;

    public static ComponentUI createUI(JComponent c) {
        return new CleanTextFieldUI((JTextField) c);
    }

    private CleanTextFieldUI(JTextField t) {
        this.txt = t;

    }

    @Override
    protected void paintSafely(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        super.paintSafely(g);
    }

    @Override
    public void focusGained(FocusEvent e) {
        //

    }

    @Override
    public void focusLost(FocusEvent e) {
        //
    }

}
