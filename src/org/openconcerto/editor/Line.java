package org.openconcerto.editor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.openconcerto.editor.Document.LineSeparator;

public class Line {
    private long length = 0;
    private long lengthWithEOL = 0;
    private int lineIndex;
    private final List<String> parts;
    private boolean carriageReturn;
    private boolean endsWithNewLine;

    public Line(List<String> parts, int lineIndex) {
        this.parts = new ArrayList<>(parts.size());
        this.parts.addAll(parts);
        for (String p : this.parts) {
            this.length += p.length();
        }
        this.lineIndex = lineIndex;
        computeLengthWithEOL();
    }

    public void dump(PrintStream out) {
        final int size = this.parts.size();
        out.println("--line (" + size + " parts) lineIndex: " + this.lineIndex + " length=" + this.length + ", index=" + this.lineIndex + " carriageReturn=" + this.carriageReturn + " endsWithNewLine:"
                + this.endsWithNewLine);
        for (int i = 0; i < size; i++) {
            out.print("part ");
            out.print(String.valueOf(i));
            out.print(":\'");
            out.print(this.parts.get(i));
            out.println('\'');
        }

    }

    public List<String> getParts() {
        return this.parts;
    }

    /**
     * Length (without \n or \r)
     * 
     * @return length
     */
    public long length() {
        return this.length;
    }

    /**
     * Length with EOL
     * 
     * @return length
     */
    public long lengthWithEOL() {
        return this.lengthWithEOL;
    }

    private void computeLengthWithEOL() {
        if (endsWithNewLine()) {
            if (hasCarriageReturn()) {
                this.lengthWithEOL = this.length + 2;
            } else {
                this.lengthWithEOL = this.length + 1;
            }
        } else {
            this.lengthWithEOL = this.length;
        }
    }

    public int getLineIndex() {
        return this.lineIndex;
    }

    public void setLineIndex(int index) {
        this.lineIndex = index;
    }

    public char charAt(long index) {
        if (hasCarriageReturn()) {
            if (index == this.length) {
                return '\r';
            } else if (index == this.length + 1) {
                return '\n';
            }
        } else {
            if (index == this.length) {
                return '\n';
            }
        }

        long i = 0;
        for (String s : this.parts) {
            i += s.length();

            if (index < i) {
                final int index2 = (int) (index - (i - s.length()));
                return s.charAt(index2);
            }
        }
        throw new IllegalArgumentException("index " + index + " is invalid, length is " + this.length);
    }

    public void setUseCarriageReturn(boolean b) {
        this.carriageReturn = b;
        computeLengthWithEOL();
    }

    public boolean hasCarriageReturn() {
        return this.carriageReturn;
    }

    public String getText() {
        return getString(0, this.length);
    }

    public String getString(long index, long maxLength) {
        StringBuilder b = new StringBuilder();
        long maxIndex = Math.min(this.length, maxLength + index);
        long partStartIndex = 0;
        long partEndIndex = 0;
        int partIndex = 0;

        // Find the starting part and starting index within that part
        final int size = this.parts.size();
        while (partIndex < size && partEndIndex <= index) {
            partStartIndex = partEndIndex;
            partEndIndex += this.parts.get(partIndex).length();
            partIndex++;
        }

        // Append characters from the parts to the StringBuilder
        for (long i = index; i < maxIndex; i++) {
            if (i >= partEndIndex) {
                if (partIndex >= size) {
                    break;
                }
                partStartIndex = partEndIndex;
                partEndIndex += this.parts.get(partIndex).length();
                partIndex++;
            }
            final int indexInPart = (int) (i - partStartIndex);
            b.append(this.parts.get(partIndex - 1).charAt(indexInPart));
        }
        return b.toString();
    }

    @Override
    public String toString() {
        String text;
        if (this.length > 100) {
            text = getString(0, 100) + " ...[truncated]";
        } else {
            text = getString(0, this.length);
        }
        String end = "EOF";
        if (this.endsWithNewLine) {
            if (this.carriageReturn) {
                end = "CRLF";
            } else {
                end = "LF";
            }
        }

        return "Line [index=" + this.lineIndex + " length=" + this.length + ", index=" + this.lineIndex + "(" + this.parts.size() + "parts) " + end + " ] " + text;
    }

    /**
     * Append characters from start to end index (in the line)
     * 
     * @param b the builder to append
     * @param start
     * @param end
     * @return the number of appended char
     */
    public int append(StringBuilder b, long start, long end) {

        if (start > end) {
            throw new IllegalArgumentException("start index is greater than the end offset (" + start + ">" + end + ")");
        }
        if (end - start > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range is too big");
        }

        if (end > (this.lengthWithEOL())) {
            throw new IllegalArgumentException("end index (" + end + ") greater than length with new line (" + (this.length + 1) + ")");
        }

        b.append(getString(start, (int) (end - start)));
        if (end > this.length) {
            b.append('\n');
        }

        return (int) (end - start);

    }

