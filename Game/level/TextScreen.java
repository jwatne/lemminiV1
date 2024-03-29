package game.level;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import game.Core;
import game.GameController;
import game.LemmFont;
import game.LevelCode;
import game.LevelPack;
import game.MiscGfx;
import game.TextDialog;
import gameutil.FaderHandler;
import lemmini.Constants;
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
 * Class to print text screens which can be navigated with the mouse. Uses
 * {@link TextDialog}
 *
 * @author Volker Oth
 */
public final class TextScreen {

    /**
     * Y position = -120.
     */
    private static final int Y_120 = -120;
    /**
     * Rotation factor = 0.1.
     */
    private static final double FACTOR_POINT_ONE = 0.1;
    /**
     * Delta value used for rotation animation = -0.1.
     */
    private static final double DELTA_NEG_POINT_ONE = -0.1;
    /**
     * 9 charcters to the right.
     */
    private static final int X9 = 9;
    /**
     * 12 characters to the left.
     */
    private static final int X_12 = -12;
    /**
     * Line 5 relative to line 0.
     */
    private static final int L5 = 5;
    /**
     * 95 percent.
     */
    private static final int NINETY_FIVE_PERCENT = 95;
    /**
     * 50 per cent.
     */
    private static final int FIFTY_PERCENT = 50;
    /**
     * Line -4 relative to line 0.
     */
    private static final int L_4 = -4;
    /**
     * Line -5 relative to line 0.
     */
    private static final int L_5 = -5;
    /**
     * Line -7 relative to line 0.
     */
    private static final int L_7 = -7;
    /**
     * 9 characters to the left.
     */
    private static final int X_9 = -9;
    /**
     * Line -2 relative to line 0.
     */
    private static final int L_2 = -2;
    /**
     * Preview map y-position.
     */
    private static final int MAP_PREVIEW_Y = -200;
    /**
     * Line 4, relative to line 0, of text on screen.
     */
    private static final int L4 = 4;
    /**
     * Line 3, relative to line 0, of text on screen.
     */
    private static final int L3 = 3;

    /** Mode (type of screen to present). */
    public enum Mode {
        /** initial state. */
        INIT,
        /** main introduction screen. */
        INTRO,
        /** level briefing screen. */
        BRIEFING,
        /** level debriefing screen. */
        DEBRIEFING
    }

    /** Button: continue. */
    public static final int BUTTON_CONTINUE = 0;
    /** Button: restart level. */
    public static final int BUTTON_RESTART = 1;
    /** Button: back to menu. */
    public static final int BUTTON_MENU = 2;
    /** Button: replay level. */
    public static final int BUTTON_REPLAY = 3;
    /** Button: save replay. */
    public static final int BUTTON_SAVEREPLAY = 4;

    /** y position of scroll text - pixels relative to center. */
    private static final int SCROLL_Y = 150;
    /** width of scroll text in characters. */
    private static final int SCROLL_WIDTH = 39;
    /** height of scroll text in pixels. */
    private static final int SCROLL_HEIGHT = LemmFont.getHeight() * 2;
    /** step width of scroll text in pixels. */
    private static final int SCROLL_STEP = 2;
    /** scroll text. */
    private static final String SCROLL_TEXT = """
             \s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s
             \s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\s\
            Lemmini - a game engine for Lemmings (tm) in Java.\s\
            Thanks to Martin Cameron for his MicroMod Library,\s\
            Jef Poskanzer for his GifEncoder Library,\s\
            Mindless for his MOD conversions of the original Amiga
             Lemmings tunes,\s\
            the guys of DMA Design for writing the original Lemmings,\s\
            ccexplore and the other nice folks at the Lemmingswelt Forum
             for discussion and advice\s\
            and to Oracle and the OpenJDK community for maintaining Java
             and providing the community with a free development
              environment.""";

