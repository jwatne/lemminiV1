package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gameutil.FaderHandler;

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

/**
 * Dialog for entering level codes.
 *
 * @author Volker Oth
 */
public class LevelCodeDialog extends JDialog {

    /**
     * Ok button height.
     */
    private static final int OK_HEIGHT = 50;

    /**
     * Ok button width.
     */
    private static final int OK_WIDTH = 90;

    /**
     * Inset of 6.
     */
    private static final int INSET_6 = 6;

    /**
     * Gridy of 3.
     */
    private static final int GRIDY_3 = 3;

    /**
     * Gridy of 4.
     */
    private static final int GRIDY_4 = 4;

    /**
     * Inset of 4.
     */
    private static final int INSET_4 = 4;

    /**
     * Inset of 24.
     */
    private static final int INSET_24 = 24;

    /**
     * Inset of 8.
     */
    private static final int INSET_8 = 8;

    /**
     * Default height, in pixels.
     */
    private static final int DEFAULT_HEIGHT = 153;

    /**
     * Default width, in pixels.
     */
    private static final int DEFAULT_WIDTH = 300;

    private static final long serialVersionUID = 1L;

    /** Content pane. */
    private JPanel jContentPane = null;

    /** Level pack label. */
    private JLabel jLabelLvlPack = null;

    /** Level pack combo box. */
    @SuppressWarnings("rawtypes")
    private JComboBox jComboBoxLvlPack = null;

    /** Code label. */
    private JLabel jLabelCode = null;

    /** Code text field. */
    private JTextField jTextFieldCode = null;

    /** Ok button. */
    private JButton jButtonOk = null;

    /** Cancel button. */
    private JButton jButtonCancel = null;

    // own stuff
    /** Level pack index. */
    private int levelPackIndex;
    /** Code. */
    private String code; // @jve:decl-index=0:

    /**
     * Initialize manually generated resources.
     */
    @SuppressWarnings("unchecked")
    private void init() {
        // level pack 0 is the dummy level pack -> not selectable
        for (int i = 1; i < FaderHandler.getLevelPackNum(); i++) {
            jComboBoxLvlPack.addItem(FaderHandler.getLevelPack(i).getName());
        }

        int lpi = FaderHandler.getCurLevelPackIdx();

        if (lpi == 0) {
            lpi = 1;
        }

        jComboBoxLvlPack.setSelectedIndex(lpi - 1);
        levelPackIndex = lpi;
        jTextFieldCode.setText("");
    }

    /**
     * Get entered level code.
     *
     * @return entered level code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Get selected level pack.
     *
     * @return selected level pack
     */
    public int getLevelPack() {
        return levelPackIndex;
    }

    /**
     * Constructor for modal dialog in parent frame.
     *
     * @param frame parent frame
     * @param modal create modal dialog?
     */
    public LevelCodeDialog(final JFrame frame, final boolean modal) {
        super(frame, modal);
        initialize();

        // own stuff
        final Point p = frame.getLocation();
        this.setLocation(p.x + frame.getWidth() / 2 - getWidth() / 2,
                p.y + frame.getHeight() / 2 - getHeight() / 2);
        init();
    }

