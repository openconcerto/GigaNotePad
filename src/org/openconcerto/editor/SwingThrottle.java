package org.openconcerto.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SwingThrottle {
    private Timer timer;
    private final Runnable runnable;
    private long last = System.currentTimeMillis();
    private int delay;

    public SwingThrottle(int delayInMs, final Runnable runnable) {
        this.delay = delayInMs;
        this.runnable = runnable;
        this.timer = new Timer(delayInMs, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingThrottle.this.timer.stop();
                SwingUtilities.invokeLater(runnable);

            }
        });
    }

    public synchronized void execute() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalArgumentException("must be called in EDT");
        }
        long t = System.currentTimeMillis();
        if (t - this.last < this.delay) {
            this.timer.restart();

        } else {
            SwingUtilities.invokeLater(this.runnable);
            this.last = t;
        }

    }

    public synchronized void executeNow() {
        if (this.timer.isRunning()) {
            this.timer.stop();
            this.runnable.run();
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
        this.timer.setDelay(delay);
    }

    public int getDelay() {
        return this.delay;
    }

}
