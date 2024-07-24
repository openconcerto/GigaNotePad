package org.openconcerto.editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Document {
    // Max 2,147,483,647 lines
    private final List<Line> lines = new ArrayList<>();
    static final int CHUNK_SIZE = 1 * 1024 * 1024;

    public enum LineSeparator {
        AUTO, LF, CRLF
    }

    public Document() {
        List<String> emptyParts = new ArrayList<>();
        emptyParts.add("");
        this.lines.add(new Line(emptyParts, 0));
    }

    public List<Line> getLines() {
        return this.lines;
    }

    public int getLineCount() {
        return this.lines.size();
    }

    public void preLoadFrom(File file, int skip, Charset charset, int max) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            if (skip > 0) {
                is.read(new byte[skip]);
            }
            byte[] a = new byte[500000];
            is.read(a);
            String start = new String(a, charset);
            loadFrom(start, max);
        }

    }

    // public void loadFrom(File file, Charset charset) throws IOException {
    // FIXME loadFrom(file, charset, CHUNK_SIZE);
    // }

    public void loadFrom(File file, int skip, Charset charset, int max) throws IOException {

        System.out.println("Document.loadFrom() " + file + "  from " + skip + " " + charset.displayName());
        if (skip == 0) {
            String all = new String(Files.readAllBytes(file.toPath()), charset);
            loadFrom(all, max);
        } else {

            try (BufferedInputStream b = new BufferedInputStream(new FileInputStream(file))) {
                int r = b.read(new byte[skip]);
                System.out.println("Document.loadFrom() read " + r);
                byte[] bytes = b.readNBytes((int) (file.length() - skip));
                String all = new String(bytes, charset);
                loadFrom(all, max);
            }
        }
    }

    public void loadFrom(String str, int maxPartSize) {
        this.lines.clear();
        long tt = System.currentTimeMillis();

        int size = str.length();
        List<String> parts = new ArrayList<>();
        StringBuilder b = new StringBuilder(maxPartSize);
        int counter = 0;
        boolean returnFound = false;
        for (int i = 0; i < size; i++) {

            char c = str.charAt(i);

            if (c == '\n') {
                if (b.length() > 0) {
                    parts.add(b.toString());
                }
                Line line = new Line(parts, this.lines.size());
                line.setEndsWithNewLine(true);
                line.setUseCarriageReturn(returnFound);
                this.lines.add(line);
                b.setLength(0);
                counter = 0;
                parts.clear();
                returnFound = false;
            } else if (c == '\r') {
                // nothing
                returnFound = true;
            } else {
                if (counter > maxPartSize) {
                    parts.add(b.toString());
                    b.setLength(0);
                    counter = 0;
                }
                b.append(c);

            }

            counter++;

        }
        if (b.length() > 0) {
            parts.add(b.toString());
        }
        Line line = new Line(parts, this.lines.size());
        this.lines.add(line);
        System.out.println("Document.loadFrom()" + (System.currentTimeMillis() - tt) + "ms");
    }

    public void dump(PrintStream out) {
        for (Line line : this.lines) {
            line.dump(out);
        }

    }

    /**
     * 
     * @param globalCharIndexStart
     * @param maxTextLine
     * @param split
     * @return a list of TextLine, the first TextLine contains the character at globalCharIndex
     *         index.
     */
    public List<TextLine> createTextLines(long globalCharIndexStart, int maxTextLine, int split) {
        Index index = getLineIndexAtCharIndex(globalCharIndexStart);
        int lineIndex = index.getLineIndex();
        final int lineCount = this.getLineCount();
        final List<TextLine> result = new ArrayList<>();
        int cCount = 0;
        long indexOfFirstChar = index.getCharIndexInLine();

        indexOfFirstChar = indexOfFirstChar - indexOfFirstChar % split;
        final Line firstLine = this.lines.get(lineIndex);
        long globalStartIndex = getGlobalIndex(firstLine) + indexOfFirstChar;
        for (int i = lineIndex; i < lineCount; i++) {
            Line line = this.lines.get(i);
            String str = line.getString(indexOfFirstChar, maxTextLine * split - (long) cCount);
            final List<TextLine> partsOfTheLine = createTextLines(globalStartIndex, indexOfFirstChar, line, str, split);

            result.addAll(partsOfTheLine);
            cCount += str.length();
            if (result.size() >= maxTextLine) {
                break;
            }
            globalStartIndex += line.lengthWithEOL() - indexOfFirstChar;
            indexOfFirstChar = 0;

        }
        return result;
    }

    public long getGlobalIndex(Line firstLine) {
        long index = 0;
        for (Line line : this.lines) {
            if (line == firstLine) {
                return index;
            }
            index += line.lengthWithEOL();
        }
        return -1;
    }

    private List<TextLine> createTextLines(long globalCharIndex, long indexOfFirstChar, Line line, String str, int split) {
        if (str.isEmpty()) {
            final TextLine l = new TextLine(globalCharIndex, indexOfFirstChar, line, "", true);
            return Arrays.asList(l);
        }
        final int length = str.length();
        final List<TextLine> r = new ArrayList<>(1 + length / split);

        long index = indexOfFirstChar;
        long index2 = globalCharIndex;
        for (int i = 0; i < length; i += split) {
            final String subString = str.substring(i, Math.min(str.length(), i + split));
            final TextLine l = new TextLine(index2, index, line, subString, false);
            r.add(l);
            final int lineLength = l.length();
            index += lineLength;
            index2 += lineLength;
        }
        if (!r.isEmpty()) {
            final TextLine lastPart = r.get(r.size() - 1);
            if (lastPart.getIndexOfFirstChar() + lastPart.length() == line.length()) {
                lastPart.setEndOfLine(true);
            }
        }
        return r;
    }

    public Index getLineIndexAtCharIndex(long charIndex) {
        long index = 0;
        final int lineCount = this.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            Line line = this.lines.get(i);
            long size = line.lengthWithEOL();

            index += size;
            if (charIndex < index) {
                return new Index(i, charIndex - (index - size));
            }
        }
        if (charIndex == length()) {
            return new Index(lineCount - 1, this.lines.get(lineCount - 1).lengthWithEOL());
        }
        throw new IllegalArgumentException("invalid character index : " + charIndex);
    }

    public long length() {
        long l = 0;
        for (Line line : this.lines) {
            l += line.lengthWithEOL();
        }
        return l;
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public char getCharAt(long index) {
        Index lineIndexAtCharIndex = getLineIndexAtCharIndex(index);
        int firstLineIndex = lineIndexAtCharIndex.getLineIndex();
        Line line = getLine(firstLineIndex);
        final long start = lineIndexAtCharIndex.getCharIndexInLine();
        return line.charAt(start);
    }

    public String getText(long selectionStart, long selectionEnd) {
        StringBuilder b = new StringBuilder();

        final int lineCount = this.getLineCount();

        Index lineIndexAtCharIndex = getLineIndexAtCharIndex(selectionStart);
        int firstLineIndex = lineIndexAtCharIndex.getLineIndex();
        Line line = getLine(firstLineIndex);

        long size = line.lengthWithEOL();
        // Start
        final long start = lineIndexAtCharIndex.getCharIndexInLine();
        long toRead = selectionEnd - selectionStart;
        long end = Math.min(size, start + toRead);
        int r = line.append(b, start, end);
        toRead -= r;
        for (int j = firstLineIndex + 1; j < lineCount; j++) {
            if (toRead > 0) {
                line = this.lines.get(j);
                size = line.lengthWithEOL();
                end = Math.min(size, toRead);
                r = line.append(b, 0, end);
                toRead -= r;
            } else {
                break;
            }
        }

        return b.toString();

    }

    public void replace(long start, long end, String text) {
        // delete
        delete(start, end);
        // insert
        insert(start, text);
    }

    public void delete(long selectionStart, long selectionEnd) {
        int lineCount = this.getLineCount();
        int firstLineIndex = getLineIndexAtCharIndex(selectionStart).getLineIndex();
        Line firstline = getLine(firstLineIndex);
        long lineGlobalIndex = getGlobalIndex(firstline);
        long size = firstline.lengthWithEOL();

        // Start
        final long start = selectionStart - lineGlobalIndex;
        long toRead = selectionEnd - selectionStart;
        long end = Math.min(size, start + toRead);
        int firstDeletedLineIndex = -1;
        firstline.delete(start, end);

        toRead -= (end - start);
        if (toRead <= 0) {
            // return;
        }

        int linesToRemove = 0;

        if (!firstline.endsWithNewLine()) {
            firstDeletedLineIndex = firstLineIndex;
            linesToRemove++;
        }

        Line line;
        for (int j = firstLineIndex + 1; j < lineCount; j++) {
            if (toRead > 0) {
                line = this.lines.get(j);
                size = line.lengthWithEOL();

                end = Math.min(size, toRead);
                line.delete(0, end);
                if (!line.endsWithNewLine() && j != lineCount - 1) {
                    if (firstDeletedLineIndex < 0) {
                        firstDeletedLineIndex = j;
                    }
                    linesToRemove++;
                }

                toRead -= end;
            } else {
                break;
            }
        }

        if (linesToRemove > 0) {
            final int endIndex = firstDeletedLineIndex + linesToRemove;
            for (int i = firstDeletedLineIndex; i < endIndex; i++) {
                Line l = this.lines.get(firstDeletedLineIndex);
                // Insert text to next line and remove
                final int nextIndex = firstDeletedLineIndex + 1;
                if (nextIndex < this.lines.size()) {
                    final Line nextLine = this.lines.get(nextIndex);
                    nextLine.insert(0, l.getText());
                    this.lines.remove(firstDeletedLineIndex);
                }
            }
            // Update line indexes
            lineCount = this.lines.size();
            for (int index = firstDeletedLineIndex; index < lineCount; index++) {
                Line l = this.lines.get(index);
                l.setLineIndex(index);
            }
        }
    }

    public void insert(long start, String text) {
        if (start < 0) {
            throw new IllegalArgumentException("start must be >=0");
        }
        if (start > this.length()) {
            throw new IllegalArgumentException("start must be <= " + this.length());
        }

        if (text.contains("\n")) {
            text = text.replace("\r\n", "\n");
            final Index index = getLineIndexAtCharIndex(start);

            Line line = getLine(index.getLineIndex());
            int firstIndex = line.getLineIndex();
            boolean end = line.endsWithNewLine();
            long indexInLine = index.getCharIndexInLine();
            String remainingText = line.getString(indexInLine, line.length() - indexInLine);
            if (line.length() > indexInLine) {
                line.delete(indexInLine, line.length());
            }
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    line.insert(indexInLine, b.toString());
                    line.setEndsWithNewLine(true);
                    b.setLength(0);

                    final ArrayList<String> parts = new ArrayList<>();
                    parts.add("");
                    Line newLine = new Line(parts, line.getLineIndex() + 1);
                    this.lines.add(line.getLineIndex() + 1, newLine);
                    line = newLine;
                    indexInLine = 0;

                } else {
                    b.append(c);
                }
            }
            if (b.length() > 0) {
                line.insert(indexInLine, b.toString());
            }
            if (line.endsWithNewLine()) {
                final ArrayList<String> parts = new ArrayList<>();
                parts.add("");
                Line newLine = new Line(parts, line.getLineIndex() + 1);
                this.lines.add(line.getLineIndex() + 1, newLine);
            }
            line.insert(line.length(), remainingText);
            line.setEndsWithNewLine(end);
            // Update line index
            for (int i = firstIndex; i < lines.size(); i++) {
                lines.get(i).setLineIndex(i);
            }

        } else {
            final Index index = getLineIndexAtCharIndex(start);
            // Insert in first line
            Line line = getLine(index.getLineIndex());
            line.insert(index.getCharIndexInLine(), text);
        }
    }

    public Line getLine(int lineIndex) {
        return this.lines.get(lineIndex);
    }

    public List<Long> find(String text) {
        long globalIndex = 0;
        final List<Long> result = new ArrayList<>();
        for (Line line : this.lines) {
            line.find(text, globalIndex, result);
            globalIndex += line.lengthWithEOL();
        }
        return result;
    }

    public Long findNext(long from, String text) {
        System.out.println("Document.findNext() " + from + " : " + text);

        Index index = getLineIndexAtCharIndex(from);
        long globalIndex = getGlobalIndex(this.lines.get(index.getLineIndex()));
        long indexInLine = index.getCharIndexInLine();
        for (int i = index.getLineIndex(); i < this.getLineCount(); i++) {
            Line line = this.lines.get(i);

            long result = line.indexOf(text, indexInLine);
            if (result >= 0) {
                return result + globalIndex;
            }
            globalIndex += line.lengthWithEOL();

            indexInLine = 0;
        }
        System.out.println("Document.findNext() " + from + " : " + text + " not found");
        return null;

    }

    public long getTextLineCount(int split) {
        long count = 0;
        for (Line line : this.lines) {
            final long length = line.length();
            if (length < split) {
                count++;
            } else {
                long parts = length / split;
                count += parts;
                if (length % split != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public long getTextLineOffset(long globalIndex, int split) {
        long count = 0;
        long totalLenght = 0;
        for (Line line : this.lines) {
            final long length = line.length();
            totalLenght += line.lengthWithEOL();
            if (totalLenght > globalIndex) {
                long remaining = totalLenght - globalIndex - line.lengthWithEOL() + length;
                long parts = (length - remaining) / split;
                count += parts;
                return count;
            }
            if (length < split) {
                count++;
            } else {
                long parts = length / split;
                count += parts;
                if (length % split != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public long getTextLineOffsetAtIndex(int i, int split) {
        long count = 0;
        long totalLenght = 0;

        long currentCount = 0;
        long currentLength = 0;
        for (Line line : this.lines) {
            final long length = line.length();
            currentLength = totalLenght;
            totalLenght += line.lengthWithEOL();
            currentCount = count;
            if (length < split) {
                count++;
            } else {
                long parts = length / split;
                count += parts;
                if (length % split != 0) {
                    count++;
                }
            }

            if (count >= i) {
                break;
            }
        }

        if ((i - count) != 0) {
            totalLenght = currentLength + (i - currentCount) * split;

        }
        return totalLenght;
    }

    public void save(File file, Charset charset, LineSeparator lineSeparator) throws FileNotFoundException, IOException {
        if (charset == null) {
            throw new IllegalArgumentException("null charset");
        }
        try (BufferedOutputStream bOut = new BufferedOutputStream(new FileOutputStream(file))) {
            for (Line line : this.lines) {
                line.writeTo(bOut, charset, lineSeparator);
            }
        }

    }

}