    /** TextDialog used as base component. */
    private static TextDialog textScreen;
    /** factor used for the rotation animation. */
    private static double rotFact = 1.0;
    /** delta used for the rotation animation. */
    private static double rotDelta;
    /** source image for rotation animation. */
    private static BufferedImage imgSrc;
    /** target image for rotation animation. */
    private static BufferedImage imgTrg;
    /** graphics for rotation animation. */
    private static Graphics2D imgGfx;
    /** flip state for rotation: true - image is flipped in Y direction. */
    private static boolean flip;
    /** affine transformation used for rotation animation. */
    private static AffineTransform at;
    /**
     * Counter used to trigger the rotation animation (in animation update
     * frames).
     */
    private static int rotCtr;
    /**
     * Counter threshold used to trigger the rotation animation (in animation
     * update frames).
     */
    private static final int MAX_ROT_CTR = 99;
    /**
     * Used to stop the rotation only after it was flipped twice -> original
     * direction.
     */
    private static int flipCtr;
    /** counter for scrolled characters. */
    private static int scrollCharCtr;
    /** counter for scrolled pixels. */
    private static int scrollPixCtr;
    /** image used for scroller. */
    private static BufferedImage scrollerImg;
    /** graphics used for scroller. */
    private static Graphics2D scrollerGfx;
    /** screen type to display. */
    private static Mode mode;
    /** synchronization monitor. */
    private static Object monitor = new Object();
    /**
     * Initial value of zoom scale.
     */
    private static double oldScale = Core.getScale();

    /**
     * Private constructor for utility class.
     */
    private TextScreen() {

    }

    /**
     * Set mode.
     *
     * @param m mode.
     */
    public static void setMode(final Mode m) {
        synchronized (monitor) {
            final double scale = Core.getScale();
            if (mode != m || oldScale != scale) {
                switch (m) {
                case INTRO:
                    textScreen.init();
                    textScreen.fillBackground(
                            MiscGfx.getImage(MiscGfx.Index.TILE_BROWN));
                    textScreen.printCentered(
                            "A game engine for Lemmings(tm) in Java", 0,
                            LemmFont.Color.RED);
                    textScreen.printCentered("Release 1.02 08/2023", 1,
                            LemmFont.Color.BLUE);
                    textScreen.printCentered("Coded by Volker Oth 2005-2017", 2,
                            LemmFont.Color.VIOLET);
                    textScreen.printCentered("Updated by John Watne 2023", L3,
                            LemmFont.Color.BLUE);
                    textScreen.printCentered("www.lemmini.de", L4,
                            LemmFont.Color.GREEN);
                    textScreen.copyToBackBuffer();
                    break;
                case BRIEFING:
                    initBriefing();
                    break;
                case DEBRIEFING:
                    initDebriefing();
                    break;
                default:
                    break;
                }
            }
            mode = m;
            oldScale = scale;
        }
    }

    /**
     * Initialize the briefing dialog.
     */
    static void initBriefing() {
        textScreen.init();
        textScreen.fillBackground(MiscGfx.getImage(MiscGfx.Index.TILE_GREEN));
        final Level level = GameController.getLevel();
        // LevelInfo li;
        textScreen.restore();
        final String rating = FaderHandler.getCurLevelPack()
                .getDiffLevels()[FaderHandler.getCurDiffLevel()];
        textScreen.drawImage(GameController.getMapPreview(), MAP_PREVIEW_Y);
        textScreen
                .printCentered(
                        "Level " + (FaderHandler.getCurLevelNumber() + 1) + " "
                                + level.getLevelName(),
                        L_2, LemmFont.Color.RED);
        textScreen.print("Number of Lemmings " + level.getNumLemmings(), X_9, 0,
                LemmFont.Color.BLUE);
        textScreen.print(
                "" + (level.getNumToRescue() * Constants.ONE_HUNDRED_PERCENT
                        / level.getNumLemmings()) + "% to be saved",
                X_9, 1, LemmFont.Color.GREEN);
        textScreen.print("Release Rate " + level.getReleaseRate(), X_9, 2,
                LemmFont.Color.BROWN);
        final int minutes = level.getTimeLimitSeconds()
                / Constants.SECONDS_PER_MINUTE;
        final int seconds = level.getTimeLimitSeconds()
                % Constants.SECONDS_PER_MINUTE;

        if (seconds == 0) {
            textScreen.print("Time         " + minutes + " Minutes", X_9, L3,
                    LemmFont.Color.TURQUOISE);
        } else {
            textScreen.print(
                    "Time         " + minutes + "-" + seconds + " Minutes", X_9,
                    L3, LemmFont.Color.TURQUOISE);
        }

        textScreen.print("Rating       " + rating, X_9, L4,
                LemmFont.Color.VIOLET);
        textScreen.copyToBackBuffer(); // though not really needed
    }

