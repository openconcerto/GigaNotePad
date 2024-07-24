package test;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;
import org.openconcerto.editor.TextLine;

class DocumentOffset {

    @Test
    void testTextLineOffsetAtIndex() {
        Document doc = new Document();
        int split = 4;
        doc.loadFrom("123456\nabcd\r\n9876\r\nhelloworldforever", split);
        List<TextLine> tLines = doc.createTextLines(0, 1000, split);
        List<Long> ok = new ArrayList<>();
        for (int i = 0; i < tLines.size(); i++) {
            TextLine l = tLines.get(i);
            ok.add(l.getGlobalIndexOfFirstChar());
            System.out.println(l);
        }
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < tLines.size(); i++) {

            result.add(doc.getTextLineOffsetAtIndex(i, split));
        }
        System.out.println(ok);
        System.out.println(result);
        Assertions.assertEquals(ok, result);
    }

    @Test
    void testTextLineOffset() {
        Document doc = new Document();
        int split = 4;
        doc.loadFrom("123456\nabcd\r\n9876\r\nhelloworldforever", split);
        List<TextLine> tLines = doc.createTextLines(0, 1000, split);
        List<Long> ok = new ArrayList<>();
        for (int i = 0; i < tLines.size(); i++) {
            TextLine l = tLines.get(i);
            ok.add((long) i);
            System.out.println(l);
        }
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < tLines.size(); i++) {
            result.add(doc.getTextLineOffset(tLines.get(i).getGlobalIndexOfFirstChar(), split));
        }
        System.out.println("DocumentOffset.enclosing_method()" + ok);
        System.out.println("DocumentOffset.enclosing_method()" + result);
        Assertions.assertEquals(ok, result);
    }

}
