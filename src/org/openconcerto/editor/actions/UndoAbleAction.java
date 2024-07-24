package org.openconcerto.editor.actions;

public interface UndoAbleAction {

    public void doAction();

    public void undoAction();

    public void reoAction();
}
