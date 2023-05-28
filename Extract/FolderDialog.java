package extract;

/*
 * Copyright 2009 Volker Oth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Dialog to enter source and target paths for resource extraction.
 *
 * @author Volker Oth
 */
public class FolderDialog extends JDialog {
    /**
     * Preferred height of source text field in pixels.
     */
    private static final int SOURCE_TEXT_FIELD_HEIGHT = 19;
    /**
     * Preferred width of source text field in pixels.
     */
    private static final int SOURCE_TEXT_FIELD_WIDTH = 100;
    /**
     * Preferred height of target text field in pixels.
     */
    private static final int TARGET_TEXT_FIELD_HEIGHT = 19;
    /**
     * Preferred width of target text field in pixels.
     */
    private static final int TARGET_TEXT_FIELD_WIDTH = 100;
    /**
     * Grid y value for quit button.
     */
    private static final int QUIT_BUTTON_GRIDY = 7;
    /**
     * Grid y value for extract button.
     */
    private static final int EXTRACT_BUTTON_GRIDY = 7;
    /**
     * Top inset value for those elements not having a top inset of 0.
     */
    private static final int TOP_INSET = 20;
    /**
     * Grid y value for source label.
     */
    private static final int SOURCE_LABEL_GRIDY = 3;
    /**
     * Grid y value for target text field.
     */
    private static final int TARGET_TEXT_FIELD_GRIDY = 6;
    /**
     * Grid y value for source text field.
     */
    private static final int SOURCE_TEXT_FIELD_GRIDY = 4;
    /**
     * Grid y value for source button.
     */
    private static final int SOURCE_BUTTON_GRIDY = 4;
    /**
     * Grid y value for target button.
     */
    private static final int TARGET_BUTTON_GRIDY = 6;
    /**
     * Grid y value for target label.
     */
    private static final int TARGET_LABEL_GRIDY = 5;
    /**
     * Insets value.
     */
    private static final int INSET = 10;
    /**
     * Initial height.
     */
    private static final int INITIAL_HEIGHT = 208;
    /**
     * Initial width.
     */
    private static final int INITIAL_WIDTH = 457;
    /** Content pane. */
    private javax.swing.JPanel jContentPane = null;
    /** Target path label. */
    private JLabel jLabelTrg = null;
    /** Target path text field. */
    private JTextField jTextFieldTrg = null;
    /** Source label. */
    private JLabel jLabelSrc = null;
    /** Source text field. */
    private JTextField jTextFieldSrc = null;
    /** Source button. */
    private JButton jButtonSrc = null;
    /** Target path button. */
    private JButton jButtonTrg = null;
    /** Quit button. */
    private JButton jButtonQuit = null;
    /** Extract button. */
    private JButton jButtonExtract = null;
    /** Header label. */
    private JLabel jLabelHeader = null;

    // own stuff
    private static final long serialVersionUID = 0x01;

    /** target (Lemmini resource) path for extraction. */
    private String targetPath;
    /** source (WINLEMM) path for extraction. */
    private String sourcePath; // @jve:decl-index=0:
    /** self reference to this dialog. */
    private final JDialog thisDialog;
    /** flag that tells whether to extract or not. */
    private boolean doExtract;

    /**
     * Constructor for modal dialog in parent frame.
     *
     * @param frame parent frame
     * @param modal create modal dialog?
     */
    public FolderDialog(final JFrame frame, final boolean modal) {
        super(frame, modal);
        initialize();

        // own stuff
        thisDialog = this;
        doExtract = false;

        if (frame != null) {
            final Point p = frame.getLocation();
            this.setLocation(p.x + frame.getWidth() / 2 - getWidth() / 2,
                    p.y + frame.getHeight() / 2 - getHeight() / 2);
        } else {
            final GraphicsEnvironment ge = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            final Point p = ge.getCenterPoint();
            p.x -= this.getWidth() / 2;
            p.y -= this.getHeight() / 2;
            this.setLocation(p);
        }
    }

    /**
     * Set parameters for text edit boxes.
     *
     * @param srcPath source (WINLEMM) path for extraction
     * @param trgPath target (Lemmini resource) path for extraction
     */
    public void setParameters(final String srcPath, final String trgPath) {
        jTextFieldSrc.setText(srcPath);
        sourcePath = srcPath;
        jTextFieldTrg.setText(trgPath);
        targetPath = trgPath;
    }

