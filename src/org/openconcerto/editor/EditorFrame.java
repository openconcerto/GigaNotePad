package org.openconcerto.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.apache.tika.parser.html.charsetdetector.PreScanner;
import org.openconcerto.editor.Document.LineSeparator;
import org.openconcerto.editor.actions.CopyAction;
import org.openconcerto.editor.actions.CutAction;
import org.openconcerto.editor.actions.PasteAction;
import org.openconcerto.editor.actions.UndoAbleAction;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class EditorFrame extends JFrame {

    private static final String LAST_PATH = "GigaNotePad-lastPath";
    private static final String APPNAME = "GigaNotePad";

    private JLabel labelFileName;
    private JLabel labelSelection = new JLabel();
    private JLabel labelCurrentLine = new JLabel();
    private JLabel labelCurrentColumn = new JLabel();
    private JLabel labelCurrentIndex = new JLabel();
    private TextEditorPanel editor;
    private File file;
    private Charset charset;
    private List<UndoAbleAction> history = new ArrayList<>();
    private int historyIndex;
    final JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL);
    final JMenuBar menubar = new JMenuBar();
    private FindPanel findPanel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean needSave = false;
    private LineSeparator lineSeparator = LineSeparator.AUTO;

    EditorFrame() {

        // Icons : 16x16, 32x32, 64x64 and 128x128
        final List<Image> icons = new ArrayList<>();
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon16.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon32.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon64.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon128.png")));
        this.setIconImages(icons);

        this.setBackground(Color.WHITE);

        this.labelFileName = new JLabel();
        this.editor = new TextEditorPanel();
        setBackground(Color.white);

        setLayout(new BorderLayout(0, 3));
        setTitle(APPNAME);
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BorderLayout());
        pCenter.add(this.editor, BorderLayout.CENTER);
        this.scrollbar.getModel().setRangeProperties(0, 0, 0, 0, false);
        pCenter.add(this.scrollbar, BorderLayout.EAST);
        add(pCenter, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.BOTH;
        this.findPanel = new FindPanel(this.editor, this);
        this.findPanel.setVisible(false);
        bottomPanel.add(this.findPanel, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy++;
        c.weightx = 1;

        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 5, 5, 2);
        bottomPanel.add(this.labelFileName, c);
        c.gridx++;
        c.weightx = 0;
        bottomPanel.add(this.labelSelection, c);
        c.gridx++;
        bottomPanel.add(this.labelCurrentIndex, c);
        c.gridx++;
        bottomPanel.add(this.labelCurrentLine, c);
        c.gridx++;
        bottomPanel.add(this.labelCurrentColumn, c);
        add(bottomPanel, BorderLayout.SOUTH);

        final JMenu mFile = new JMenu("File");
        final JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK));
        mFile.add(newItem);
        final JMenuItem openItem = new JMenuItem("Open File...");
        openItem.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        mFile.add(openItem);
        mFile.addSeparator();
        final JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
        mFile.add(saveItem);
        final JMenuItem saveAsItem = new JMenuItem("Save As...");
        mFile.add(saveAsItem);
        mFile.addSeparator();
        final JMenuItem menuItemOutputEncoding = new JMenu("Output Encoding");
        final JCheckBoxMenuItem utf8MenuItem = new JCheckBoxMenuItem("UTF-8");
        utf8MenuItem.setSelected(true);
        this.charset = StandardCharsets.UTF_8;
        menuItemOutputEncoding.add(utf8MenuItem);
        final JCheckBoxMenuItem utf16MenuItem = new JCheckBoxMenuItem("UTF-16");
        menuItemOutputEncoding.add(utf16MenuItem);
        final JCheckBoxMenuItem isoLatinMenuItem = new JCheckBoxMenuItem("ISO-8859-1");
        menuItemOutputEncoding.add(isoLatinMenuItem);
        final JCheckBoxMenuItem asciiMenuItem = new JCheckBoxMenuItem("US-ASCII");
        menuItemOutputEncoding.add(asciiMenuItem);
        mFile.add(menuItemOutputEncoding);

        final JMenuItem menuItemSeparator = new JMenu("Line Separator");
        final JCheckBoxMenuItem separatorAuto = new JCheckBoxMenuItem("Auto");
        separatorAuto.setSelected(true);
        menuItemSeparator.add(separatorAuto);
        final JCheckBoxMenuItem separatorLF = new JCheckBoxMenuItem("LF");
        menuItemSeparator.add(separatorLF);
        final JCheckBoxMenuItem separatorCRLF = new JCheckBoxMenuItem("CR LF");
        menuItemSeparator.add(separatorCRLF);
        mFile.add(menuItemSeparator);
        mFile.addSeparator();
        final JMenuItem menuItemExit = new JMenuItem("Exit");
        mFile.add(menuItemExit);
        this.menubar.add(mFile);

        final JMenu mEdit = new JMenu("Edit");
        final JMenuItem undoItem = new JMenuItem("Undo Typing");
        undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(undoItem);
        final JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(redoItem);
        mEdit.addSeparator();
        final JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(cutItem);
        final JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(copyItem);
        final JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(pasteItem);
        mEdit.addSeparator();
        final JMenuItem findItem = new JMenuItem("Find/Replace...");
        findItem.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
        mEdit.add(findItem);
        mEdit.addSeparator();
        final JMenuItem toUpperItem = new JMenuItem("To Uppercase");
        mEdit.add(toUpperItem);
        final JMenuItem toLowerItem = new JMenuItem("To Lowercase");
        mEdit.add(toLowerItem);
        this.menubar.add(mEdit);
        final JMenu mAbout = new JMenu("About");
        final JMenuItem aboutItem = new JMenuItem("About " + APPNAME);
        mAbout.add(aboutItem);

        this.menubar.add(mAbout);
        this.setJMenuBar(this.menubar);

        this.editor.addListener(new TextEditorListener() {

            @Override
            public void cursorMoved() {

                EditorFrame.this.labelCurrentLine.setText("Line : " + (EditorFrame.this.editor.getCurrentLineIndex() + 1) + "/" + EditorFrame.this.editor.getTotalLineCount());

                final StringBuilder textColumn = new StringBuilder();
                textColumn.append("Column : ");
                textColumn.append((EditorFrame.this.editor.getCurrentColumnIndex() + 1));
                textColumn.append("  Global index: ");
                textColumn.append(EditorFrame.this.editor.getCursorGlobalIndex());

                TextLine t = EditorFrame.this.editor.getCursorTextLine();
                Line line = t.getLine();
                if (line.endsWithNewLine()) {
                    if (line.hasCarriageReturn()) {
                        textColumn.append(" [CRLF]");
                    } else {
                        textColumn.append(" [LF]");
                    }

                }
                textColumn.append(" ");
                EditorFrame.this.labelCurrentColumn.setText(textColumn.toString());
                setSelectionInfo(EditorFrame.this.editor.getSelectionInfo());

                updateScrollbar();
            }

            @Override
            public void viewMoved() {
                updateScrollbar();

            }

            @Override
            public void textModified() {
                setNeedSave(true);
            }

        });
        newItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                showNewEditor(new Document());

            }
        });
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                String str = Preferences.userRoot().get(LAST_PATH, System.getProperty("user.home"));
                if (str != null && new File(str).exists()) {
                    fileChooser.setCurrentDirectory(new File(str));
                } else {
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                }
                int result = fileChooser.showOpenDialog(EditorFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    final File fileToLoad = fileChooser.getSelectedFile();
                    try {
                        if (EditorFrame.this.editor.getDocument().isEmpty() && EditorFrame.this.file == null) {
                            load(fileToLoad);
                        } else {
                            EditorFrame frame = showNewEditor(new Document());
                            frame.load(fileToLoad);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // TODO: handle exception
                        JOptionPane.showMessageDialog(EditorFrame.this, "Error while loading " + fileToLoad.getAbsolutePath() + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });
        saveItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (EditorFrame.this.file != null) {
                    save(EditorFrame.this.file);

                } else {
                    saveAs();
                }
            }
        });
        saveAsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });

        // Charsets
        utf8MenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.charset = StandardCharsets.UTF_8;
                utf16MenuItem.setSelected(false);
                isoLatinMenuItem.setSelected(false);
                asciiMenuItem.setSelected(false);
                setNeedSave(true);
            }
        });
        utf16MenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.charset = StandardCharsets.UTF_16;
                utf8MenuItem.setSelected(false);
                isoLatinMenuItem.setSelected(false);
                asciiMenuItem.setSelected(false);
                setNeedSave(true);
            }
        });
        isoLatinMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.charset = StandardCharsets.ISO_8859_1;
                utf8MenuItem.setSelected(false);
                utf16MenuItem.setSelected(false);
                asciiMenuItem.setSelected(false);
                setNeedSave(true);
            }
        });
        asciiMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.charset = StandardCharsets.US_ASCII;
                utf8MenuItem.setSelected(false);
                utf16MenuItem.setSelected(false);
                isoLatinMenuItem.setSelected(false);
                setNeedSave(true);
            }
        });

        separatorAuto.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.lineSeparator = LineSeparator.AUTO;
                separatorLF.setSelected(false);
                separatorCRLF.setSelected(false);
                setNeedSave(true);
            }
        });

        separatorLF.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.lineSeparator = LineSeparator.LF;
                separatorAuto.setSelected(false);
                separatorCRLF.setSelected(false);
                setNeedSave(true);
            }
        });

        separatorCRLF.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditorFrame.this.lineSeparator = LineSeparator.CRLF;
                separatorLF.setSelected(false);
                separatorAuto.setSelected(false);
                setNeedSave(true);
            }
        });

        // Cut / Copy / Paste
        cutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!EditorFrame.this.editor.getSelection().isEmpty()) {
                    final UndoAbleAction a = new CutAction(EditorFrame.this.editor);
                    addToHistory(a);
                    setNeedSave(true);
                }
            }
        });

        copyItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!EditorFrame.this.editor.getSelection().isEmpty()) {
                    final UndoAbleAction a = new CopyAction(EditorFrame.this.editor);
                    addToHistory(a);
                }
            }
        });

        pasteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                final UndoAbleAction a = new PasteAction(EditorFrame.this.editor);
                addToHistory(a);
                setNeedSave(true);
            }
        });

        findItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (EditorFrame.this.findPanel.isVisible()) {
                    EditorFrame.this.findPanel.doFind();
                } else {
                    setFindPanelVisible(true);
                }
            }
        });

        aboutItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(EditorFrame.this, APPNAME
                        + " is a simple text editor which can handle big files with very long lines.\n\nIn 2024: vi, vim, nano, emacs, vscode, notepad++... were not able to handle a 1 gibagyte file having only one line.\n\nI had to write one.\nSorry.",
                        APPNAME, JOptionPane.PLAIN_MESSAGE);

            }
        });
        toUpperItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = EditorFrame.this.editor.getSelectedText().toUpperCase();
                if (!text.isEmpty()) {
                    EditorFrame.this.editor.replaceSelectedText(text);
                    setNeedSave(true);
                }
            }
        });
        toLowerItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = EditorFrame.this.editor.getSelectedText().toLowerCase();
                if (!text.isEmpty()) {
                    EditorFrame.this.editor.replaceSelectedText(text);
                    setNeedSave(true);
                }
            }
        });

        menuItemExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                quit();
            }
        });

        this.scrollbar.addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (e.getValueIsAdjusting()) {
                    EditorFrame.this.editor.scrollToTexline(e.getValue());
                }

            }
        });
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }

        });
    }

    protected void quit() {
        if (!this.needSave) {
            destroy();
            dispose();
        } else {
            int answer = JOptionPane.showConfirmDialog(this, "Do you want to save this document before exiting ?", "Confirm save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                if (EditorFrame.this.file != null) {
                    save(EditorFrame.this.file);
                } else {
                    saveAs();
                }
            }

            if (answer != JOptionPane.CANCEL_OPTION) {
                EditorFrame.this.executorService.execute(new Runnable() {

                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                destroy();
                                dispose();
                            }
                        });

                    }

                });
            }

        }

    }

    public void destroy() {
        this.editor.dispose();
        this.executorService.shutdown();
    }

    public void save(File fileToSave) {
        setNeedSave(false);
        setState("Saving " + fileToSave.getAbsolutePath());
        setMenuEnabled(false);
        long t1 = System.currentTimeMillis();
        EditorFrame.this.executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    EditorFrame.this.editor.getDocument().save(fileToSave, EditorFrame.this.charset, EditorFrame.this.lineSeparator);
                    long t2 = System.currentTimeMillis();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setTitle(fileToSave.getName() + " - " + APPNAME);
                            setState(getFilePath() + "  (saved in " + (t2 - t1) + "ms)");
                            setMenuEnabled(true);
                        }
                    });
                    EditorFrame.this.file = fileToSave;
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setNeedSave(true);
                            setState("Error saving " + fileToSave.getAbsolutePath() + " : " + e.getMessage());
                            setMenuEnabled(true);
                        }
                    });
                    JOptionPane.showMessageDialog(EditorFrame.this, "Error while saving file :\n" + fileToSave.getAbsolutePath() + "\nError : " + e.getMessage());

                }

            }
        });
    }

    public void saveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        if (this.file != null) {
            fileChooser.setCurrentDirectory(this.file.getParentFile());
        } else {
            String str = Preferences.userRoot().get(LAST_PATH, System.getProperty("user.home"));
            if (str != null && new File(str).exists()) {
                fileChooser.setCurrentDirectory(new File(str));
            } else {
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            }
        }
        if (this.file != null) {
            fileChooser.setSelectedFile(new File(this.file.getName()));
        } else {
            fileChooser.setSelectedFile(new File("New document.txt"));
        }

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave.exists()) {
                int answer = JOptionPane.showConfirmDialog(fileChooser, fileToSave.getName() + " already exists.\nDo you want to replace it?", "Confirm save", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            save(fileToSave);
            Preferences.userRoot().put(LAST_PATH, fileToSave.getAbsolutePath());

        }

    }

    public void setFindPanelVisible(boolean b) {
        this.findPanel.setVisible(b);
        this.findPanel.grabFocus();
        doLayout();
    }

    protected void setMenuEnabled(boolean enable) {
        final int menuCount = this.menubar.getMenuCount();
        for (int i = 0; i < menuCount; i++) {
            this.menubar.getMenu(i).setEnabled(enable);
        }
    }

    protected void clearHistory() {
        this.historyIndex = 0;
        this.history.clear();
    }

    protected void addToHistory(UndoAbleAction a) {
        a.doAction();
        for (int i = this.history.size() - 1; i >= this.historyIndex; i--) {
            this.history.remove(i);
        }
        EditorFrame.this.history.add(a);
        this.historyIndex++;
    }

    protected void undo() {
        UndoAbleAction a = this.history.get(this.historyIndex);
        this.historyIndex--;
        a.undoAction();
        setNeedSave(true);
    }

    protected void redo() {
        UndoAbleAction a = this.history.get(this.historyIndex);
        this.historyIndex++;
        a.doAction();
        setNeedSave(true);
    }

    protected void setDocument(Document document) {
        this.editor.setDocument(document);
    }

    private void load(File fileToLoad) throws IOException {
        final long max = Integer.MAX_VALUE;
        if (!fileToLoad.exists()) {
            JOptionPane.showMessageDialog(this, "File not found :\n" + fileToLoad.getAbsolutePath(), "Missing file", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fileToLoad.length() > max) {
            JOptionPane.showMessageDialog(this, "The maximum size supported is 2 gigabytes.\nYour file is too big for this little editor.", "Limits are limits", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Charset detectedCharset = StandardCharsets.US_ASCII;

        // Read all bytes from the file into a byte array
        long t1 = System.currentTimeMillis();
        int skip = 0;
        try {

            try (final BufferedInputStream bIn = new BufferedInputStream(new FileInputStream(fileToLoad))) {
                byte[] bytes = bIn.readNBytes(10000);
                final String txt = new String(bytes, StandardCharsets.UTF_8);
                if (txt.substring(0, 100).toLowerCase().contains("encoding=\"utf-8\"")) {
                    detectedCharset = StandardCharsets.UTF_8;
                }
                if (detectedCharset == null) {
                    try (final ByteArrayInputStream bytesInputStream = new ByteArrayInputStream(bytes)) {
                        final PreScanner pScanner = new PreScanner(bytesInputStream);
                        detectedCharset = pScanner.detectBOM();
                        skip = pScanner.getBOMSize();
                        if (detectedCharset != null) {
                            System.out.println(detectedCharset.displayName() + " from BOM detected for " + fileToLoad.getAbsolutePath() + " " + pScanner.getBOMSize());
                        } else {
                            detectedCharset = pScanner.scan();
                            if (detectedCharset != null) {
                                System.out.println(detectedCharset.displayName() + " from metadata detected for " + fileToLoad.getAbsolutePath());
                            }
                        }
                    }
                }
            }
            long t2 = System.currentTimeMillis();
            if (detectedCharset == null) {
                final CharsetDetector detector = new CharsetDetector();
                final long toLoad = Math.min(fileToLoad.length(), 50000);
                final byte[] fileBytes = new byte[(int) (toLoad - skip)];

                try (FileInputStream is = new FileInputStream(fileToLoad)) {
                    if (skip > 0) {
                        is.read(new byte[skip]);
                    }
                    is.read(fileBytes);
                    detector.setText(fileBytes);

                    CharsetMatch charsetMatch = detector.detect();
                    final String name = charsetMatch.getName();
                    System.out.println(name + " detected for " + fileToLoad.getAbsolutePath());
                    detectedCharset = Charset.forName(name);
                }

            }
            long t3 = System.currentTimeMillis();
            System.out.println("EditorFrame.load() " + (t2 - t1) + "ms " + (t3 - t2) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO handle gz

        load(fileToLoad, skip, detectedCharset, 10 * 1024);

    }

    private void load(File file, int skip, Charset charset, int split) {

        this.charset = charset;

        setTitle(file.getName() + " - " + APPNAME);

        try {
            setState("Loading " + file.getCanonicalPath());
        } catch (IOException e) {
            setState("Loading " + file.getAbsolutePath());
        }
        setMenuEnabled(false);
        long t1 = System.currentTimeMillis();

        if (file.length() > 2000000) {
            // Preload start of the file, if > 2 MB
            this.executorService.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        final Document doc = new Document();
                        doc.preLoadFrom(file, skip, charset, split);

                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                EditorFrame.this.editor.setDocument(doc);
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();

                    }
                }
            });
        }

        this.executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    final Document doc = new Document();
                    doc.loadFrom(file, skip, charset, split);

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            EditorFrame.this.file = file;
                            clearHistory();
                            EditorFrame.this.editor.setDocument(doc);
                            long t2 = System.currentTimeMillis();
                            setState(getFilePath() + "  (loaded in " + (t2 - t1) + "ms)");
                            setMenuEnabled(true);

                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            setState("Error loading " + getFilePath());
                            JOptionPane.showMessageDialog(EditorFrame.this, "Error while loading file :\n" + file.getAbsolutePath() + "\nErreur: " + e.getMessage());
                            setMenuEnabled(true);
                        }
                    });
                }
            }
        });

    }

    private String getFilePath() {
        String canonicalPath;
        try {
            canonicalPath = this.file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = this.file.getAbsolutePath();
            e.printStackTrace();
        }
        return canonicalPath;
    }

    private void setState(String string) {
        this.labelFileName.setText(string);

    }

    private void setSelectionInfo(String string) {
        this.labelSelection.setText(string);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void updateScrollbar() {

        long total = this.editor.getDocument().getTextLineCount(this.editor.getMaxCharactersPerLine());
        long pos = this.editor.getDocument().getTextLineOffset(this.editor.getFirstVisibleLineGlobalIndex(), this.editor.getMaxCharactersPerLine());

        while (total > Integer.MAX_VALUE) {
            total = total / 1000;
            pos = pos / 1000;
        }

        this.scrollbar.setValues((int) pos, 1, 0, (int) total);

    }

    public void setNeedSave(boolean needSave) {
        this.needSave = needSave;
        if (needSave) {
            String title = this.getTitle();
            if (!title.startsWith("*")) {
                setTitle("*" + title);
            }
        } else {
            String title = this.getTitle();
            if (title.startsWith("*")) {
                setTitle(title.substring(1));
            }
        }

    }

    public EditorFrame showNewEditor(Document doc) {
        EditorFrame f = new EditorFrame();

        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        f.setMinimumSize(new Dimension(320, 200));
        f.setSize(800, 480);
        f.setLocation(EditorFrame.this.getLocationOnScreen().x + 10, EditorFrame.this.getLocationOnScreen().y + 10);

        f.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                f.setDocument(doc);
            }
        });
        return f;
    }

    public static void main(String[] args) {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }

                EditorFrame f = new EditorFrame();

                f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                f.setMinimumSize(new Dimension(320, 200));
                f.setSize(800, 480);
                f.setLocationRelativeTo(null);
                f.setVisible(true);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        f.setDocument(new Document());
                        if (args.length == 1) {
                            try {
                                f.load(new File(args[0]));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        });

    }
}
