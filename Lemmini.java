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
import javax.swing.JRadioButtonMenuItem;
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
		initializeMenus();
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
	 * Initializes all menus for the Lemmini window.
	 */
	private void initializeMenus() {
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
				setScale(3);
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
				setScale(2.5);
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
				setScale(2);
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
				setScale(1.5);
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
				setScale(1);
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
					gp.setCursor(LemmCursor.Type.NORMAL);
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
				final String replayPath = ToolBox.getFileName(thisFrame, Core.resourcePath, Core.REPLAY_EXTENSIONS,
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
				String p = ToolBox.getFileName(thisFrame, lvlPath, Core.LEVEL_EXTENSIONS, true);

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

		diffLevelMenus = new HashMap<String, ArrayList<LvlMenuItem>>(); // store menus to access them later
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
				diffLevelMenus.put(LevelPack.getID(lPack.getName(), difficulties[i]), menuItems);
			}

			jMenuSelect.add(jMenuPack);
		}
	}

	/**
	 * Initializes the Manage Player menu item.
	 */
	private void getManagePlayerMenuItem() {
		jMenuItemManagePlayer = new JMenuItem("Manage Players");
		jMenuItemManagePlayer.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				Core.player.store(); // save player in case it is changed
				final PlayerDialog d = new PlayerDialog((JFrame) Core.getCmp(), true);
				d.setVisible(true);
				// blocked until dialog returns
				final List<String> players = d.getPlayers();

				if (players != null) {
					String player = Core.player.getName(); // old player
					final int playerIdx = d.getSelection();

					if (playerIdx != -1) {
						player = players.get(playerIdx); // remember selected player
					}

					// check for players to delete
					for (int i = 0; i < Core.getPlayerNum(); i++) {
						final String p = Core.getPlayer(i);

						if (!players.contains(p)) {
							final File f = new File(Core.resourcePath + "players/" + p + ".ini");
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
						final JCheckBoxMenuItem item = addPlayerItem(Core.getPlayer(idx));

						if (Core.player.getName().equals(Core.getPlayer(idx))) {
							item.setSelected(true);
						}
					}

					updateLevelMenus();
				}
			}
		});
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
	 * Add a menu item for a player.
	 * 
	 * @param name player name
	 * @return JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem addPlayerItem(final String name) {
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
	 * Convert String to int.
	 * 
	 * @param s String with decimal integer value
	 * @return integer value (0 if no valid number)
	 */
	private static int getInt(final String s) {
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException ex) {
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
		} catch (final Exception e) {
			/* don't care */}
		/*
		 * Apple menu bar for MacOS
		 */
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		/*
		 * Check JVM version
		 */
		checkJvmVersion();
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

	/**
	 * Checks to ensure that the Java version is at least 1.5, and exits with an
	 * error in the now unlikely event that it is.
	 */
	private static void checkJvmVersion() {
		final String jreStr = System.getProperty("java.version");
		final String vs[] = jreStr.split("[._]");
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
	}

	void setScale(final double scale) {
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
			final LevelPack lPack = GameController.getLevelPack(lp);
			final String difficulties[] = lPack.getDiffLevels();

			for (int i = 0; i < difficulties.length; i++) {
				// get activated levels for this group
				final GroupBitfield bf = Core.player.getBitField(lPack.getName(), difficulties[i]);
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
	void updateLevelMenu(final String pack, final String diff, final GroupBitfield bf) {
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
