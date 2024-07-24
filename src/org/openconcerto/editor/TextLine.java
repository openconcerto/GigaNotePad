package org.openconcerto.editor;

public class TextLine {
    // Index in the Line
    private final long indexOfFirstChar;
    // Index in the document
    private final long globalIndexOfFirstChar;
    private final Line line;
    private final String str;
    private boolean isEndOfLine;

    public TextLine(long globalIndexOfFirstChar, long indexOfFirstChar, Line line, String str, boolean isEndOfLine) {
        super();
        this.globalIndexOfFirstChar = globalIndexOfFirstChar;
        this.indexOfFirstChar = indexOfFirstChar;
        this.line = line;
        this.str = str;
        this.isEndOfLine = isEndOfLine;
    }

    public long getGlobalIndexOfFirstChar() {
        return this.globalIndexOfFirstChar;
    }

    public long getIndexOfFirstChar() {
        return this.indexOfFirstChar;
    }

    public Line getLine() {
        return this.line;
    }

    public String getText() {
        return this.str;
    }

    public boolean isEndOfLine() {
        return this.isEndOfLine;
    }

    public void setEndOfLine(boolean isEndOfLine) {
        this.isEndOfLine = isEndOfLine;
    }

    @Override
    public String toString() {
        return "TextLine [globalIndexOfFirstChar=" + this.globalIndexOfFirstChar + " indexOfFirstChar=" + this.indexOfFirstChar + ", str=" + this.str + ", isEndOfLine=" + this.isEndOfLine + "], line="
                + this.line + "";
    }

    public int length() {
        return this.str.length();

    }

}
