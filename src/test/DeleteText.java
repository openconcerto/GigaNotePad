package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;

class DeleteText {
    @Test
    void testDeleteOneLine() {
        Document doc = new Document();
        doc.loadFrom("123456", 40);
        doc.delete(0, 1);
        Assertions.assertEquals("23456", doc.getLine(0).getText());
    }

    @Test
    void testDeleteOneChar() {
        Document doc = new Document();
        doc.loadFrom("123456\nabcd\r\n9876", 4);
        doc.delete(0, 1);
        Assertions.assertEquals("23456", doc.getLine(0).getText());
    }

    @Test
    void testDeleteOneCharEndLine() {
        Document doc = new Document();
        doc.loadFrom("123456\nabcd\r\n9876", 4);
        doc.dump(System.out);
        doc.delete(6, 7);
        System.out.println("---------------------------------------");
        doc.dump(System.out);
        Assertions.assertEquals(2, doc.getLineCount());
        Assertions.assertEquals("123456abcd", doc.getLine(0).getText());
        Assertions.assertEquals(0, doc.getLine(0).getLineIndex());
        Assertions.assertEquals("9876", doc.getLine(1).getText());
        Assertions.assertEquals(1, doc.getLine(1).getLineIndex());
    }

    @Test
    void testDelete() {
        Document doc = new Document();
        doc.loadFrom("123456\nabcd\r\n9876", 4);
        doc.dump(System.out);
        doc.delete(1, 15);
        System.out.println("---------------------------------------");
        doc.dump(System.out);
        Assertions.assertEquals(1, doc.getLineCount());
        Assertions.assertEquals("176", doc.getLine(0).getText());
        Assertions.assertEquals(0, doc.getLine(0).getLineIndex());

    }

}