    /**
     * Initialize the debriefing dialog.
     */
    static void initDebriefing() {
        textScreen.init();
        textScreen.fillBackground(MiscGfx.getImage(MiscGfx.Index.TILE_GREEN));
        final int toRescue = GameController.getNumToRescue() * 100
                / GameController.getNumLemmingsMax(); // % to rescue
                                                      // of
        // total number
        final int rescued = GameController.getNumLeft() * 100
                / GameController.getNumLemmingsMax(); // % rescued of
                                                      // total
        // number
        final int rescuedOfToRescue = GameController.getNumLeft() * 100
                / GameController.getNumToRescue(); // % rescued
                                                   // of no.
        // to rescue
        textScreen.restore();
        if (GameController.getTime() == 0) {
            textScreen.printCentered("Time is up.", L_7,
                    LemmFont.Color.TURQUOISE);
        } else {
            textScreen.printCentered("All lemmings accounted for.", L_7,
                    LemmFont.Color.TURQUOISE);
        }

        textScreen.print("You needed:  " + Integer.toString(toRescue) + "%",
                L_7, L_5, LemmFont.Color.VIOLET);
        textScreen.print("You rescued: " + Integer.toString(rescued) + "%", L_7,
                L_4, LemmFont.Color.VIOLET);

        if (GameController.wasLost()) {
            if (rescued == 0) {
                textScreen.printCentered("ROCK BOTTOM! I hope for your sake",
                        L_2, LemmFont.Color.RED);
                textScreen.printCentered("that you nuked that level", -1,
                        LemmFont.Color.RED);
            } else if (rescuedOfToRescue < FIFTY_PERCENT) {
                textScreen.printCentered("Better rethink your strategy before",
                        L_2, LemmFont.Color.RED);
                textScreen.printCentered("you try this level again!", -1,
                        LemmFont.Color.RED);
            } else if (rescuedOfToRescue < NINETY_FIVE_PERCENT) {
                textScreen.printCentered("A little more practice on this level",
                        L_2, LemmFont.Color.RED);
                textScreen.printCentered("is definitely recommended.", -1,
                        LemmFont.Color.RED);
            } else {
                textScreen.printCentered("You got pretty close that time.", L_2,
                        LemmFont.Color.RED);
                textScreen.printCentered("Now try again for that few % extra.",
                        -1, LemmFont.Color.RED);
            }

            textScreen.addTextButton(L_2, L5, BUTTON_RESTART, "Retry", "Retry",
                    LemmFont.Color.BLUE, LemmFont.Color.BROWN);
        } else {
            if (rescued == Constants.ONE_HUNDRED_PERCENT) {
                textScreen.printCentered("Superb! You rescued every lemming on",
                        L_2, LemmFont.Color.RED);
                textScreen.printCentered("that level. Can you do it again....?",
                        -1, LemmFont.Color.RED);
            } else if (rescued > toRescue) {
                textScreen.printCentered("You totally stormed that level!", L_2,
                        LemmFont.Color.RED);
                textScreen.printCentered(
                        "Let's see if you can storm the next...", -1,
                        LemmFont.Color.RED);
            } else if (rescued == toRescue) {
                textScreen.printCentered("SPOT ON. You can't get much closer",
                        L_2, LemmFont.Color.RED);
                textScreen.printCentered("than that. Let's try the next....",
                        -1, LemmFont.Color.RED);
            } else {
                textScreen.printCentered(
                        "That level seemed no problem to you on", L_2,
                        LemmFont.Color.RED);
                textScreen.printCentered(
                        "that attempt. Onto the next....       ", -1,
                        LemmFont.Color.RED);
            }

            final LevelPack lp = FaderHandler.getCurLevelPack();
            final int ln = FaderHandler.getCurLevelNumber();

            if (lp.getLevels(FaderHandler.getCurDiffLevel()).length > ln + 1) {
                textScreen.printCentered(
                        "Your access code for level " + (ln + 2), 1,
                        LemmFont.Color.BROWN);
                final int absLevel = FaderHandler.absLevelNum(
                        FaderHandler.getCurLevelPackIdx(),
                        FaderHandler.getCurDiffLevel(), ln + 1);
                final String code = LevelCode.create(lp.getCodeSeed(), absLevel,
                        rescued, 0, lp.getCodeOffset());
                textScreen.printCentered("is " + code, 2, LemmFont.Color.BROWN);
                textScreen.addTextButton(L_4, L5, BUTTON_CONTINUE, "Continue",
                        "Continue", LemmFont.Color.BLUE, LemmFont.Color.BROWN);
            } else {
                textScreen.printCentered("Congratulations!", 1,
                        LemmFont.Color.BROWN);
                textScreen.printCentered("You finished all the "
                        + lp.getDiffLevels()[FaderHandler.getCurDiffLevel()]
                        + " levels!", 2, LemmFont.Color.GREEN);
            }
        }

        textScreen.copyToBackBuffer(); // though not really needed
        textScreen.addTextButton(X_12, L4, BUTTON_REPLAY, "Replay", "Replay",
                LemmFont.Color.BLUE, LemmFont.Color.BROWN);

        if (FaderHandler.getCurLevelPackIdx() != 0) {
            textScreen.addTextButton(L_4, L4, BUTTON_SAVEREPLAY, "Save Replay",
                    "Save Replay", LemmFont.Color.BLUE, LemmFont.Color.BROWN);
        }

        textScreen.addTextButton(X9, L4, BUTTON_MENU, "Menu", "Menu",
                LemmFont.Color.BLUE, LemmFont.Color.BROWN);
    }

