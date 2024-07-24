package org.openconcerto.editor;

public class Index {
    private final int lineIndex;
    private final long charIndexInLine;

    Index(int lineIndex, long charIndexInLine) {
        this.lineIndex = lineIndex;
        this.charIndexInLine = charIndexInLine;
    }

    public int getLineIndex() {
        return this.lineIndex;
    }

    public long getCharIndexInLine() {
        return this.charIndexInLine;
    }

    @Override
    public String toString() {
        return "[Line Index:" + getLineIndex() + " charIndexInLine:" + this.charIndexInLine + "]";
    }
}
