package Game;

import java.util.Iterator;
import java.util.List;

import GameUtil.Fader;
import Graphics.Color;
import Graphics.GraphicsContext;
import Graphics.Image;
import Tools.MicrosecondTimer;
import Tools.ToolBox;

/*
 * Copyright 2010 Arne Limburg
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

public abstract class AbstractGameEngine implements Runnable {
	/** minimum sleep duration in milliseconds - values too small may cause system clock shift under WinXP etc. */
	final static int MIN_SLEEP = 10;
	/** threshold for sleep - don't sleep if time to wait is shorter than this as sleep might return too late */
	final static int THR_SLEEP = 16;
	/** size of auto scrolling range in pixels (from the left and right border) */
	final static int AUTOSCROLL_RANGE = 20;
	/** step size in pixels for horizontal scrolling */
	public final static int X_STEP = 4;
	/** step size in pixels for fast horizontal scrolling */
	public final static int X_STEP_FAST = 8;
	/** y coordinate of score display in pixels */
	final static int scoreY = Level.HEIGHT;
	/** y coordinate of counter displays in pixels */
	final static int counterY = scoreY+40;
	/** y coordinate of icons in pixels */
	public final static int iconsY = counterY+14;
	/** x coordinate of minimap in pixels */
	public final static int smallX = 640-16/*-32*/-200;
	/** y coordinate of minimap in pixels */
	public final static int smallY = iconsY;
	/** image for information string display */
	private Image outStrImg;
	/** graphics object for information string display */
	private GraphicsContext outStrGfx;
	/** array of offscreen images (one is active, one is passive) */
	private Image offImage[];
	/** graphics objects for the two offscreen images */
	private GraphicsContext offGraphics[];
	/** index of the active buffer in the image buffer */
	private int activeBuffer;
	/** monitoring object used for synchronized painting */
	private final Object paintSemaphore = new Object();

	public abstract int getWidth();
	
	public abstract int getHeight();
	
	/**
	 * Initialization.
	 */
	public void init() {
		offImage = new Image[2];
		offGraphics = new GraphicsContext[2];
		offImage[0] = ToolBox.INSTANCE.get().createOpaqueImage(this.getWidth(), this.getHeight());
		offImage[1] = ToolBox.INSTANCE.get().createOpaqueImage(this.getWidth(), this.getHeight());
		offGraphics[0] = offImage[0].createGraphicsContext();
		offGraphics[1] = offImage[1].createGraphicsContext();

		outStrImg = ToolBox.INSTANCE.get().createBitmaskImage(this.getWidth(), LemmFont.getHeight());
		outStrGfx = outStrImg.createGraphicsContext();
		outStrGfx.setBackground(new Color(0,0,0));

		TextScreen.init(this.getWidth(), this.getHeight());
		GameController.setGameState(GameController.State.INTRO);
		GameController.setTransition(GameController.TransitionState.NONE);
		Fader.setBounds(this.getWidth(), this.getHeight());
		Fader.setState(Fader.State.IN);
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY+1);
		MicrosecondTimer timerRepaint = new MicrosecondTimer();
		try {
			while (isRunning()) {
				GameController.State gameState = GameController.getGameState();
				// Try to keep the Amiga timing. Note that no frames are skipped
				// If one frame is too late, the next one will be a little earlier
				// to compensate. No frames are skipped though!
				// On a slow CPU this might slow down gameplay...
				if (timerRepaint.timePassedAdd(GameController.MICROSEC_PER_FRAME)) {
					// time passed -> redraw necessary
					redraw();
					// special handling for fast forward or super lemming mode only during real gameplay
					if (gameState == GameController.State.LEVEL) {
						// in fast forward or super lemming modes, update the game mechanics
						// multiple times per (drawn) frame
						if (GameController.isFastForward())
							for (int f=0; f<GameController.FAST_FWD_MULTI-1;f++)
								GameController.update();
						else if (GameController.isSuperLemming())
							for (int f=0; f<GameController.SUPERLEMM_MULTI-1;f++)
								GameController.update();
					}
				} else {
					try {
						// determine time until next frame
						long diff = GameController.MICROSEC_PER_FRAME - timerRepaint.delta();
						if (diff > GameController.MICROSEC_RESYNC) {
							timerRepaint.update(); // resync to time base
							System.out.println("Resynced, diff was "+(diff/1000)+" millis");
						} else if (diff > THR_SLEEP*1000)
							Thread.sleep(MIN_SLEEP);
					} catch (InterruptedException ex) {}
				}
			}
		} catch (Exception ex) {
			ToolBox.INSTANCE.get().showException(ex);
			System.exit(1);
		}  catch (Error ex) {
			ToolBox.INSTANCE.get().showException(ex);
			System.exit(1);
		}
	}
	/**
	 * redraw the offscreen image, then flip buffers and force repaint.
	 */
	private void redraw() {
		int drawBuffer;
		GraphicsContext offGfx;

		synchronized (paintSemaphore) {
			if (offImage == null)
				init();
			drawBuffer = (activeBuffer == 0) ? 1:0;
			offGfx = offGraphics[drawBuffer];
		}

		Image bgImage = GameController.getBgImage();
		switch (GameController.getGameState()) {
			case INTRO:
				TextScreen.setMode(TextScreen.Mode.INTRO);
				TextScreen.update();
				offGfx.drawImage(TextScreen.getScreen(), 0, 0);
				//offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x, LemmCursor.y, null);
				break;
			case BRIEFING:
				TextScreen.setMode(TextScreen.Mode.BRIEFING);
				TextScreen.update();
				offGfx.drawImage(TextScreen.getScreen(), 0, 0);
				//offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x, LemmCursor.y, null);
				break;
			case DEBRIEFING:
				TextScreen.setMode(TextScreen.Mode.DEBRIEFING);
				TextScreen.update();
				offGfx.drawImage(TextScreen.getScreen(), 0,0);
				TextScreen.getDialog().handleMouseMove(getScreenMouseX(), getScreenMouseY());
				//offGfx.drawImage(LemmCursor.getImage(LemmCursor.TYPE_NORMAL), LemmCursor.x, LemmCursor.y, null);
				break;
			case LEVEL:
			case LEVEL_END:
				if (bgImage != null) {
					GameController.update();
					// mouse movement
					if (getScreenMouseY() > 40 && getScreenMouseY() <scoreY) { // avoid scrolling if menu is selected
						int xOfsTemp;
						if (getScreenMouseX() > this.getWidth() - AUTOSCROLL_RANGE) {
							xOfsTemp = GameController.getxPos() + (isShiftPressed() ? X_STEP_FAST : X_STEP);
							if (xOfsTemp < Level.WIDTH-this.getWidth())
								GameController.setxPos(xOfsTemp);
							else
								GameController.setxPos(Level.WIDTH-this.getWidth());
						} else if (getScreenMouseX() < AUTOSCROLL_RANGE) {
							xOfsTemp = GameController.getxPos() - (isShiftPressed() ? X_STEP_FAST : X_STEP);
							if (xOfsTemp > 0)
								GameController.setxPos(xOfsTemp);
							else
								GameController.setxPos(0);
						}
					}
					// store local copy of xOfs to avoid sync problems with AWT threads
					// (scrolling by dragging changes xOfs as well)
					int xOfsTemp = GameController.getxPos();

					//timeBaseRedraw +=GameController.timePerFrame;
					int w = this.getWidth();
					int h = Level.HEIGHT;
					if (h>this.getHeight())
						h = this.getHeight();

					Level level = GameController.getLevel();
					if (level != null) {

						// clear screen
						offGfx.setClip(0,0,w,h);
						offGfx.setBackground(level.getBgColor());
						offGfx.clearRect(0, 0, w, h);

						// draw "behind" objects
						GameController.getLevel().drawBehindObjects(offGfx, w, xOfsTemp);

						// draw background
						offGfx.drawImage(bgImage, 0, 0, w, h, xOfsTemp, 0, xOfsTemp+w, h);

						// draw "in front" objects
						GameController.getLevel().drawInFrontObjects(offGfx, w, xOfsTemp);
					}
					// clear parts of the screen for menu etc.
					offGfx.setClip(0,Level.HEIGHT,w,this.getHeight());
					offGfx.setBackground(Color.BLACK);
					offGfx.clearRect(0,scoreY,w,this.getHeight());
					// draw counter, icons, small level pic
					// draw menu
					//Icons icons = GameController.getIcons();
					GameController.drawIcons(offGfx, 0, iconsY);
					offGfx.drawImage(MiscGfx.getImage(MiscGfx.Index.BORDER), smallX-4, smallY-4);
					MiniMap.draw(offGfx, smallX, smallY, xOfsTemp);
					// draw counters
					GameController.drawCounters(offGfx,counterY);

					// draw lemmings
					offGfx.setClip(0,0,w,h);
					GameController.getLemmsUnderCursor().clear();
					List<Lemming> lemmings = GameController.getLemmings();
					synchronized (GameController.getLemmings()) {
						Iterator<Lemming> it = lemmings.iterator();
						while (it.hasNext()) {
							Lemming l = it.next();
							int lx = l.screenX();
							int ly = l.screenY();
							int mx = l.midX()-16;
							if (lx+l.width() > xOfsTemp && lx < xOfsTemp+w) {
								offGfx.drawImage(l.getImage(),lx-xOfsTemp,ly);
								if (LemmCursor.doesCollide(l, xOfsTemp)) {
									GameController.getLemmsUnderCursor().add(l);
								}
								Image cd = l.getCountdown();
								if (cd!=null)
									offGfx.drawImage(cd,mx-xOfsTemp,ly-cd.getHeight());

								Image sel = l.getSelectImg();
								if (sel!=null)
									offGfx.drawImage(sel,mx-xOfsTemp,ly-sel.getHeight());

							}
						}
						// draw pixels in mini map
						offGfx.setClip(0,0,w,this.getHeight());
						it = lemmings.iterator();
						while (it.hasNext()) {
							Lemming l = it.next();
							int lx = l.screenX();
							int ly = l.screenY();
							// draw pixel in mini map
							MiniMap.drawLemming(offGfx,  lx, ly);
						}
					}
					Lemming lemmUnderCursor = GameController.lemmUnderCursor(LemmCursor.getType());
					offGfx.setClip(0,0,w,h);
					// draw explosions
					GameController.drawExplosions(offGfx,offImage[0].getWidth(), Level.HEIGHT, xOfsTemp);
					offGfx.setClip(0,0,w,this.getHeight());

					// draw info string
					outStrGfx.clearRect(0, 0, outStrImg.getWidth(), outStrImg.getHeight());
					if (GameController.isCheat()) {
						Stencil stencil = GameController.getStencil();
						if (stencil != null) {
							int stencilVal = stencil.get(getMouseX()+getMouseY()*Level.WIDTH);
							String test = "x: "+getMouseX()+", y: "+getMouseY()+", mask: "+(stencilVal&0xffff)+" "+Stencil.getObjectID(stencilVal);
							LemmFont.strImage(outStrGfx, test);
							offGfx.drawImage(outStrImg,4,Level.HEIGHT+8);
						}
					} else {
						StringBuffer sb = new StringBuffer();
						sb.append("OUT ");
						String s = Integer.toString(GameController.getLemmings().size());
						sb.append(s);
						if (s.length()==1)
							sb.append(" ");
						sb.append("  IN ");
						s = Integer.toString(GameController.getNumLeft()*100/GameController.getNumLemmingsMax());
						if (s.length()==1)
							sb.append("0");
						sb.append(s);
						sb.append("%  TIME ").append(GameController.getTimeString());
						//BufferedImage iout = LemmFont.strImage(out);
						String n=null;
						if (lemmUnderCursor != null) {
							n = lemmUnderCursor.getName();
							// display also the total number of lemmings under the cursor
							int num = GameController.getLemmsUnderCursor().size();
							if (num > 1)
								n = n + " " + Integer.toString(num);
						}
						if (n!=null) {
							int ln = n.length();
							if (ln>14)
								ln = 14;
							sb.insert(0,"              ".substring(0, 14-ln));
							sb.insert(0,n);
						} else
							sb.insert(0,"              ");
						LemmFont.strImage(outStrGfx, sb.toString());
						offGfx.drawImage(outStrImg,4,Level.HEIGHT+8);
					}
					// replay icon
					Image replayImage = GameController.getReplayImage();
					if (replayImage != null)
						offGfx.drawImage(replayImage,this.getWidth()-2*replayImage.getWidth(),replayImage.getHeight());
					// draw cursor
					if (lemmUnderCursor != null) {
						int lx = lemmUnderCursor.midX()-xOfsTemp;
						int ly = lemmUnderCursor.midY();
						Image cursorImg = LemmCursor.getBoxImage();
						lx -= cursorImg.getWidth()/2;
						ly -= cursorImg.getHeight()/2;
						offGfx.drawImage(cursorImg,lx,ly);
					}
					//offGfx.drawImage(LemmCursor.getImage(0), LemmCursor.x, LemmCursor.y, null);
				}
		}

		// fader
		GameController.fade(offGfx);
		// and all onto screen
		activeBuffer = drawBuffer;

		repaint();
	}
	
	public Object getPaintSemaphore() {
		return paintSemaphore;
	}
	
	public Image getActiveBuffer() {
		if (offImage == null) {
			return null;
		}
		return offImage[activeBuffer];
	}
	
	protected abstract boolean isRunning();
	
	protected abstract int getMouseX();
	
	protected abstract int getMouseY();
	
	protected abstract int getScreenMouseX();
	
	protected abstract int getScreenMouseY();
	
	protected abstract boolean isShiftPressed();
	
	protected abstract void repaint();
}
