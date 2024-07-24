package org.openconcerto.editor.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.openconcerto.editor.Selection;
import org.openconcerto.editor.TextEditorPanel;

public class PasteAction implements UndoAbleAction {
    private TextEditorPanel editor;
    private String previous = "";
    private long start;
    private long end;

    public PasteAction(TextEditorPanel editor) {
        this.editor = editor;
    }

    @Override
    public void doAction() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text = "";
        try {
            text = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        if (text == null) {
            text = "";
        }
        this.previous = this.editor.getSelectedText();
        final Selection selection = this.editor.getSelection();
        this.start = selection.getStartIndex();
        this.end = selection.getEndIndex();
        this.editor.replace(this.start, this.end, text);
    }

    @Override
    public void undoAction() {
        this.editor.replace(this.start, this.end, this.previous);
    }

    @Override
    public void reoAction() {
        this.editor.replace(this.start, this.end, this.previous);

    }

}