    /**
     * Get text dialog.
     *
     * @return text dialog.
     */
    public static TextDialog getDialog() {
        synchronized (monitor) {
            return textScreen;
        }
    }

    /**
     * Initialize text screen.
     *
     * @param width  width in pixels
     * @param height height in pixels
     */
    public static void init(final int width, final int height) {
        synchronized (monitor) {
            rotFact = 1.0;
            rotDelta = DELTA_NEG_POINT_ONE;
            imgSrc = MiscGfx.getImage(MiscGfx.Index.LEMMINI);
            at = new AffineTransform();
            flip = false;
            rotCtr = 0;
            flipCtr = 0;
            imgTrg = ToolBox.createImage(imgSrc.getWidth(), imgSrc.getHeight(),
                    Transparency.TRANSLUCENT);
            imgGfx = imgTrg.createGraphics();
            imgGfx.setBackground(new Color(0, 0, 0, 0)); // invisible
            scrollCharCtr = 0;
            scrollPixCtr = 0;

            scrollerImg = ToolBox.createImage(
                    LemmFont.getWidth() * (1 + SCROLL_WIDTH), SCROLL_HEIGHT,
                    Transparency.BITMASK);
            scrollerGfx = scrollerImg.createGraphics();
            scrollerGfx.setBackground(new Color(0, 0, 0, 0));

            textScreen = new TextDialog(width, height);
        }
    }

