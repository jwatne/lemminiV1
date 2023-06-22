package game.level;

import game.GameController;
import game.SoundController;
import gameutil.KeyRepeat;
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
 * Lemmings release rate handler. Code moved from GameController by John Watne
 * 6/2023.
 */
public final class ReleaseRateHandler {
    /** maximum release rate. */
    private static final int MAX_RELEASE_RATE = 99;
    /**
     * The base lemming release rate.
     */
    private static final int BASE_RELEASE_RATE = 8;
    /** key repeat object for plus key/icon. */
    private static KeyRepeat plus;
    /** key repeat object for minus key/icon. */
    private static KeyRepeat minus;
    /** release rate 0..99. */
    private static int releaseRate;
    /** threshold to release a new Lemming. */
    private static int releaseBase;

    /**
     * Private default constructor for utility class.
     */
    private ReleaseRateHandler() {

    }

    /**
     * Returns threshold to release a new Lemming.
     *
     * @return threshold to release a new Lemming.
     */
    public static int getReleaseBase() {
        return releaseBase;
    }

    /**
     * Sets threshold to release a new Lemming.
     *
     * @param releaseThreshold threshold to release a new Lemming.
     */
    public static void setReleaseBase(final int releaseThreshold) {
        ReleaseRateHandler.releaseBase = releaseThreshold;
    }

    /**
     * Returns release rate 0..99.
     *
     * @return release rate 0..99.
     */
    public static int getReleaseRate() {
        return releaseRate;
    }

    /**
     * Sets release rate 0..99.
     *
     * @param rate release rate 0..99.
     */
    public static void setReleaseRate(final int rate) {
        releaseRate = rate;
    }

    /**
     * Returns key repeat object for plus key/icon.
     *
     * @return key repeat object for plus key/icon.
     */
    public static KeyRepeat getPlus() {
        return plus;
    }

    /**
     * Sets key repeat object for plus key/icon.
     *
     * @param plusKey key repeat object for plus key/icon.
     */
    public static void setPlus(final KeyRepeat plusKey) {
        plus = plusKey;
    }

    /**
     * Returns key repeat object for minus key/icon.
     *
     * @return key repeat object for minus key/icon.
     */
    public static KeyRepeat getMinus() {
        return minus;
    }

    /**
     * Sets key repeat object for minus key/icon.
     *
     * @param minusKey key repeat object for minus key/icon.
     */
    public static void setMinus(final KeyRepeat minusKey) {
        minus = minusKey;
    }

    /**
     * +/- icons: maximum time between two mouse clicks for double click
     * detection (in microseconds).
     */
    private static final long MICROSEC_RELEASE_DOUBLE_CLICK = 200 * 1000;
    /** +/- icons: time for key repeat to kick in. */
    private static final long MICROSEC_KEYREPEAT_START = 250 * 1000;
    /** +/- icons: time for key repeat rate. */
    private static final long MICROSEC_KEYREPEAT_REPEAT = 67 * 1000;

    /**
     * Initialization of plus and minus buttons.
     */
    public static void init() {
        plus = new KeyRepeat(MICROSEC_KEYREPEAT_START,
                MICROSEC_KEYREPEAT_REPEAT, MICROSEC_RELEASE_DOUBLE_CLICK);
        minus = new KeyRepeat(MICROSEC_KEYREPEAT_START,
                MICROSEC_KEYREPEAT_REPEAT, MICROSEC_RELEASE_DOUBLE_CLICK);

    }

    /**
     * Initialize plus and minus buttons for a level after it was loaded.
     */
    public static void initLevel() {
        plus.init();
        minus.init();
    }

    /**
     * Processes clicks of plus and minus buttons.
     */
    public static void checkPlusMinusButtons() {
        KeyRepeat.Event fired = plus.fired();

        if (fired != KeyRepeat.Event.NONE) {
            if (releaseRate < MAX_RELEASE_RATE) {
                if (fired == KeyRepeat.Event.DOUBLE_CLICK) {
                    releaseRate = MAX_RELEASE_RATE;
                } else {
                    releaseRate += 1;
                }

                calcReleaseBase();
                SoundController.playPitched(releaseRate);
            } else {
                SoundController.playTingSound();
            }
        }

        fired = minus.fired();

        if (fired != KeyRepeat.Event.NONE) {
            final Level level = GameController.getLevel();

            if (releaseRate > level.getReleaseRate()) {
                if (fired == KeyRepeat.Event.DOUBLE_CLICK) {
                    releaseRate = level.getReleaseRate();
                } else {
                    releaseRate -= 1;
                }

                calcReleaseBase();
                SoundController.playPitched(releaseRate);
            } else {
                SoundController.playTingSound();
            }
        }
    }

    /**
     * Calculate the counter threshold for releasing a new Lemmings.
     */
    public static void calcReleaseBase() {
        // the original formula is: release lemming every 4+(99-speed)/2 time
        // steps
        // where one step is 60ms (3s/50) or 66ms (4s/60).
        // Lemmini runs at 30ms/33ms, so the term has to be multiplied by 2
        // 8+(99-releaseRate) should be correct
        releaseBase = BASE_RELEASE_RATE + (MAX_RELEASE_RATE - releaseRate);
    }

    /**
     * Plus was pressed.
     *
     * @param d bitmask: key or icon
     */
    public static void pressPlus(final int d) {
        plus.pressed(d);
    }

    /**
     * Plus was released.
     *
     * @param d bitmask: key or icon
     */
    public static void releasePlus(final int d) {
        plus.released(d);
    }

    /**
     * Minus was pressed.
     *
     * @param d bitmask: key or icon
     */
    public static void pressMinus(final int d) {
        minus.pressed(d);
    }

    /**
     * Minus was released.
     *
     * @param d bitmask: key or icon
     */
    public static void releaseMinus(final int d) {
        minus.released(d);
    }

}
