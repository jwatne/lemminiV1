package lemmini;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import game.Core;
import game.GameController;
import game.GroupBitfield;
import game.Icons;
import game.LemmCursor;
import game.LemmException;
import game.LemmingHandler;
import game.LevelPack;
import game.ResourceException;
import game.GameState;
import game.TransitionState;
import game.lemmings.Lemming;
import game.lemmings.SkillHandler;
import game.level.Level;
import game.level.ReleaseRateHandler;
import gameutil.Fader;
import gameutil.FaderState;
import tools.Props;
import tools.ToolBox;

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
 * Lemmini - a game engine for Lemmings.<br>
 * This is the main window including input handling. The game logic is located
 * in {@link GameController}, some core components are in {@link Core}.<br>
 * <br>
 * Note: this was developed for JRE1.4 and only ported to JRE1.5 after it was
 * finished. Also the design evolved during two years of development and thus
 * isn't nearly as clean as it should be. During the porting to 1.5, I cleaned
 * up some things here and there, but didn't want to redesign the whole thing
 * from scratch.
 * <p>
 * For the LemminiV1 fork, John Watne adapted for JDK 17 and dropped separate
 * logic for runtimes prior to JDK 1.5. Extensive refactoring to modularize the
 * code into more cohesive Objects and methods. Implemented several Checkstyle
 * fixes compliant with the standard Sun checks.
 * </p>
 *
 * @author Volker Oth
 */
public class Lemmini extends JFrame implements KeyListener {
    /**
     * 1K = 1024.
     */
    private static final int ONE_K = 1024;
    /**
     * Minimum free memory, in MB.
     */
    private static final int MIN_FREE_MEMORY_MB = 60;
    /**
     * minimum sleep duration in milliseconds - values too small may cause
     * system clock shift under WinXP etc.
     */
    static final int MIN_SLEEP = 10;
    /**
     * Threshold for sleep - don't sleep if time to wait is shorter than this as
     * sleep might return too late.
     */
    static final int THR_SLEEP = 16;

    private static final long serialVersionUID = 0x01;

    /** Self reference.. */
    private static JFrame thisFrame;

    /**
     * Returns the main Lemmini object.
     *
     * @return the main Lemmini object.
     */
    public static JFrame getThisFrame() {
        return thisFrame;
    }

    /**
     * Sets the main Lemmini object.
     *
     * @param lemminiFrame the main Lemmini object.
     */
    public static void setThisFrame(final JFrame lemminiFrame) {
        Lemmini.thisFrame = lemminiFrame;
    }

    /** Path for loading single level files.. */
    @SuppressWarnings("unused")
    private final String lvlPath;
    /** Map to store menu items for difficulty levels.. */
    private final Map<String, List<LvlMenuItem>> levelMenus = new HashMap<>();
    /** Panel for the game graphics.. */
    private final GraphicsPane gp;

