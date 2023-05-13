package lemmini;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Game.Core;
import Game.GameController;
import Game.Icons;
import Game.LemmCursor;
import Game.LemmFont;
import Game.Lemming;
import Game.Level;
import Game.MiniMap;
import Game.MiscGfx;
import Game.Stencil;
import Game.TextScreen;
import GameUtil.Fader;
import Tools.MicrosecondTimer;
import Tools.ToolBox;

/**
 * A graphics panel in which the actual game contents is displayed.
 * 
 * @author Volker Oth
 */
public class GraphicsPane extends JPanel implements Runnable, MouseListener, MouseMotionListener {
	/** step size in pixels for horizontal scrolling */
	final static int X_STEP = 4;
	/** step size in pixels for fast horizontal scrolling */
	final static int X_STEP_FAST = 8;
	/** size of auto scrolling range in pixels (from the left and right border) */
	final static int AUTOSCROLL_RANGE = 20;
	/** y coordinate of score display in pixels */
	final static int scoreY = Level.HEIGHT;
	/** y coordinate of counter displays in pixels */
	final static int counterY = scoreY + 40;
	/** y coordinate of icons in pixels */
	final static int iconsY = counterY + 14;
	/** x coordinate of minimap in pixels */
	final static int smallX = 640 - 16/*-32*/ - 200;
	/** y coordinate of minimap in pixels */
	final static int smallY = iconsY;

	private final static long serialVersionUID = 0x01;

	/** start position of mouse drag (for mouse scrolling) */
	private int mouseDragStartX;
	/** x position of cursor in level */
	private int xMouse;
	/** x position of cursor on screen */
	private int xMouseScreen;
	/** y position of cursor in level */
	private int yMouse;
	/** y position of cursor on screen */
	private int yMouseScreen;
	/** mouse drag length in x direction (pixels) */
	private int mouseDx;
	/** mouse drag length in y direction (pixels) */
	private int mouseDy;
	/** flag: Shift key is pressed */
	private boolean shiftPressed;
	/** flag: left mouse button is currently pressed */
	private boolean leftMousePressed;
	/** flag: debug draw is active */
	private boolean draw;
	/** image for information string display */
	private transient BufferedImage outStrImg;
	/** graphics object for information string display */
	private transient Graphics2D outStrGfx;
	/** array of offscreen images (one is active, one is passive) */
	private transient BufferedImage offImage[];
	/** graphics objects for the two offscreen images */
	private transient Graphics2D offGraphics[];
	/** index of the active buffer in the image buffer */
	private int activeBuffer;
	/** monitoring object used for synchronized painting */
	private final Object paintSemaphore;
	private Component frame;

