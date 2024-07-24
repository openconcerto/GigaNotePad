package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openconcerto.editor.Document;

class FindText {

    @Test
    void findNext() {
        Document doc = new Document();
        final String str = "1234\nab\r\ntoto1234hello";
        doc.loadFrom(str, 5);
        Assertions.assertEquals(str.indexOf("1234", 0), doc.findNext(0, "1234"));
        Assertions.assertEquals(str.indexOf("1234", 1), doc.findNext(1, "1234"));
        Assertions.assertEquals(str.indexOf("toto", 1), doc.findNext(1, "toto"));
        Assertions.assertEquals(null, doc.findNext(1, "too"));
    }

}
