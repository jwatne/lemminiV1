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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import Game.Core;
import Game.GameController;
import Game.GroupBitfield;
import Game.Icons;
import Game.LemmCursor;
import Game.LemmException;
import Game.Lemming;
import Game.Level;
import Game.LevelPack;
import Game.ResourceException;
import GameUtil.Fader;
import Tools.ToolBox;

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
 * in
 * {@link GameController}, some core components are in {@link Core}.<br>
 * <br>
 * Note: this was developed for JRE1.4 and only ported to JRE1.5 after it was
 * finished.
 * Also the design evolved during two years of development and thus isn't nearly
 * as clean
 * as it should be. During the porting to 1.5, I cleaned up some things here and
 * there,
 * but didn't want to redesign the whole thing from scratch.
 *
 * @author Volker Oth
 */
public class Lemmini extends JFrame implements KeyListener {
	/**
	 * minimum sleep duration in milliseconds - values too small may cause system
	 * clock shift under WinXP etc.
	 */
	final static int MIN_SLEEP = 10;
	/**
	 * threshold for sleep - don't sleep if time to wait is shorter than this as
	 * sleep might return too late
	 */
	final static int THR_SLEEP = 16;

	private final static long serialVersionUID = 0x01;

	/** self reference */
	static JFrame thisFrame;

	/** path for loading single level files */
	@SuppressWarnings("unused")
	private String lvlPath;
	/** HashMap to store menu items for difficulty levels */
	private HashMap<String, ArrayList<LvlMenuItem>> diffLevelMenus;

	public HashMap<String, ArrayList<LvlMenuItem>> getDiffLevelMenus() {
		return diffLevelMenus;
	}

	public void setDiffLevelMenus(HashMap<String, ArrayList<LvlMenuItem>> diffLevelMenus) {
		this.diffLevelMenus = diffLevelMenus;
	}

	/** panel for the game graphics */
	private GraphicsPane gp;

	/**
	 * Returns the panel for the game graphics.
	 * 
	 * @return the panel for the game graphics.
	 */
	public GraphicsPane getGp() {
		return gp;
	}

