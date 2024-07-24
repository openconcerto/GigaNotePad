package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;

class GetText {
    Document doc1;
    Document doc2;
    Document doc3;
    static final String STR1 = "123456abcde";
    static final String STR2 = "123456abcd\nefghijk";
    static final String STR3 = "123456abcd\nef\nghijk";
    static final String STR4 = "1234\nabcdef";

    @BeforeEach
    void setUp() {
        this.doc1 = new Document();
        this.doc1.loadFrom(STR1, 4);
        this.doc2 = new Document();
        this.doc2.loadFrom(STR2, 4);
        this.doc3 = new Document();
        this.doc3.loadFrom(STR3, 4);

    }

    @Test
    void testGetLineCount() {
        Assertions.assertEquals(1, this.doc1.getLineCount());
        Assertions.assertEquals(2, this.doc2.getLineCount());
        Assertions.assertEquals(3, this.doc3.getLineCount());
    }

    @Test
    void testLength() {
        Assertions.assertEquals(11, this.doc1.length());
        Assertions.assertEquals(STR1.length(), this.doc1.length());
        Assertions.assertEquals(STR2.length(), this.doc2.length());
        Assertions.assertEquals(STR3.length(), this.doc3.length());
    }

    @Test
    void testGetTextBig() {
        Document doc = new Document();
        doc.loadFrom(STR4, 100);
        Assertions.assertEquals("1234", doc.getText(0, 4));
        Assertions.assertEquals("234", doc.getText(1, 4));
        Assertions.assertEquals("23", doc.getText(1, 3));
        Assertions.assertEquals("bc", doc.getText(6, 8));
        Assertions.assertEquals("cd", doc.getText(7, 9));

    }

    @Test
    void testGetText() {
        Assertions.assertEquals(STR1.substring(0, 1), this.doc1.getText(0, 1));
        Assertions.assertEquals(STR1.substring(2, 10), this.doc1.getText(2, 10));
        Assertions.assertEquals(STR2.substring(0, 14), this.doc2.getText(0, 14));
        Assertions.assertEquals(STR2.substring(2, 10), this.doc2.getText(2, 10));
        // 3456abcd\nefg
        String substring = STR2.substring(2, 14);
        Assertions.assertEquals("3456abcd\nefg", substring);
        Assertions.assertEquals(substring, this.doc2.getText(2, 14));
        final String t3 = this.doc3.getText(0, 14);
        Assertions.assertEquals(STR3.substring(0, 14), t3);
        for (int i = 0; i < STR3.length(); i++) {
            final String t = this.doc3.getText(0, i);
            Assertions.assertEquals(STR3.substring(0, i), t);
        }
    }

}
