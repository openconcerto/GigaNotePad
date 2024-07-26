package cleanlaf;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.LazyInputMap;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.text.DefaultEditorKit;

public class CleanLF extends BasicLookAndFeel {

    public CleanLF() {
        //
    }

    @Override
    public String getDescription() {
        return "Clean Swing Look and Feel";
    }

    @Override
    public String getID() {
        return "cleanlaf";
    }

    @Override
    public String getName() {
        return "CleanLAF";
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return false;
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return true;
    }

    @Override
    protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        setupActions(table);
    }

    @Override
    protected void initClassDefaults(UIDefaults table) {
        super.initClassDefaults(table);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.border", BorderFactory.createLineBorder(new Color(173, 173, 173), 1));

        UIManager.put("Panel.background", new Color(240, 240, 240));
        UIManager.put("Frame.background", Color.WHITE);
        UIManager.put("Menu.background", new Color(242, 242, 242));
        UIManager.put("Menu.selectionBackground", new Color(145, 201, 247));
        UIManager.put("Menu.selectionForeground", Color.BLACK);
        UIManager.put("Menu.acceleratorSelectionForeground", Color.BLACK);
        UIManager.put("Menu.margin", new InsetsUIResource(3, 4, 3, 4));
        //
        UIManager.put("MenuItem.background", new Color(242, 242, 242));
        UIManager.put("MenuItem.foreground", Color.BLACK);
        UIManager.put("MenuItem.selectionBackground", new Color(145, 201, 247));
        UIManager.put("MenuItem.selectionForeground", Color.BLACK);
        UIManager.put("MenuItem.acceleratorSelectionForeground", Color.BLACK);
        UIManager.put("MenuItem.margin", new InsetsUIResource(3, 4, 3, 4));

        UIManager.put("CheckBoxMenuItem.background", new Color(242, 242, 242));
        UIManager.put("CheckBoxMenuItem.foreground", Color.BLACK);
        UIManager.put("CheckBoxMenuItem.selectionBackground", new Color(145, 201, 247));
        UIManager.put("CheckBoxMenuItem.selectionForeground", Color.BLACK);
        UIManager.put("CheckBoxMenuItem.acceleratorSelectionForeground", Color.BLACK);
        UIManager.put("CheckBoxMenuItem.margin", new InsetsUIResource(3, 4, 3, 4));

        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(new Color(173, 173, 173), 1));
        UIManager.put("Separator.foreground", new Color(215, 215, 215));
        UIManager.put("Viewport.background", new Color(240, 240, 240));

        UIManager.put("OptionPane.border", BorderFactory.createLineBorder(new Color(240, 240, 240), 10));
        UIManager.put("OptionPane.background", new Color(240, 240, 240));

        UIManager.put("ComboBox.buttonBackground", Color.WHITE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", new Color(145, 201, 247));
        UIManager.put("TextField.selectionBackground", new Color(0, 120, 215));

        //
        UIManager.put("ScrollBar.track", new Color(240, 240, 240));
        UIManager.put("ScrollBar.thumb", new Color(240, 240, 240));
        UIManager.put("ScrollBar.thumbHighlight", new Color(205, 205, 205));

        UIManager.put("ScrollBar.thumbDarkShadow", Color.DARK_GRAY);

        UIManager.put("ScrollBar.trackHighlight", new Color(240, 240, 240));
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(24, 24));
        // Custom UIS

        table.put("ButtonUI", CleanButtonUI.class.getCanonicalName());
        table.put("LabelUI", CleanLabelUI.class.getCanonicalName());
        table.put("TextFieldUI", CleanTextFieldUI.class.getCanonicalName());
        table.put("MenuBarUI", CleanMenuBarUI.class.getCanonicalName());
        table.put("MenuItemUI", CleanMenuItemUI.class.getCanonicalName());
        table.put("CheckBoxMenuItemUI", CleanCheckBoxMenuItemUI.class.getCanonicalName());
        table.put("MenuUI", CleanMenuUI.class.getCanonicalName());
        table.put("PopupMenuSeparatorUI", CleanPopupMenuSeparatorUI.class.getCanonicalName());
        table.put("ComboBoxUI", CleanComboBoxUI.class.getCanonicalName());
        table.put("ScrollBarUI", CleanScrollBarUI.class.getCanonicalName());

    }

    private static void setupActions(UIDefaults table) {
        // !! Should get actions from the native L&F for all map defaults
        Object fieldInputMap = new LazyInputMap(new Object[] { "ctrl C", DefaultEditorKit.copyAction, "ctrl V", DefaultEditorKit.pasteAction, "ctrl X", DefaultEditorKit.cutAction, "COPY",
                DefaultEditorKit.copyAction, "PASTE", DefaultEditorKit.pasteAction, "CUT", DefaultEditorKit.cutAction, "shift LEFT", DefaultEditorKit.selectionBackwardAction, "shift KP_LEFT",
                DefaultEditorKit.selectionBackwardAction, "shift RIGHT", DefaultEditorKit.selectionForwardAction, "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction, "ctrl LEFT",
                DefaultEditorKit.previousWordAction, "ctrl KP_LEFT", DefaultEditorKit.previousWordAction, "ctrl RIGHT", DefaultEditorKit.nextWordAction, "ctrl KP_RIGHT",
                DefaultEditorKit.nextWordAction, "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction, "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl A", DefaultEditorKit.selectAllAction, "HOME",
                DefaultEditorKit.beginLineAction, "END", DefaultEditorKit.endLineAction, "shift HOME", DefaultEditorKit.selectionBeginLineAction, "shift END", DefaultEditorKit.selectionEndLineAction,
                "typed \010", DefaultEditorKit.deletePrevCharAction, "DELETE", DefaultEditorKit.deleteNextCharAction, "RIGHT", DefaultEditorKit.forwardAction, "LEFT", DefaultEditorKit.backwardAction,
                "KP_RIGHT", DefaultEditorKit.forwardAction, "KP_LEFT", DefaultEditorKit.backwardAction, "ENTER", JTextField.notifyAction, "ctrl BACK_SLASH", "unselect"
                /* DefaultEditorKit.unselectAction */, "control shift O", "toggle-componentOrientation"
                /* DefaultEditorKit.toggleComponentOrientation */ });

        Object multilineInputMap = new LazyInputMap(new Object[] { "ctrl C", DefaultEditorKit.copyAction, "ctrl V", DefaultEditorKit.pasteAction, "ctrl X", DefaultEditorKit.cutAction, "COPY",
                DefaultEditorKit.copyAction, "PASTE", DefaultEditorKit.pasteAction, "CUT", DefaultEditorKit.cutAction, "shift LEFT", DefaultEditorKit.selectionBackwardAction, "shift KP_LEFT",
                DefaultEditorKit.selectionBackwardAction, "shift RIGHT", DefaultEditorKit.selectionForwardAction, "shift KP_RIGHT", DefaultEditorKit.selectionForwardAction, "ctrl LEFT",
                DefaultEditorKit.previousWordAction, "ctrl KP_LEFT", DefaultEditorKit.previousWordAction, "ctrl RIGHT", DefaultEditorKit.nextWordAction, "ctrl KP_RIGHT",
                DefaultEditorKit.nextWordAction, "ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction, "ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
                "ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction, "ctrl A", DefaultEditorKit.selectAllAction, "HOME",
                DefaultEditorKit.beginLineAction, "END", DefaultEditorKit.endLineAction, "shift HOME", DefaultEditorKit.selectionBeginLineAction, "shift END", DefaultEditorKit.selectionEndLineAction,

                "UP", DefaultEditorKit.upAction, "KP_UP", DefaultEditorKit.upAction, "DOWN", DefaultEditorKit.downAction, "KP_DOWN", DefaultEditorKit.downAction, "PAGE_UP",
                DefaultEditorKit.pageUpAction, "PAGE_DOWN", DefaultEditorKit.pageDownAction, "shift PAGE_UP", "selection-page-up", "shift PAGE_DOWN", "selection-page-down", "ctrl shift PAGE_UP",
                "selection-page-left", "ctrl shift PAGE_DOWN", "selection-page-right", "shift UP", DefaultEditorKit.selectionUpAction, "shift KP_UP", DefaultEditorKit.selectionUpAction, "shift DOWN",
                DefaultEditorKit.selectionDownAction, "shift KP_DOWN", DefaultEditorKit.selectionDownAction, "ENTER", DefaultEditorKit.insertBreakAction, "typed \010",
                DefaultEditorKit.deletePrevCharAction, "DELETE", DefaultEditorKit.deleteNextCharAction, "RIGHT", DefaultEditorKit.forwardAction, "LEFT", DefaultEditorKit.backwardAction, "KP_RIGHT",
                DefaultEditorKit.forwardAction, "KP_LEFT", DefaultEditorKit.backwardAction, "TAB", DefaultEditorKit.insertTabAction, "ctrl BACK_SLASH", "unselect"
                /* DefaultEditorKit.unselectAction */, "ctrl HOME", DefaultEditorKit.beginAction, "ctrl END", DefaultEditorKit.endAction, "ctrl shift HOME", DefaultEditorKit.selectionBeginAction,
                "ctrl shift END", DefaultEditorKit.selectionEndAction, "ctrl T", "next-link-action", "ctrl shift T", "previous-link-action", "ctrl SPACE", "activate-link-action", "control shift O",
                "toggle-componentOrientation"
                /* DefaultEditorKit.toggleComponentOrientation */ });

        Object[] actionDefaults = {
                // these are just copied from Metal L&F -- no values in Basic L&F
                // !! Should get input maps from the native L&F for all map defaults
                "TextField.focusInputMap", fieldInputMap, "PasswordField.focusInputMap", fieldInputMap, "TextArea.focusInputMap", multilineInputMap, "TextPane.focusInputMap", multilineInputMap,
                "EditorPane.focusInputMap", multilineInputMap, };

        table.putDefaults(actionDefaults);
    }
}