	/**
	 * Constructor of the main frame.
	 */
	Lemmini() {
		try {
			Core.init(this); // initialize Core object
			GameController.init();
			GameController.setLevelMenuUpdateListener(new LevelMenuUpdateListener(this));
		} catch (final ResourceException ex) {
			Core.resourceError(ex.getMessage());
		} catch (final LemmException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
		final Image img = Toolkit.getDefaultToolkit().getImage(loader.getResource("icon_32.png"));
		setIconImage(img);
		// set component pane
		gp = new GraphicsPane();
		gp.setDoubleBuffered(false);
		this.setContentPane(gp);
		this.pack();
		this.validate(); // force redraw
		this.setTitle("Lemmini");
		final MenuCreator menuCreator = new MenuCreator();
		// getLemminiMenuBar();
		this.setJMenuBar(menuCreator.getLemminiMenuBar(this));
		this.addWindowListener(new WindowClosingListener(this));
		this.setVisible(true);
		gp.init();
		GameController.setGameState(GameController.State.INTRO);
		GameController.setTransition(GameController.TransitionState.NONE);
		Fader.setBounds(Core.getDrawWidth(), Core.getDrawHeight());
		Fader.setState(Fader.State.IN);
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
		int posX, posY;
		this.setSize((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale()));
		this.setMinimumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale())));
		this.setMaximumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale())));
		this.setResizable(false); // at least for the moment: forbid resize
		final Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		p.x -= this.getWidth() / 2;
		p.y -= this.getHeight() / 2;
		posX = Core.programProps.get("framePosX", p.x > 0 ? p.x : 0);
		posY = Core.programProps.get("framePosY", p.y > 0 ? p.y : 0);
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
			/* don't care */}
		/*
		 * Apple menu bar for MacOS
		 */
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// check free memory
		final long free = Runtime.getRuntime().maxMemory();

		if (free < 60 * 1024 * 1024) { // 64MB doesn't seem to work even if set with -Xmx64M
			JOptionPane.showMessageDialog(null, "You need at least 64MB of heap", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		// workaround to adjust time base to 1ms under XP
		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6435126
		new Thread() {
			{
				this.setDaemon(true);
				this.start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (final InterruptedException ex) {
					}
				}
			}
		};

		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		thisFrame = new Lemmini();
	}

	public void setScale(final double scale) {
		gp.shutdown();
		Core.setScale(scale);
		setSize((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale()));
		this.setMinimumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale())));
		this.setMaximumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
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
	public void updateLevelMenu(final String pack, final String diff, final GroupBitfield bf) {
		final ArrayList<LvlMenuItem> menuItems = diffLevelMenus.get(LevelPack.getID(pack, diff));

		for (int k = 0; k < menuItems.size(); k++) {
			// select level, e.g. "All fall down"
			final JMenuItem level = menuItems.get(k);

			if (k == 0 || Core.player.isAvailable(bf, k)) {
				level.setEnabled(true);
			} else {
				level.setEnabled(false);
			}
		}

	}

	/**
	 * Development function: patch current level x offset in the level configuration
	 * file.
	 * Works only in cheat mode.
	 * 
	 * @param lvlPath path of level configuration files
	 */
	private void patchLevel(final String lvlPath) {
		try {
			final ArrayList<String> lines = new ArrayList<String>();
			final BufferedReader r = new BufferedReader(new FileReader(lvlPath));
			String l;

			while ((l = r.readLine()) != null) {
				lines.add(l);
			}

			r.close();
			final FileWriter sw = new FileWriter(lvlPath);

			for (int i = 0; i < lines.size(); i++) {
				final String s = lines.get(i);

				if (s.startsWith("xPos =")) {
					sw.write("xPos = " + Integer.toString(GameController.getxPos()) + "\n");
				} else {
					sw.write(s + "\n");
				}
			}

			sw.close();
		} catch (final FileNotFoundException ex) {
		} catch (final IOException ex) {
		}
	}

	@Override
	public void keyPressed(final KeyEvent keyevent) {
		final int code = keyevent.getKeyCode();

		if (GameController.getGameState() == GameController.State.LEVEL) {
			switch (code) {
				case KeyEvent.VK_1:
				case KeyEvent.VK_F3:
					GameController.handleIconButton(Icons.Type.CLIMB);
					break;
				case KeyEvent.VK_2:
				case KeyEvent.VK_F4:
					GameController.handleIconButton(Icons.Type.FLOAT);
					break;
				case KeyEvent.VK_3:
				case KeyEvent.VK_F5:
					GameController.handleIconButton(Icons.Type.BOMB);
					break;
				case KeyEvent.VK_4:
				case KeyEvent.VK_F6:
					GameController.handleIconButton(Icons.Type.BLOCK);
					break;
				case KeyEvent.VK_5:
				case KeyEvent.VK_F7:
					GameController.handleIconButton(Icons.Type.BUILD);
					break;
				case KeyEvent.VK_6:
				case KeyEvent.VK_F8:
					GameController.handleIconButton(Icons.Type.BASH);
					break;
				case KeyEvent.VK_7:
				case KeyEvent.VK_F9:
					GameController.handleIconButton(Icons.Type.MINE);
					break;
				case KeyEvent.VK_8:
				case KeyEvent.VK_F10:
					GameController.handleIconButton(Icons.Type.DIG);
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
					GameController.pressPlus(GameController.KEYREPEAT_KEY);
					break;
				case KeyEvent.VK_MINUS:
				case KeyEvent.VK_SUBTRACT:
				case KeyEvent.VK_F1:
					GameController.pressMinus(GameController.KEYREPEAT_KEY);
					break;
				case KeyEvent.VK_F12:
					GameController.handleIconButton(Icons.Type.NUKE);
					break;
			}

			keyevent.consume();
		}
	}

	/**
	 * Puts a new Lemming at the current cursor position, if cheat mode is enabled.
	 */
	private void putNewLemmingAtCursorPosition() {
		if (GameController.isCheat()) {
			final Lemming l = new Lemming(gp.getCursorX(), gp.getCursorY());

			synchronized (GameController.getLemmings()) {
				GameController.getLemmings().add(l);
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
					- ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST : GraphicsPane.X_STEP);

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
					+ ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST : GraphicsPane.X_STEP);

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
			patchLevel(GameController.getLevelPack(GameController.getCurLevelPackIdx())
					.getInfo(GameController.getCurDiffLevel(), GameController.getCurLevelNumber())
					.getFileName());
		}
	}

	/**
	 * Toggles cheat mode.
	 */
	private void toggleCheatMode() {
		if (Core.player.isCheat()) {
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
				final File file = new File(Core.resourcePath + "/level.png");
				final BufferedImage tmp = GameController.getLevel().createMiniMap(null,
						GameController.getBgImage(), 1, 1, false);
				ImageIO.write(tmp, "png", file);
			} catch (final Exception ex) {
			}
		}
	}

	/**
	 * Prints the current level on the console if cheat mode is enabled.
	 */
	private void printCurrentLevelOnConsole() {
		if (GameController.isCheat()) {
			System.out.println(GameController.getLevelPack(GameController.getCurLevelPackIdx())
					.getInfo(GameController.getCurDiffLevel(), GameController.getCurLevelNumber())
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
	public void keyReleased(final KeyEvent keyevent) {
		final int code = keyevent.getKeyCode();

		if (GameController.getGameState() == GameController.State.LEVEL) {
			switch (code) {
				case KeyEvent.VK_SHIFT:
					gp.setShiftPressed(false);
					break;
				case KeyEvent.VK_PLUS:
				case KeyEvent.VK_ADD:
				case KeyEvent.VK_F2:
					GameController.releasePlus(GameController.KEYREPEAT_KEY);
					break;
				case KeyEvent.VK_MINUS:
				case KeyEvent.VK_SUBTRACT:
				case KeyEvent.VK_F1:
					GameController.releaseMinus(GameController.KEYREPEAT_KEY);
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
		Core.programProps.set("frameWidth", d.width);
		Core.programProps.set("frameHeight", d.height);
		// store frame pos
		final Point p = this.getLocation();
		Core.programProps.set("framePosX", p.x);
		Core.programProps.set("framePosY", p.y);
		//
		Core.saveProgramProps();
		System.exit(0);
	}
}