    /**
     * Constructor of the main frame.
     */
    Lemmini() {
        try {
            Core.init(this); // initialize Core object
            GameController.init(this);
            GameController.setLevelMenuUpdateListener(
                    new LevelMenuUpdateListener(this));
        } catch (final ResourceException ex) {
            Core.resourceError(ex.getMessage());
        } catch (final LemmException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (final Exception ex) {
            ToolBox.showException(ex);
            System.exit(1);
        } catch (final Error ex) {
            ToolBox.showException(ex);
            System.exit(1);
        }

        setFrameSizeAndPosition();
        this.validate(); // force redraw
        this.setTitle("Lemmini");
        final ClassLoader loader = Lemmini.class.getClassLoader();
        final Image img = Toolkit.getDefaultToolkit()
                .getImage(loader.getResource("icon_32.png"));
        setIconImage(img);
        // set component pane
        gp = new GraphicsPane(this);
        gp.setDoubleBuffered(false);
        this.setContentPane(gp);
        this.pack();
        this.validate(); // force redraw
        this.setTitle("Lemmini");
        final MenuCreator menuCreator = new MenuCreator(this);
        this.setJMenuBar(
                menuCreator.getLemminiMenuBar(this, this.gp, this.levelMenus));
        this.addWindowListener(new WindowClosingListener(this));
        this.setVisible(true);
        gp.init();
        GameController.setGameState(GameState.INTRO);
        GameController.setTransition(TransitionState.NONE);
        Fader.setBounds(Core.getDrawWidth(), Core.getDrawHeight());
        Fader.setState(FaderState.IN);
        final Thread t = new Thread(gp);
        lvlPath = ".";
        addKeyListener(this);
        t.start();
    }

    /**
     * Sets the size and position for this frame.
     */
    private void setFrameSizeAndPosition() {
        // read frame props
        int posX;
        int posY;
        this.setSize((int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale()));
        this.setMinimumSize(new Dimension(
                (int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale())));
        this.setMaximumSize(new Dimension(
                (int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale())));
        this.setResizable(false); // at least for the moment: forbid resize
        final Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getCenterPoint();
        p.x -= this.getWidth() / 2;
        p.y -= this.getHeight() / 2;
        posX = Core.getProgramProps().get("framePosX", p.x > 0 ? p.x : 0);
        posY = Core.getProgramProps().get("framePosY", p.y > 0 ? p.y : 0);
        this.setLocation(posX, posY);
    }

    /**
     * The main function. Entry point of the program.
     *
     * @param args
     */
    public static void main(final String[] args) {
        /*
         * Set "Look and Feel" to system default
         */
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
            System.out.println(
                    "Unable to set look and feel to system: " + e.getMessage());
            /* don't care */
        }

        /*
         * Apple menu bar for MacOS
         */
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // check free memory
        final long free = Runtime.getRuntime().maxMemory();

        if (free < MIN_FREE_MEMORY_MB * ONE_K * ONE_K) { // 64MB doesn't seem to
                                                         // work even if set
            // with -Xmx64M
            JOptionPane.showMessageDialog(null,
                    "You need at least 64MB of heap", "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        thisFrame = new Lemmini();

    }

    /**
     * Sets the size of the Lemmini window.
     *
     * @param scale the scaling factor.
     */
    public final void setScale(final double scale) {
        gp.shutdown();
        Core.setScale(scale);
        setSize((int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale()));
        this.setMinimumSize(new Dimension(
                (int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale())));
        this.setMaximumSize(new Dimension(
                (int) Math.round(Core.getDrawWidth() * Core.getScale()),
                (int) Math.round(Core.getDrawHeight() * Core.getScale())));
        pack();
        validate(); // force redraw
        gp.init();
    }

    /**
     * Update the level menus according to the given progress information.
     *
     * @param pack name of level pack
     * @param diff name of difficulty level
     * @param bf   bitmap containing availability flags for each level
     */
    public void updateLevelMenu(final String pack, final String diff,
            final GroupBitfield bf) {
        final List<LvlMenuItem> menuItems = levelMenus
                .get(LevelPack.getID(pack, diff));

        for (int k = 0; k < menuItems.size(); k++) {
            // select level, e.g. "All fall down"
            final JMenuItem level = menuItems.get(k);

            if (k == 0 || Core.getPlayer().isAvailable(bf, k)) {
                level.setEnabled(true);
            } else {
                level.setEnabled(false);
            }
        }
    }

    /**
     * Development function: patch current level x offset in the level
     * configuration file. Works only in cheat mode.
     *
     * @param configPath path of level configuration files
     */
    private void patchLevel(final String configPath) {
        final List<String> lines = new ArrayList<String>();

        try (BufferedReader r = new BufferedReader(
                new FileReader(configPath));) {
            String l;

            while ((l = r.readLine()) != null) {
                lines.add(l);
            }
        } catch (final IOException e) {
            System.out.println("Error reading patchLevel: " + e.getMessage());
        }

        try (FileWriter sw = new FileWriter(configPath)) {
            for (int i = 0; i < lines.size(); i++) {
                final String s = lines.get(i);

                if (s.startsWith("xPos =")) {
                    sw.write("xPos = "
                            + Integer.toString(GameController.getxPos())
                            + "\n");
                } else {
                    sw.write(s + "\n");
                }
            }
        } catch (final IOException ex) {
            System.out.println("Error writing position: " + ex.getMessage());
        }
    }

    @Override
    public final void keyPressed(final KeyEvent keyevent) {
        final int code = keyevent.getKeyCode();

        if (GameController.getGameState() == GameState.LEVEL) {
            switch (code) {
            case KeyEvent.VK_1:
            case KeyEvent.VK_F3:
                SkillHandler.handleIconButton(Icons.Type.CLIMB);
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_F4:
                SkillHandler.handleIconButton(Icons.Type.FLOAT);
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_F5:
                SkillHandler.handleIconButton(Icons.Type.BOMB);
                break;
            case KeyEvent.VK_4:
            case KeyEvent.VK_F6:
                SkillHandler.handleIconButton(Icons.Type.BLOCK);
                break;
            case KeyEvent.VK_5:
            case KeyEvent.VK_F7:
                SkillHandler.handleIconButton(Icons.Type.BUILD);
                break;
            case KeyEvent.VK_6:
            case KeyEvent.VK_F8:
                SkillHandler.handleIconButton(Icons.Type.BASH);
                break;
            case KeyEvent.VK_7:
            case KeyEvent.VK_F9:
                SkillHandler.handleIconButton(Icons.Type.MINE);
                break;
            case KeyEvent.VK_8:
            case KeyEvent.VK_F10:
                SkillHandler.handleIconButton(Icons.Type.DIG);
                break;
            case KeyEvent.VK_D:
                toggleDebugDraw();
                break;
            case KeyEvent.VK_W:
                handleWKey();
                break;
            case KeyEvent.VK_L:
                printCurrentLevelOnConsole();
                break;
            case KeyEvent.VK_S: // superlemming on/off
                toggleSuperLemming();
                break;
            case KeyEvent.VK_C:
                toggleCheatMode();
                break;
            case KeyEvent.VK_F11:
            case KeyEvent.VK_P:
                GameController.setPaused(!GameController.isPaused());
                GameController.pressIcon(Icons.Type.PAUSE);
                break;
            case KeyEvent.VK_F:
            case KeyEvent.VK_ENTER:
                GameController.setFastForward(!GameController.isFastForward());
                GameController.pressIcon(Icons.Type.FFWD);
                break;
            case KeyEvent.VK_X:
                doPatchLevelIfCheatEnabled();
                break;
            case KeyEvent.VK_RIGHT /* 39 */:
                processRightArrow();
                break;
            case KeyEvent.VK_LEFT /* 37 */:
                processLeftArrow();
                break;
            case KeyEvent.VK_UP:
                gp.setCursor(LemmCursor.Type.WALKER);
                break;
            case KeyEvent.VK_SHIFT:
                gp.setShiftPressed(true);
                break;
            case KeyEvent.VK_SPACE:
                putNewLemmingAtCursorPosition();
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_F2:
                ReleaseRateHandler.pressPlus(GameController.KEYREPEAT_KEY);
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_F1:
                ReleaseRateHandler.pressMinus(GameController.KEYREPEAT_KEY);
                break;
            case KeyEvent.VK_F12:
                SkillHandler.handleIconButton(Icons.Type.NUKE);
                break;
            default:
                break;
            }

            keyevent.consume();
        }
    }

    /**
     * Puts a new Lemming at the current cursor position, if cheat mode is
     * enabled.
     */
    private void putNewLemmingAtCursorPosition() {
        if (GameController.isCheat()) {
            final Lemming l = new Lemming(gp.getCursorX(), gp.getCursorY());

            synchronized (LemmingHandler.getLemmings()) {
                LemmingHandler.getLemmings().add(l);
            }
        }
    }

    /**
     * Processes left arrow keypress.
     */
    private void processLeftArrow() {
        if (GameController.isAdvancedSelect()) {
            gp.setCursor(LemmCursor.Type.LEFT);
        } else {
            final int xOfsTemp = GameController.getxPos()
                    - ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST
                            : GraphicsPane.X_STEP);

            if (xOfsTemp > 0) {
                GameController.setxPos(xOfsTemp);
            } else {
                GameController.setxPos(0);
            }
        }
    }

    /**
     * Processes right arrow keypress.
     */
    private void processRightArrow() {
        if (GameController.isAdvancedSelect()) {
            gp.setCursor(LemmCursor.Type.RIGHT);
        } else {
            final int xOfsTemp = GameController.getxPos()
                    + ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST
                            : GraphicsPane.X_STEP);

            if (xOfsTemp < Level.WIDTH - this.getWidth()) {
                GameController.setxPos(xOfsTemp);
            } else {
                GameController.setxPos(Level.WIDTH - this.getWidth());
            }
        }
    }