    /**
     * Update the text screen (for animations).
     */
    public static void update() {
        synchronized (monitor) {
            textScreen.restore();

            switch (mode) {
            case INTRO:
                updateIntro();
                break;
            case BRIEFING:
                updateBriefing();
                break;
            case DEBRIEFING:
                updateDebriefing();
                break;
            default:
                break;
            }
        }
    }

    /**
     * Update the into screen.
     */
    private static void updateIntro() {
        // manage logo rotation
        if (++rotCtr > MAX_ROT_CTR) {
            // animate
            rotFact += rotDelta;

            if (rotFact <= 0.0) {
                // minimum size reached -> flip and increase again
                rotFact = FACTOR_POINT_ONE;
                rotDelta = -rotDelta;
                flip = !flip;
            } else if (rotFact > 1.0) {
                // maximum size reached -> decrease again
                rotFact = 1.0;
                rotDelta = -rotDelta;

                // reset only after two rounds (flipped back)
                if (++flipCtr > 1) {
                    rotCtr = 0;
                }
            }

            if (flip) {
                at.setToScale(1, -rotFact);
                at.translate(1, -imgSrc.getHeight());
            } else {
                at.setToScale(1, rotFact);
            }

            final AffineTransformOp op = new AffineTransformOp(at,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            imgGfx.clearRect(0, 0, imgTrg.getWidth(), imgTrg.getHeight());
            op.filter(imgSrc, imgTrg);
            textScreen.drawImage(imgTrg,
                    Y_120 - (int) (imgSrc.getHeight() / 2 * Math.abs(rotFact)
                            + Constants.HALF));
        } else {
            // display original image
            flipCtr = 0;
            textScreen.drawImage(imgSrc, Y_120 - imgSrc.getHeight() / 2);
        }

        // manage scroller
        String out;
        boolean wrapAround = false;
        int endIdx = scrollCharCtr + SCROLL_WIDTH + 1;

        if (endIdx > SCROLL_TEXT.length()) {
            endIdx = SCROLL_TEXT.length();
            wrapAround = true;
        }

        out = SCROLL_TEXT.substring(scrollCharCtr, endIdx);

        if (wrapAround) {
            out += SCROLL_TEXT.substring(0,
                    scrollCharCtr + SCROLL_WIDTH + 1 - SCROLL_TEXT.length());
        }

        scrollerGfx.clearRect(0, 0, scrollerImg.getWidth(),
                scrollerImg.getHeight());
        LemmFont.strImage(scrollerGfx, out, LemmFont.Color.BLUE);
        final int w = SCROLL_WIDTH * LemmFont.getWidth();
        final int dx = (textScreen.getScreen().getWidth() - w) / 2;
        final int dy = (textScreen.getScreen().getHeight() / 2) + SCROLL_Y;
        textScreen.getScreen().createGraphics().drawImage(scrollerImg, dx, dy,
                dx + w, dy + SCROLL_HEIGHT, scrollPixCtr, 0, scrollPixCtr + w,
                SCROLL_HEIGHT / 2, null);

        scrollPixCtr += SCROLL_STEP;

        if (scrollPixCtr >= LemmFont.getWidth()) {
            scrollCharCtr++;
            scrollPixCtr = 0;
            if (scrollCharCtr >= SCROLL_TEXT.length()) {
                scrollCharCtr = 0;
            }
        }
    }

    /**
     * Update the briefing screen.
     */
    private static void updateBriefing() {

    }

    /**
     * Update the debriefing screen.
     */
    private static void updateDebriefing() {
        textScreen.drawButtons();
    }

    /**
     * Get image of text screen.
     *
     * @return image of text screen
     */
    public static BufferedImage getScreen() {
        synchronized (monitor) {
            return textScreen.getScreen();
        }
    }
}
