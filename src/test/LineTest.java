package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Line;

class LineTest {

    private Line line1;
    private Line line2;
    private Line line3;

    @BeforeEach
    void setUp() {
        this.line1 = new Line(Arrays.asList("abc"), 0);
        this.line2 = new Line(Arrays.asList("abc", "def", "ghi"), 0);
        this.line3 = new Line(Arrays.asList("abc", "def", "ghi"), 0);
        this.line3.setEndsWithNewLine(true);
    }

    @Test
    void testGetString() {
        List<String> parts = new ArrayList<>();
        parts.add("Hello");
        parts.add("World");
        Line line = new Line(parts, 0);

        Assertions.assertEquals("Hello", line.getString(0, 5));
        Assertions.assertEquals("HelloW", line.getString(0, 6));
        Assertions.assertEquals("elloWor", line.getString(1, 7));

        String all = "abcdefghi";
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                System.out.println("LineTest.testGetString() " + i + "-" + j);
                Assertions.assertEquals(all.substring(i, j), this.line2.getString(i, (j - i)));
            }
        }

    }

    @Test
    public void testCharAt() {
        List<String> parts = new ArrayList<>();
        parts.add("Hello");
        parts.add("World");
        Line line = new Line(parts, 0);

        Assertions.assertEquals('H', line.charAt(0));
        Assertions.assertEquals('o', line.charAt(4));
        Assertions.assertEquals('W', line.charAt(5));
        Assertions.assertEquals('d', line.charAt(9));
    }

    @Test
    public void testInsert() {
        List<String> parts = new ArrayList<>();
        parts.add("Hello");
        parts.add("World");
        Line line = new Line(parts, 0);

        line.insert(5, " ");
        Assertions.assertEquals("Hello World", line.getString(0, (int) line.length()));
        Assertions.assertEquals(11, line.length());
    }

    @Test
    public void testDelete() {
        List<String> parts = new ArrayList<>();
        parts.add("Hello");
        parts.add("World");
        Line line = new Line(parts, 0);

        line.delete(4, 5);
        Assertions.assertEquals(9, line.length());
        Assertions.assertEquals("HellWorld", line.getString(0, (int) line.length()));

    }

    @Test
    void testLength() {
        Assertions.assertEquals(3, this.line1.length());
        Assertions.assertEquals(9, this.line2.length());
        Assertions.assertEquals(9, this.line3.length());
    }

    @Test
    void testDeleteAll() {
        this.line2.delete(0, 9);
        Assertions.assertEquals(0, this.line2.length());
    }

    @Test
    void testDeleteSimpleFirst() {
        this.line2.delete(0, 1);
        Assertions.assertEquals(8, this.line2.length());
    }

    @Test
    void testDeleteEmpty() {
        Line line = new Line(Arrays.asList("", "def", "ghi"), 0);
        Assertions.assertEquals(6, line.length());
        line.delete(0, 1);
        Assertions.assertEquals(5, line.length());
        Assertions.assertEquals("efghi", line.getString(0, 5));
    }

    @Test
    void testDeleteFirst() {
        for (int i = 0; i < 8; i++) {
            this.line2.delete(0, 1);
            System.out.println("LineTest.testDeleteFirst()" + i + " : " + this.line2.length());
            Assertions.assertEquals(8L - i, this.line2.length());
        }
    }

    @Test
    public void testIndexOf() {
        List<String> parts = new ArrayList<>();
        parts.add("This is ");
        parts.add("a test test");
        parts.add(" line");
        Line line = new Line(parts, 0);

        // Test case where the text is present in the line
        String text = "test";
        int expectedIndex = 10; // Expected index of "test" in the line
        long actualIndex = line.indexOf(text, 0);
        Assertions.assertEquals(expectedIndex, actualIndex);
        actualIndex = line.indexOf(text, 11);
        Assertions.assertEquals(15, actualIndex);

        // Test case where the text is not present in the line
        String notPresentText = "example";
        int notExpectedIndex = -1;
        long actualNotPresentIndex = line.indexOf(notPresentText, 0);
        Assertions.assertEquals(notExpectedIndex, actualNotPresentIndex);

        // Test case where the text is present but spans multiple parts
        String multiPartText = "is a";
        int expectedMultiPartIndex = 5; // Expected index of "is a" in the line
        long actualMultiPartIndex = line.indexOf(multiPartText, 0);
        Assertions.assertEquals(expectedMultiPartIndex, actualMultiPartIndex);

        // Test case where the text is empty
        String emptyText = "";
        int expectedEmptyIndex = 0;
        long actualEmptyIndex = line.indexOf(emptyText, 0);
        Assertions.assertEquals(expectedEmptyIndex, actualEmptyIndex);
    }
}
