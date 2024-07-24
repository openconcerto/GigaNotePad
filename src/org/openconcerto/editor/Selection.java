package org.openconcerto.editor;

public class Selection {
    private long initIndex;
    private long startIndex;
    private long endIndex;

    Selection(long initIndex) {
        init(initIndex);
    }

    public void init(long initIndex) {
        this.initIndex = initIndex;
        this.startIndex = initIndex;
        this.endIndex = initIndex;

    }

    public long getInitIndex() {
        return this.initIndex;
    }

    public void setRange(long startIndex, long endIndex) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException("startIndex > endIndex (" + startIndex + " > " + endIndex + ")");
        }
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public void setStartIndex(long index) {
        this.startIndex = index;
    }

    public long getStartIndex() {
        return this.startIndex;
    }

    public void setEndIndex(long index) {
        this.endIndex = index;
    }

    public long getEndIndex() {
        return this.endIndex;
    }

    public boolean isEmpty() {
        return this.startIndex == this.endIndex;
    }

    @Override
    public String toString() {
        return "Selection [initIndex=" + this.initIndex + ", startIndex=" + this.startIndex + ", endIndex=" + this.endIndex + "]";
    }

}