    /**
     * Calls {@link #patchLevel(String)} if cheat mode is enabled.
     */
    private void doPatchLevelIfCheatEnabled() {
        if (GameController.isCheat()) {
            patchLevel(
                    GameController
                            .getLevelPack(GameController.getCurLevelPackIdx())
                            .getInfo(GameController.getCurDiffLevel(),
                                    GameController.getCurLevelNumber())
                            .getFileName());
        }
    }

    /**
     * Toggles cheat mode.
     */
    private void toggleCheatMode() {
        if (Core.getPlayer().isCheat()) {
            GameController.setCheat(!GameController.isCheat());

            if (GameController.isCheat()) {
                GameController.setWasCheated(true);
            }
        } else {
            GameController.setCheat(false);
        }
    }

    /**
     * Toggles super lemming on/off, if cheat mode is enabled.
     */
    private void toggleSuperLemming() {
        if (GameController.isCheat()) {
            GameController.setSuperLemming(!GameController.isSuperLemming());
        } else {
            try {
                final File file = new File(
                        Core.getResourcePath() + "/level.png");
                final BufferedImage tmp = GameController.getLevel()
                        .createMiniMap(null, GameController.getBgImage(), 1, 1,
                                false);
                ImageIO.write(tmp, "png", file);
            } catch (final Exception ex) {
                System.out.println("I/O error: " + ex.getMessage());
            }
        }
    }