    /**
     * Get target (Lemmini resource) path for extraction.
     *
     * @return target (Lemmini resource) path for extraction
     */
    public String getTarget() {
        if (targetPath != null) {
            return targetPath;
        } else {
            return "";
        }
    }

    /**
     * Get source (WINLEMM) path for extraction.
     *
     * @return source (WINLEMM) path for extraction
     */
    public String getSource() {
        if (sourcePath != null) {
            return sourcePath;
        } else {
            return "";
        }
    }

    /**
     * Get extraction selection status.
     *
     * @return true if extraction was chosen, false otherwise
     */
    public boolean getSuccess() {
        return doExtract;
    }

    /**
     * Initialize manually generated resources.
     */
    private void initialize() {
        this.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        this.setContentPane(getJContentPane());
        this.setTitle("Lemmini Resource Extractor");
    }

    /**
     * This method initializes jContentPane.
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            final GridBagConstraints gbLabelHeader = new GridBagConstraints();
            gbLabelHeader.gridx = 0;
            gbLabelHeader.gridwidth = 2;
            gbLabelHeader.anchor = GridBagConstraints.NORTHWEST;
            gbLabelHeader.insets = new Insets(INSET, INSET, INSET, 0);
            gbLabelHeader.gridy = 0;
            jLabelHeader = new JLabel();
            jLabelHeader
                    .setText("Extract the resources from Lemmings for Windows");
            final GridBagConstraints gridBagLabelTrg = new GridBagConstraints();
            gridBagLabelTrg.gridx = 0;
            gridBagLabelTrg.anchor = GridBagConstraints.WEST;
            gridBagLabelTrg.insets = new Insets(INSET, INSET, 0, 0);
            gridBagLabelTrg.gridy = TARGET_LABEL_GRIDY;
            final GridBagConstraints gbButtonTrg = new GridBagConstraints();
            gbButtonTrg.gridx = 1;
            gbButtonTrg.anchor = GridBagConstraints.EAST;
            gbButtonTrg.insets = new Insets(0, 0, 0, INSET);
            gbButtonTrg.gridy = TARGET_BUTTON_GRIDY;
            final GridBagConstraints gbButtonSrc = new GridBagConstraints();
            gbButtonSrc.gridx = 1;
            gbButtonSrc.anchor = GridBagConstraints.EAST;
            gbButtonSrc.insets = new Insets(0, 0, 0, INSET);
            gbButtonSrc.gridy = SOURCE_BUTTON_GRIDY;
            final GridBagConstraints gbTextFieldSrc = new GridBagConstraints();
            gbTextFieldSrc.fill = GridBagConstraints.BOTH;
            gbTextFieldSrc.gridy = SOURCE_TEXT_FIELD_GRIDY;
            gbTextFieldSrc.weightx = 1.0;
            gbTextFieldSrc.anchor = GridBagConstraints.WEST;
            gbTextFieldSrc.insets = new Insets(0, INSET, 0, INSET);
            gbTextFieldSrc.gridx = 0;
            final GridBagConstraints gbTextFieldTrg = new GridBagConstraints();
            gbTextFieldTrg.fill = GridBagConstraints.BOTH;
            gbTextFieldTrg.gridy = TARGET_TEXT_FIELD_GRIDY;
            gbTextFieldTrg.weightx = 1.0;
            gbTextFieldTrg.anchor = GridBagConstraints.WEST;
            gbTextFieldTrg.insets = new Insets(0, INSET, 0, INSET);
            gbTextFieldTrg.gridx = 0;
            final GridBagConstraints gridBagLabelSrc = new GridBagConstraints();
            gridBagLabelSrc.gridx = 0;
            gridBagLabelSrc.anchor = GridBagConstraints.WEST;
            gridBagLabelSrc.insets = new Insets(0, INSET, 0, 0);
            gridBagLabelSrc.gridy = SOURCE_LABEL_GRIDY;
            final GridBagConstraints gbButtonExtract = new GridBagConstraints();
            gbButtonExtract.gridx = 1;
            gbButtonExtract.insets = new Insets(TOP_INSET, 0, 0, INSET);
            gbButtonExtract.anchor = GridBagConstraints.EAST;
            gbButtonExtract.gridy = EXTRACT_BUTTON_GRIDY;
            final GridBagConstraints gbButtonQuit = new GridBagConstraints();
            gbButtonQuit.gridx = 0;
            gbButtonQuit.anchor = GridBagConstraints.WEST;
            gbButtonQuit.insets = new Insets(TOP_INSET, INSET, 0, 0);
            gbButtonQuit.gridy = QUIT_BUTTON_GRIDY;
            jLabelSrc = new JLabel();
            jLabelTrg = new JLabel();
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jLabelTrg.setText("Target Path");
            jLabelSrc.setText("Source Path (\"WINLEMM\" directory)");
            jLabelSrc.setComponentOrientation(
                    java.awt.ComponentOrientation.UNKNOWN);
            jContentPane.setComponentOrientation(
                    java.awt.ComponentOrientation.UNKNOWN);
            jContentPane.add(jLabelTrg, gridBagLabelTrg);
            jContentPane.add(getJTextFieldTrg(), gbTextFieldTrg);
            jContentPane.add(jLabelSrc, gridBagLabelSrc);
            jContentPane.add(getJTextFieldSrc(), gbTextFieldSrc);
            jContentPane.add(getJButtonSrc(), gbButtonSrc);
            jContentPane.add(getJButtonTrg(), gbButtonTrg);
            jContentPane.add(getJButtonQuit(), gbButtonQuit);
            jContentPane.add(getJButtonExtract(), gbButtonExtract);
            jContentPane.add(jLabelHeader, gbLabelHeader);
        }
        return jContentPane;
    }

    /**
     * This method initializes jTextFieldTrg.
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldTrg() {
        if (jTextFieldTrg == null) {
            jTextFieldTrg = new JTextField();
            jTextFieldTrg.setPreferredSize(new java.awt.Dimension(
                    TARGET_TEXT_FIELD_WIDTH, TARGET_TEXT_FIELD_HEIGHT));
            jTextFieldTrg
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            targetPath = jTextFieldTrg.getText();
                        }
                    });
        }

        return jTextFieldTrg;
    }

    /**
     * This method initializes jTextFieldSrc.
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldSrc() {
        if (jTextFieldSrc == null) {
            jTextFieldSrc = new JTextField();
            jTextFieldSrc.setPreferredSize(new java.awt.Dimension(
                    SOURCE_TEXT_FIELD_WIDTH, SOURCE_TEXT_FIELD_HEIGHT));
            jTextFieldSrc
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            sourcePath = jTextFieldSrc.getText();
                        }
                    });
        }
        return jTextFieldSrc;
    }

    /**
     * This method initializes jButtonSrc.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonSrc() {
        if (jButtonSrc == null) {
            jButtonSrc = new JButton();
            jButtonSrc.setText("Browse");
            jButtonSrc.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    final JFileChooser jf = new JFileChooser(sourcePath);
                    jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    final int returnVal = jf.showOpenDialog(thisDialog);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        sourcePath = jf.getSelectedFile().getAbsolutePath();
                        jTextFieldSrc.setText(sourcePath);
                    }
                }
            });
        }
        return jButtonSrc;
    }

    /**
     * This method initializes jButtonTrg.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonTrg() {
        if (jButtonTrg == null) {
            jButtonTrg = new JButton();
            jButtonTrg.setText("Browse");
            jButtonTrg.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    final JFileChooser jf = new JFileChooser(targetPath);
                    jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    final int returnVal = jf.showOpenDialog(thisDialog);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        targetPath = jf.getSelectedFile().getAbsolutePath();
                        jTextFieldTrg.setText(targetPath);
                    }
                }
            });
        }
        return jButtonTrg;
    }

    /**
     * This method initializes jButtonQuit.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonQuit() {
        if (jButtonQuit == null) {
            jButtonQuit = new JButton();
            jButtonQuit.setText("Quit");
            jButtonQuit.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    sourcePath = jTextFieldSrc.getText();
                    targetPath = jTextFieldTrg.getText();
                    dispose();
                }
            });
        }

        return jButtonQuit;
    }

    /**
     * This method initializes jButtonExtract.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonExtract() {
        if (jButtonExtract == null) {
            jButtonExtract = new JButton();
            jButtonExtract.setText("Extract");
            jButtonExtract
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            sourcePath = jTextFieldSrc.getText();
                            targetPath = jTextFieldTrg.getText();
                            doExtract = true;
                            dispose();
                        }
                    });
        }
        return jButtonExtract;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
