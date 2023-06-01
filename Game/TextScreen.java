package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

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
     * Add 0.5 to int value to round up as needed.
     */
    private static final double ROUND_UP = 0.5;
    /**
     * Image base y position before adjustments.
     */
    private static final int IMAGE_BASE_Y = -120;
    /**
     * One tenth (0.1).
     */
    private static final double ONE_TENTH = 0.1;
    /**
     * Default rotDelta value.
     */
    private static final double DEFAULT_ROT_DELTA = -0.1;
    /**
     * Menu button y position.
     */
    private static final int MENU_BUTTON_Y = 4;
    /**
     * Menu button x position.
     */
    private static final int MENU_BUTTON_X = 9;
    /**
     * Save replay button y position.
     */
    private static final int SAVE_REPLAY_BUTTON_Y = 4;
    /**
     * Replay button y position.
     */
    private static final int REPLAY_BUTTON_Y = 4;
    /**
     * Replay button x position.
     */
    private static final int REPLAY_BUTTON_X = -12;
    /**
     * Continue button y position.
     */
    private static final int CONTINUE_BUTTON_Y = 5;
    /**
     * Restart button y position.
     */
    private static final int RESTART_BUTTON_Y = 5;
    /**
     * 95%.
     */
    private static final int NINETY_FIVE_PERCENT = 95;
    /**
     * Half (50%).
     */
    private static final int HALF = 50;
    /**
     * # of lemmings rescued y position relative to center.
     */
    private static final int RESCUED_Y = -4;
    /**
     * # of lemmings needed to rescue y position relative to center.
     */
    private static final int NEEDED_TO_RESCUE_Y = -5;
    /**
     * Messages for how level ended y position relative to center.
     */
    private static final int LEVEL_END_Y = -7;
    /**
     * Rating y position relative to center.
     */
    private static final int RATING_Y = 4;
    /**
     * Minutes y position relative to center.
     */
    private static final int MINUTES_Y = 3;
    /**
     * Number of seconds per minute.
     */
    private static final int SECONDS_PER_MINUTE = 60;
    /**
     * Multiplier to convert decimal value to percentage.
     */
    private static final int ONE_HUNDRED_PERCENT = 100;
    /**
     * # of lemmings y position relative to center.
     */
    private static final int NUM_LEMMINGS_Y = -9;
    /**
     * Level number and description y position relative to center.
     */
    private static final int LEVEL_Y = -2;
    /**
     * Map preview Y position relative to center.
     */
    private static final int MAP_PREVIEW_Y = -200;
    /**
     * 4-character height.
     */
    private static final int CHAR4 = 4;
    /**
     * 3-character height.
     */
    private static final int CHAR3 = 3;

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
    private static final String SCROLL_TEXT = "                   "
            + "                        "
            + "Lemmini - a game engine for Lemmings (tm) in Java. "
            + "Thanks to Martin Cameron for his MicroMod Library, "
            + "Jef Poskanzer for his GifEncoder Library, "
            + "Mindless for his MOD conversions of the "
            + "original Amiga Lemmings tunes, "
            + "the guys of DMA Design for writing the original Lemmings, "
            + "ccexplore and the other nice folks at the Lemmingswelt "
            + "Forum for discussion and advice "
            + "and to Oracle for maintaining Java and providing the "
            + "community with a free OpenJDK development environment.";

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
     * counter used to trigger the rotation animation (in animation update
     * frames).
     */
    private static int rotCtr;
    /**
     * counter threshold used to trigger the rotation animation (in animation
     * update frames).
     */
    private static final int MAX_ROT_CTR = 99;
    /**
     * used to stop the rotation only after it was flipped twice -> original
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
    /** Initial scale value. */
    private static double oldScale = Core.getScale();

    /**
     * Private default constructor for utility class.
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
                            LemmingsFontColor.RED);
                    textScreen.printCentered("Release 1.00 06/2023", 1,
                            LemmingsFontColor.BLUE);
                    textScreen.printCentered("Coded by Volker Oth 2005-2017", 2,
                            LemmingsFontColor.VIOLET);
                    textScreen.printCentered("Updated by John Watne 2023",
                            CHAR3, LemmingsFontColor.BLUE);
                    textScreen.printCentered("www.lemmini.de", CHAR4,
                            LemmingsFontColor.GREEN);
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
        final String rating = GameController.getCurLevelPack()
                .getDiffLevels()[GameController.getCurDiffLevel()];
        textScreen.drawImage(GameController.getMapPreview(), MAP_PREVIEW_Y);
        textScreen
                .printCentered(
                        "Level " + (GameController.getCurLevelNumber() + 1)
                                + " " + level.getLevelName(),
                        LEVEL_Y, LemmingsFontColor.RED);
        textScreen.print("Number of " + "Lemmings " + level.getNumLemmings(),
                NUM_LEMMINGS_Y, 0, LemmingsFontColor.BLUE);
        textScreen.print(
                "" + (level.getNumToRescue() * ONE_HUNDRED_PERCENT
                        / level.getNumLemmings()) + "% to be saved",
                NUM_LEMMINGS_Y, 1, LemmingsFontColor.GREEN);
        textScreen.print("Release Rate " + level.getReleaseRate(),
                NUM_LEMMINGS_Y, 2, LemmingsFontColor.BROWN);
        final int minutes = level.getTimeLimitSeconds() / SECONDS_PER_MINUTE;
        final int seconds = level.getTimeLimitSeconds() % SECONDS_PER_MINUTE;

        if (seconds == 0) {
            textScreen.print("Time         " + minutes + " Minutes",
                    NUM_LEMMINGS_Y, MINUTES_Y, LemmingsFontColor.TURQUOISE);
        } else {
            textScreen.print(
                    "Time         " + minutes + "-" + seconds + " Minutes",
                    NUM_LEMMINGS_Y, MINUTES_Y, LemmingsFontColor.TURQUOISE);
        }

        textScreen.print("Rating       " + rating, NUM_LEMMINGS_Y, RATING_Y,
                LemmingsFontColor.VIOLET);
        textScreen.copyToBackBuffer(); // though not really needed
    }

    /**
     * Initialize the debriefing dialog.
     */
    static void initDebriefing() {
        textScreen.init();
        textScreen.fillBackground(MiscGfx.getImage(MiscGfx.Index.TILE_GREEN));
        final int toRescue = GameController.getNumToRecue() * 100
                / GameController.getNumLemmingsMax(); // % to rescue
                                                      // of
        // total number
        final int rescued = GameController.getNumLeft() * 100
                / GameController.getNumLemmingsMax(); // % rescued of
                                                      // total
        // number
        final int rescuedOfToRescue = GameController.getNumLeft() * 100
                / GameController.getNumToRecue();
        // % rescued of no. to rescue
        textScreen.restore();

        if (GameController.getTime() == 0) {
            textScreen.printCentered("Time is up.", LEVEL_END_Y,
                    LemmingsFontColor.TURQUOISE);
        } else {
            textScreen.printCentered("All lemmings accounted for.", LEVEL_END_Y,
                    LemmingsFontColor.TURQUOISE);
        }

        textScreen.print("You needed:  " + Integer.toString(toRescue) + "%",
                LEVEL_END_Y, NEEDED_TO_RESCUE_Y, LemmingsFontColor.VIOLET);
        textScreen.print("You rescued: " + Integer.toString(rescued) + "%",
                LEVEL_END_Y, RESCUED_Y, LemmingsFontColor.VIOLET);

        if (GameController.wasLost()) {
            if (rescued == 0) {
                textScreen.printCentered("ROCK BOTTOM! I hope for your sake",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("that you nuked that level", -1,
                        LemmingsFontColor.RED);
            } else if (rescuedOfToRescue < HALF) {
                textScreen.printCentered("Better rethink your strategy before",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("you try this level again!", -1,
                        LemmingsFontColor.RED);
            } else if (rescuedOfToRescue < NINETY_FIVE_PERCENT) {
                textScreen.printCentered("A little more practice on this level",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("is definitely recommended.", -1,
                        LemmingsFontColor.RED);
            } else {
                textScreen.printCentered("You got pretty close that time.",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("Now try again for that few % extra.",
                        -1, LemmingsFontColor.RED);
            }

            textScreen.addTextButton(LEVEL_Y, RESTART_BUTTON_Y, BUTTON_RESTART,
                    "Retry", "Retry", LemmingsFontColor.BLUE,
                    LemmingsFontColor.BROWN);
        } else {
            if (rescued == ONE_HUNDRED_PERCENT) {
                textScreen.printCentered("Superb! You rescued every lemming on",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("that level. Can you do it again....?",
                        -1, LemmingsFontColor.RED);
            } else if (rescued > toRescue) {
                textScreen.printCentered("You totally stormed that level!",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered(
                        "Let's see if you can storm the next...", -1,
                        LemmingsFontColor.RED);
            } else if (rescued == toRescue) {
                textScreen.printCentered("SPOT ON. You can't get much closer",
                        LEVEL_Y, LemmingsFontColor.RED);
                textScreen.printCentered("than that. Let's try the next....",
                        -1, LemmingsFontColor.RED);
            } else {
                textScreen.printCentered(
                        "That level seemed no problem to you on", LEVEL_Y,
                        LemmingsFontColor.RED);
                textScreen.printCentered(
                        "that attempt. Onto the next....       ", -1,
                        LemmingsFontColor.RED);
            }
            final LevelPack lp = GameController.getCurLevelPack();
            final int ln = GameController.getCurLevelNumber();
            if (lp.getLevels(GameController.getCurDiffLevel()).length > ln
                    + 1) {
                textScreen.printCentered(
                        "Your access code for level " + (ln + 2), 1,
                        LemmingsFontColor.BROWN);
                final int absLevel = GameController.absLevelNum(
                        GameController.getCurLevelPackIdx(),
                        GameController.getCurDiffLevel(), ln + 1);
                final String code = LevelCode.create(lp.getCodeSeed(), absLevel,
                        rescued, 0, lp.getCodeOffset());
                textScreen.printCentered("is " + code, 2,
                        LemmingsFontColor.BROWN);
                textScreen.addTextButton(RESCUED_Y, CONTINUE_BUTTON_Y,
                        BUTTON_CONTINUE, "Continue", "Continue",
                        LemmingsFontColor.BLUE, LemmingsFontColor.BROWN);
            } else {
                textScreen.printCentered("Congratulations!", 1,
                        LemmingsFontColor.BROWN);
                textScreen
                        .printCentered(
                                "You finished all the "
                                        + lp.getDiffLevels()[GameController
                                                .getCurDiffLevel()]
                                        + " levels!",
                                2, LemmingsFontColor.GREEN);
            }
        }

        textScreen.copyToBackBuffer(); // though not really needed
        textScreen.addTextButton(REPLAY_BUTTON_X, REPLAY_BUTTON_Y,
                BUTTON_REPLAY, "Replay", "Replay", LemmingsFontColor.BLUE,
                LemmingsFontColor.BROWN);

        if (GameController.getCurLevelPackIdx() != 0) {
            // started via "load
            // level"
            textScreen.addTextButton(RESCUED_Y, SAVE_REPLAY_BUTTON_Y,
                    BUTTON_SAVEREPLAY, "Save Replay", "Save Replay",
                    LemmingsFontColor.BLUE, LemmingsFontColor.BROWN);
        }

        textScreen.addTextButton(MENU_BUTTON_X, MENU_BUTTON_Y, BUTTON_MENU,
                "Menu", "Menu", LemmingsFontColor.BLUE,
                LemmingsFontColor.BROWN);
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
            rotDelta = DEFAULT_ROT_DELTA;
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
                rotFact = ONE_TENTH;
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
                    IMAGE_BASE_Y
                            - (int) (imgSrc.getHeight() / 2 * Math.abs(rotFact)
                                    + ROUND_UP));
        } else {
            // display original image
            flipCtr = 0;
            textScreen.drawImage(imgSrc, IMAGE_BASE_Y - imgSrc.getHeight() / 2);
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
        LemmFont.strImage(scrollerGfx, out, LemmingsFontColor.BLUE);
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