    /**
     * Automatically generated init.
     */
    private void initialize() {
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setTitle("Enter Level Code");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane.
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            final GridBagConstraints gridBagCancel = new GridBagConstraints();
            gridBagCancel.gridx = 1;
            gridBagCancel.insets = new Insets(INSET_8, INSET_24, 0, INSET_4);
            gridBagCancel.anchor = GridBagConstraints.EAST;
            gridBagCancel.weightx = 1.0D;
            gridBagCancel.fill = GridBagConstraints.HORIZONTAL;
            gridBagCancel.gridy = GRIDY_4;
            final GridBagConstraints gridBagButtonOk = new GridBagConstraints();
            gridBagButtonOk.gridx = 0;
            gridBagButtonOk.insets = new Insets(INSET_8, INSET_4, 0, INSET_24);
            gridBagButtonOk.anchor = GridBagConstraints.WEST;
            gridBagButtonOk.weightx = 1.0D;
            gridBagButtonOk.fill = GridBagConstraints.HORIZONTAL;
            gridBagButtonOk.gridy = GRIDY_4;
            final GridBagConstraints gridBagText = new GridBagConstraints();
            gridBagText.fill = GridBagConstraints.BOTH;
            gridBagText.gridy = GRIDY_3;
            gridBagText.weightx = 1.0;
            gridBagText.insets = new Insets(0, INSET_4, 0, INSET_4);
            gridBagText.gridwidth = 2;
            gridBagText.gridx = 0;
            final GridBagConstraints gridBagLabel = new GridBagConstraints();
            gridBagLabel.gridx = 0;
            gridBagLabel.anchor = GridBagConstraints.WEST;
            gridBagLabel.insets = new Insets(INSET_8, INSET_4, 0, INSET_4);
            gridBagLabel.fill = GridBagConstraints.HORIZONTAL;
            gridBagLabel.gridwidth = 2;
            gridBagLabel.gridy = 2;
            jLabelCode = new JLabel();
            jLabelCode.setText("Enter Level Code");
            final GridBagConstraints gridBagLvlPack = new GridBagConstraints();
            gridBagLvlPack.fill = GridBagConstraints.BOTH;
            gridBagLvlPack.gridy = 1;
            gridBagLvlPack.weightx = 1.0;
            gridBagLvlPack.anchor = GridBagConstraints.WEST;
            gridBagLvlPack.insets = new Insets(0, INSET_4, INSET_6, INSET_4);
            gridBagLvlPack.gridwidth = 2;
            gridBagLvlPack.gridx = 0;
            final GridBagConstraints gridBagLblLPack = new GridBagConstraints();
            gridBagLblLPack.gridx = 0;
            gridBagLblLPack.anchor = GridBagConstraints.NORTHWEST;
            gridBagLblLPack.insets = new Insets(INSET_4, INSET_4, 0, INSET_4);
            gridBagLblLPack.fill = GridBagConstraints.HORIZONTAL;
            gridBagLblLPack.gridwidth = 2;
            gridBagLblLPack.gridy = 0;
            jLabelLvlPack = new JLabel();
            jLabelLvlPack.setText("Chose level pack");
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabelLvlPack, gridBagLblLPack);
            jContentPane.add(getJComboBoxLvlPack(), gridBagLvlPack);
            jContentPane.add(jLabelCode, gridBagLabel);
            jContentPane.add(getJTextFieldCode(), gridBagText);
            jContentPane.add(getJButtonOk(), gridBagButtonOk);
            jContentPane.add(getJButtonCancel(), gridBagCancel);
        }

        return jContentPane;
    }

    /**
     * This method initializes jComboBoxLvlPack.
     *
     * @return javax.swing.JComboBox
     */
    @SuppressWarnings("rawtypes")
    private JComboBox getJComboBoxLvlPack() {
        if (jComboBoxLvlPack == null) {
            jComboBoxLvlPack = new JComboBox();
        }

        return jComboBoxLvlPack;
    }

    /**
     * This method initializes jTextFieldCode.
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldCode() {
        if (jTextFieldCode == null) {
            jTextFieldCode = new JTextField();
            jTextFieldCode
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            code = jTextFieldCode.getText();
                            levelPackIndex = jComboBoxLvlPack.getSelectedIndex()
                                    + 1;
                            dispose();
                        }
                    });
        }

        return jTextFieldCode;
    }

    /**
     * This method initializes jButtonOk.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonOk() {
        if (jButtonOk == null) {
            jButtonOk = new JButton();
            jButtonOk.setText("Ok");
            jButtonOk.setPreferredSize(new Dimension(OK_WIDTH, OK_HEIGHT));
            jButtonOk.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    code = jTextFieldCode.getText();
                    levelPackIndex = jComboBoxLvlPack.getSelectedIndex() + 1;
                    dispose();
                }
            });
        }

        return jButtonOk;
    }

    /**
     * This method initializes jButtonCancel.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setText("Cancel");
            jButtonCancel
                    .addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            code = null;
                            levelPackIndex = -1;
                            dispose();
                        }
                    });
        }
        return jButtonCancel;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
