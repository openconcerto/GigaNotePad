package org.openconcerto.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class TextEditorPanel extends JPanel {
    private static final Color CURSOR_COLOR = new Color(1, 2, 20);
    private static final Color BORDER_COLOR = new Color(245, 245, 250);
    private static final Color SELECTION_COLOR = new Color(0, 120, 215);
    private static final Color SELECTION_COLOR_DARK = new Color(0, 110, 205);
    private static final Color TEXT_COLOR = new Color(5, 5, 5);

    private static Color colorBlue = Color.decode("#e8f2fe");
    private static Color colorBlueDark = Color.decode("#e0e9f7");

    private int charWidth;
    private int lineHeight;
    private int leftMargin;
    private int rightMargin;

    private Document doc;
    private long firstVisibleLineGlobalIndex;
    private long cursorGlobalIndex;

    private List<TextLine> lines = new ArrayList<>();
    private int maxCharactersPerLine = 40;
    private Font currentFont;
    private final Selection selection = new Selection(0);

    private long lastComputedIndex = -1;
    private int lastComputedNumberOfVisibleLines = -1;
    private int lastComputedNumberOfVisibleCharactersPerLine = -1;

    private TextLine cursorTextLine;
    private Timer timer = new Timer();

    private int cursorIndexInLine;
    protected int blink;
    private boolean mousePressed;
    // Hightligted texts
    private final List<Highlight> highlights = new ArrayList<>();

    // TODO : refaire l'highlight sur toutes les nouvelles TextLine (scroll)
    List<TextEditorListener> listeners = new ArrayList<>();

    TextEditorPanel() {
        this.setBackground(Color.WHITE);
        InputStream stream = getClass().getResourceAsStream("RobotoMono-Regular.ttf");
        try {
            this.currentFont = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(13f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
        this.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                int wheelRotation = e.getWheelRotation();

                if (wheelRotation > 0) {
                    // Scroll up
                    if (TextEditorPanel.this.lines.size() > 5) {
                        TextLine lineToGo = TextEditorPanel.this.lines.get(5);
                        setCurrentIndex(lineToGo.getGlobalIndexOfFirstChar());
                    } else {
                        // Go to last line
                        TextLine lineToGo = TextEditorPanel.this.lines.get(TextEditorPanel.this.lines.size() - 1);
                        setCurrentIndex(lineToGo.getGlobalIndexOfFirstChar());
                    }

                } else {
                    // Scroll down
                    long globalCharIndex = TextEditorPanel.this.firstVisibleLineGlobalIndex - 5 * TextEditorPanel.this.maxCharactersPerLine;
                    if (globalCharIndex < 0) {
                        globalCharIndex = 0;
                    }

                    final List<TextLine> tLines = TextEditorPanel.this.doc.createTextLines(globalCharIndex, 6, TextEditorPanel.this.maxCharactersPerLine);
                    int count = 0;
                    TextLine first = tLines.get(0);
                    for (int i = tLines.size() - 1; i >= 0; i--) {
                        first = tLines.get(i);
                        if (count == 5) {
                            break;
                        }
                        if (TextEditorPanel.this.firstVisibleLineGlobalIndex > first.getGlobalIndexOfFirstChar()) {
                            count++;
                        }
                    }
                    setCurrentIndex(first.getGlobalIndexOfFirstChar());
                }
            }
        });
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                TextEditorPanel.this.mousePressed = true;
                TextEditorPanel.this.grabFocus();

                TextLinePosition pos = getPositionFrom(e.getX(), e.getY());
                if (pos == null) {
                    return;
                }

                //
                final long cursorIndex = pos.line.getGlobalIndexOfFirstChar() + pos.indexInLine;
                setCursorLocation(cursorIndex);
                TextEditorPanel.this.selection.init(cursorIndex);
                fireCursorMoved();
                TextEditorPanel.this.blink = 1;
                repaint(0);

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                TextEditorPanel.this.mousePressed = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                final int clickCount = e.getClickCount();
                if (clickCount == 2) {
                    // Select current word or space
                    final long currentIndex = getCursorGlobalIndex();
                    char currentChar = TextEditorPanel.this.doc.getCharAt(currentIndex);
                    if (currentChar == ' ') {
                        long start = currentIndex;
                        long end = currentIndex + 1;
                        long endOfDocument = TextEditorPanel.this.doc.length();
                        for (long i = end; i < endOfDocument; i++) {
                            char c = TextEditorPanel.this.doc.getCharAt(i);
                            if (c != ' ' && c != '\t') {
                                end = i;
                                break;
                            }
                            end = i;
                        }
                        for (long i = start; i >= 0; i--) {
                            char c = TextEditorPanel.this.doc.getCharAt(i);
                            if (c != ' ' && c != '\t') {
                                break;
                            }
                            start = i;

                        }
                        setCursorLocation(end);
                        TextEditorPanel.this.selection.setRange(start, end);
                        repaint();
                    } else {
                        long start = currentIndex;
                        long end = currentIndex + 1;
                        long endOfDocument = TextEditorPanel.this.doc.length();
                        int count = 0;
                        for (long i = end; i < endOfDocument; i++) {
                            char c = TextEditorPanel.this.doc.getCharAt(i);
                            if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
                                end = i;
                                break;
                            }
                            end = i;
                            count++;
                            if (count > 10000) {
                                // Prevent very long selection
                                break;
                            }
                        }
                        for (long i = start; i >= 0; i--) {
                            char c = TextEditorPanel.this.doc.getCharAt(i);
                            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                                break;
                            }
                            start = i;
                            count++;
                            if (count > 10000) {
                                // Prevent very long selection
                                break;
                            }
                        }
                        setCursorLocation(end);
                        TextEditorPanel.this.selection.setRange(start, end);
                        repaint();
                    }

                } else if (clickCount >= 3) {
                    // Selection of a complete line
                    final long start = TextEditorPanel.this.cursorTextLine.getGlobalIndexOfFirstChar();
                    final long end = start + TextEditorPanel.this.cursorTextLine.length();
                    TextEditorPanel.this.selection.setRange(start, end);
                    repaint();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // Nothing
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                TextLinePosition pos = getPositionFrom(e.getX(), e.getY());
                if (pos == null) {
                    return;
                }
                final long newLocation = pos.line.getGlobalIndexOfFirstChar() + pos.indexInLine;
                final long initIndex = TextEditorPanel.this.selection.getInitIndex();
                if (newLocation == initIndex) {
                    TextEditorPanel.this.selection.setRange(newLocation, newLocation);
                } else if (newLocation > initIndex) {
                    TextEditorPanel.this.selection.setRange(initIndex, newLocation);
                } else {
                    TextEditorPanel.this.selection.setRange(newLocation, initIndex);
                }

                setCursorLocation(newLocation);
                TextEditorPanel.this.blink = 1;
                fireCursorMoved();
                repaint(0);

            }
        });
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (TextEditorPanel.this.doc != null) {
                    computeVisibleLines(true);
                    fireViewMoved();
                }
                repaint();
            }

        });
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT || e.getKeyCode() == KeyEvent.VK_ALT_GRAPH || e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    return;
                }

                TextEditorPanel.this.blink = 1;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    long cursorIndex = getCursorGlobalIndex();
                    long newIndex = cursorIndex + 1;
                    if (TextEditorPanel.this.cursorIndexInLine == TextEditorPanel.this.cursorTextLine.length() && TextEditorPanel.this.cursorTextLine.isEndOfLine()
                            && TextEditorPanel.this.cursorTextLine.getLine().hasCarriageReturn()) {
                        newIndex++;
                    }
                    final long length = TextEditorPanel.this.doc.length();
                    if (newIndex > length) {
                        newIndex = length;
                    }

                    setCursorLocation(newIndex);
                    ensureCursorVisible();

                    if (e.isShiftDown()) {
                        TextEditorPanel.this.selection.setEndIndex(Math.max(newIndex, TextEditorPanel.this.selection.getEndIndex()));
                        if (newIndex == TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.init(newIndex);
                        } else if (newIndex < TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.setStartIndex(newIndex);
                        } else {
                            TextEditorPanel.this.selection.setStartIndex(TextEditorPanel.this.selection.getInitIndex());
                        }

                    } else {
                        // Clear text selection
                        TextEditorPanel.this.selection.init(newIndex);
                    }

                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {

                    if (TextEditorPanel.this.firstVisibleLineGlobalIndex >= 0) {
                        long cursorIndex = getCursorGlobalIndex();
                        long newIndex = cursorIndex - 1;
                        if (TextEditorPanel.this.cursorIndexInLine == 0) {
                            // go to previous line
                            Index index = TextEditorPanel.this.doc.getLineIndexAtCharIndex(newIndex);
                            Line line = TextEditorPanel.this.doc.getLine(index.getLineIndex());
                            if (line.hasCarriageReturn()) {
                                newIndex--;
                            }
                        }
                        if (newIndex < 0) {
                            newIndex = 0;
                        }

                        setCursorLocation(newIndex);
                        ensureCursorVisible();

                        if (e.isShiftDown()) {
                            if (newIndex == TextEditorPanel.this.selection.getInitIndex()) {
                                TextEditorPanel.this.selection.setStartIndex(newIndex);
                                TextEditorPanel.this.selection.setEndIndex(newIndex);
                            } else if (newIndex > TextEditorPanel.this.selection.getInitIndex()) {
                                TextEditorPanel.this.selection.setEndIndex(newIndex);
                            } else {
                                TextEditorPanel.this.selection.setEndIndex(TextEditorPanel.this.selection.getInitIndex());
                                TextEditorPanel.this.selection.setStartIndex(Math.min(newIndex, TextEditorPanel.this.selection.getStartIndex()));
                            }
                        } else {
                            // Clear text selection
                            TextEditorPanel.this.selection.init(newIndex);
                        }

                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    TextLine previous = getPreviousLine(TextEditorPanel.this.cursorTextLine);
                    if (previous == null) {
                        previous = createPreviousLine(TextEditorPanel.this.cursorTextLine);
                        System.out.println("TextEditorPanel.TextEditorPanel() previous of " + TextEditorPanel.this.cursorTextLine + " is " + previous);

                    }
                    long newIndex = 0;
                    if (previous != null) {
                        newIndex = previous.getGlobalIndexOfFirstChar() + Math.min(TextEditorPanel.this.cursorIndexInLine, previous.length());
                    }
                    setCursorLocation(newIndex);
                    ensureCursorVisible();
                    if (e.isShiftDown()) {
                        TextEditorPanel.this.selection.setStartIndex(Math.min(newIndex, TextEditorPanel.this.selection.getStartIndex()));
                        if (newIndex == TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.init(newIndex);
                        } else if (newIndex > TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.setEndIndex(newIndex);
                        } else {
                            TextEditorPanel.this.selection.setEndIndex(TextEditorPanel.this.selection.getInitIndex());
                        }

                    } else {
                        // Clear text selection
                        TextEditorPanel.this.selection.init(newIndex);

                    }

                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    TextLine next = getNextLine(TextEditorPanel.this.cursorTextLine);
                    if (lastTextLineIsNotFullyVisible() && TextEditorPanel.this.lines.get(TextEditorPanel.this.lines.size() - 1) == next) {
                        next = null;
                    }
                    if (next == null) {
                        // Down pressed, no next
                        TextEditorPanel.this.lastComputedIndex = -1;
                        if (TextEditorPanel.this.lines.size() == 1) {
                            return;
                        }

                        setCurrentIndex(TextEditorPanel.this.lines.get(1).getGlobalIndexOfFirstChar());
                        if (lastTextLineIsNotFullyVisible()) {
                            next = TextEditorPanel.this.lines.get(TextEditorPanel.this.lines.size() - 2);
                        } else {
                            next = TextEditorPanel.this.lines.get(TextEditorPanel.this.lines.size() - 1);
                        }
                    }

                    final long newIndex = next.getGlobalIndexOfFirstChar() + Math.min(TextEditorPanel.this.cursorIndexInLine, next.length());
                    setCursorLocation(newIndex);
                    ensureCursorVisible();

                    if (e.isShiftDown()) {
                        TextEditorPanel.this.selection.setEndIndex(Math.max(newIndex, TextEditorPanel.this.selection.getEndIndex()));
                        if (newIndex == TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.init(newIndex);
                        } else if (newIndex < TextEditorPanel.this.selection.getInitIndex()) {
                            TextEditorPanel.this.selection.setStartIndex(newIndex);
                        } else {
                            TextEditorPanel.this.selection.setStartIndex(TextEditorPanel.this.selection.getInitIndex());
                        }
                    } else {
                        // Clear text selection
                        TextEditorPanel.this.selection.init(newIndex);
                    }

                } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
                    TextEditorPanel.this.cursorIndexInLine = 0;
                    if (e.isControlDown()) {
                        // Move cursor to start of the document
                        setCursorLocation(0);
                        setCurrentIndex(0);
                        TextEditorPanel.this.cursorTextLine = TextEditorPanel.this.lines.get(0);
                    } else {
                        // Move cursor to start of line
                        Line currentLine = TextEditorPanel.this.cursorTextLine.getLine();
                        long newIndex = TextEditorPanel.this.doc.getGlobalIndex(currentLine);
                        setCursorLocation(newIndex);
                        setCurrentIndex(newIndex);
                        ensureCursorVisible();
                    }

                } else if (e.getKeyCode() == KeyEvent.VK_END) {
                    TextEditorPanel.this.cursorIndexInLine = TextEditorPanel.this.cursorTextLine.length();
                    if (e.isControlDown()) {
                        // Move cursor to the end of the document

                        final long lastIndex = TextEditorPanel.this.doc.length();
                        setCursorLocation(lastIndex);
                        setCurrentIndex(lastIndex);
                        ensureCursorVisible();
                        TextEditorPanel.this.cursorTextLine = TextEditorPanel.this.lines.get(TextEditorPanel.this.lines.size() - 1);
                    } else {
                        // Move cursor to end of line
                        Line currentLine = TextEditorPanel.this.cursorTextLine.getLine();
                        long newIndex = TextEditorPanel.this.doc.getGlobalIndex(currentLine) + currentLine.length();
                        setCursorLocation(newIndex);
                        setCurrentIndex(newIndex);
                        ensureCursorVisible();
                    }

                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                    TextEditorPanel.this.cursorIndexInLine = TextEditorPanel.this.cursorTextLine.length();
                    long newIndex = getCursorGlobalIndex() - getNumberOfFullyVisibleLines() * TextEditorPanel.this.maxCharactersPerLine;
                    if (newIndex < 0) {
                        newIndex = 0;
                    }
                    setCursorLocation(newIndex);
                    ensureCursorVisible();
                    TextEditorPanel.this.cursorIndexInLine = 0;

                    TextEditorPanel.this.blink = 1;
                } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                    long newIndex = getCursorGlobalIndex() + getNumberOfFullyVisibleLines() * TextEditorPanel.this.maxCharactersPerLine;
                    final long lastIndex = TextEditorPanel.this.doc.length();
                    if (newIndex > lastIndex) {
                        newIndex = lastIndex;
                    }
                    setCursorLocation(newIndex);
                    setCurrentIndex(lastIndex);
                    ensureCursorVisible();
                    TextEditorPanel.this.cursorIndexInLine = 0;

                    TextEditorPanel.this.blink = 1;
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    long cursorIndex = getCursorGlobalIndex();
                    if (!getSelection().isEmpty()) {
                        long newIndex = getSelection().getStartIndex();
                        TextEditorPanel.this.doc.delete(getSelection().getStartIndex(), getSelection().getEndIndex());
                        setCursorLocation(newIndex);
                        TextEditorPanel.this.selection.init(newIndex);
                        ensureCursorVisible();
                        computeVisibleLines(true);
                        repaint();
                    } else if (cursorIndex < TextEditorPanel.this.doc.length()) {
                        TextEditorPanel.this.doc.delete(cursorIndex, cursorIndex + 1);
                        computeVisibleLines(true);
                        repaint();
                    }
                    fireTextModified();

                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    long cursorIndex = getCursorGlobalIndex();
                    if (!getSelection().isEmpty()) {
                        long newIndex = getSelection().getStartIndex();
                        TextEditorPanel.this.doc.delete(getSelection().getStartIndex(), getSelection().getEndIndex());
                        setCursorLocation(newIndex);
                        TextEditorPanel.this.selection.init(newIndex);
                        ensureCursorVisible();
                        computeVisibleLines(true);
                        repaint();
                    } else if (cursorIndex > 0) {
                        TextEditorPanel.this.doc.delete(cursorIndex - 1, cursorIndex);
                        computeVisibleLines(true);
                        setCursorLocation(cursorIndex - 1);
                        ensureCursorVisible();
                        repaint();
                    }
                    fireTextModified();
                } else {
                    final char keyChar = e.getKeyChar();
                    if (keyChar != KeyEvent.CHAR_UNDEFINED && !e.isControlDown()) {
                        if (!getSelection().isEmpty()) {
                            long newIndex = getSelection().getStartIndex();
                            TextEditorPanel.this.doc.delete(getSelection().getStartIndex(), getSelection().getEndIndex());
                            setCursorLocation(newIndex);
                            TextEditorPanel.this.selection.init(newIndex);
                            ensureCursorVisible();

                        }
                        TextEditorPanel.this.doc.insert(getCursorGlobalIndex(), String.valueOf(keyChar));

                        final Index index = TextEditorPanel.this.doc.getLineIndexAtCharIndex(getCursorGlobalIndex() + 1);
                        if (index.getCharIndexInLine() > TextEditorPanel.this.doc.getLine(index.getLineIndex()).length()) {
                            setCursorLocation(getCursorGlobalIndex() + 2);
                        } else {
                            setCursorLocation(getCursorGlobalIndex() + 1);
                        }
                        ensureCursorVisible();

                        computeVisibleLines(true);
                        repaint();
                        fireTextModified();
                    }

                }
                TextEditorPanel.this.blink = 1;
                fireCursorMoved();
                repaint();

            }
        });
        requestFocus();

        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (hasFocus()) {
                    TextEditorPanel.this.blink = TextEditorPanel.this.blink + 1;
                    if (TextEditorPanel.this.blink > 1) {
                        TextEditorPanel.this.blink = 0;
                    }
                    int y = getTopMargin();
                    long cursorIndex = getCursorGlobalIndex();
                    TextLine t = getTextLineAtIndex(cursorIndex);
                    y += TextEditorPanel.this.lines.indexOf(t) * TextEditorPanel.this.lineHeight;
                    int x = getLeftMargin() + TextEditorPanel.this.cursorIndexInLine * TextEditorPanel.this.charWidth;
                    repaint(x, y, 4, TextEditorPanel.this.lineHeight);
                }

            }
        }, 0, 500);

    }

    protected TextLine createPreviousLine(TextLine line) {
        if (line.getGlobalIndexOfFirstChar() == 0) {
            return null;
        }
        final long currentIndex = line.getGlobalIndexOfFirstChar();
        long index = currentIndex;
        index = index - this.maxCharactersPerLine;
        if (index < 0) {
            index = 0;
        }
        final List<TextLine> tLines = this.doc.createTextLines(index, this.maxCharactersPerLine + 5, this.maxCharactersPerLine);
        for (int i = tLines.size() - 1; i >= 0; i--) {
            final TextLine textLine = tLines.get(i);
            System.out.println("TextEditorPanel.createPreviousLine()" + i + " : " + currentIndex + " : " + textLine);
            if (currentIndex >= textLine.getGlobalIndexOfFirstChar() && currentIndex <= textLine.getGlobalIndexOfFirstChar() + textLine.length()) {

                if (i > 0) {
                    return tLines.get(i - 1);
                } else {
                    return null;
                }
            }
        }
        for (int i = tLines.size() - 1; i >= 0; i--) {
            final TextLine textLine = tLines.get(i);
            System.err.println(textLine);

        }
        throw new IllegalStateException("unable to find previous at index " + currentIndex + " for " + line);
    }

    protected void ensureCursorVisible() {
        long min = this.lines.get(0).getGlobalIndexOfFirstChar();
        long max;
        final TextLine textLine;
        if (lastTextLineIsNotFullyVisible()) {
            textLine = this.lines.get(this.lines.size() - 2);
            max = textLine.getGlobalIndexOfFirstChar() + textLine.length();
        } else {
            textLine = this.lines.get(this.lines.size() - 1);
            max = textLine.getGlobalIndexOfFirstChar() + textLine.length();

        }
        if (!textLine.isEndOfLine()) {
            max--;
        }

        // System.out.println("TextEditorPanel.ensureCursorVisible() min:" + min + " max:" + max + "
        // cursor:" + getCursorGlobalIndex());
        if (getCursorGlobalIndex() < min) {
            // Need to scroll Up
            // First visible TextLine must contains cursor index
            int numberOfVisibleLines = getNumberOfFullyVisibleLines();
            if (lastTextLineIsNotFullyVisible()) {
                numberOfVisibleLines++;
            }
            List<TextLine> tLines = this.doc.createTextLines(getCursorGlobalIndex(), numberOfVisibleLines, this.maxCharactersPerLine);
            Line first = tLines.get(0).getLine();
            long delta = first.length() % this.maxCharactersPerLine;
            setFirstVisibleLineGlobalIndex(tLines.get(0).getGlobalIndexOfFirstChar() + tLines.get(0).length() - delta, false);

        } else if (getCursorGlobalIndex() > max) {
            // Need to scroll Down

            if (this.lines.size() > getNumberOfFullyVisibleLines() - 1) {
                TextLine line = this.lines.get(1);
                setFirstVisibleLineGlobalIndex(line.getGlobalIndexOfFirstChar(), false);
            }
        }
    }

    protected TextLine getTextLineAtIndex(long cursorIndex) {
        for (TextLine l : this.lines) {
            long max = l.getGlobalIndexOfFirstChar() + l.length();
            if (l.isEndOfLine()) {
                max++;
            }
            if (l.getGlobalIndexOfFirstChar() <= cursorIndex && cursorIndex < max) {
                return l;
            }
        }
        return null;
    }

    protected TextLine getPreviousLine(TextLine l) {
        TextLine previous = null;
        for (TextLine line : this.lines) {
            if (line == l) {
                return previous;
            }
            previous = line;
        }
        return previous;
    }

    protected TextLine getNextLine(TextLine l) {
        boolean next = false;
        for (TextLine line : this.lines) {
            if (next) {
                return line;
            }
            if (line == l) {
                next = true;
            }
        }
        return null;
    }

    protected void setCurrentIndex(long l) {
        if (l < 0) {
            l = 0;
        } else if (l >= this.doc.length()) {
            l = this.doc.length() - 1;
        }
        setFirstVisibleLineGlobalIndex(l, false);

    }

    void setDocument(Document doc) {
        // Reset state
        this.lastComputedIndex = -1;
        this.lastComputedNumberOfVisibleLines = -1;
        this.lastComputedNumberOfVisibleCharactersPerLine = -1;
        this.selection.init(0);

        this.cursorIndexInLine = 0;
        this.blink = 0;

        // Start with the new document
        this.doc = doc;
        this.setFirstVisibleLineGlobalIndex(0, true);

        this.cursorTextLine = this.lines.get(0);

        fireCursorMoved();
        repaint();
    }

    boolean lastTextLineIsNotFullyVisible() {
        final int fullyVisibleLines = getNumberOfFullyVisibleLines();
        return this.lines.size() > fullyVisibleLines;
    }

    public int getNumberOfFullyVisibleLines() {
        if (this.lineHeight <= 0) {
            return 1;
        }
        return (this.getHeight() - getTopMargin()) / this.lineHeight;
    }

    private void computeVisibleLines(boolean force) {

        int numberOfVisibleLines = getNumberOfFullyVisibleLines();
        if (lastTextLineIsNotFullyVisible()) {
            numberOfVisibleLines++;
        }
        this.maxCharactersPerLine = getWidth() / this.charWidth - 4;

        long index = Math.min(this.doc.length(), this.firstVisibleLineGlobalIndex);

        int maxLineNumber = this.doc.getLineIndexAtCharIndex(index).getLineIndex() + numberOfVisibleLines;
        final int margin = (String.valueOf(maxLineNumber).length() + 2) * this.charWidth;
        this.maxCharactersPerLine = (getWidth() - margin) / this.charWidth;
        setLeftMargin(margin);

        if (!force && this.lastComputedIndex == index && this.lastComputedNumberOfVisibleLines == numberOfVisibleLines
                && this.lastComputedNumberOfVisibleCharactersPerLine == this.maxCharactersPerLine) {
            return;
        }
        this.lines.clear();
        if (this.doc == null) {
            return;
        }

        List<TextLine> tLines = this.doc.createTextLines(index, numberOfVisibleLines, this.maxCharactersPerLine);
        final int stop = Math.min(tLines.size(), numberOfVisibleLines);
        for (int i = 0; i < stop; i++) {
            final TextLine line = tLines.get(i);
            this.lines.add(line);
        }

        updateCursorTextLine();

        this.lastComputedIndex = index;
        this.lastComputedNumberOfVisibleLines = numberOfVisibleLines;
        this.lastComputedNumberOfVisibleCharactersPerLine = this.maxCharactersPerLine;

    }

    public long getCursorGlobalIndex() {
        return this.cursorGlobalIndex;
    }

    public void setCursorLocation(long globalIndex) {
        this.cursorGlobalIndex = globalIndex;
        updateCursorTextLine();
        this.blink = 1;
        repaint();
    }

    public void updateCursorTextLine() {
        TextLine tLine = getTextLineAtIndex(this.cursorGlobalIndex);

        if (tLine != null) {
            this.cursorTextLine = tLine;
            this.cursorIndexInLine = (int) (this.cursorGlobalIndex - tLine.getGlobalIndexOfFirstChar());
        } else {
            this.cursorIndexInLine = 0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setFont(this.currentFont);
        if (this.lineHeight == 0) {
            FontMetrics fontMetrics = g.getFontMetrics(this.currentFont);
            // get the height of a line of text in this
            // font and render context
            this.lineHeight = fontMetrics.getHeight();
            this.charWidth = fontMetrics.charWidth(' ');
        }
        // Draw line numbers
        int maxLineIndex = 0;
        for (TextLine line : this.lines) {
            final int index = line.getLine().getLineIndex();
            if (index > maxLineIndex) {
                maxLineIndex = index;
            }
        }
        int maxLineIndexLenght = String.valueOf(maxLineIndex + 1).length();
        // Line number background
        g.setColor(BORDER_COLOR);
        final int lineNumberBackgroundWidth = (maxLineIndexLenght + 2) * this.charWidth;
        int offsetX = getLeftMargin();

        g.fillRect(0, 0, lineNumberBackgroundWidth, getHeight());
        // right background
        g.setColor(BORDER_COLOR);
        final int right = getLeftMargin() + (this.maxCharactersPerLine) * this.charWidth;
        setRightMargin(getWidth() - right);
        g.fillRect(right, 0, this.charWidth * 5, getHeight());

        // Hightlight current line
        int y = getTopMargin();
        int selectedLineY = -1;

        int minClipY = g.getClipBounds().y;
        int maxClipY = g.getClipBounds().y + g.getClipBounds().height;

        if (this.cursorTextLine != null) {
            for (TextLine line : this.lines) {
                // Use clip to reduce the number of lines to draw
                if (y + this.lineHeight <= minClipY) {
                    y += this.lineHeight;
                    continue;
                }
                if (y >= maxClipY) {
                    break;
                }

                if (line == this.cursorTextLine) {
                    selectedLineY = y;
                    g.setColor(BORDER_COLOR);
                    int w = offsetX + line.length() * this.charWidth;

                    g.fillRect(w, y, getWidth() - w, this.lineHeight);
                    g.setColor(TextEditorPanel.colorBlue);
                    g.fillRect(0, y, w, this.lineHeight);
                    g.setColor(TextEditorPanel.colorBlueDark);
                    g.fillRect(0, y, lineNumberBackgroundWidth, this.lineHeight);

                    break;
                }
                y += this.lineHeight;

            }

        }
        // Hightlighted texts (yellow background)
        drawHighlights(g);

        // Line number
        y = this.lineHeight;
        g.setColor(Color.BLACK);
        for (TextLine line : this.lines) {
            // Use clip to reduce the number of lines to draw
            if (y + this.lineHeight <= minClipY) {
                y += this.lineHeight;
                continue;
            }
            if (y >= maxClipY + this.lineHeight) {
                break;
            }

            String lineNumber = String.valueOf(line.getLine().getLineIndex() + 1);
            int x = this.charWidth * (1 + maxLineIndexLenght - lineNumber.length());
            g.drawString(lineNumber, x, y);
            y += this.lineHeight;

        }

        // Highlight
        y = getTopMargin();

        // Lines
        g.setColor(TextEditorPanel.TEXT_COLOR);

        if (this.selection == null || this.selection.isEmpty()) {
            // No selection

            for (TextLine line : this.lines) {
                // Use clip to reduce the number of lines to draw
                if (y + this.lineHeight <= minClipY) {
                    y += this.lineHeight;
                    continue;
                }
                if (y >= maxClipY) {
                    break;
                }

                String txt = line.getText();
                int x = offsetX;
                drawText(g, TextEditorPanel.TEXT_COLOR, txt, x, y);
                // Next
                y += this.lineHeight;

            }

        } else {
            // Selection
            for (TextLine line : this.lines) {
                // Use clip to reduce the number of lines to draw
                if (y + this.lineHeight <= minClipY) {
                    y += this.lineHeight;
                    continue;
                }
                if (y >= maxClipY) {
                    break;
                }
                // up to 3 parts : no selection, selection, no selection
                long firstChar = line.getGlobalIndexOfFirstChar();
                if (this.selection.getEndIndex() <= firstChar || this.selection.getStartIndex() > firstChar + line.length()) {
                    // selection is before or after
                    drawText(g, TextEditorPanel.TEXT_COLOR, line.getText(), offsetX, y);
                } else {
                    int x = offsetX;

                    // 1
                    long i1 = Math.max(0, this.selection.getStartIndex() - firstChar);
                    long i2 = Math.min(line.length(), this.selection.getEndIndex() - firstChar);
                    int x2 = x;
                    if (i1 > 0) {
                        String str1 = line.getText().substring(0, (int) i1);
                        drawText(g, TextEditorPanel.TEXT_COLOR, str1, x, y);
                        final int w1 = str1.length() * this.charWidth;
                        x2 += w1;
                    }
                    // 2
                    String str2 = line.getText().substring((int) i1, (int) i2);
                    int w2 = str2.length() * this.charWidth;
                    g.setColor(SELECTION_COLOR);
                    g.fillRect(x2, y, w2, this.lineHeight);

                    drawText(g, Color.WHITE, str2, x2, y);
                    int x3 = w2 + x2;

                    if (i2 < line.length()) {
                        String str3 = line.getText().substring((int) i2, line.length());
                        drawText(g, TextEditorPanel.TEXT_COLOR, str3, x3, y);
                    } else if (this.selection.getEndIndex() > line.getGlobalIndexOfFirstChar() + line.length()) {
                        String str3 = line.getText().substring((int) i2, line.length());
                        int w3 = str3.length() * this.charWidth;
                        int x4 = x3 + w3;
                        g.setColor(SELECTION_COLOR_DARK);
                        g.fillRect(x4, y, getWidth() - getRightMargin() - x4, this.lineHeight);
                    }

                }

                // Next
                y += this.lineHeight;

            }
        }
        // Cursor
        if (selectedLineY >= 0 && (this.mousePressed || this.blink == 1)) {
            g.setColor(TextEditorPanel.CURSOR_COLOR);
            int x = offsetX + this.cursorIndexInLine * this.charWidth;
            g.fillRect(x, selectedLineY, 2, this.lineHeight);

        }

    }

    private void drawHighlights(Graphics g) {
        int offsetX = getLeftMargin();
        int y = getTopMargin();
        final List<Highlight> all = new ArrayList<>(this.highlights);
        final List<Highlight> toRemove = new ArrayList<>(all.size());
        for (TextLine line : this.lines) {
            if (all.isEmpty()) {
                break;
            }
            toRemove.clear();

            for (Highlight h : all) {
                long first = line.getGlobalIndexOfFirstChar();
                long stop = first + line.length();
                if (h.getEndIndex() < first) {
                    toRemove.add(h);
                } else {
                    for (long index = first; index < stop; index++) {
                        if (h.contains(index)) {
                            int x = offsetX + (int) (index - first) * this.charWidth;
                            g.setColor(Color.YELLOW);
                            g.fillRect(x, y, this.charWidth, this.lineHeight);
                        }

                    }
                }
            }
            all.removeAll(toRemove);
            y += this.lineHeight;
        }
    }

    int tt = 0;
    static final char[] specialChars = new char[32];
    static final Color[] specialCharsColor = new Color[32];
    static final String[] specialCharsName = new String[33];
    static {
        specialCharsName[0] = "Null character";
        specialCharsName[1] = "Start of Heading";
        specialCharsName[2] = "Start of Text";
        specialCharsName[3] = "End of Text";
        specialCharsName[4] = "End of Transmission";
        specialCharsName[5] = "Enquiry";
        specialCharsName[6] = "Acknowledge";
        specialCharsName[7] = "Bell, Alert";
        specialCharsName[8] = "Backspace";
        specialCharsName[9] = "Horizontal Tab";
        specialCharsName[10] = "Line Feed";
        specialCharsName[11] = "Vertical Tabulation";
        specialCharsName[12] = "Form Feed";
        specialCharsName[13] = "Carriage Return";
        specialCharsName[14] = "Shift Out";
        specialCharsName[15] = "Shift In";
        specialCharsName[16] = "Data Link Escape";
        specialCharsName[17] = "Device Control One (XON)";
        specialCharsName[18] = "Device Control Two";
        specialCharsName[19] = "Device Control Three (XOFF)";
        specialCharsName[20] = "Device Control Four";
        specialCharsName[21] = "Negative Acknowledge";
        specialCharsName[22] = "Synchronous Idle";
        specialCharsName[23] = "End of Transmission Block";
        specialCharsName[24] = "Cancel";
        specialCharsName[25] = "End of medium";
        specialCharsName[26] = "Substitute";
        specialCharsName[27] = "Escape";
        specialCharsName[28] = "File Separator";
        specialCharsName[29] = "Group Separator";
        specialCharsName[30] = "Record Separator";
        specialCharsName[31] = "Unit Separator";
        specialCharsName[32] = "Space";

        for (int i = 0; i < 32; i++) {
            specialChars[i] = '\u00BA';
            specialCharsColor[i] = Color.ORANGE;
        }
        specialChars[0] = '·';
        specialCharsColor[0] = Color.RED;
        specialChars[1] = '<';
        specialCharsColor[1] = Color.RED;
        specialChars[2] = '<';
        specialCharsColor[2] = Color.ORANGE;

        specialChars[3] = '>';
        specialCharsColor[3] = Color.ORANGE;
        specialChars[4] = '>';
        specialCharsColor[4] = Color.RED;

        // Tab
        specialChars[9] = '―';
        specialCharsColor[9] = Color.ORANGE;

        specialChars[27] = '\u00BA';
        specialCharsColor[27] = Color.RED;
        specialChars[28] = '‡';
        specialCharsColor[28] = Color.RED;
        specialChars[29] = '‡';
        specialCharsColor[29] = Color.ORANGE;
        specialChars[30] = '‡';
        specialCharsColor[30] = Color.PINK;
        specialChars[31] = '‡';
        specialCharsColor[31] = Color.GREEN;
    }
    private Map<Color, BitmapCache> caches = new HashMap<>();

    private void drawText(Graphics g, Color defaultColor, String txt, int x, int y) {
        final int length = txt.length();
        final Rectangle clipBounds = g.getClipBounds();
        final int min = (int) clipBounds.getMinX();
        final int max = (int) clipBounds.getMaxX();
        for (int i = 0; i < length; i++) {
            char c = txt.charAt(i);
            if (x > max) {
                break;
            }

            if (x >= min && c != ' ') {
                Color color = defaultColor;
                if (c < 32) {
                    char newChar = specialChars[c];
                    if (specialCharsColor[c] != null) {
                        color = specialCharsColor[c];
                    }

                    c = newChar;
                }

                BitmapCache cache = this.caches.get(color);
                if (cache == null) {
                    cache = new BitmapCache(g.getFont(), color);
                    this.caches.put(color, cache);
                }
                g.drawImage(cache.getCharacterImage(c), x, y, null);

            }

            x += this.charWidth;
        }

    }

    private void setLeftMargin(int x) {
        this.leftMargin = x;
    }

    public int getLeftMargin() {
        return this.leftMargin;
    }

    private void setRightMargin(int x) {
        this.rightMargin = x;

    }

    public int getRightMargin() {
        return this.rightMargin;
    }

    public int getTopMargin() {
        return this.lineHeight / 4 + 1;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    public void addListener(TextEditorListener listener) {
        this.listeners.add(listener);
    }

    void fireCursorMoved() {
        for (TextEditorListener l : this.listeners) {
            l.cursorMoved();
        }
    }

    protected void fireTextModified() {
        for (TextEditorListener l : this.listeners) {
            l.textModified();
        }
    }

    public int getCurrentLineIndex() {
        return this.cursorTextLine.getLine().getLineIndex();
    }

    public int getTotalLineCount() {
        return this.doc.getLineCount();
    }

    public long getFirstVisibleLineGlobalIndex() {
        return this.firstVisibleLineGlobalIndex;
    }

    public void setFirstVisibleLineGlobalIndex(long firstVisibleLineGlobalIndex, boolean adjusting) {
        this.firstVisibleLineGlobalIndex = firstVisibleLineGlobalIndex;
        computeVisibleLines(true);
        if (!adjusting) {
            fireViewMoved();
        }
    }

    public void fireViewMoved() {
        for (TextEditorListener l : this.listeners) {
            l.viewMoved();
        }
    }

    public long getTotalCharacter() {
        return this.doc.length();
    }

    public long getCurrentColumnIndex() {
        return this.cursorIndexInLine + this.cursorTextLine.getIndexOfFirstChar();
    }

    public int getCurrentLineLength() {
        return this.cursorTextLine.length();
    }

    public TextLinePosition getPositionFrom(int x, int y) {
        final int m = x - getLeftMargin();
        if (m < 0) {
            return null;
        }

        final int lineIndex = (y - getTopMargin()) / this.lineHeight;
        if (lineIndex < 0 || lineIndex >= this.lines.size()) {
            return null;
        }

        final TextLine line = this.lines.get(lineIndex);
        final TextLinePosition result = new TextLinePosition();
        result.line = line;
        result.indexInLine = m / this.charWidth;
        result.indexInLine = Math.min(result.indexInLine, line.length());
        return result;
    }

    public String getSelectedText() {
        if (this.selection.isEmpty()) {
            return "";
        }
        return this.doc.getText(this.selection.getStartIndex(), this.selection.getEndIndex());
    }

    public void replace(long start, long end, String text) {
        this.doc.replace(start, end, text);
        computeVisibleLines(true);
    }

    public void replaceSelectedText(String text) {
        final long start = this.selection.getStartIndex();
        final long end = this.selection.getEndIndex();
        this.doc.replace(start, end, text);
        computeVisibleLines(true);
    }

    public void insert(long start, String text) {
        this.doc.insert(start, text);
        computeVisibleLines(true);
    }

    public Document getDocument() {
        return this.doc;
    }
    // Selection

    public String getSelectionInfo() {

        String txt = getSelectedText();
        if (txt.isEmpty()) {
            return "No selection";
        }
        if (txt.length() == 1) {
            char c = txt.charAt(0);
            if (c < 33) {
                return "Selection : " + specialCharsName[c] + " (0x" + String.format("%02x", (int) c) + ")";
            } else {
                return "Selection : " + c + " (0x" + String.format("%02x", (int) c) + ")";
            }
        }
        return txt.length() + " selected chars";
    }

    public Selection getSelection() {
        return this.selection;
    }

    // Hightlight
    public void setHighlights(List<Highlight> highlights) {
        this.highlights.clear();
        this.highlights.addAll(highlights);
        Collections.sort(highlights);
        repaint();
    }

    public List<TextLine> getVisibleTextLines() {
        return Collections.unmodifiableList(this.lines);
    }

    public TextLine getCursorTextLine() {
        return this.cursorTextLine;
    }

    public int getMaxCharactersPerLine() {
        return this.maxCharactersPerLine;
    }

    public void scrollToTexline(int textLineIndex) {
        long globalIndex = this.doc.getTextLineOffsetAtIndex(textLineIndex, getMaxCharactersPerLine());
        if (getFirstVisibleLineGlobalIndex() != globalIndex)
            this.setFirstVisibleLineGlobalIndex(globalIndex, true);

    }

    public void dispose() {
        this.timer.cancel();
    }
}
