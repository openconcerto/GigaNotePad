package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;

class InsertText {

    private Document doc1;
    private Document doc2;
    private static final String STR1 = "123456abcd";
    private static final String STR2 = "123456abcd\nefghijk";

    @BeforeEach
    void setUp() {
        this.doc1 = new Document();
        this.doc1.loadFrom(STR1, 4);
        this.doc2 = new Document();
        this.doc2.loadFrom(STR2, 4);

    }

    @Test
    void testInsertNewLineBlank() {
        Document doc = new Document();
        doc.dump(System.err);
        doc.insert(0, "\n");
        doc.dump(System.out);
        Assertions.assertEquals(0, doc.getLine(0).length());
        Assertions.assertEquals(1, doc.getLine(0).lengthWithEOL());
        Assertions.assertEquals(0, doc.getLine(1).length());
        Assertions.assertEquals(true, doc.getLine(0).endsWithNewLine());
        Assertions.assertEquals(false, doc.getLine(1).endsWithNewLine());

    }

    @Test
    void testInsertNewLine() {
        Document doc = new Document();
        doc.loadFrom("12345", 13);
        doc.insert(0, "\n");
        doc.dump(System.out);
        Assertions.assertEquals(0, doc.getLine(0).length());
        Assertions.assertEquals(1, doc.getLine(0).lengthWithEOL());
        Assertions.assertEquals(true, doc.getLine(0).endsWithNewLine());
        Assertions.assertEquals(false, doc.getLine(1).endsWithNewLine());
        Assertions.assertEquals(5, doc.getLine(1).length());
        Assertions.assertEquals("12345", doc.getLine(1).getText());
    }

    @Test
    void testInsertNewLineA() {
        Document doc = new Document();
        doc.loadFrom("12345", 13);
        doc.insert(0, "a\n");
        doc.dump(System.out);
        Assertions.assertEquals(1, doc.getLine(0).length());
        Assertions.assertEquals(2, doc.getLine(0).lengthWithEOL());
        Assertions.assertEquals(true, doc.getLine(0).endsWithNewLine());
        Assertions.assertEquals(false, doc.getLine(1).endsWithNewLine());
        Assertions.assertEquals(1, doc.getLine(0).length());
        Assertions.assertEquals(5, doc.getLine(1).length());
        Assertions.assertEquals("a", doc.getLine(0).getText());
        Assertions.assertEquals("12345", doc.getLine(1).getText());
    }

    @Test
    void testInsertNewLineEnd() {
        Document doc = new Document();
        doc.loadFrom("12345", 10);
        doc.dump(System.err);
        doc.insert(5, "\n");
        doc.dump(System.out);
        Assertions.assertEquals(5, doc.getLine(0).length());
        Assertions.assertEquals(6, doc.getLine(0).lengthWithEOL());
        Assertions.assertEquals(2, doc.getLineCount());
    }

    @Test
    void testInsertNewLineEndDouble() {
        Document doc = new Document();
        doc.loadFrom("12345", 3);
        doc.insert(5, "\n\n");
        Assertions.assertEquals(5, doc.getLine(0).length());
        Assertions.assertEquals(6, doc.getLine(0).lengthWithEOL());
        Assertions.assertEquals(0, doc.getLine(1).length());
        Assertions.assertEquals(3, doc.getLineCount());
    }

    @Test
    void testInsertNewLines() {
        Document doc = new Document();
        doc.loadFrom("12345", 3);
        doc.insert(2, "a\nb\nc");
        // 12a\nb\nc345
        Assertions.assertEquals(3, doc.getLineCount());
        Assertions.assertEquals(3, doc.getLine(0).length());
        Assertions.assertEquals(1, doc.getLine(1).length());
        Assertions.assertEquals(4, doc.getLine(2).length());

    }

    @Test
    void testIndex() {
        int index = 0;
        for (int i = 0; i < 11; i++) {
            Assertions.assertEquals(0, this.doc2.getLineIndexAtCharIndex(i).getLineIndex());
            Assertions.assertEquals(index, this.doc2.getLineIndexAtCharIndex(i).getCharIndexInLine());
            index++;
        }
        index = 0;
        for (int i = 11; i < 18; i++) {
            Assertions.assertEquals(1, this.doc2.getLineIndexAtCharIndex(i).getLineIndex());
            Assertions.assertEquals(index, this.doc2.getLineIndexAtCharIndex(i).getCharIndexInLine());
            index++;
        }

    }

    @Test
    void testInsertBounds() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.doc1.insert(-1, "boom"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.doc1.insert(12, "boom"));
    }

    @Test
    void testInsertStart() {
        this.doc1.insert(0, "0");
        Assertions.assertEquals("0" + STR1, this.doc1.getText(0, this.doc1.length()));
    }

    @Test
    void testInsert() {
        this.doc1.insert(4, "zo");
        Assertions.assertEquals("1234zo56abcd", this.doc1.getText(0, this.doc1.length()));
    }

    @Test
    void testInsertBeforeLF() {
        this.doc2.insert(10, "zo");
        Assertions.assertEquals("123456abcdzo\nefghijk", this.doc2.getText(0, this.doc2.length()));
    }

    @Test
    void testInsertAfterLF() {
        this.doc2.insert(11, "zo");
        Assertions.assertEquals("123456abcd\nzoefghijk", this.doc2.getText(0, this.doc2.length()));
    }

    @Test
    void testInsertEnd() {
        long length = this.doc1.length();
        Assertions.assertEquals(10, length);
        System.out.println("InsertText.testInsertEnd()" + length);
        this.doc1.insert(length, "zo");
        Assertions.assertEquals("123456abcdzo", this.doc1.getText(0, this.doc1.length()));
    }
}
