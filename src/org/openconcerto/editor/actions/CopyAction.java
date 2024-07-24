package org.openconcerto.editor.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.openconcerto.editor.TextEditorPanel;

public class CopyAction implements UndoAbleAction {
    private TextEditorPanel editor;

    public CopyAction(TextEditorPanel editor) {
        this.editor = editor;
    }

    @Override
    public void doAction() {
        StringSelection stringSelection = new StringSelection(this.editor.getSelectedText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

    }

    @Override
    public void undoAction() {
        // nothing
    }

    @Override
    public void reoAction() {
        // nothing
    }

}
