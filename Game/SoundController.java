package game;

import gameutil.Sound;

/**
 * Class controlling sound and music operations. Extracted from GameController
 * by John Watne 06/2023.
 */
public final class SoundController {
    /** bang sound. */
    public static final int SND_BANG = 0;
    /** brick wheel trap sound. */
    public static final int SND_CHAIN = 1;
    /** setting new skill sound. */
    public static final int SND_CHANGE_OP = 2;
    /** only some builder steps left sound. */
    public static final int SND_CHINK = 3;
    /** Dying sound. */
    public static final int SND_DIE = 4;
    /** trap door opening sound. */
    public static final int SND_DOOR = 5;
    /** electric sound. */
    public static final int SND_ELECTRIC = 6;
    /** explode sound. */
    public static final int SND_EXPLODE = 7;
    /** fire sound. */
    public static final int SND_FIRE = 8;
    /** drowning sound. */
    public static final int SND_GLUG = 9;
    /** start of level sound. */
    public static final int SND_LETSGO = 10;
    /** bear/twiner trap sound. */
    public static final int SND_MANTRAP = 11;
    /** mouse clicked sound. */
    public static final int SND_MOUSEPRE = 12;
    /** nuke command sound. */
    public static final int SND_OHNO = 13;
    /** leaving exit sound. */
    public static final int SND_OING = 14;
    /** scrape sound. */
    public static final int SND_SCRAPE = 15;
    /** slicer sound. */
    public static final int SND_SLICER = 16;
    /** splash sound. */
    public static final int SND_SPLASH = 17;
    /** faller splat sound. */
    public static final int SND_SPLAT = 18;
    /** ten tons sound, also pipe sucking lemmings in. */
    public static final int SND_TEN_TONS = 19;
    /** icycle, brick stamper sound. */
    public static final int SND_THUD = 20;
    /** thunk sound. */
    public static final int SND_THUNK = 21;
    /** ting sound. */
    public static final int SND_TING = 22;
    /** yipee sound. */
    public static final int SND_YIPEE = 23;
    /**
     * Number of sound samples to use.
     */
    private static final int NUM_SAMPLES = 24;
    /** sound object. */
    private static Sound sound;
    /** gain for sound 0..1.0. */
    private static double soundGain = 1.0;
    /** gain for music 0..1.0. */
    private static double musicGain = 1.0;
    /** flag: play music. */
    private static boolean musicOn;
    /** flag: play sounds. */
    private static boolean soundOn;

    /**
     * Enable music.
     *
     * @param on true: music on, false otherwise
     */
    public static void setMusicOn(final boolean on) {
        musicOn = on;
    }

    /**
     * Get music enable state.
     *
     * @return true: music is on, false otherwise
     */
    public static boolean isMusicOn() {
        return musicOn;
    }

    /**
     * Enable sound.
     *
     * @param on true: sound on, false otherwise
     */
    public static void setSoundOn(final boolean on) {
        soundOn = on;
    }

    /**
     * Get sound enable state.
     *
     * @return true: sound is on, false otherwise
     */
    public static boolean isSoundOn() {
        return soundOn;
    }

    /**
     * Returns gain for music 0..1.0.
     *
     * @return gain for music 0..1.0.
     */
    public static double getMusicGain() {
        return musicGain;
    }

    /**
     * Sets gain for music 0..1.0.
     *
     * @param gain gain for music 0..1.0.
     */
    public static void setMusicGain(final double gain) {
        SoundController.musicGain = gain;
    }

    /**
     * Private default constructor for utility class.
     */
    private SoundController() {

    }

    /**
     * Returns sound object.
     *
     * @return sound object.
     */
    public static Sound getSound() {
        return sound;
    }

    /**
     * Sets sound object.
     *
     * @param soundObject sound object.
     */
    public static void setSound(final Sound soundObject) {
        SoundController.sound = soundObject;
    }

    /**
     * Initialize sound and music.
     *
     * @throws ResourceException for an error encountered during resource
     *                           extraction.
     */
    public static void initSound() throws ResourceException {
        sound = new Sound(NUM_SAMPLES, SND_MOUSEPRE);
        sound.setGain(soundGain);

    }

    /**
     * Plays trap door opening sound.
     */
    public static void playTrapDoorOpenSound() {
        sound.play(SND_DOOR);
    }

    /**
     * Plays setting new skill sound.
     */
    public static void playSettingNewSKillSound() {
        sound.play(SND_CHANGE_OP);
    }

    /**
     * Plays the pitched sample.
     *
     * @param pitch pitch value 0..99.
     */
    public static void playPitched(final int pitch) {
        sound.playPitched(pitch);
    }

    /**
     * Plays ting sound.
     */
    public static void playTingSound() {
        sound.play(SND_TING);
    }

    /**
     * Plays mouse clicked sound.
     */
    public static void playMouseClickedSound() {
        sound.play(SND_MOUSEPRE);
    }

    /**
     * Plays nuke command sound..
     */
    public static void playNukeSound() {
        sound.play(SND_OHNO);
    }

    /**
     * Plays start of level sound.
     */
    public static void playStartOfLevelSound() {
        sound.play(SND_LETSGO);
    }

    /**
     * Set sound gain.
     *
     * @param g gain (0..1.0)
     */
    public static void setSoundGain(final double g) {
        soundGain = g;

        if (sound != null) {
            sound.setGain(soundGain);
        }
    }

    /**
     * Plays only some builder steps left sound.
     */
    public static void playLastFewStepsSound() {
        sound.play(SND_CHINK);
    }

    /**
     * Play background music if music is selected to be on.
     */
    public static void playMusicIfMusicOn() {
        if (musicOn) {
            Music.play();
        }
    }

}