    public void insert(long indexInLine, String text) {
        if (text.isEmpty()) {
            return;
        }
        if (indexInLine == this.length) {
            // insert at the end
            int indexOfLast = this.parts.size() - 1;
            String s = this.parts.get(indexOfLast);
            s += text;
            this.parts.set(indexOfLast, s);
            this.length += text.length();
            computeLengthWithEOL();
            return;
        }

        long i = 0;

        int size = this.parts.size();
        for (int partIndex = 0; partIndex < size; partIndex++) {
            String s = this.parts.get(partIndex);
            i += s.length();
            if (indexInLine < i) {
                final int index2 = (int) (indexInLine - (i - s.length()));
                String before = s.substring(0, index2);
                String after = s.substring(index2);
                String newString = before + text + after;
                this.parts.set(partIndex, newString);
                this.length += text.length();
                computeLengthWithEOL();
                return;
            }
        }
    }

    public void setEndsWithNewLine(boolean b) {
        this.endsWithNewLine = b;
        computeLengthWithEOL();
    }

    public boolean endsWithNewLine() {
        return this.endsWithNewLine;
    }

    /**
     * Delete text between start and end index (index in line)
     * 
     * @param start
     * @param end
     * 
     */
    public void delete(long start, long end) {
        if (start < 0 || end < start || end > this.lengthWithEOL()) {
            throw new IllegalArgumentException("Invalid start or end index (" + start + ", " + end + "). Line length : " + this.length + " (" + this.lengthWithEOL() + " with EOL)");
        }
        // Check if line ends with a new line character and adjust accordingly
        if ((end == this.length + 1 || end == this.length + 2) && this.endsWithNewLine) {
            this.endsWithNewLine = false;
        }

        // Keep track of the current index in the line
        long currentIndex = 0;
        // Iterate through each part of the line
        final int size = this.parts.size();
        for (int i = 0; i < size; i++) {
            String part = this.parts.get(i);
            long partLength = part.length();

            // Check if the current part overlaps with the range to delete
            if (currentIndex < end && currentIndex + partLength > start) {
                // Calculate the start and end indices within this part
                int startIndexInPart = (int) Math.max(0, start - currentIndex);
                int endIndexInPart = (int) Math.min(partLength, end - currentIndex);

                // Delete the text from the current part
                this.parts.set(i, part.substring(0, startIndexInPart) + part.substring(endIndexInPart));

                // Update current index and length
                currentIndex += partLength;
                this.length -= (endIndexInPart - startIndexInPart);
                computeLengthWithEOL();
            } else {
                // Move the current index forward
                currentIndex += partLength;
            }
        }

    }

    /**
     * Returns the index within this string of the first occurrence of the specified character.
     * 
     * @param text string to search for
     * @param fromIndex the index to start the search from
     * @return the index of the first occurrence of the specified text, or -1 if there is no such
     *         occurrence
     */
    public long indexOf(String text, long fromIndex) {
        System.out.println("Line.indexOf()" + fromIndex);
        int stop = this.parts.size();
        int textLength = text.length();
        long index = 0;
        for (int i = 0; i < stop; i++) {
            String part = this.parts.get(i);
            if (index + part.length() < fromIndex) {
                index += part.length();
                continue;
            }
            int foundIndex = part.indexOf(text, (int) (fromIndex - index));
            if (foundIndex != -1) {

                return foundIndex + index;
            }
            // Maybe partially in this part
            if (i < stop - 1) {
                String nextPart = this.parts.get(i + 1);
                if (part.length() > textLength && nextPart.length() >= textLength - 1) {
                    final int start = part.length() - textLength;
                    String s = part.substring(start) + nextPart.substring(0, textLength - 1);
                    foundIndex = s.indexOf(text, (int) (fromIndex - index - start));
                    if (foundIndex != -1) {
                        return foundIndex + index + start;
                    }
                }
            }
            index += part.length();
        }
        return -1;

    }

    public void find(String text, long globalIndexOfLine, List<Long> result) {
        long foundIndex = indexOf(text, 0);
        while (foundIndex >= 0) {
            result.add(foundIndex + globalIndexOfLine);
            foundIndex = indexOf(text, foundIndex + text.length());
        }
    }

    public void writeTo(BufferedOutputStream bOut, Charset charset, LineSeparator separator) throws IOException {
        final int stop = this.parts.size();
        for (int i = 0; i < stop; i++) {
            bOut.write(this.parts.get(i).getBytes(charset));
        }
        if (this.endsWithNewLine) {
            if (separator == LineSeparator.AUTO) {
                if (this.carriageReturn) {
                    bOut.write('\r');
                }
                bOut.write('\n');
            } else if (separator == LineSeparator.LF) {
                bOut.write('\n');
            } else if (separator == LineSeparator.CRLF) {
                bOut.write('\r');
                bOut.write('\n');
            }
        }
    }

}
