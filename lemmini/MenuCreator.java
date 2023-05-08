package lemmini;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import GUI.GainDialog;
import GUI.LevelCodeDialog;
import Game.Core;
import Game.GameController;
import Game.GroupBitfield;
import Game.LemmCursor;
import Game.LevelCode;
import Game.LevelPack;
import Game.Music;
import Game.Player;
import Game.ReplayLevelInfo;
import Tools.ToolBox;

/**
 * This service class creates the menu system for the lemmini application.
 */
public class MenuCreator {
    // Swing stuff
    private JMenuItem jMenuItemRestart = null;
    private JMenuItem jMenuItemLevelCode = null;
    private JMenuItem jMenuSelect = null;
    private JMenu jMenuSFX = null;
    private JMenuItem jMenuItemLoad = null;
    private JMenuItem jMenuItemReplay = null;
    private JCheckBoxMenuItem jMenuItemMusic = null;
    private JCheckBoxMenuItem jMenuItemSound = null;
    private JMenuBar jMenuBar = null;
    private JMenu jMenuLevel = null;
    private JMenu jMenuFile = null;
    private JMenu jMenuPlayer = null;
    private JMenuItem jMenuItemExit = null;
    JMenu jMenuSelectPlayer = null;
    private JMenuItem jMenuItemManagePlayer = null;
    ButtonGroup playerGroup = null;
    private ButtonGroup zoomGroup = null;
    private JMenu jMenuOptions = null;
    private JMenuItem jMenuItemClassicalCursor = null;
    private JMenu jMenuSound = null;
    private JMenuItem jMenuItemVolume = null;
    private JMenuItem jMenuItemCursor = null;
    /** path for loading single level files */
    private String lvlPath;
    private Lemmini lemmini;

    /**
     * Returns the initialized menu bar for the Lemmini window.
     * 
     * @return the initialized menu bar for the Lemmini window.
     */
    public JMenuBar getLemminiMenuBar(final Lemmini lemmini) {
        this.lemmini = lemmini;
        // create Menu
        jMenuFile = new JMenu("File");
        getExitMenuItem();
        jMenuFile.add(jMenuItemExit);
        // Player Menu
        jMenuPlayer = new JMenu("Player");
        getManagePlayerMenuItem();
        jMenuSelectPlayer = new JMenu("Select Player");
        playerGroup = new ButtonGroup();

        for (int idx = 0; idx < Core.getPlayerNum(); idx++) {
            final JCheckBoxMenuItem item = addPlayerItem(Core.getPlayer(idx));

            if (Core.player.getName().equals(Core.getPlayer(idx))) {
                item.setSelected(true);
            }
        }

        jMenuPlayer.add(jMenuItemManagePlayer);
        jMenuPlayer.add(jMenuSelectPlayer);
        loadLevelPacksAndCreateLevelMenu();
        initializeRestartLevelMenuItem();
        initializeLoadLevelMenuItem();
        initializeLoadReplayMenuItem();
        initializeEnterLevelCodeMenuItem();
        jMenuLevel = new JMenu("Level");
        jMenuLevel.add(jMenuSelect);
        jMenuLevel.add(jMenuItemRestart);
        jMenuLevel.add(jMenuItemLoad);
        jMenuLevel.add(jMenuItemReplay);
        jMenuLevel.add(jMenuItemLevelCode);
        initializeSoundMenu();
        initializeOptionsMenu();
        jMenuBar = new JMenuBar();
        jMenuBar.add(jMenuFile);
        jMenuBar.add(jMenuPlayer);
        jMenuBar.add(jMenuLevel);
        jMenuBar.add(jMenuSound);
        jMenuBar.add(jMenuOptions);
        initializeZoomMenu();
        return jMenuBar;
    }

