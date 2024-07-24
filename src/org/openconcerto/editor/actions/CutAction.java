package org.openconcerto.editor.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.openconcerto.editor.TextEditorPanel;

public class CutAction implements UndoAbleAction {
    private final TextEditorPanel editor;

    private long selectionStart;
    private long selectionEnd;
    private String selectedText;

    public CutAction(TextEditorPanel editor) {
        this.editor = editor;
    }

    @Override
    public void doAction() {
        this.selectedText = this.editor.getSelectedText();
        this.selectionStart = this.editor.getSelection().getStartIndex();
        this.selectionEnd = this.editor.getSelection().getEndIndex();
        // Remove selected text
        this.editor.replace(this.selectionStart, this.selectionEnd, "");
        // Store in clipboard
        StringSelection stringSelection = new StringSelection(this.selectedText);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    @Override
    public void undoAction() {
        this.editor.insert(this.selectionStart, this.selectedText);
    }

    @Override
    public void reoAction() {
        this.editor.replace(this.selectionStart, this.selectionEnd, "");
    }

}