	/**
	 * Constructor.
	 * 
	 * @param frame the parent component (main frame of the application).
	 */
	public GraphicsPane(final Component frame) {
		super();
		this.frame = frame;
		paintSemaphore = new Object();
		this.requestFocus();
		this.setCursor(LemmCursor.getCursor());
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	@SuppressWarnings("unused")
	private GraphicsPane() {
		// Private default no-args constructor - not used.
		super();
		frame = null;
		paintSemaphore = null;
	}

	/**
	 * Set cursor type.
	 * 
	 * @param c Cursor
	 */
	public void setCursor(final LemmCursor.Type c) {
		LemmCursor.setType(c);
		this.setCursor(LemmCursor.getCursor());
	}

	/**
	 * Show/hide Mouse cursor.
	 * 
	 * @param en true to show the Mouse cursor, false to hide it
	 */
	public void enableCursor(final boolean en) {
		LemmCursor.setEnabled(en);
		this.setCursor(LemmCursor.getCursor());
	}

	@Override
	public void paint(final Graphics g) {
		// super.paint(iconGfx);
		synchronized (paintSemaphore) {
			if (offImage != null) {
				final int w = Core.getDrawWidth();
				final int h = Core.getDrawHeight();
				final double scale = Core.getScale();
				// g.drawImage(offImage[activeBuffer],0,0,null);
				g.drawImage(offImage[activeBuffer], 0, 0, (int) Math.round(w * scale), (int) Math.round(h * scale), 0,
						0, w, h, null);
			}
		}
	}

	@Override
	public void update(final Graphics g) {
		// super.update(iconGfx);
		synchronized (paintSemaphore) {
			if (offImage != null) {
				final int w = Core.getDrawWidth();
				final int h = Core.getDrawHeight();
				final double scale = Core.getScale();
				// g.drawImage(offImage[activeBuffer],0,0,null);
				g.drawImage(offImage[activeBuffer], 0, 0, (int) Math.round(w * scale), (int) Math.round(h * scale), 0,
						0, w, h, null);
			}
		}
	}

	/**
	 * Initialization.
	 */
	public void init() {
		synchronized (paintSemaphore) {
			final int w = Core.getDrawWidth();
			final int h = Core.getDrawHeight();
			final double scale = Core.getScale();

			this.setSize((int) Math.round(scale * w), (int) Math.round(scale * h));

			offImage = new BufferedImage[2];
			offGraphics = new Graphics2D[2];
			offImage[0] = ToolBox.createImage(w, h, Transparency.OPAQUE);
			offImage[1] = ToolBox.createImage(w, h, Transparency.OPAQUE);
			offGraphics[0] = offImage[0].createGraphics();
			offGraphics[1] = offImage[1].createGraphics();

			outStrImg = ToolBox.createImage(w, LemmFont.getHeight(), Transparency.BITMASK);
			outStrGfx = outStrImg.createGraphics();
			outStrGfx.setBackground(new Color(0, 0, 0));

			TextScreen.init(w, (int) Math.round(this.getHeight() / scale));
			shiftPressed = false;
		}
	}

	/**
	 * Delete offImage to avoid redraw and force init.
	 */
	public void shutdown() {
		synchronized (paintSemaphore) {
			offImage = null;
		}
	}

	/**
	 * redraw the offscreen image, then flip buffers and force repaint.
	 */
	private void redraw() {
		final double scale = Core.getScale();
		int drawBuffer;
		Graphics2D offGfx;

		synchronized (paintSemaphore) {
			// if (offImage == null)
			// init();
			drawBuffer = (activeBuffer == 0) ? 1 : 0;
			offGfx = offGraphics[drawBuffer];

			final BufferedImage bgImage = GameController.getBgImage();

			switch (GameController.getGameState()) {
				case INTRO:
					TextScreen.setMode(TextScreen.Mode.INTRO);
					TextScreen.update();
					offGfx.drawImage(TextScreen.getScreen(), 0, 0, null);
					// offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x,
					// LemmCursor.y, null);
					break;
				case BRIEFING:
					TextScreen.setMode(TextScreen.Mode.BRIEFING);
					TextScreen.update();
					offGfx.drawImage(TextScreen.getScreen(), 0, 0, null);
					// offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x,
					// LemmCursor.y, null);
					break;
				case DEBRIEFING:
					TextScreen.setMode(TextScreen.Mode.DEBRIEFING);
					TextScreen.update();
					offGfx.drawImage(TextScreen.getScreen(), 0, 0, null);
					TextScreen.getDialog().handleMouseMove((int) Math.round(xMouseScreen / scale),
							(int) Math.round(yMouseScreen / scale));
					// offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x,
					// LemmCursor.y, null);
					break;
				case LEVEL:
				case LEVEL_END:
					if (bgImage != null) {
						GameController.update();

						// mouse movement
						if (yMouseScreen > 40 && yMouseScreen < scoreY * scale) { // avoid scrolling if menu is selected
							int xOfsTemp;

							if (xMouseScreen > this.getWidth() - AUTOSCROLL_RANGE * scale) {
								xOfsTemp = GameController.getxPos() + ((shiftPressed) ? X_STEP_FAST : X_STEP);

								if (xOfsTemp < Level.WIDTH - this.getWidth() / scale) {
									GameController.setxPos(xOfsTemp);
								} else {
									GameController.setxPos((int) Math.round(Level.WIDTH - this.getWidth() / scale));
								}
							} else if (xMouseScreen < AUTOSCROLL_RANGE * scale) {
								xOfsTemp = GameController.getxPos() - ((shiftPressed) ? X_STEP_FAST : X_STEP);

								if (xOfsTemp > 0) {
									GameController.setxPos(xOfsTemp);
								} else {
									GameController.setxPos(0);
								}
							}
						}

						// store local copy of xOfs to avoid sync problems with AWT threads
						// (scrolling by dragging changes xOfs as well)
						final int xOfsTemp = GameController.getxPos();

						// timeBaseRedraw +=GameController.timePerFrame;
						final int w = this.getWidth();
						int h = Level.HEIGHT;

						if (h > this.getHeight()) {
							h = this.getHeight();
						}

						final Level level = GameController.getLevel();

						if (level != null) {
							// clear screen
							offGfx.setClip(0, 0, w, h);
							offGfx.setBackground(level.getBgColor());
							offGfx.clearRect(0, 0, w, h);

							// draw "behind" objects
							GameController.getLevel().drawBehindObjects(offGfx, w, xOfsTemp);

							// draw background
							offGfx.drawImage(bgImage, 0, 0, w, h, xOfsTemp, 0, xOfsTemp + w, h, this);

							// draw "in front" objects
							GameController.getLevel().drawInFrontObjects(offGfx, w, xOfsTemp);
						}

						// clear parts of the screen for menu etc.
						offGfx.setClip(0, Level.HEIGHT, w, this.getHeight());
						offGfx.setBackground(Color.BLACK);
						offGfx.clearRect(0, scoreY, w, this.getHeight());
						// draw counter, icons, small level pic
						// draw menu
						// Icons icons = GameController.getIcons();
						GameController.drawIcons(offGfx, 0, iconsY);
						offGfx.drawImage(MiscGfx.getImage(MiscGfx.Index.BORDER), smallX - 4, smallY - 4, null);
						MiniMap.draw(offGfx, smallX, smallY, xOfsTemp);
						// draw counters
						GameController.drawCounters(offGfx, counterY);

						// draw lemmings
						offGfx.setClip(0, 0, w, h);
						GameController.getLemmsUnderCursor().clear();
						final List<Lemming> lemmings = GameController.getLemmings();

						synchronized (GameController.getLemmings()) {
							for (final Lemming l : lemmings) {
								final int lx = l.screenX();
								final int ly = l.screenY();
								final int mx = l.midX() - 16;

								if (lx + l.width() > xOfsTemp && lx < xOfsTemp + w) {
									offGfx.drawImage(l.getImage(), lx - xOfsTemp, ly, null);

									if (LemmCursor.doesCollide(l, xOfsTemp)) {
										GameController.getLemmsUnderCursor().add(l);
									}

									final BufferedImage cd = l.getCountdown();

									if (cd != null) {
										offGfx.drawImage(cd, mx - xOfsTemp, ly - cd.getHeight(), null);
									}

									final BufferedImage sel = l.getSelectImg();

									if (sel != null) {
										offGfx.drawImage(sel, mx - xOfsTemp, ly - sel.getHeight(), null);
									}
								}
							}

							// draw pixels in mini map
							offGfx.setClip(0, 0, w, this.getHeight());

							for (final Lemming l : lemmings) {
								final int lx = l.screenX();
								final int ly = l.screenY();
								// draw pixel in mini map
								MiniMap.drawLemming(offGfx, lx, ly);
							}
						}

						final Lemming lemmUnderCursor = GameController.lemmUnderCursor(LemmCursor.getType());
						offGfx.setClip(0, 0, w, h);
						// draw explosions
						GameController.drawExplosions(offGfx, offImage[0].getWidth(), Level.HEIGHT, xOfsTemp);
						offGfx.setClip(0, 0, w, this.getHeight());

						// draw info string
						outStrGfx.clearRect(0, 0, outStrImg.getWidth(), outStrImg.getHeight());

						if (GameController.isCheat()) {
							final Stencil stencil = GameController.getStencil();

							if (stencil != null) {
								final int stencilVal = stencil.get(xMouse + yMouse * Level.WIDTH);
								final String test = "x: " + xMouse + ", y: " + yMouse + ", mask: "
										+ (stencilVal & 0xffff)
										+ " " + Stencil.getObjectID(stencilVal);
								LemmFont.strImage(outStrGfx, test);
								offGfx.drawImage(outStrImg, 4, Level.HEIGHT + 8, null);
							}
						} else {
							final StringBuffer sb = new StringBuffer();
							sb.append("OUT ");
							String s = Integer.toString(GameController.getLemmings().size());
							sb.append(s);

							if (s.length() == 1) {
								sb.append(" ");
							}

							sb.append("  IN ");
							s = Integer
									.toString(GameController.getNumLeft() * 100 / GameController.getNumLemmingsMax());

							if (s.length() == 1) {
								sb.append("0");
							}

							sb.append(s);
							sb.append("%  TIME ").append(GameController.getTimeString());
							// BufferedImage iout = LemmFont.strImage(out);
							String n = null;

							if (lemmUnderCursor != null) {
								n = lemmUnderCursor.getName();
								// display also the total number of lemmings under the cursor
								final int num = GameController.getLemmsUnderCursor().size();

								if (num > 1) {
									n = n + " " + Integer.toString(num);
								}
							}

							if (n != null) {
								int ln = n.length();

								if (ln > 14) {
									ln = 14;
								}

								sb.insert(0, "              ".substring(0, 14 - ln));
								sb.insert(0, n);
							} else {
								sb.insert(0, "              ");
							}

							LemmFont.strImage(outStrGfx, sb.toString());
							offGfx.drawImage(outStrImg, 4, Level.HEIGHT + 8, null);
						}

						// replay icon
						final BufferedImage replayImage = GameController.getReplayImage();

						if (replayImage != null) {
							offGfx.drawImage(replayImage, this.getWidth() - 2 * replayImage.getWidth(),
									replayImage.getHeight(), null);
						}

						// draw cursor
						if (lemmUnderCursor != null) {
							int lx, ly;

							if (GameController.isClassicalCursor()) {
								lx = (int) Math.round(xMouseScreen / scale);
								ly = (int) Math.round(yMouseScreen / scale);
								enableCursor(false);
							} else {
								lx = lemmUnderCursor.midX() - xOfsTemp;
								ly = lemmUnderCursor.midY();
							}

							final BufferedImage cursorImg = LemmCursor.getBoxImage();
							lx -= cursorImg.getWidth() / 2;
							ly -= cursorImg.getHeight() / 2;
							offGfx.drawImage(cursorImg, lx, ly, null);
						} else if (LemmCursor.getEnabled() == false) {
							enableCursor(true);
						}
					}

					break;
				default:
					break;
			}

			// fader
			GameController.fade(offGfx, frame);
			// and all onto screen
			activeBuffer = drawBuffer;

			repaint();
		}

	}

	@Override
	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
		final MicrosecondTimer timerRepaint = new MicrosecondTimer();

		try {
			while (true) {
				final GameController.State gameState = GameController.getGameState();

				// Try to keep the Amiga timing. Note that no frames are skipped
				// If one frame is too late, the next one will be a little earlier
				// to compensate. No frames are skipped though!
				// On a slow CPU this might slow down gameplay...
				if (timerRepaint.timePassedAdd(GameController.MICROSEC_PER_FRAME)) {
					// time passed -> redraw necessary
					redraw();

					// special handling for fast forward or super lemming mode only during real
					// gameplay
					if (gameState == GameController.State.LEVEL) {
						// in fast forward or super lemming modes, update the game mechanics
						// multiple times per (drawn) frame
						if (GameController.isFastForward())
							for (int f = 0; f < GameController.FAST_FWD_MULTI - 1; f++)
								GameController.update();
						else if (GameController.isSuperLemming())
							for (int f = 0; f < GameController.SUPERLEMM_MULTI - 1; f++)
								GameController.update();
					}
				} else {
					try {
						// determine time until next frame
						final long diff = GameController.MICROSEC_PER_FRAME - timerRepaint.delta();

						if (diff > GameController.MICROSEC_RESYNC) {
							timerRepaint.update(); // resync to time base
							System.out.println("Resynced, diff was " + (diff / 1000) + " millis");
						} else if (diff > Lemmini.THR_SLEEP * 1000) {
							Thread.sleep(Lemmini.MIN_SLEEP);
						}
					} catch (final InterruptedException ex) {
					}
				}
			}
		} catch (final Exception ex) {
			ToolBox.showException(ex);
			System.exit(1);
		} catch (final Error ex) {
			ToolBox.showException(ex);
			System.exit(1);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent mouseevent) {
		final double scale = Core.getScale();
		final int x = (int) Math.round(mouseevent.getX() / scale);
		final int y = (int) Math.round(mouseevent.getY() / scale);
		mouseDx = 0;
		mouseDy = 0;

		if (mouseevent.getButton() == MouseEvent.BUTTON1) {
			leftMousePressed = false;
		}

		switch (GameController.getGameState()) {
			case LEVEL:
				if (y > iconsY && y < iconsY + Icons.HEIGHT) {
					final Icons.Type type = GameController.getIconType(x);

					if (type != Icons.Type.INVALID) {
						GameController.releaseIcon(type);
					}
				}

				// always release icons which don't stay pressed
				// this is to avoid the icons get stuck when they're pressed,
				// the the mouse is dragged out and released outside
				GameController.releasePlus(GameController.KEYREPEAT_ICON);
				GameController.releaseMinus(GameController.KEYREPEAT_ICON);
				GameController.releaseIcon(Icons.Type.MINUS);
				GameController.releaseIcon(Icons.Type.PLUS);
				GameController.releaseIcon(Icons.Type.NUKE);
				mouseevent.consume();
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent mouseevent) {
	}

	@Override
	public void mousePressed(final MouseEvent mouseevent) {
		final double scale = Core.getScale();
		final int x = (int) Math.round(mouseevent.getX() / scale);
		final int y = (int) Math.round(mouseevent.getY() / scale);
		mouseDx = 0;
		mouseDy = 0;

		if (mouseevent.getButton() == MouseEvent.BUTTON1) {
			leftMousePressed = true;
		}

		if (Fader.getState() != Fader.State.OFF) {
			return;
		}

		switch (GameController.getGameState()) {
			case BRIEFING:
				MiniMap.init(smallX, smallY, 16, 8, true);
				GameController.setTransition(GameController.TransitionState.TO_LEVEL);
				Fader.setState(Fader.State.OUT);
				mouseevent.consume();
				break;
			case DEBRIEFING:
				final int button = TextScreen.getDialog().handleLeftClick(x, y);

				switch (button) {
					case TextScreen.BUTTON_CONTINUE:
						GameController.nextLevel(); // continue to next level
						GameController.requestChangeLevel(GameController.getCurLevelPackIdx(),
								GameController.getCurDiffLevel(),
								GameController.getCurLevelNumber(), false);
						break;
					case TextScreen.BUTTON_RESTART:
						GameController.requestRestartLevel(false);
						break;
					case TextScreen.BUTTON_MENU:
						GameController.setTransition(GameController.TransitionState.TO_INTRO);
						Fader.setState(Fader.State.OUT);
						((JFrame) frame).setTitle("Lemmini");
						break;
					case TextScreen.BUTTON_REPLAY:
						GameController.requestRestartLevel(true);
						break;
					case TextScreen.BUTTON_SAVEREPLAY:
						String replayPath = ToolBox.getFileName(Lemmini.thisFrame, Core.resourcePath,
								Core.REPLAY_EXTENSIONS, false);

						if (replayPath != null) {
							try {
								final String ext = ToolBox.getExtension(replayPath);

								if (ext == null) {
									replayPath += ".rpl";
								}

								if (GameController.saveReplay(replayPath)) {
									return;
								}

								// else: no success
								JOptionPane.showMessageDialog(frame, "Error!", "Saving replay failed",
										JOptionPane.INFORMATION_MESSAGE);
							} catch (final Exception ex) {
								ToolBox.showException(ex);
							}
						}

						break;
				}

				mouseevent.consume();
				break;
			case LEVEL:
				// debug drawing
				debugDraw(x, y, leftMousePressed);

				if (leftMousePressed) {
					if (y > iconsY && y < iconsY + Icons.HEIGHT) {
						final Icons.Type type = GameController.getIconType(x);

						if (type != Icons.Type.INVALID) {
							GameController.handleIconButton(type);
						}
					} else {
						final Lemming l = GameController.lemmUnderCursor(LemmCursor.getType());

						if (l != null) {
							GameController.requestSkill(l);
						}
					}

					// check minimap mouse move
					final int ofs = MiniMap.move(x, y, (int) Math.round(this.getWidth() / scale));

					if (ofs != -1) {
						GameController.setxPos(ofs);
					}

					mouseevent.consume();
				}

			default:
				break;
		}
	}

	/**
	 * Debug routine to draw terrain pixels in stencil and background image.
	 * 
	 * @param x      x position in pixels
	 * @param y      y position in pixels
	 * @param doDraw true: draw, false: erase
	 */
	private void debugDraw(final int x, final int y, final boolean doDraw) {
		if (draw && GameController.isCheat()) {
			final int rgbVal = (doDraw) ? 0xffffffff : 0x0;
			final int maskVal = (doDraw) ? Stencil.MSK_BRICK : Stencil.MSK_EMPTY;
			final int xOfs = GameController.getxPos();

			if (x + xOfs > 0 && x + xOfs < Level.WIDTH - 1 && y > 0 && y < Level.HEIGHT - 1) {
				GameController.getBgImage().setRGB(x + xOfs, y, rgbVal);
				GameController.getStencil().set(x + xOfs, y, maskVal);
				GameController.getBgImage().setRGB(x + xOfs + 1, y, rgbVal);
				GameController.getStencil().set(x + xOfs + 1, y, maskVal);
				GameController.getBgImage().setRGB(x + xOfs, y + 1, rgbVal);
				GameController.getStencil().set(x + xOfs, y + 1, maskVal);
				GameController.getBgImage().setRGB(x + xOfs + 1, y + 1, rgbVal);
				GameController.getStencil().set(x + xOfs + 1, y + 1, maskVal);
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent mouseevent) {
		final double scale = Core.getScale();
		mouseDx = 0;
		mouseDy = 0;
		final int x = (int) Math.round(mouseevent.getX() / scale/*-LemmCursor.width/2*/);
		final int y = (int) Math.round(mouseevent.getY() / scale/*-LemmCursor.height/2*/);
		LemmCursor.setX(x/*-LemmCursor.width/2*/);
		LemmCursor.setY(y/*-LemmCursor.height/2*/);
	}

	@Override
	public void mouseExited(final MouseEvent mouseevent) {
		final double scale = Core.getScale();
		int x = xMouseScreen + mouseDx;

		switch (GameController.getGameState()) {
			case BRIEFING:
			case DEBRIEFING:
			case LEVEL:
				if (x >= this.getWidth()) {
					x = this.getWidth() - 1;
				}

				if (x < 0) {
					x = 0;
				}

				xMouseScreen = x;
				x += GameController.getxPos() * scale;

				if (x >= Level.WIDTH) {
					x = Level.WIDTH - 1;
				}

				xMouse = x;
				LemmCursor.setX((int) Math.round(xMouseScreen / scale/*-LemmCursor.width/2*/));

				int y = yMouseScreen + mouseDy;

				if (y >= this.getHeight()) {
					y = this.getHeight() - 1;
				}

				if (y < 0) {
					y = 0;
				}

				yMouseScreen = y;
				y = yMouse + mouseDy;

				if (y >= Level.HEIGHT) {
					y = Level.HEIGHT - 1;
				}

				if (y < 0) {
					y = 0;
				}

				yMouse = y;
				LemmCursor.setY((int) Math.round(yMouseScreen / scale/*-LemmCursor.height/2*/));
				mouseevent.consume();
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseDragged(final MouseEvent mouseevent) {
		final double scale = Core.getScale();
		mouseDx = 0;
		mouseDy = 0;

		// check minimap mouse move
		switch (GameController.getGameState()) {
			case LEVEL:
				final int x = (int) Math.round(mouseevent.getX() / scale);
				final int y = (int) Math.round(mouseevent.getY() / scale);

				if (leftMousePressed) {
					final int ofs = MiniMap.move(x, y, (int) Math.round(this.getWidth() / scale));

					if (ofs != -1) {
						GameController.setxPos(ofs);
					}
				} else {
					int xOfsTemp = GameController.getxPos() + (x - mouseDragStartX);

					if (xOfsTemp < 0) {
						xOfsTemp = 0;
					} else if (xOfsTemp >= Level.WIDTH - this.getWidth() / scale) {
						GameController.setxPos((int) Math.round(Level.WIDTH - this.getWidth() / scale));
					} else {
						GameController.setxPos(xOfsTemp);
					}
				}

				// debug drawing
				debugDraw(x, y, leftMousePressed);
				mouseMoved(mouseevent);
				mouseevent.consume();
				break;
			default:
				break;
		}
	}

	@Override
	public void mouseMoved(final MouseEvent mouseevent) {
		// long t = System.currentTimeMillis();
		final double scale = Core.getScale();
		int x, y;
		final int oldX = xMouse;
		final int oldY = yMouse;

		x = (int) Math.round((mouseevent.getX() / scale + GameController.getxPos()));
		y = (int) Math.round(mouseevent.getY() / scale);

		if (x >= Level.WIDTH) {
			x = Level.WIDTH - 1;
		}

		if (y >= Level.HEIGHT) {
			y = Level.HEIGHT - 1;
		}

		xMouse = (int) Math.round(x * scale);
		yMouse = (int) Math.round(y * scale);
		// LemmCursor
		xMouseScreen = mouseevent.getX();

		if (xMouseScreen >= this.getWidth()) {
			xMouseScreen = this.getWidth();
		} else if (xMouseScreen < 0) {
			xMouseScreen = 0;
		}

		yMouseScreen = mouseevent.getY();

		if (yMouseScreen >= this.getHeight()) {
			yMouseScreen = this.getHeight();
		} else if (yMouseScreen < 0) {
			yMouseScreen = 0;
		}

		LemmCursor.setX((int) Math.round(xMouseScreen / scale/*-LemmCursor.width/2*/));
		LemmCursor.setY((int) Math.round(yMouseScreen / scale/*-LemmCursor.height/2*/));

		switch (GameController.getGameState()) {
			case INTRO:
			case BRIEFING:
			case DEBRIEFING:
				TextScreen.getDialog().handleMouseMove((int) Math.round(xMouseScreen / scale),
						(int) Math.round(yMouseScreen / scale));
				//$FALL-THROUGH$
			case LEVEL:
				mouseDx = (xMouse - oldX);
				mouseDy = (yMouse - oldY);
				mouseDragStartX = (int) Math.round(mouseevent.getX() / scale);
				mouseevent.consume();
				break;

			default:
				break;
		}
	}

	/**
	 * Get cursor x position in pixels.
	 * 
	 * @return cursor x position in pixels
	 */
	int getCursorX() {
		return xMouse;
	}

	/**
	 * Get cursor y position in pixels.
	 * 
	 * @return cursor y position in pixels
	 */
	int getCursorY() {
		return yMouse;
	}

	/**
	 * Get flag: Shift key is pressed?
	 * 
	 * @return true if Shift key is pressed, false otherwise
	 */
	boolean isShiftPressed() {
		return shiftPressed;
	}

	/**
	 * Set flag: Shift key is pressed.
	 * 
	 * @param p true: Shift key is pressed,false otherwise
	 */
	void setShiftPressed(final boolean p) {
		shiftPressed = p;
	}

	/**
	 * Get state of debug draw option.
	 * 
	 * @return true: debug draw is active, false otherwise
	 */
	boolean getDebugDraw() {
		return draw;
	}

	/**
	 * Set state of debug draw option.
	 * 
	 * @param d true: debug draw is active, false otherwise
	 */
	void setDebugDraw(final boolean d) {
		draw = d;
	}
}