    /**
     * Initializes the Exit menu item.
     */
    private void getExitMenuItem() {
        jMenuItemExit = new JMenuItem("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                exit();
            }
        });
    }

    /**
     * Common exit method to use in exit events.
     */
    void exit() {
        // store width and height
        final Dimension d = lemmini.getSize();
        Core.programProps.set("frameWidth", d.width);
        Core.programProps.set("frameHeight", d.height);
        // store frame pos
        final Point p = lemmini.getLocation();
        Core.programProps.set("framePosX", p.x);
        Core.programProps.set("framePosY", p.y);
        //
        Core.saveProgramProps();
        System.exit(0);
    }

    /**
     * Initializes the Manage Player menu item.
     */
    private void getManagePlayerMenuItem() {
        jMenuItemManagePlayer = new JMenuItem("Manage Players");

        jMenuItemManagePlayer.addActionListener(new ManagePlayerMenuItemActionListener(this));
    }

    /**
     * Add a menu item for a player.
     * 
     * @param name player name
     * @return JCheckBoxMenuItem
     */
    JCheckBoxMenuItem addPlayerItem(final String name) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                Core.player.store(); // save player in case it is changed
                final JMenuItem item = (JMenuItem) e.getSource();
                final String player = item.getText();
                final Player p = new Player(player);
                Core.player = p; // default player
                item.setSelected(true);
                updateLevelMenus();
            }
        });

        playerGroup.add(item);
        jMenuSelectPlayer.add(item);
        return item;
    }

    /**
     * Initilizes the Zoom menu.
     */
    private void initializeZoomMenu() {
        zoomGroup = new ButtonGroup();
        final JMenu jMenuZoom = new JMenu("Zoom");
        jMenuOptions.add(jMenuZoom);
        final JRadioButtonMenuItem jMenuRadioItemX1 = getX1RadioButtonMenuItem();
        jMenuZoom.add(jMenuRadioItemX1);
        zoomGroup.add(jMenuRadioItemX1);
        final JRadioButtonMenuItem jMenuRadioItemX1P5 = getX1P5RadioButtonMenuItem();
        jMenuZoom.add(jMenuRadioItemX1P5);
        zoomGroup.add(jMenuRadioItemX1P5);
        final JRadioButtonMenuItem jMenuRadioItemX2 = getX2RadioButtonMenuItem();
        jMenuZoom.add(jMenuRadioItemX2);
        zoomGroup.add(jMenuRadioItemX2);
        final JRadioButtonMenuItem jMenuRadioItemX2P5 = getX2P5RadioButtonMenuItem();
        jMenuZoom.add(jMenuRadioItemX2P5);
        zoomGroup.add(jMenuRadioItemX2P5);
        final JRadioButtonMenuItem jMenuRadioItemX3 = getX3RadioButtonMenuItem();
        jMenuZoom.add(jMenuRadioItemX3);
        zoomGroup.add(jMenuRadioItemX3);

        switch ((int) Math.round(Core.getScale() * 2)) {
            case 3:
                jMenuRadioItemX1P5.setSelected(true);
                break;
            case 4:
                jMenuRadioItemX2.setSelected(true);
                break;
            case 6:
                jMenuRadioItemX3.setSelected(true);
                break;
            default:
                jMenuRadioItemX1.setSelected(true);
        }
    }

    /**
     * Returns the initialized x3 radio button menu item.
     * 
     * @return the initialized x3 radio button menu item.
     */
    private JRadioButtonMenuItem getX3RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX3 = new JRadioButtonMenuItem("x3");

        jMenuRadioItemX3.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(3);
            }
        });

        return jMenuRadioItemX3;
    }

    /**
     * Returns the initialized X2.5 radio button menu item.
     * 
     * @return the initialized X2.5 radio button menu item.
     */
    private JRadioButtonMenuItem getX2P5RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX2P5 = new JRadioButtonMenuItem("X2.5");

        jMenuRadioItemX2P5.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(2.5);
            }
        });

        return jMenuRadioItemX2P5;
    }

    /**
     * Returns the initialized x2 radio button menu item.
     * 
     * @return the initialized x2 radio button menu item.
     */
    private JRadioButtonMenuItem getX2RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX2 = new JRadioButtonMenuItem("x2");

        jMenuRadioItemX2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(2);
            }
        });

        return jMenuRadioItemX2;
    }

    /**
     * Returns the initialized X1.5 radio button menu item.
     * 
     * @return the initialized X1.5 radio button menu item.
     */
    private JRadioButtonMenuItem getX1P5RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX1P5 = new JRadioButtonMenuItem("X1.5");

        jMenuRadioItemX1P5.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(1.5);
            }
        });

        return jMenuRadioItemX1P5;
    }

    /**
     * Returns the initialized x1 radio button menu item.
     * 
     * @return the initialized x1 radio button menu item.
     */
    private JRadioButtonMenuItem getX1RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX1 = new JRadioButtonMenuItem("x1");

        jMenuRadioItemX1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(1);
            }
        });

        return jMenuRadioItemX1;
    }

    /**
     * Initializes the Options menu.
     */
    private void initializeOptionsMenu() {
        initializeAdvancedSelectCheckboxMenuItem();
        jMenuItemClassicalCursor = new JCheckBoxMenuItem("Classical Cursor", false);
        jMenuItemClassicalCursor.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemClassicalCursor.isSelected();
                if (selected) {
                    GameController.setClassicalCursor(true);
                } else {
                    GameController.setClassicalCursor(false);
                }

                Core.programProps.set("classicalCursor", GameController.isClassicalCursor());
            }
        });

        jMenuItemClassicalCursor.setSelected(GameController.isClassicalCursor());

        jMenuOptions = new JMenu();
        jMenuOptions.setText("Options");
        jMenuOptions.add(jMenuItemCursor);
        jMenuOptions.add(jMenuItemClassicalCursor);
    }

    /**
     * Initializes Advanced select checkbox menu item.
     */
    private void initializeAdvancedSelectCheckboxMenuItem() {
        jMenuItemCursor = new JCheckBoxMenuItem("Advanced select", false);
        jMenuItemCursor.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemCursor.isSelected();

                if (selected) {
                    GameController.setAdvancedSelect(true);
                } else {
                    GameController.setAdvancedSelect(false);
                    lemmini.getGp().setCursor(LemmCursor.Type.NORMAL);
                }

                Core.programProps.set("advancedSelect", GameController.isAdvancedSelect());
            }
        });

        jMenuItemCursor.setSelected(GameController.isAdvancedSelect());
    }

    /**
     * Initializes Sound menu.
     */
    private void initializeSoundMenu() {
        initializeMusicCheckboxMenuItem();
        initializeSoundCheckboxMenuItem();
        initializeSFXMixerMenu();
        jMenuItemVolume = new JMenuItem("Volume Control");
        jMenuItemVolume.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final GainDialog v = new GainDialog((JFrame) Core.getCmp(), true);
                v.setVisible(true);
            }
        });

        jMenuSound = new JMenu();
        jMenuSound.setText("Sound");
        jMenuSound.add(jMenuItemVolume);
        jMenuSound.add(jMenuItemMusic);
        jMenuSound.add(jMenuItemSound);
        jMenuSound.add(jMenuSFX);
    }

    /**
     * Initializees SFX Mixer menu.
     */
    private void initializeSFXMixerMenu() {
        jMenuSFX = new JMenu("SFX Mixer");
        final String mixerNames[] = GameController.sound.getMixers();
        final ButtonGroup mixerGroup = new ButtonGroup();
        String lastMixerName = Core.programProps.get("mixerName", "Java Sound Audio Engine");

        // special handling of mixer from INI that doesn't exist (any more)
        boolean found = false;

        for (int i = 0; i < mixerNames.length; i++) {
            if (mixerNames[i].equals(lastMixerName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            lastMixerName = "Java Sound Audio Engine";
        }

        for (int i = 0; i < mixerNames.length; i++) {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            item.setText(mixerNames[i]);
            item.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    final String mixerNames[] = GameController.sound.getMixers();
                    final String mixerName = e.getActionCommand();

                    for (int i = 0; i < mixerNames.length; i++) {
                        if (mixerNames[i].equals(mixerName)) {
                            GameController.sound.setMixer(i);
                            Core.programProps.set("mixerName", mixerName);
                            break;
                        }
                    }
                }
            });

            if (mixerNames[i].equals(lastMixerName)) { // default setting
                item.setState(true);
                GameController.sound.setMixer(i);
            }

            jMenuSFX.add(item);
            mixerGroup.add(item);
        }
    }

    /**
     * Initializes the Sound checkbox menu item.
     */
    private void initializeSoundCheckboxMenuItem() {
        jMenuItemSound = new JCheckBoxMenuItem("Sound", false);
        jMenuItemSound.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemSound.isSelected();

                if (selected) {
                    GameController.setSoundOn(true);
                } else {
                    GameController.setSoundOn(false);
                }

                Core.programProps.set("sound", GameController.isSoundOn());
            }
        });

        jMenuItemSound.setSelected(GameController.isSoundOn());
    }

    /**
     * Initializes Music checkbox menu item.
     */
    private void initializeMusicCheckboxMenuItem() {
        jMenuItemMusic = new JCheckBoxMenuItem("Music", false);
        jMenuItemMusic.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemMusic.isSelected();
                // jMenuItemMusic.setSelected(selected);

                if (selected) {
                    GameController.setMusicOn(true);
                } else {
                    GameController.setMusicOn(false);
                }

                Core.programProps.set("music", GameController.isMusicOn());

                if (GameController.getLevel() != null) { // to be improved: level is running (game state)
                    if (GameController.isMusicOn()) {
                        Music.play();
                    } else {
                        Music.stop();
                    }
                }
            }
        });

        jMenuItemMusic.setSelected(GameController.isMusicOn());
    }

    /**
     * Initializes Enter Level Code menu item.
     */
    private void initializeEnterLevelCodeMenuItem() {
        jMenuItemLevelCode = new JMenuItem("Enter Level Code");
        jMenuItemLevelCode.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final LevelCodeDialog lcd = new LevelCodeDialog((JFrame) Core.getCmp(), true);
                lcd.setVisible(true);
                String levelCode = lcd.getCode();
                final int lvlPack = lcd.getLevelPack();

                if (levelCode != null && levelCode.length() != 0 && lvlPack > 0) {
                    levelCode = levelCode.trim();

                    // cheat mode
                    if (levelCode.equals("0xdeadbeef")) {
                        JOptionPane.showMessageDialog(Core.getCmp(), "All levels and debug mode enabled", "Cheater!",
                                JOptionPane.INFORMATION_MESSAGE);
                        Core.player.enableCheatMode();
                        updateLevelMenus();
                        return;
                    }

                    // real level code -> get absolute level
                    levelCode = levelCode.toUpperCase();
                    final LevelPack lpack = GameController.getLevelPack(lvlPack);
                    final int lvlAbs = LevelCode.getLevel(lpack.getCodeSeed(), levelCode, lpack.getCodeOffset());

                    if (lvlAbs != -1) {
                        // calculate level pack and relative levelnumber from absolute number
                        final int l[] = GameController.relLevelNum(lvlPack, lvlAbs);
                        final int diffLvl = l[0];
                        final int lvlRel = l[1];
                        Core.player.setAvailable(lpack.getName(), lpack.getDiffLevels()[diffLvl], lvlRel);
                        GameController.requestChangeLevel(lvlPack, diffLvl, lvlRel, false);
                        updateLevelMenus();
                        return;
                    }
                }

                // not found
                JOptionPane.showMessageDialog(Core.getCmp(), "Invalid Level Code", "Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Initializes the Load Replay menu Item.
     */
    private void initializeLoadReplayMenuItem() {
        jMenuItemReplay = new JMenuItem();
        jMenuItemReplay.setText("Load Replay");
        jMenuItemReplay.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final String replayPath = ToolBox.getFileName(lemmini, Core.resourcePath, Core.REPLAY_EXTENSIONS,
                        true);

                if (replayPath != null) {
                    try {
                        if (ToolBox.getExtension(replayPath).equalsIgnoreCase("rpl")) {
                            final ReplayLevelInfo rli = GameController.loadReplay(replayPath);

                            if (rli != null) {
                                int lpn = -1;

                                for (int i = 0; i < GameController.getLevelPackNum(); i++) {
                                    if (GameController.getLevelPack(i).getName().equals(rli.getLevelPack())) {
                                        lpn = i;
                                    }
                                }

                                if (lpn > -1) {
                                    GameController.requestChangeLevel(lpn, rli.getDiffLevel(), rli.getLvlNumber(),
                                            true);
                                    return; // success
                                }
                            }
                        }

                        // else: no success
                        JOptionPane.showMessageDialog(Core.getCmp(), "Wrong format!", "Loading replay failed",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (final Exception ex) {
                        ToolBox.showException(ex);
                    }
                }
            }
        });
    }

    /**
     * Initializes the Load Level menu item.
     */
    private void initializeLoadLevelMenuItem() {
        jMenuItemLoad = new JMenuItem();
        jMenuItemLoad.setText("Load Level");
        jMenuItemLoad.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                String p = ToolBox.getFileName(lemmini, lvlPath, Core.LEVEL_EXTENSIONS, true);

                if (p != null) {
                    try {
                        if (ToolBox.getExtension(p).equalsIgnoreCase("lvl")) {
                            Extract.ExtractLevel.convertLevel(p, Core.resourcePath + "/temp.ini");
                            p = Core.resourcePath + "/temp.ini";
                        }

                        if (ToolBox.getExtension(p).equalsIgnoreCase("ini")) {
                            final String id = new String(ToolBox.getFileID(p, 5));

                            if (id.equalsIgnoreCase("# LVL")) {
                                // this is a hack - maybe find a better way
                                GameController.getLevelPack(0).getInfo(0, 0).setFileName(p);
                                GameController.getLevelPack(0).getInfo(0, 0).setMusic(Music.getRandomTrack());
                                GameController.requestChangeLevel(0, 0, 0, false);
                                lvlPath = p;
                                return;
                            }
                        }

                        JOptionPane.showMessageDialog(Core.getCmp(), "Wrong format!", "Loading level failed",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (final Exception ex) {
                        ToolBox.showException(ex);
                    }
                }
            }
        });
    }

    /**
     * Initializes the Restart Level menu item.
     */
    private void initializeRestartLevelMenuItem() {
        jMenuItemRestart = new JMenuItem();
        jMenuItemRestart.setText("Restart Level");
        jMenuItemRestart.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                if (!GameController.getLevel().isReady())
                    GameController.requestChangeLevel(GameController.getCurLevelPackIdx(),
                            GameController.getCurDiffLevel(), GameController.getCurLevelNumber(), false);
                else
                    GameController.requestRestartLevel(false);
            }
        });
    }

    /**
     * Loads level packs and creates the level menu.
     */
    private void loadLevelPacksAndCreateLevelMenu() {
        final java.awt.event.ActionListener lvlListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final LvlMenuItem item = (LvlMenuItem) e.getSource();
                GameController.requestChangeLevel(item.levelPack, item.diffLevel, item.level, false);
            }
        };

        lemmini.setDiffLevelMenus(new HashMap<String, ArrayList<LvlMenuItem>>()); // store menus to access them later
        jMenuSelect = new JMenu("Select Level");

        for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip dummy level pack
            final LevelPack lPack = GameController.getLevelPack(lp);
            final JMenu jMenuPack = new JMenu(lPack.getName());
            final String difficulties[] = lPack.getDiffLevels();

            for (int i = 0; i < difficulties.length; i++) {
                // get activated levels for this group
                final GroupBitfield bf = Core.player.getBitField(lPack.getName(), difficulties[i]);
                final String names[] = lPack.getLevels(i);
                final JMenu jMenuDiff = new JMenu(difficulties[i]);
                // store menus to access them later
                final ArrayList<LvlMenuItem> menuItems = new ArrayList<LvlMenuItem>();

                for (int n = 0; n < names.length; n++) {
                    final LvlMenuItem jMenuLvl = new LvlMenuItem(names[n], lp, i, n);
                    jMenuLvl.addActionListener(lvlListener);

                    if (Core.player.isAvailable(bf, n)) {
                        jMenuLvl.setEnabled(true);
                    } else {
                        jMenuLvl.setEnabled(false);
                    }

                    jMenuDiff.add(jMenuLvl);
                    menuItems.add(jMenuLvl);
                }

                jMenuPack.add(jMenuDiff);
                // store menus to access them later
                lemmini.getDiffLevelMenus().put(LevelPack.getID(lPack.getName(), difficulties[i]), menuItems);
            }

            jMenuSelect.add(jMenuPack);
        }
    }

    /**
     * Update the level menus according to the progress of the current player.
     */
    void updateLevelMenus() {
        // update level menus
        for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip dummy level pack
            final LevelPack lPack = GameController.getLevelPack(lp);
            final String difficulties[] = lPack.getDiffLevels();

            for (int i = 0; i < difficulties.length; i++) {
                // get activated levels for this group
                final GroupBitfield bf = Core.player.getBitField(lPack.getName(), difficulties[i]);
                lemmini.updateLevelMenu(lPack.getName(), difficulties[i], bf);
            }
        }
    }

}
