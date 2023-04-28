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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import GUI.GainDialog;
import GUI.LevelCodeDialog;
import GUI.PlayerDialog;
import Game.Core;
import Game.GameController;
import Game.GroupBitfield;
import Game.Icons;
import Game.LemmCursor;
import Game.LemmException;
import Game.Lemming;
import Game.Level;
import Game.LevelCode;
import Game.LevelPack;
import Game.Music;
import Game.Player;
import Game.ReplayLevelInfo;
import Game.ResourceException;
import Game.UpdateListener;
import GameUtil.Fader;
import Tools.ToolBox;
import javax.swing.JRadioButtonMenuItem;

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
	private String lvlPath;
	/** HashMap to store menu items for difficulty levels */
	private HashMap<String, ArrayList<LvlMenuItem>> diffLevelMenus;
	/** panel for the game graphics */
	private GraphicsPane gp;

	// Swing stuff
	private JMenuBar jMenuBar = null;
	private JMenu jMenuLevel = null;
	private JMenuItem jMenuItemRestart = null;
	private JMenuItem jMenuItemLevelCode = null;
	private JMenuItem jMenuSelect = null;
	private JMenu jMenuFile = null;
	private JMenu jMenuPlayer = null;
	private JMenu jMenuSelectPlayer = null;
	private JMenu jMenuSound = null;
	private JMenu jMenuSFX = null;
	private JMenuItem jMenuItemVolume = null;
	private JMenu jMenuOptions = null;
	private JMenuItem jMenuItemCursor = null;
	private JMenuItem jMenuItemClassicalCursor = null;
	private JMenuItem jMenuItemExit = null;
	private JMenuItem jMenuItemManagePlayer = null;
	private JMenuItem jMenuItemLoad = null;
	private JMenuItem jMenuItemReplay = null;
	private JCheckBoxMenuItem jMenuItemMusic = null;
	private JCheckBoxMenuItem jMenuItemSound = null;
	private ButtonGroup playerGroup = null;
	private ButtonGroup zoomGroup = null;

	/**
	 * Constructor of the main frame.
	 */
	Lemmini() {
		try {
			Core.init(this); // initialize Core object
			GameController.init();
			GameController.setLevelMenuUpdateListener(new LevelMenuUpdateListener());
		} catch (ResourceException ex) {
			Core.resourceError(ex.getMessage());
		} catch (LemmException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} catch (Exception ex) {
			ToolBox.showException(ex);
			System.exit(1);
		} catch (Error ex) {
			ToolBox.showException(ex);
			System.exit(1);
		}

		// read frame props
		int posX, posY;
		this.setSize((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale()));
		this.setMinimumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale())));
		this.setMaximumSize(new Dimension((int) Math.round(Core.getDrawWidth() * Core.getScale()),
				(int) Math.round(Core.getDrawHeight() * Core.getScale())));
		this.setResizable(false); // at least for the moment: forbid resize
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		p.x -= this.getWidth() / 2;
		p.y -= this.getHeight() / 2;
		posX = Core.programProps.get("framePosX", p.x > 0 ? p.x : 0);
		posY = Core.programProps.get("framePosY", p.y > 0 ? p.y : 0);
		this.setLocation(posX, posY);
		this.validate(); // force redraw
		this.setTitle("Lemmini");

		ClassLoader loader = Lemmini.class.getClassLoader();
		Image img = Toolkit.getDefaultToolkit().getImage(loader.getResource("icon_32.png"));
		setIconImage(img);

		// set component pane
		gp = new GraphicsPane();
		gp.setDoubleBuffered(false);
		this.setContentPane(gp);

		this.pack();
		this.validate(); // force redraw
		this.setTitle("Lemmini");

		// create Menu
		jMenuItemExit = new JMenuItem("Exit");
		jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				exit();
			}
		});

		jMenuFile = new JMenu("File");
		jMenuFile.add(jMenuItemExit);

		// Player Menu
		jMenuItemManagePlayer = new JMenuItem("Manage Players");
		jMenuItemManagePlayer.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Core.player.store(); // save player in case it is changed
				PlayerDialog d = new PlayerDialog((JFrame) Core.getCmp(), true);
				d.setVisible(true);
				// blocked until dialog returns
				List<String> players = d.getPlayers();

				if (players != null) {
					String player = Core.player.getName(); // old player
					int playerIdx = d.getSelection();

					if (playerIdx != -1) {
						player = players.get(playerIdx); // remember selected player
					}

					// check for players to delete
					for (int i = 0; i < Core.getPlayerNum(); i++) {
						String p = Core.getPlayer(i);

						if (!players.contains(p)) {
							File f = new File(Core.resourcePath + "players/" + p + ".ini");
							f.delete();

							if (p.equals(player)) {
								player = "default";
							}
						}
					}

					// rebuild players list
					Core.clearPlayers();

					// add default player if missing
					if (!players.contains("default")) {
						players.add("default");
					}

					// now copy all player and create properties
					for (int i = 0; i < players.size(); i++) {
						Core.addPlayer(players.get(i));
					}

					// select new default player
					Core.player = new Player(player);

					// rebuild players menu
					playerGroup = new ButtonGroup();
					jMenuSelectPlayer.removeAll();

					for (int idx = 0; idx < Core.getPlayerNum(); idx++) {
						JCheckBoxMenuItem item = addPlayerItem(Core.getPlayer(idx));

						if (Core.player.getName().equals(Core.getPlayer(idx))) {
							item.setSelected(true);
						}
					}

					updateLevelMenus();
				}
			}
		});

		jMenuSelectPlayer = new JMenu("Select Player");
		playerGroup = new ButtonGroup();

		for (int idx = 0; idx < Core.getPlayerNum(); idx++) {
			JCheckBoxMenuItem item = addPlayerItem(Core.getPlayer(idx));

			if (Core.player.getName().equals(Core.getPlayer(idx))) {
				item.setSelected(true);
			}
		}

		jMenuPlayer = new JMenu("Player");
		jMenuPlayer.add(jMenuItemManagePlayer);
		jMenuPlayer.add(jMenuSelectPlayer);

		// load level packs and create Level menu
		java.awt.event.ActionListener lvlListener = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				LvlMenuItem item = (LvlMenuItem) e.getSource();
				GameController.requestChangeLevel(item.levelPack, item.diffLevel, item.level, false);
			}
		};

		diffLevelMenus = new HashMap<String, ArrayList<LvlMenuItem>>(); // store menus to access them later
		jMenuSelect = new JMenu("Select Level");

		for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip dummy level pack
			LevelPack lPack = GameController.getLevelPack(lp);
			JMenu jMenuPack = new JMenu(lPack.getName());
			String difficulties[] = lPack.getDiffLevels();

			for (int i = 0; i < difficulties.length; i++) {
				// get activated levels for this group
				GroupBitfield bf = Core.player.getBitField(lPack.getName(), difficulties[i]);
				String names[] = lPack.getLevels(i);
				JMenu jMenuDiff = new JMenu(difficulties[i]);
				// store menus to access them later
				ArrayList<LvlMenuItem> menuItems = new ArrayList<LvlMenuItem>();

				for (int n = 0; n < names.length; n++) {
					LvlMenuItem jMenuLvl = new LvlMenuItem(names[n], lp, i, n);
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
				diffLevelMenus.put(LevelPack.getID(lPack.getName(), difficulties[i]), menuItems);
			}

			jMenuSelect.add(jMenuPack);
		}

		jMenuItemRestart = new JMenuItem();
		jMenuItemRestart.setText("Restart Level");
		jMenuItemRestart.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (!GameController.getLevel().isReady())
					GameController.requestChangeLevel(GameController.getCurLevelPackIdx(),
							GameController.getCurDiffLevel(), GameController.getCurLevelNumber(), false);
				else
					GameController.requestRestartLevel(false);
			}
		});

		jMenuItemLoad = new JMenuItem();
		jMenuItemLoad.setText("Load Level");
		jMenuItemLoad.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String p = ToolBox.getFileName(thisFrame, lvlPath, Core.LEVEL_EXTENSIONS, true);

				if (p != null) {
					try {
						if (ToolBox.getExtension(p).equalsIgnoreCase("lvl")) {
							Extract.ExtractLevel.convertLevel(p, Core.resourcePath + "/temp.ini");
							p = Core.resourcePath + "/temp.ini";
						}

						if (ToolBox.getExtension(p).equalsIgnoreCase("ini")) {
							String id = new String(ToolBox.getFileID(p, 5));

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
					} catch (Exception ex) {
						ToolBox.showException(ex);
					}
				}
			}
		});

		jMenuItemReplay = new JMenuItem();
		jMenuItemReplay.setText("Load Replay");
		jMenuItemReplay.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				String replayPath = ToolBox.getFileName(thisFrame, Core.resourcePath, Core.REPLAY_EXTENSIONS, true);

				if (replayPath != null) {
					try {
						if (ToolBox.getExtension(replayPath).equalsIgnoreCase("rpl")) {
							ReplayLevelInfo rli = GameController.loadReplay(replayPath);

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
					} catch (Exception ex) {
						ToolBox.showException(ex);
					}
				}
			}
		});

		jMenuItemLevelCode = new JMenuItem("Enter Level Code");
		jMenuItemLevelCode.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				LevelCodeDialog lcd = new LevelCodeDialog((JFrame) Core.getCmp(), true);
				lcd.setVisible(true);
				String levelCode = lcd.getCode();
				int lvlPack = lcd.getLevelPack();

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
					LevelPack lpack = GameController.getLevelPack(lvlPack);
					int lvlAbs = LevelCode.getLevel(lpack.getCodeSeed(), levelCode, lpack.getCodeOffset());

					if (lvlAbs != -1) {
						// calculate level pack and relative levelnumber from absolute number
						int l[] = GameController.relLevelNum(lvlPack, lvlAbs);
						int diffLvl = l[0];
						int lvlRel = l[1];
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

		jMenuLevel = new JMenu("Level");
		jMenuLevel.add(jMenuSelect);
		jMenuLevel.add(jMenuItemRestart);
		jMenuLevel.add(jMenuItemLoad);
		jMenuLevel.add(jMenuItemReplay);
		jMenuLevel.add(jMenuItemLevelCode);

		jMenuItemMusic = new JCheckBoxMenuItem("Music", false);
		jMenuItemMusic.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				boolean selected = jMenuItemMusic.isSelected();
				// jMenuItemMusic.setSelected(selected);

				if (selected) {
					GameController.setMusicOn(true);
				} else {
					GameController.setMusicOn(false);
				}

				Core.programProps.set("music", GameController.isMusicOn());
				if (GameController.getLevel() != null) // to be improved: level is running (game state)
					if (GameController.isMusicOn())
						Music.play();
					else
						Music.stop();
			}
		});

		jMenuItemMusic.setSelected(GameController.isMusicOn());

		jMenuItemSound = new JCheckBoxMenuItem("Sound", false);
		jMenuItemSound.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				boolean selected = jMenuItemSound.isSelected();

				if (selected) {
					GameController.setSoundOn(true);
				} else {
					GameController.setSoundOn(false);
				}

				Core.programProps.set("sound", GameController.isSoundOn());
			}
		});

		jMenuItemSound.setSelected(GameController.isSoundOn());

		jMenuSFX = new JMenu("SFX Mixer");
		String mixerNames[] = GameController.sound.getMixers();
		ButtonGroup mixerGroup = new ButtonGroup();
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
			JCheckBoxMenuItem item = new JCheckBoxMenuItem();
			item.setText(mixerNames[i]);
			item.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String mixerNames[] = GameController.sound.getMixers();
					String mixerName = e.getActionCommand();

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

		jMenuItemVolume = new JMenuItem("Volume Control");
		jMenuItemVolume.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				GainDialog v = new GainDialog((JFrame) Core.getCmp(), true);
				v.setVisible(true);
			}
		});

		jMenuSound = new JMenu();
		jMenuSound.setText("Sound");
		jMenuSound.add(jMenuItemVolume);
		jMenuSound.add(jMenuItemMusic);
		jMenuSound.add(jMenuItemSound);
		jMenuSound.add(jMenuSFX);

		jMenuItemCursor = new JCheckBoxMenuItem("Advanced select", false);
		jMenuItemCursor.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				boolean selected = jMenuItemCursor.isSelected();

				if (selected) {
					GameController.setAdvancedSelect(true);
				} else {
					GameController.setAdvancedSelect(false);
					gp.setCursor(LemmCursor.Type.NORMAL);
				}

				Core.programProps.set("advancedSelect", GameController.isAdvancedSelect());
			}
		});

		jMenuItemCursor.setSelected(GameController.isAdvancedSelect());

		jMenuItemClassicalCursor = new JCheckBoxMenuItem("Classical Cursor", false);
		jMenuItemClassicalCursor.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				boolean selected = jMenuItemClassicalCursor.isSelected();
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

		jMenuBar = new JMenuBar();
		jMenuBar.add(jMenuFile);
		jMenuBar.add(jMenuPlayer);
		jMenuBar.add(jMenuLevel);
		jMenuBar.add(jMenuSound);
		jMenuBar.add(jMenuOptions);

		zoomGroup = new ButtonGroup();
		JMenu jMenuZoom = new JMenu("Zoom");
		jMenuOptions.add(jMenuZoom);

		JRadioButtonMenuItem jMenuRadioItemX1 = new JRadioButtonMenuItem("x1");
		jMenuRadioItemX1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setScale(1);
			}
		});

		jMenuZoom.add(jMenuRadioItemX1);
		zoomGroup.add(jMenuRadioItemX1);

		JRadioButtonMenuItem jMenuRadioItemX1P5 = new JRadioButtonMenuItem("X1.5");
		jMenuRadioItemX1P5.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setScale(1.5);
			}
		});

		jMenuZoom.add(jMenuRadioItemX1P5);
		zoomGroup.add(jMenuRadioItemX1P5);

		JRadioButtonMenuItem jMenuRadioItemX2 = new JRadioButtonMenuItem("x2");
		jMenuRadioItemX2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setScale(2);
			}
		});

		jMenuZoom.add(jMenuRadioItemX2);
		zoomGroup.add(jMenuRadioItemX2);

		JRadioButtonMenuItem jMenuRadioItemX2P5 = new JRadioButtonMenuItem("X2.5");
		jMenuRadioItemX2P5.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setScale(2.5);
			}
		});

		jMenuZoom.add(jMenuRadioItemX2P5);
		zoomGroup.add(jMenuRadioItemX2P5);

		JRadioButtonMenuItem jMenuRadioItemX3 = new JRadioButtonMenuItem("x3");
		jMenuRadioItemX3.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setScale(3);
			}
		});

		jMenuZoom.add(jMenuRadioItemX3);
		zoomGroup.add(jMenuRadioItemX3);
		this.setJMenuBar(jMenuBar);

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

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				exit();
			}

			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				exit();
			}
		});

		this.setVisible(true);
		gp.init();
		GameController.setGameState(GameController.State.INTRO);
		GameController.setTransition(GameController.TransitionState.NONE);
		Fader.setBounds(Core.getDrawWidth(), Core.getDrawHeight());
		Fader.setState(Fader.State.IN);
		Thread t = new Thread(gp);

		lvlPath = ".";

		addKeyListener(this);

		t.start();
	}

	/**
	 * Add a menu item for a player.
	 * 
	 * @param name player name
	 * @return JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem addPlayerItem(final String name) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
		item.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Core.player.store(); // save player in case it is changed
				JMenuItem item = (JMenuItem) e.getSource();
				String player = item.getText();
				Player p = new Player(player);
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
	 * Convert String to int.
	 * 
	 * @param s String with decimal integer value
	 * @return integer value (0 if no valid number)
	 */
	private static int getInt(final String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return 0;
		}
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
		} catch (Exception e) {
			/* don't care */}
		/*
		 * Apple menu bar for MacOS
		 */
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		/*
		 * Check JVM version
		 */
		String jreStr = System.getProperty("java.version");
		String vs[] = jreStr.split("[._]");
		double vnum;

		if (vs.length >= 3) {
			vnum = (getInt(vs[0]))
					+ (getInt(vs[1])) * 0.1
					+ (getInt(vs[2])) * 0.01;

			if (vnum < 1.5) {
				JOptionPane.showMessageDialog(null, "Run this with JVM >= 1.5", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}

		// check free memory
		long free = Runtime.getRuntime().maxMemory();

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
					} catch (InterruptedException ex) {
					}
				}
			}
		};

		Toolkit.getDefaultToolkit().setDynamicLayout(true);
		thisFrame = new Lemmini();
	}

	void setScale(double scale) {
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
	 * Update the level menus according to the progress of the current player.
	 */
	private void updateLevelMenus() {
		// update level menus
		for (int lp = 1; lp < GameController.getLevelPackNum(); lp++) { // skip dummy level pack
			LevelPack lPack = GameController.getLevelPack(lp);
			String difficulties[] = lPack.getDiffLevels();

			for (int i = 0; i < difficulties.length; i++) {
				// get activated levels for this group
				GroupBitfield bf = Core.player.getBitField(lPack.getName(), difficulties[i]);
				updateLevelMenu(lPack.getName(), difficulties[i], bf);
			}
		}
	}

	/**
	 * Update the level menus according to the given progress information.
	 * 
	 * @param pack name of level pack
	 * @param diff name of difficulty level
	 * @param bf   bitmap containing availability flags for each level
	 */
	private void updateLevelMenu(final String pack, final String diff, final GroupBitfield bf) {
		ArrayList<LvlMenuItem> menuItems = diffLevelMenus.get(LevelPack.getID(pack, diff));

		for (int k = 0; k < menuItems.size(); k++) {
			// select level, e.g. "All fall down"
			JMenuItem level = menuItems.get(k);

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
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader r = new BufferedReader(new FileReader(lvlPath));
			String l;

			while ((l = r.readLine()) != null) {
				lines.add(l);
			}

			r.close();
			FileWriter sw = new FileWriter(lvlPath);

			for (int i = 0; i < lines.size(); i++) {
				String s = lines.get(i);

				if (s.startsWith("xPos =")) {
					sw.write("xPos = " + Integer.toString(GameController.getxPos()) + "\n");
				} else {
					sw.write(s + "\n");
				}
			}

			sw.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}

	@Override
	public void keyPressed(final KeyEvent keyevent) {
		int code = keyevent.getKeyCode();

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
					if (GameController.isCheat())
						gp.setDebugDraw(!gp.getDebugDraw());
					break;
				case KeyEvent.VK_W:
					if (GameController.isCheat()) {
						GameController.setNumLeft(GameController.getNumLemmingsMax());
						GameController.endLevel();
					}
					break;
				case KeyEvent.VK_L: // print current level on the console
					if (GameController.isCheat())
						System.out.println(GameController.getLevelPack(GameController.getCurLevelPackIdx())
								.getInfo(GameController.getCurDiffLevel(), GameController.getCurLevelNumber())
								.getFileName());
					break;
				case KeyEvent.VK_S: // superlemming on/off
					if (GameController.isCheat())
						GameController.setSuperLemming(!GameController.isSuperLemming());
					else {
						try {
							File file = new File(Core.resourcePath + "/level.png");
							BufferedImage tmp = GameController.getLevel().createMiniMap(null,
									GameController.getBgImage(), 1, 1, false);
							ImageIO.write(tmp, "png", file);
						} catch (Exception ex) {
						}
					}
					break;
				case KeyEvent.VK_C:
					if (Core.player.isCheat()) {
						GameController.setCheat(!GameController.isCheat());

						if (GameController.isCheat()) {
							GameController.setWasCheated(true);
						}
					} else {
						GameController.setCheat(false);
					}

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
					if (GameController.isCheat()) {
						patchLevel(GameController.getLevelPack(GameController.getCurLevelPackIdx())
								.getInfo(GameController.getCurDiffLevel(), GameController.getCurLevelNumber())
								.getFileName());
					}

					break;
				case KeyEvent.VK_RIGHT /* 39 */: {
					if (GameController.isAdvancedSelect()) {
						gp.setCursor(LemmCursor.Type.RIGHT);
					} else {
						int xOfsTemp = GameController.getxPos()
								+ ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST : GraphicsPane.X_STEP);
						if (xOfsTemp < Level.WIDTH - this.getWidth())
							GameController.setxPos(xOfsTemp);
						else
							GameController.setxPos(Level.WIDTH - this.getWidth());
					}

					break;
				}

				case KeyEvent.VK_LEFT /* 37 */: {
					if (GameController.isAdvancedSelect()) {
						gp.setCursor(LemmCursor.Type.LEFT);
					} else {
						int xOfsTemp = GameController.getxPos()
								- ((gp.isShiftPressed()) ? GraphicsPane.X_STEP_FAST : GraphicsPane.X_STEP);
						if (xOfsTemp > 0)
							GameController.setxPos(xOfsTemp);
						else
							GameController.setxPos(0);
					}

					break;
				}

				case KeyEvent.VK_UP: {
					gp.setCursor(LemmCursor.Type.WALKER);
					break;
				}

				case KeyEvent.VK_SHIFT:
					gp.setShiftPressed(true);
					break;
				case KeyEvent.VK_SPACE:
					if (GameController.isCheat()) {
						Lemming l = new Lemming(gp.getCursorX(), gp.getCursorY());

						synchronized (GameController.getLemmings()) {
							GameController.getLemmings().add(l);
						}
					}

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
		// System.out.println(keyevent.getKeyCode());
	}

	@Override
	public void keyReleased(final KeyEvent keyevent) {
		int code = keyevent.getKeyCode();

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
					if (LemmCursor.getType() == LemmCursor.Type.LEFT)
						gp.setCursor(LemmCursor.Type.NORMAL);
					break;
				case KeyEvent.VK_RIGHT:
					if (LemmCursor.getType() == LemmCursor.Type.RIGHT)
						gp.setCursor(LemmCursor.Type.NORMAL);
					break;
				case KeyEvent.VK_UP:
					if (LemmCursor.getType() == LemmCursor.Type.WALKER)
						gp.setCursor(LemmCursor.Type.NORMAL);
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
	private void exit() {
		// store width and height
		Dimension d = this.getSize();
		Core.programProps.set("frameWidth", d.width);
		Core.programProps.set("frameHeight", d.height);
		// store frame pos
		Point p = this.getLocation();
		Core.programProps.set("framePosX", p.x);
		Core.programProps.set("framePosY", p.y);
		//
		Core.saveProgramProps();
		System.exit(0);
	}

	/**
	 * Listener to inform the GUI of the player's progress.
	 * 
	 * @author Volker Oth
	 */
	class LevelMenuUpdateListener implements UpdateListener {
		@Override
		public void update() {
			if (GameController.getCurLevelPackIdx() != 0) { // 0 is the dummy pack
				LevelPack lvlPack = GameController.getLevelPack(GameController.getCurLevelPackIdx());
				String pack = lvlPack.getName();
				String diff = lvlPack.getDiffLevels()[GameController.getCurDiffLevel()];
				// get next level
				int num = GameController.getCurLevelNumber() + 1;

				if (num >= lvlPack.getLevels(GameController.getCurDiffLevel()).length) {
					num = GameController.getCurLevelNumber();
				}

				// set next level as available
				GroupBitfield bf = Core.player.setAvailable(pack, diff, num);
				// update the menu
				updateLevelMenu(pack, diff, bf);
			}
		}
	}

	/**
	 * Specialized menu item for level selection menus.
	 * 
	 * @author Volker Oth
	 */
	class LvlMenuItem extends JMenuItem {
		private final static long serialVersionUID = 0x01;

		/** index of level pack */
		int levelPack;
		/** index of difficulty level */
		int diffLevel;
		/** level number */
		int level;

		/**
		 * Constructor
		 * 
		 * @param text level name
		 * @param pack index level pack
		 * @param diff index of difficulty level
		 * @param lvl  level number
		 */
		LvlMenuItem(final String text, final int pack, final int diff, final int lvl) {
			super(text);
			levelPack = pack;
			diffLevel = diff;
			level = lvl;
		}
	}
}
