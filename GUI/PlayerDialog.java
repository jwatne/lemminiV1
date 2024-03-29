package gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import game.Core;

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
 * Dialog for managing players.
 *
 * @author Volker Oth
 */
public class PlayerDialog extends JDialog {

    /**
     * GridHeight of 4.
     */
    private static final int GRIDHEIGHT_4 = 4;
    /**
     * Gridy of 3.
     */
    private static final int GRIDY_3 = 3;
    /**
     * Gridy of 5.
     */
    private static final int GRIDY_5 = 5;
    /**
     * Default height, in pixels.
     */
    private static final int DEFAULT_HEIGHT = 199;
    /**
     * Default width, in pixels.
     */
    private static final int DEFAULT_WIDTH = 442;
    private static final long serialVersionUID = 1L;
    /** Content pane. */
    private JPanel jContentPane = null;
    /** Scroll pane. */
    private JScrollPane jScrollPane = null;
    /** List. */
    @SuppressWarnings("rawtypes")
    private JList jList = null;
    /** New button. */
    private JButton jButtonNew = null;
    /** Delete button. */
    private JButton jButtonDelete = null;
    /** Ok button. */
    private JButton jButtonOK = null;
    /** Cancel button. */
    private JButton jButtonCancel = null;

    // own stuff
    /**
     * List of players - still needs to be old Vector type to be passed to JList
     * constructor.
     */
    private Vector<String> players;
    /** Frame for dialog. */
    private final Component frame;

    /**
     * Get list of players.
     *
     * @return list of players.
     */
    public List<String> getPlayers() {
        // Return copy to avoid malicious code vulnerability of exposed mutable
        // object.
        return players.stream().collect(Collectors.toList());
    }

    /**
     * Get selected list index.
     *
     * @return selected list index
     */
    public int getSelection() {
        return jList.getSelectedIndex();
    }

    /**
     * Initialize manually generated resources.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void init() {
        players = new Vector<String>();
        for (int i = 0; i < Core.getPlayerNum(); i++) {
            players.add(Core.getPlayer(i));
        }
        jList = new JList(players);
        jScrollPane.setViewportView(jList);
    }

    /**
     * Constructor for modal dialog in parent frame.
     *
     * @param parent parent frame
     * @param modal  create modal dialog?
     */
    public PlayerDialog(final JFrame parent, final boolean modal) {
        super(parent, modal);
        this.frame = parent;
        initialize();

        // own stuff
        final Point p = parent.getLocation();
        this.setLocation(p.x + parent.getWidth() / 2 - getWidth() / 2,
                p.y + parent.getHeight() / 2 - getHeight() / 2);
        init();
    }

    /**
     * Automatically generated init.
     */
    private void initialize() {
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setTitle("Manage Players");
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane.
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            final GridBagConstraints gbcButtonNew = new GridBagConstraints();
            gbcButtonNew.fill = GridBagConstraints.HORIZONTAL;
            gbcButtonNew.insets = new Insets(0, 0, 0, 0);
            final GridBagConstraints gbcButtonCancel = new GridBagConstraints();
            gbcButtonCancel.gridx = 0;
            gbcButtonCancel.insets = new Insets(0, 0, 0, 0);
            gbcButtonCancel.anchor = GridBagConstraints.WEST;
            gbcButtonCancel.gridy = GRIDY_5;
            final GridBagConstraints gridBagButtonOk = new GridBagConstraints();
            gridBagButtonOk.gridx = 1;
            gridBagButtonOk.fill = GridBagConstraints.HORIZONTAL;
            gridBagButtonOk.insets = new Insets(0, 0, 0, 0);
            gridBagButtonOk.gridy = GRIDY_5;
            final GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.gridx = 1;
            gbc2.insets = new Insets(0, 2, 2, 2);
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.anchor = GridBagConstraints.NORTH;
            gbc2.gridy = GRIDY_3;
            final GridBagConstraints gbcButtonDelete = new GridBagConstraints();
            gbcButtonDelete.gridx = 1;
            gbcButtonDelete.insets = new Insets(0, 0, 0, 0);
            gbcButtonDelete.fill = GridBagConstraints.HORIZONTAL;
            gbcButtonDelete.gridy = 2;
            final GridBagConstraints gbcScrollPane = new GridBagConstraints();
            gbcScrollPane.fill = GridBagConstraints.BOTH;
            gbcScrollPane.gridy = 0;
            gbcScrollPane.weightx = 1.0;
            gbcScrollPane.weighty = 1.0;
            gbcScrollPane.gridheight = GRIDHEIGHT_4;
            gbcScrollPane.insets = new Insets(0, 0, 0, 0);
            gbcScrollPane.gridx = 0;
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(getJScrollPane(), gbcScrollPane);
            jContentPane.add(getJButtonNew(), gbcButtonNew);
            jContentPane.add(getJButtonDelete(), gbcButtonDelete);
            jContentPane.add(getJButtonOK(), gridBagButtonOk);
            jContentPane.add(getJButtonCancel(), gbcButtonCancel);
        }
        return jContentPane;
    }

    /**
     * This method initializes jScrollPane.
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJList());
        }

        return jScrollPane;
    }

    /**
     * This method initializes jList.
     *
     * @return javax.swing.JList
     */
    @SuppressWarnings("rawtypes")
    private JList getJList() {
        if (jList == null) {
            jList = new JList();
        }
        return jList;
    }

    /**
     * This method initializes jButtonNew.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonNew() {
        if (jButtonNew == null) {
            jButtonNew = new JButton();
            jButtonNew.setText("New Player");
            jButtonNew.addActionListener(new java.awt.event.ActionListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    String player = JOptionPane.showInputDialog(frame,
                            "Enter Player Name", "Input",
                            JOptionPane.QUESTION_MESSAGE);

                    if (player != null) {
                        // check if this player already exists
                        // it it alread exists, reset the existing profile
                        boolean found = false;
                        for (int pli = 0; pli < players.size(); pli++) {
                            if (players.get(pli).equalsIgnoreCase(player)) {
                                player = players.get(pli);
                                found = true;
                                break;
                            }
                        }
                        // really a new player
                        if (!found) {
                            players.add(player);
                            jList.setListData(players);
                            final int i = players.size() - 1;
                            if (i >= 0) {
                                jList.setSelectedIndex(i);
                            }
                        }
                    }
                }
            });
        }
        return jButtonNew;
    }

    /**
     * This method initializes jButtonDelete.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonDelete() {
        if (jButtonDelete == null) {
            jButtonDelete = new JButton();
            jButtonDelete.setText("Delete Player");
            jButtonDelete
                    .addActionListener(new java.awt.event.ActionListener() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            final int idx = jList.getSelectedIndex();

                            if (idx != -1) {
                                players.remove(idx);
                                jList.setListData(players);
                            }
                        }
                    });
        }

        return jButtonDelete;
    }

    /**
     * This method initializes jButtonOK.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonOK() {
        if (jButtonOK == null) {
            jButtonOK = new JButton();
            jButtonOK.setText("Ok");
            jButtonOK.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(
                        final java.awt.event.ActionEvent e) {
                    dispose();
                }
            });
        }

        return jButtonOK;
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
                            players.clear();
                            players = null;
                            dispose();
                        }
                    });
        }

        return jButtonCancel;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
