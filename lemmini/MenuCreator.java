package lemmini;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import game.Core;
import game.GameController;
import game.GroupBitfield;
import game.LemmCursor;
import game.LevelCode;
import game.LevelPack;
import game.Music;
import game.Player;
import game.ReplayLevelInfo;
import game.SoundController;
import gameutil.Sound;
import gui.GainDialog;
import gui.LevelCodeDialog;
import tools.Props;
import tools.ToolBox;

/**
 * This service class creates the menu system for the lemmini application.
 */
public class MenuCreator {
    /**
     * Scale factor = 1.5.
     */
    private static final double SCALE_1_POINT_5 = 1.5;
    /**
     * Scale factor = 2.5.
     */
    private static final double SCALE_2_POINT_5 = 2.5;
    /**
     * Scale factor = 3.
     */
    private static final int SCALE_3 = 3;
    /**
     * 2x value for scale = 3.
     */
    private static final int TWICE_3 = 6;
    /**
     * 2x value for scale = 2.
     */
    private static final int TWICE_TWO = 4;
    /**
     * 2x value for scale = 1.5.
     */
    private static final int TWICE_ONE_POINT_FIVE = 3;

    private static final class SFXMixerActionListener
            implements ActionListener {
        @Override
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            final Sound sound = SoundController.getSound();
            final String[] mixerNames = sound.getMixers();
            final String mixerName = e.getActionCommand();

            for (int i = 0; i < mixerNames.length; i++) {
                if (mixerNames[i].equals(mixerName)) {
                    sound.setMixer(i);
                    Core.getProgramProps().set("mixerName", mixerName);
                    break;
                }
            }
        }
    }

    private static final class RestartLevelActionListener
            implements ActionListener {
        @Override
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            if (!GameController.getLevel().isReady()) {
                GameController.requestChangeLevel(
                        GameController.getCurLevelPackIdx(),
                        GameController.getCurDiffLevel(),
                        GameController.getCurLevelNumber(), false);
            } else {
                GameController.requestRestartLevel(false);
            }
        }
    }

    private static final class LevelMenuActionListener
            implements ActionListener {
        @Override
        public void actionPerformed(final java.awt.event.ActionEvent e) {
            final LvlMenuItem item = (LvlMenuItem) e.getSource();
            GameController.requestChangeLevel(item.getLevelPack(),
                    item.getDiffLevel(), item.getLevel(), false);
        }
    }

    // Swing stuff
    /** Restart menu item. */
    private JMenuItem jMenuItemRestart = null;
    /** Level code menu item. */
    private JMenuItem jMenuItemLevelCode = null;
    /** Select menu. */
    private JMenuItem jMenuSelect = null;
    /** SFX menu. */
    private JMenu jMenuSFX = null;
    /** Load menu item. */
    private JMenuItem jMenuItemLoad = null;
    /** Replay menu item. */
    private JMenuItem jMenuItemReplay = null;
    /** Music menu item. */
    private JCheckBoxMenuItem jMenuItemMusic = null;
    /** Sound menu item. */
    private JCheckBoxMenuItem jMenuItemSound = null;
    /** Level menu. */
    private JMenu jMenuLevel = null;
    /** File menu. */
    private JMenu jMenuFile = null;
    /** Player menu. */
    private JMenu jMenuPlayer = null;
    /** Exit menu item. */
    private JMenuItem jMenuItemExit = null;
    /** Select player menu. */
    private JMenu jMenuSelectPlayer = null;
    /** Manage player menu item. */
    private JMenuItem jMenuItemManagePlayer = null;
    /** Player button group. */
    private ButtonGroup playerGroup = null;
    /** Zoom button group. */
    private ButtonGroup zoomGroup = null;
    /** Options menu. */
    private JMenu jMenuOptions = null;
    /** Classical cursor menu item. */
    private JMenuItem jMenuItemClassicalCursor = null;
    /** Sound menu. */
    private JMenu jMenuSound = null;
    /** Volume menu item. */
    private JMenuItem jMenuItemVolume = null;
    /** Cursor menu item. */
    private JMenuItem jMenuItemCursor = null;
    /** path for loading single level files. */
    private String lvlPath;
    /** Parent Lemmini application class. */
    private Lemmini lemmini;
    /** Graphics pane. */
    private GraphicsPane graphicsPane;
    /** Parent frame. */
    private JFrame frame;

    /**
     * Initializes the MenuCreator.
     *
     * @param parentFrame the parent component (main frame of the application).
     */
    public MenuCreator(final JFrame parentFrame) {
        this.frame = parentFrame;
    }

    @SuppressWarnings("unused")
    private MenuCreator() {
        // Make default no-args constructor private so not callable.
    }

    /**
     * Returns the initialized menu bar for the Lemmini window.
     *
     * @param callingWindow        the calling {@link Lemmini} window.
     * @param lemminiPane          the {@link GraphicsPane} associated with the
     *                             Lemmini window.
     * @param difficultyLevelMenus
     *
     * @return the initialized menu bar for the Lemmini window.
     */
    public final JMenuBar getLemminiMenuBar(final Lemmini callingWindow,
            final GraphicsPane lemminiPane,
            final Map<String, List<LvlMenuItem>> difficultyLevelMenus) {
        this.lemmini = callingWindow;
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

            if (Core.getPlayer().getName().equals(Core.getPlayer(idx))) {
                item.setSelected(true);
            }
        }

        jMenuPlayer.add(jMenuItemManagePlayer);
        jMenuPlayer.add(jMenuSelectPlayer);
        loadLevelPacksAndCreateLevelMenu(difficultyLevelMenus);
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
        initializeOptionsMenu(lemminiPane);
        final JMenuBar jMenuBar = new JMenuBar();
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
        jMenuItemExit.addActionListener(new ActionListener() {
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
        final Props programProps = Core.getProgramProps();
        programProps.set("frameWidth", d.width);
        programProps.set("frameHeight", d.height);
        // store frame pos
        final Point p = lemmini.getLocation();
        programProps.set("framePosX", p.x);
        programProps.set("framePosY", p.y);
        //
        Core.saveProgramProps();
        System.exit(0);
    }

    /**
     * Initializes the Manage Player menu item.
     */
    private void getManagePlayerMenuItem() {
        jMenuItemManagePlayer = new JMenuItem("Manage Players");

        jMenuItemManagePlayer.addActionListener(
                new ManagePlayerMenuItemActionListener(this, frame));
    }

    /**
     * Add a menu item for a player.
     *
     * @param name player name
     * @return JCheckBoxMenuItem
     */
    JCheckBoxMenuItem addPlayerItem(final String name) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                Core.getPlayer().store(); // save player in case it is changed
                final JMenuItem item = (JMenuItem) e.getSource();
                final String player = item.getText();
                final Player p = new Player(player);
                Core.setPlayer(p); // default player
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
        final JRadioButtonMenuItem radioItemX1 = getX1RadioButtonMenuItem();
        jMenuZoom.add(radioItemX1);
        zoomGroup.add(radioItemX1);
        final JRadioButtonMenuItem radioItemX1P5 = getX1P5RadioButtonMenuItem();
        jMenuZoom.add(radioItemX1P5);
        zoomGroup.add(radioItemX1P5);
        final JRadioButtonMenuItem radioItemX2 = getX2RadioButtonMenuItem();
        jMenuZoom.add(radioItemX2);
        zoomGroup.add(radioItemX2);
        final JRadioButtonMenuItem radioItemX2P5 = getX2P5RadioButtonMenuItem();
        jMenuZoom.add(radioItemX2P5);
        zoomGroup.add(radioItemX2P5);
        final JRadioButtonMenuItem radioItemX3 = getX3RadioButtonMenuItem();
        jMenuZoom.add(radioItemX3);
        zoomGroup.add(radioItemX3);

        switch ((int) Math.round(Core.getScale() * 2)) {
        case TWICE_ONE_POINT_FIVE:
            radioItemX1P5.setSelected(true);
            break;
        case TWICE_TWO:
            radioItemX2.setSelected(true);
            break;
        case TWICE_3:
            radioItemX3.setSelected(true);
            break;
        default:
            radioItemX1.setSelected(true);
        }
    }

    /**
     * Returns the initialized x3 radio button menu item.
     *
     * @return the initialized x3 radio button menu item.
     */
    private JRadioButtonMenuItem getX3RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX3 = new JRadioButtonMenuItem(
                "x3");

        jMenuRadioItemX3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(SCALE_3);
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
        final JRadioButtonMenuItem radioItemX2P5 = new JRadioButtonMenuItem(
                "X2.5");

        radioItemX2P5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(SCALE_2_POINT_5);
            }
        });

        return radioItemX2P5;
    }

    /**
     * Returns the initialized x2 radio button menu item.
     *
     * @return the initialized x2 radio button menu item.
     */
    private JRadioButtonMenuItem getX2RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX2 = new JRadioButtonMenuItem(
                "x2");

        jMenuRadioItemX2.addActionListener(new ActionListener() {
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
        final JRadioButtonMenuItem radioItemX1P5 = new JRadioButtonMenuItem(
                "X1.5");

        radioItemX1P5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(SCALE_1_POINT_5);
            }
        });

        return radioItemX1P5;
    }

    /**
     * Returns the initialized x1 radio button menu item.
     *
     * @return the initialized x1 radio button menu item.
     */
    private JRadioButtonMenuItem getX1RadioButtonMenuItem() {
        final JRadioButtonMenuItem jMenuRadioItemX1 = new JRadioButtonMenuItem(
                "x1");

        jMenuRadioItemX1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                lemmini.setScale(1);
            }
        });

        return jMenuRadioItemX1;
    }

    /**
     * Initializes the Options menu.
     *
     * @param pane the {@link GraphicsPane} to which the Options menu is
     *             assigned.
     */
    private void initializeOptionsMenu(final GraphicsPane pane) {
        initializeAdvancedSelectCheckboxMenuItem(pane);
        jMenuItemClassicalCursor = new JCheckBoxMenuItem("Classical Cursor",
                false);
        jMenuItemClassicalCursor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemClassicalCursor.isSelected();
                if (selected) {
                    GameController.setClassicalCursor(true);
                } else {
                    GameController.setClassicalCursor(false);
                }

                Core.getProgramProps().set("classicalCursor",
                        GameController.isClassicalCursor());
            }
        });

        jMenuItemClassicalCursor
                .setSelected(GameController.isClassicalCursor());

        jMenuOptions = new JMenu();
        jMenuOptions.setText("Options");
        jMenuOptions.add(jMenuItemCursor);
        jMenuOptions.add(jMenuItemClassicalCursor);
    }

    /**
     * Initializes Advanced select checkbox menu item.
     *
     * @param pane the {@link GraphicsPane} whose cursor is set to normal if
     *             this checkbox is not selected.
     */
    private void initializeAdvancedSelectCheckboxMenuItem(
            final GraphicsPane pane) {
        this.graphicsPane = pane;
        jMenuItemCursor = new JCheckBoxMenuItem("Advanced select", false);
        jMenuItemCursor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemCursor.isSelected();

                if (selected) {
                    GameController.setAdvancedSelect(true);
                } else {
                    GameController.setAdvancedSelect(false);
                    pane.setCursor(LemmCursor.Type.NORMAL);
                }

                Core.getProgramProps().set("advancedSelect",
                        GameController.isAdvancedSelect());
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
        jMenuItemVolume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final GainDialog v = new GainDialog((JFrame) frame, true);
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
        final Sound sound = SoundController.getSound();
        final String[] mixerNames = sound.getMixers();
        final ButtonGroup mixerGroup = new ButtonGroup();
        String lastMixerName = Core.getProgramProps().get("mixerName",
                "Java Sound Audio Engine");

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
            item.addActionListener(new SFXMixerActionListener());

            if (mixerNames[i].equals(lastMixerName)) { // default setting
                item.setState(true);
                sound.setMixer(i);
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
        jMenuItemSound.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemSound.isSelected();

                if (selected) {
                    SoundController.setSoundOn(true);
                } else {
                    SoundController.setSoundOn(false);
                }

                Core.getProgramProps().set("sound",
                        SoundController.isSoundOn());
            }
        });

        jMenuItemSound.setSelected(SoundController.isSoundOn());
    }

    /**
     * Initializes Music checkbox menu item.
     */
    private void initializeMusicCheckboxMenuItem() {
        jMenuItemMusic = new JCheckBoxMenuItem("Music", false);
        final boolean musicOn = SoundController.isMusicOn();
        jMenuItemMusic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final boolean selected = jMenuItemMusic.isSelected();

                if (selected) {
                    SoundController.setMusicOn(true);
                } else {
                    SoundController.setMusicOn(false);
                }

                Core.getProgramProps().set("music", musicOn);

                if (GameController.getLevel() != null) { // to be improved:
                                                         // level is running
                                                         // (game state)
                    if (musicOn) {
                        Music.play();
                    } else {
                        Music.stop();
                    }
                }
            }
        });

        jMenuItemMusic.setSelected(musicOn);
    }

    /**
     * Initializes Enter Level Code menu item.
     */
    private void initializeEnterLevelCodeMenuItem() {
        jMenuItemLevelCode = new JMenuItem("Enter Level Code");
        jMenuItemLevelCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final LevelCodeDialog lcd = new LevelCodeDialog((JFrame) frame,
                        true);
                lcd.setVisible(true);
                String levelCode = lcd.getCode();
                final int lvlPack = lcd.getLevelPack();

                if (levelCode != null && levelCode.length() != 0
                        && lvlPack > 0) {
                    levelCode = levelCode.trim();

                    // cheat mode
                    if (levelCode.equals("0xdeadbeef")) {
                        JOptionPane.showMessageDialog(frame,
                                "All levels and debug mode enabled", "Cheater!",
                                JOptionPane.INFORMATION_MESSAGE);
                        Core.getPlayer().enableCheatMode();
                        updateLevelMenus();
                        return;
                    }

                    // real level code -> get absolute level
                    levelCode = levelCode.toUpperCase();
                    final LevelPack lpack = GameController
                            .getLevelPack(lvlPack);
                    final int lvlAbs = LevelCode.getLevel(lpack.getCodeSeed(),
                            levelCode, lpack.getCodeOffset());

                    if (lvlAbs != -1) {
                        // calculate level pack and relative levelnumber
                        // from absolute number
                        final int[] l = GameController.relLevelNum(lvlPack,
                                lvlAbs);
                        final int diffLvl = l[0];
                        final int lvlRel = l[1];
                        Core.getPlayer().setAvailable(lpack.getName(),
                                lpack.getDiffLevels()[diffLvl], lvlRel);
                        GameController.requestChangeLevel(lvlPack, diffLvl,
                                lvlRel, false);
                        updateLevelMenus();
                        return;
                    }
                }

                // not found
                JOptionPane.showMessageDialog(frame, "Invalid Level Code",
                        "Error", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /**
     * Initializes the Load Replay menu Item.
     */
    private void initializeLoadReplayMenuItem() {
        jMenuItemReplay = new JMenuItem();
        jMenuItemReplay.setText("Load Replay");
        jMenuItemReplay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                final String replayPath = ToolBox.getFileName(lemmini,
                        Core.getResourcePath(), Core.REPLAY_EXTENSIONS, true);

                if (replayPath != null) {
                    try {
                        if (ToolBox.getExtension(replayPath)
                                .equalsIgnoreCase("rpl")) {
                            final ReplayLevelInfo rli = GameController
                                    .loadReplay(replayPath);

                            if (rli != null) {
                                int lpn = -1;

                                for (int i = 0; i < GameController
                                        .getLevelPackNum(); i++) {
                                    if (GameController.getLevelPack(i).getName()
                                            .equals(rli.getLevelPack())) {
                                        lpn = i;
                                    }
                                }

                                if (lpn > -1) {
                                    GameController.requestChangeLevel(lpn,
                                            rli.getDiffLevel(),
                                            rli.getLvlNumber(), true);
                                    return; // success
                                }
                            }
                        }

                        // else: no success
                        JOptionPane.showMessageDialog(frame, "Wrong format!",
                                "Loading replay failed",
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
        jMenuItemLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final java.awt.event.ActionEvent e) {
                String p = ToolBox.getFileName(lemmini, lvlPath,
                        Core.LEVEL_EXTENSIONS, true);

                if (p != null) {
                    try {
                        if (ToolBox.getExtension(p).equalsIgnoreCase("lvl")) {
                            extract.ExtractLevel.convertLevel(p,
                                    Core.getResourcePath() + "/temp.ini");
                            p = Core.getResourcePath() + "/temp.ini";
                        }

                        if (ToolBox.getExtension(p).equalsIgnoreCase("ini")) {
                            final String id = new String(
                                    ToolBox.getFileID(p, 5));

                            if (id.equalsIgnoreCase("# LVL")) {
                                // this is a hack - maybe find a better way
                                GameController.getLevelPack(0).getInfo(0, 0)
                                        .setFileName(p);
                                GameController.getLevelPack(0).getInfo(0, 0)
                                        .setMusic(Music.getRandomTrack());
                                GameController.requestChangeLevel(0, 0, 0,
                                        false);
                                lvlPath = p;
                                return;
                            }
                        }

                        JOptionPane.showMessageDialog(frame, "Wrong format!",
                                "Loading level failed",
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
        jMenuItemRestart.addActionListener(new RestartLevelActionListener());
    }

    /**
     * Loads level packs and creates the level menu.
     *
     * @param difficultyLevelMenus
     */
    private void loadLevelPacksAndCreateLevelMenu(
            final Map<String, List<LvlMenuItem>> difficultyLevelMenus) {
        final ActionListener lvlListener = new LevelMenuActionListener();

        jMenuSelect = new JMenu("Select Level");

        for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip
                                                                        // dummy
                                                                        // level
                                                                        // pack
            final LevelPack lPack = GameController.getLevelPack(lp);
            final JMenu jMenuPack = new JMenu(lPack.getName());
            final String[] difficulties = lPack.getDiffLevels();

            for (int i = 0; i < difficulties.length; i++) {
                // get activated levels for this group
                final GroupBitfield bf = Core.getPlayer()
                        .getBitField(lPack.getName(), difficulties[i]);
                final String[] names = lPack.getLevels(i);
                final JMenu jMenuDiff = new JMenu(difficulties[i]);
                // store menus to access them later
                final List<LvlMenuItem> menuItems = new ArrayList<>();

                for (int n = 0; n < names.length; n++) {
                    final LvlMenuItem jMenuLvl = new LvlMenuItem(names[n], lp,
                            i, n);
                    jMenuLvl.addActionListener(lvlListener);

                    if (Core.getPlayer().isAvailable(bf, n)) {
                        jMenuLvl.setEnabled(true);
                    } else {
                        jMenuLvl.setEnabled(false);
                    }

                    jMenuDiff.add(jMenuLvl);
                    menuItems.add(jMenuLvl);
                }

                jMenuPack.add(jMenuDiff);
                // store menus to access them later
                difficultyLevelMenus.put(
                        LevelPack.getID(lPack.getName(), difficulties[i]),
                        menuItems);
            }

            jMenuSelect.add(jMenuPack);
        }
    }

    /**
     * Update the level menus according to the progress of the current player.
     */
    void updateLevelMenus() {
        // update level menus
        for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip
                                                                        // dummy
                                                                        // level
                                                                        // pack
            final LevelPack lPack = GameController.getLevelPack(lp);
            final String[] difficulties = lPack.getDiffLevels();

            for (int i = 0; i < difficulties.length; i++) {
                // get activated levels for this group
                final GroupBitfield bf = Core.getPlayer()
                        .getBitField(lPack.getName(), difficulties[i]);
                lemmini.updateLevelMenu(lPack.getName(), difficulties[i], bf);
            }
        }
    }

    /**
     * Returns restart menu item.
     *
     * @return restart menu item.
     */
    public final JMenuItem getjMenuItemRestart() {
        return jMenuItemRestart;
    }

    /**
     * Sets restart menu item.
     *
     * @param restartItem restart menu item.
     */
    public final void setjMenuItemRestart(final JMenuItem restartItem) {
        this.jMenuItemRestart = restartItem;
    }

    /**
     * Returns level code menu item.
     *
     * @return level code menu item.
     */
    public final JMenuItem getjMenuItemLevelCode() {
        return jMenuItemLevelCode;
    }

    /**
     * Sets level code menu item.
     *
     * @param levelCodeItem level code menu item.
     */
    public final void setjMenuItemLevelCode(final JMenuItem levelCodeItem) {
        this.jMenuItemLevelCode = levelCodeItem;
    }

    /**
     * Returns select menu.
     *
     * @return select menu.
     */
    public final JMenuItem getjMenuSelect() {
        return jMenuSelect;
    }

    /**
     * Sets select menu.
     *
     * @param selectMenu select menu.
     */
    public final void setjMenuSelect(final JMenuItem selectMenu) {
        this.jMenuSelect = selectMenu;
    }

    /**
     * Returns SFX menu.
     *
     * @return SFX menu.
     */
    public final JMenu getjMenuSFX() {
        return jMenuSFX;
    }

    /**
     * Sets SFX menu.
     *
     * @param sfxMenu SFX menu.
     */
    public final void setjMenuSFX(final JMenu sfxMenu) {
        this.jMenuSFX = sfxMenu;
    }

    /**
     * Returns load menu item.
     *
     * @return load menu item.
     */
    public final JMenuItem getjMenuItemLoad() {
        return jMenuItemLoad;
    }

    /**
     * Sets load menu item.
     *
     * @param loadItem load menu item.
     */
    public final void setjMenuItemLoad(final JMenuItem loadItem) {
        this.jMenuItemLoad = loadItem;
    }

    /**
     * Returns replay menu item.
     *
     * @return replay menu item.
     */
    public final JMenuItem getjMenuItemReplay() {
        return jMenuItemReplay;
    }

    /**
     * Sets replay menu item.
     *
     * @param replayItem replay menu item.
     */
    public final void setjMenuItemReplay(final JMenuItem replayItem) {
        this.jMenuItemReplay = replayItem;
    }

    /**
     * Returns music menu item.
     *
     * @return music menu item.
     */
    public final JCheckBoxMenuItem getjMenuItemMusic() {
        return jMenuItemMusic;
    }

    /**
     * Sets music menu item.
     *
     * @param musicItem music menu item.
     */
    public final void setjMenuItemMusic(final JCheckBoxMenuItem musicItem) {
        this.jMenuItemMusic = musicItem;
    }

    /**
     * Returns sound menu item.
     *
     * @return sound menu item.
     */
    public final JCheckBoxMenuItem getjMenuItemSound() {
        return jMenuItemSound;
    }

    /**
     * Sets sound menu item.
     *
     * @param soundItem sound menu item.
     */
    public final void setjMenuItemSound(final JCheckBoxMenuItem soundItem) {
        this.jMenuItemSound = soundItem;
    }

    /**
     * Returns level menu.
     *
     * @return level menu.
     */
    public final JMenu getjMenuLevel() {
        return jMenuLevel;
    }

    /**
     * Sets level menu.
     *
     * @param levelMenu level menu.
     */
    public final void setjMenuLevel(final JMenu levelMenu) {
        this.jMenuLevel = levelMenu;
    }

    /**
     * Returns file menu.
     *
     * @return file menu.
     */
    public final JMenu getjMenuFile() {
        return jMenuFile;
    }

    /**
     * Sets file menu.
     *
     * @param fileMenu file menu.
     */
    public final void setjMenuFile(final JMenu fileMenu) {
        this.jMenuFile = fileMenu;
    }

    /**
     * Returns player menu.
     *
     * @return player menu.
     */
    public final JMenu getjMenuPlayer() {
        return jMenuPlayer;
    }

    /**
     * Sets player menu.
     *
     * @param playerMenu player menu.
     */
    public final void setjMenuPlayer(final JMenu playerMenu) {
        this.jMenuPlayer = playerMenu;
    }

    /**
     * Returns exit menu item.
     *
     * @return exit menu item.
     */
    public final JMenuItem getjMenuItemExit() {
        return jMenuItemExit;
    }

    /**
     * Sets exit menu item.
     *
     * @param exitItem exit menu item.
     */
    public final void setjMenuItemExit(final JMenuItem exitItem) {
        this.jMenuItemExit = exitItem;
    }

    /**
     * Returns select player menu.
     *
     * @return select player menu.
     */
    public final JMenu getjMenuSelectPlayer() {
        return jMenuSelectPlayer;
    }

    /**
     * Sets select player menu.
     *
     * @param selectPlayerMenu select player menu.
     */
    public final void setjMenuSelectPlayer(final JMenu selectPlayerMenu) {
        this.jMenuSelectPlayer = selectPlayerMenu;
    }

    /**
     * Returns manage player menu item.
     *
     * @return manage player menu item.
     */
    public final JMenuItem getjMenuItemManagePlayer() {
        return jMenuItemManagePlayer;
    }

    /**
     * Sets manage player menu item.
     *
     * @param managePlayerItem manage player menu item.
     */
    public final void setjMenuItemManagePlayer(
            final JMenuItem managePlayerItem) {
        this.jMenuItemManagePlayer = managePlayerItem;
    }

    /**
     * Returns player button group.
     *
     * @return player button group.
     */
    public final ButtonGroup getPlayerGroup() {
        return playerGroup;
    }

    /**
     * Sets player button group.
     *
     * @param playerButtonGroup player button group.
     */
    public final void setPlayerGroup(final ButtonGroup playerButtonGroup) {
        this.playerGroup = playerButtonGroup;
    }

    /**
     * Returns zoom button group.
     *
     * @return zoom button group.
     */
    public final ButtonGroup getZoomGroup() {
        return zoomGroup;
    }

    /**
     * Sets zoom button group.
     *
     * @param zoomButtonGroup zoom button group.
     */
    public final void setZoomGroup(final ButtonGroup zoomButtonGroup) {
        this.zoomGroup = zoomButtonGroup;
    }

    /**
     * Returns options menu.
     *
     * @return options menu.
     */
    public final JMenu getjMenuOptions() {
        return jMenuOptions;
    }

    /**
     * Sets options menu.
     *
     * @param optionsMenu options menu.
     */
    public final void setjMenuOptions(final JMenu optionsMenu) {
        this.jMenuOptions = optionsMenu;
    }

    /**
     * Returns classical cursor menu item.
     *
     * @return classical cursor menu item.
     */
    public final JMenuItem getjMenuItemClassicalCursor() {
        return jMenuItemClassicalCursor;
    }

    /**
     * Sets classical cursor menu item.
     *
     * @param classicalCursorItem classical cursor menu item.
     */
    public final void setjMenuItemClassicalCursor(
            final JMenuItem classicalCursorItem) {
        this.jMenuItemClassicalCursor = classicalCursorItem;
    }

    /**
     * Returns sound menu.
     *
     * @return sound menu.
     */
    public final JMenu getjMenuSound() {
        return jMenuSound;
    }

    /**
     * Sets sound menu.
     *
     * @param soundMenu sound menu.
     */
    public final void setjMenuSound(final JMenu soundMenu) {
        this.jMenuSound = soundMenu;
    }

    /**
     * Returns volume menu item.
     *
     * @return volume menu item.
     */
    public final JMenuItem getjMenuItemVolume() {
        return jMenuItemVolume;
    }

    /**
     * Sets volume menu item.
     *
     * @param volumeItem volume menu item.
     */
    public final void setjMenuItemVolume(final JMenuItem volumeItem) {
        this.jMenuItemVolume = volumeItem;
    }

    /**
     * Returns cursor menu item.
     *
     * @return cursor menu item.
     */
    public final JMenuItem getjMenuItemCursor() {
        return jMenuItemCursor;
    }

    /**
     * Sets cursor menu item.
     *
     * @param cursorItem cursor menu item.
     */
    public final void setjMenuItemCursor(final JMenuItem cursorItem) {
        this.jMenuItemCursor = cursorItem;
    }

    /**
     * Returns path for loading single level files.
     *
     * @return path for loading single level files.
     */
    public final String getLvlPath() {
        return lvlPath;
    }

    /**
     * Sets path for loading single level files.
     *
     * @param levelFilesPath path for loading single level files.
     */
    public final void setLvlPath(final String levelFilesPath) {
        this.lvlPath = levelFilesPath;
    }

    /**
     * Returns parent Lemmini application class.
     *
     * @return parent Lemmini application class.
     */
    public final Lemmini getLemmini() {
        return lemmini;
    }

    /**
     * Sets parent Lemmini application class.
     *
     * @param parentApp parent Lemmini application class.
     */
    public final void setLemmini(final Lemmini parentApp) {
        this.lemmini = parentApp;
    }

    /**
     * Returns graphics pane.
     *
     * @return graphics pane.
     */
    public final GraphicsPane getGraphicsPane() {
        return graphicsPane;
    }

    /**
     * Sets graphics pane.
     *
     * @param pane graphics pane.
     */
    public final void setGraphicsPane(final GraphicsPane pane) {
        this.graphicsPane = pane;
    }

    /**
     * Returns parent frame.
     *
     * @return parent frame.
     */
    public final JFrame getFrame() {
        return frame;
    }

    /**
     * Sets parent frame.
     *
     * @param parentFrame parent frame.
     */
    public final void setFrame(final JFrame parentFrame) {
        this.frame = parentFrame;
    }

}
