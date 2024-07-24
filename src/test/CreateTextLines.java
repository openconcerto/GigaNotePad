package test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;
import org.openconcerto.editor.TextLine;

class CreateTextLines {
    Document doc1;
    Document doc2;
    Document doc3;

    static final String STR1 = "123456abcde";
    static final String STR2 = "12345\nabcdefghijk";
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
    void testIndex() {
        Document doc = new Document();
        doc.loadFrom("1234\nab\r\ntoto", 5);
        int split = 3;
        List<TextLine> list0 = doc.createTextLines(0, 50, split);
        Assertions.assertEquals(0, list0.get(0).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(0, list0.get(0).getIndexOfFirstChar());
        Assertions.assertFalse(list0.get(0).isEndOfLine());

        Assertions.assertEquals(3, list0.get(1).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(3, list0.get(1).getIndexOfFirstChar());
        Assertions.assertTrue(list0.get(1).isEndOfLine());

        Assertions.assertEquals(5, list0.get(2).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(0, list0.get(2).getIndexOfFirstChar());
        Assertions.assertTrue(list0.get(2).isEndOfLine());

        Assertions.assertEquals(9, list0.get(3).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(0, list0.get(3).getIndexOfFirstChar());
        Assertions.assertFalse(list0.get(3).isEndOfLine());

        Assertions.assertEquals(12, list0.get(4).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(3, list0.get(4).getIndexOfFirstChar());
        Assertions.assertTrue(list0.get(4).isEndOfLine());

        for (TextLine textLine : list0) {
            System.out.println(textLine);
        }

        for (int i = 1; i < 3; i++) {
            List<TextLine> list = doc.createTextLines(i, 50, split);
            int s = list.size();
            for (int j = 0; j < s; j++) {
                TextLine t0 = list0.get(j);
                TextLine t = list.get(j);

                System.out.println(t0);
                System.out.println(t);
                Assertions.assertEquals(t.length(), t0.length());
                Assertions.assertEquals(t.getGlobalIndexOfFirstChar(), t0.getGlobalIndexOfFirstChar());
                long index0 = doc.getGlobalIndex(t0.getLine());
                long index1 = doc.getGlobalIndex(t.getLine());
                Assertions.assertEquals(index1, index0);

            }
        }
        for (int i = 3; i < 5; i++) {
            List<TextLine> list = doc.createTextLines(i, 50, split);
            int s = list.size();
            for (int j = 0; j < s; j++) {
                TextLine t0 = list0.get(j + 1);
                TextLine t = list.get(j);
                System.out.println("--");
                System.out.println(t0);
                System.out.println(t);
                Assertions.assertEquals(t.length(), t0.length());
                Assertions.assertEquals(t.getGlobalIndexOfFirstChar(), t0.getGlobalIndexOfFirstChar());
                long index0 = doc.getGlobalIndex(t0.getLine());
                long index1 = doc.getGlobalIndex(t.getLine());
                Assertions.assertEquals(index1, index0);

            }
        }

    }

    @Test
    void testSize() {
        for (int i = 0; i < 10; i++) {
            List<TextLine> list = this.doc1.createTextLines(i, 10, 50);
            Assertions.assertEquals(1, list.size());
            TextLine l0 = list.get(0);
            System.out.println("GetParts.testSize()" + l0);
            Assertions.assertEquals(STR1.length(), l0.length());
        }
    }

    @Test
    void testSizeSmall() {
        List<TextLine> list = this.doc1.createTextLines(0, 50, 3);
        Assertions.assertEquals(4, list.size());
        list = this.doc1.createTextLines(0, 5, 3);
        Assertions.assertEquals(4, list.size());
        list = this.doc1.createTextLines(0, 4, 3);
        Assertions.assertEquals(4, list.size());
        list = this.doc1.createTextLines(0, 3, 3);
        Assertions.assertEquals(3, list.size());
        list = this.doc1.createTextLines(1, 4, 3);
        Assertions.assertEquals(4, list.size());

    }

    @Test
    void testSizeSmall2() {
        List<TextLine> list = this.doc2.createTextLines(7, 50, 3);
        Assertions.assertEquals(4, list.size());
        Assertions.assertEquals(6, list.get(0).getGlobalIndexOfFirstChar());
        Assertions.assertEquals(0, list.get(0).getIndexOfFirstChar());

    }
}