    /**
     * Prints the current level on the console if cheat mode is enabled.
     */
    private void printCurrentLevelOnConsole() {
        if (GameController.isCheat()) {
            System.out
                    .println(GameController
                            .getLevelPack(GameController.getCurLevelPackIdx())
                            .getInfo(GameController.getCurDiffLevel(),
                                    GameController.getCurLevelNumber())
                            .getFileName());
        }
    }

    /**
     * Process a W keypress.
     */
    private void handleWKey() {
        if (GameController.isCheat()) {
            GameController.setNumLeft(GameController.getNumLemmingsMax());
            GameController.endLevel();
        }
    }

    /**
     * Toggles state of debugDraw option, if cheat mode is enabled.
     */
    private void toggleDebugDraw() {
        if (GameController.isCheat()) {
            gp.setDebugDraw(!gp.getDebugDraw());
        }
    }

    @Override
    public final void keyReleased(final KeyEvent keyevent) {
        final int code = keyevent.getKeyCode();

        if (GameController.getGameState() == GameState.LEVEL) {
            switch (code) {
            case KeyEvent.VK_SHIFT:
                gp.setShiftPressed(false);
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_F2:
                ReleaseRateHandler.releasePlus(GameController.KEYREPEAT_KEY);
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_F1:
                ReleaseRateHandler.releaseMinus(GameController.KEYREPEAT_KEY);
                break;
            case KeyEvent.VK_F12:
                GameController.releaseIcon(Icons.Type.NUKE);
                break;
            case KeyEvent.VK_LEFT:
                if (LemmCursor.getType() == LemmCursor.Type.LEFT) {
                    gp.setCursor(LemmCursor.Type.NORMAL);
                }

                break;
            case KeyEvent.VK_RIGHT:
                if (LemmCursor.getType() == LemmCursor.Type.RIGHT) {
                    gp.setCursor(LemmCursor.Type.NORMAL);
                }

                break;
            case KeyEvent.VK_UP:
                if (LemmCursor.getType() == LemmCursor.Type.WALKER) {
                    gp.setCursor(LemmCursor.Type.NORMAL);
                }

                break;
            default:
                break;
            }
        }
    }

    @Override
    public void keyTyped(final KeyEvent keyevent) {

    }

    /**
     * Common exit method to use in exit events.
     */
    void exit() {
        // store width and height
        final Dimension d = this.getSize();
        Props programProps = Core.getProgramProps();
        programProps.set("frameWidth", d.width);
        programProps.set("frameHeight", d.height);
        // store frame pos
        final Point p = this.getLocation();
        programProps.set("framePosX", p.x);
        programProps.set("framePosY", p.y);
        //
        Core.saveProgramProps();
        System.exit(0);
    }
}
