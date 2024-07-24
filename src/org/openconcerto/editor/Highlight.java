package org.openconcerto.editor;

public class Highlight implements Comparable<Highlight> {
    private final long startIndex;
    private final long endIndex;

    Highlight(long startIndex, long endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public long getStartIndex() {
        return this.startIndex;
    }

    public long getEndIndex() {
        return this.endIndex;
    }

    @Override
    public int compareTo(Highlight o) {
        // compare by start index
        return Long.compare(this.startIndex, o.startIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Highlight && this.startIndex == ((Highlight) obj).startIndex && this.endIndex == ((Highlight) obj).endIndex;
    }

    @Override
    public int hashCode() {
        return (int) (this.startIndex + 42 * this.endIndex);
    }

    public boolean contains(long index) {
        return this.startIndex <= index && index < this.endIndex;
    }

    public String toString() {
        return "Highlight[" + this.startIndex + ", " + this.endIndex + "]";
    }
}
