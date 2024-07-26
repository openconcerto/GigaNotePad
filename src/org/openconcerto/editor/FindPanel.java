package org.openconcerto.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.JTextComponent;

public class FindPanel extends JPanel {
    private JComboBox<String> findTextField;

    private JComboBox<String> replaceTextField;
    private JButton bReplace;
    private JButton bReplaceAll;
    private JButton bFind;
    private TextEditorPanel editor;
    private JLabel labelStatus = new JLabel("");

    protected List<Long> lastSearchResult;

    FindPanel(TextEditorPanel editor, EditorFrame frame) {
        this.editor = editor;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // --- Line 1
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(2, 4, 2, 4);
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        // Find label + text (combo with history (16))
        JLabel labelFind = new JLabel("Find", SwingConstants.RIGHT);
        this.add(labelFind, c);

        this.findTextField = new JComboBox<>();
        Dimension d = this.findTextField.getPreferredSize();

        this.findTextField.setMinimumSize(new Dimension(d.width, d.height));
        this.findTextField.setPreferredSize(new Dimension(200, d.height));
        this.findTextField.setEditable(true);
        c.gridx++;
        this.add(this.findTextField, c);

        this.bFind = new JButton("Find");

        c.gridx++;
        this.add(this.bFind, c);

        c.gridx++;
        this.add(this.labelStatus, c);

        // Small close button
        c.gridx++;
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        final JButton closeButton = new JButton("");
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setMargin(new Insets(0, 1, 0, 1));
        closeButton.setIcon(new ImageIcon(getClass().getResource("close.png")));
        this.add(closeButton, c);

        // --- Line 2
        // Replace witch label+text (combo with history (16))
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.insets = new Insets(2, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JLabel("Replace with", SwingConstants.RIGHT), c);

        this.replaceTextField = new JComboBox<>();
        this.replaceTextField.setEditable(true);
        c.gridx++;
        this.add(this.replaceTextField, c);

        this.bReplace = new JButton("Replace");
        c.gridx++;
        this.add(this.bReplace, c);

        this.bReplaceAll = new JButton("Replace All");
        c.gridx++;
        this.add(this.bReplaceAll, c);

        // Listeners
        this.bFind.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doFind();
            }
        });

        this.findTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("comboBoxEdited".equals(e.getActionCommand())) {
                    doFind();
                }
            }
        });
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setFindPanelVisible(false);
            }
        });

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }

    @Override
    public void grabFocus() {
        this.findTextField.grabFocus();
    }

    public void doFind() {
        final String text = this.findTextField.getEditor().getItem().toString();
        System.err.println("FindPanel.doFind() " + text);
        long from = FindPanel.this.editor.getCursorGlobalIndex();
        this.labelStatus.setText("");
        //
        if (text.isEmpty()) {
            this.editor.setHighlights(new ArrayList<>(0));
            this.findTextField.grabFocus();
        } else {
            lockUI();
            SwingWorker<Long, Long> worker = new SwingWorker<Long, Long>() {

                @Override
                protected Long doInBackground() throws Exception {
                    final Document doc = FindPanel.this.editor.getDocument();
                    return doc.findNext(from, text);
                }

                @Override
                protected void done() {
                    try {
                        Long result = get();
                        if (result != null) {
                            System.out.println("FindPanel.doFind().new SwingWorker() {...}.done()" + result);
                            FindPanel.this.editor.setCursorLocation(result + text.length());
                            FindPanel.this.editor.getSelection().setRange(result, result + text.length());
                            FindPanel.this.editor.ensureCursorVisible();
                            FindPanel.this.editor.fireCursorMoved();

                        } else {
                            FindPanel.this.editor.getSelection().init(FindPanel.this.editor.getCursorGlobalIndex());
                            FindPanel.this.labelStatus.setText("Not found");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    // Unlock l'UI
                    unlockUI();

                    FindPanel.this.editor.updateHighLights(text);
                    FindPanel.this.findTextField.grabFocus();
                }

            };
            worker.execute();
        }
    }

    public void lockUI() {
        this.findTextField.setEnabled(false);

        this.replaceTextField.setEnabled(false);
        this.bReplace.setEnabled(false);
        this.bReplaceAll.setEnabled(false);
        this.bFind.setText("Stop");
    }

    public void unlockUI() {
        this.findTextField.setEnabled(true);

        this.replaceTextField.setEnabled(true);
        this.bReplace.setEnabled(true);
        this.bReplaceAll.setEnabled(true);
        this.bFind.setText("Find");
    }

    void selectSearchText() {
        JTextComponent editorComponent = (JTextComponent) this.findTextField.getEditor().getEditorComponent();
        System.out.println("FindPanel.grabFocus()" + editorComponent);
        editorComponent.selectAll();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                JFrame frame = new JFrame("Test");
                frame.setContentPane(new FindPanel(null, null));
                frame.pack();
                frame.setLocation(200, 200);
                frame.setVisible(true);

            }
        });

    }

}
