package gameutil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;

import game.Core;
import game.ResourceException;
import game.SoundController;
import lemmini.Constants;

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
 * Used to play a number of sounds. Supports upsampling and one pitched sample.
 *
 * @author Volker Oth
 */
public class Sound {
    /**
     * Minimum gain value.
     */
    private static final double MINIMUM_GAIN = 0.001;
    /**
     * Decimal value 10.0.
     */
    private static final double TEN_POINT_0 = 10.0;
    /**
     * Multiplier used to derive pitched value.
     */
    private static final double PITCH_MULTIPLIER = 0.0204;
    /**
     * Numeric value 8.
     */
    private static final int EIGHT = 8;
    /**
     * Hexidecimal value 0x80.
     */
    private static final int HEX_80 = 0x80;
    /**
     * Value of 0.1.
     */
    private static final double ONE_TENTH = 0.1;
    /**
     * Sample rate.
     */
    private static final int SAMPLE_RATE = 44100;
    /**
     * Default sample size in bits.
     */
    private static final int DEFAULT_SAMPLE_SIZE = 16;
    /** default sampling frequency. */
    private static final float DEFAULT_FREQUENCY = 22050;
    /** number of pitch levels. */
    private static final int NUMBER_PITCHED = 100;
    /** fade in the first n samples when calculating the pitched buffers. */
    private static final int PITCH_FADE_IN = 20;
    /** maximum number of sounds played in parallel. */
    private static final int MAX_SIMUL_SOUNDS = 6;

    /** line listener to be called after sample was played. */
    private final LineListener defaultListener;
    /** sound buffers to store the samples. */
    private final byte[][] soundBuffer;
    /** pitch buffers to store all pitched samples. */
    private byte[][] pitchBuffers;
    /** audio formats for normal samples (one for each sample). */
    private final AudioFormat[] format;
    /** audio format for pitched samples. */
    private AudioFormat pitchFormat;
    /** audio format for upsampling. */
    private final AudioFormat defaultFormat;
    /** line info for each sample. */
    private final DataLine.Info[] info;
    /** line info for the pitched sample. */
    private DataLine.Info pitchInfo;
    /** line info for upsampling. */
    private final DataLine.Info defaultInfo;
    /** gain/volume: 1.0 = 100%. */
    private double gain;
    /** number of sounds currently played. */
    private static int simulSounds;
    /** selected mixer index. */
    private static int mixerIdx;

    /**
     * Returns selected mixer index.
     *
     * @return selected mixer index.
     */
    public static int getMixerIdx() {
        return mixerIdx;
    }

    /**
     * Sets selected mixer index.
     *
     * @param mixerIndex selected mixer index.
     */
    public static void setMixerIdx(final int mixerIndex) {
        Sound.mixerIdx = mixerIndex;
    }

    /** array of available mixers. */
    private static Mixer[] mixers;

    /**
     * Sets array of available mixers.
     *
     * @param availableMixers array of available mixers.
     */
    public static void setMixers(final Mixer[] availableMixers) {
        Sound.mixers = availableMixers;
    }

    /** number of samples to be used. */
    private static int sampleNum;

    /**
     * Returns number of samples to be used.
     *
     * @return number of samples to be used.
     */
    public static int getSampleNum() {
        return sampleNum;
    }

    /**
     * Sets number of samples to be used.
     *
     * @param numberOfSamples number of samples to be used.
     */
    public static void setSampleNum(final int numberOfSamples) {
        Sound.sampleNum = numberOfSamples;
    }

    /**
     * Monitor object.
     */
    private static final Object MONITOR_OBJECT = new Object();

    /**
     * Constructor.
     *
     * @param snum    number of samples to use
     * @param pitchID ID of the pitched sample (-1 for none)
     * @throws ResourceException
     */
    public Sound(final int snum, final int pitchID) throws ResourceException {
        String fName = "";
        sampleNum = snum;
        soundBuffer = new byte[sampleNum][];
        format = new AudioFormat[sampleNum];
        info = new DataLine.Info[sampleNum];
        gain = 1.0;
        setSimulSounds(0);
        defaultListener = new DefaultListener();
        // upsampling to default frequency (more compatible for weird sample
        // frequencies)
        defaultFormat = new AudioFormat(DEFAULT_FREQUENCY, DEFAULT_SAMPLE_SIZE,
                1, true, false);
        defaultInfo = new DataLine.Info(Clip.class, defaultFormat);
        int maxLen = 0;

        try {
            for (int i = 0; i < sampleNum; i++) {
                final byte[] soundBuffer8;
                fName = "sound/sound_" + Integer.toString(i) + ".wav";
                final File fs = new File(Core.findResource(fName));

                try (AudioInputStream f = AudioSystem
                        .getAudioInputStream(fs.toURI().toURL())) {
                    format[i] = f.getFormat();
                    info[i] = new DataLine.Info(Clip.class, format[i]);
                    soundBuffer8 = new byte[(int) f.getFrameLength()
                            * format[i].getFrameSize()];

                    if (f.read(soundBuffer8) < 1) {
                        System.out.println("0 bytes read from file " + fName);
                    }
                }

                // convert samples with frequencies < 8kHz no work around bug in
                // JDK6
                // convert to 16bit due to bug in MacOS JRE
                if (format[i].getFrameSize() > 2) {
                    throw new ResourceException(
                            "Unsupported sample format for sample " + fName);
                }

                if ((format[i].getFrameSize() == 1) && (format[i]
                        .getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED)) {
                    throw new ResourceException(
                            "Unsupported sample format for sample " + fName);
                }

                if ((format[i].getFrameSize() == 2) && (format[i]
                        .getEncoding() != AudioFormat.Encoding.PCM_SIGNED)) {
                    throw new ResourceException(
                            "Unsupported sample format for sample " + fName);
                }

                soundBuffer[i] = convertToDefault(soundBuffer8, format[i]);
                format[i] = defaultFormat;
                info[i] = defaultInfo;

                if (soundBuffer[i].length > maxLen) {
                    maxLen = soundBuffer[i].length;
                }
            }
        } catch (final Exception ex) {
            throw new ResourceException(fName);
        }

        if (pitchID >= 0) {
            // create buffers for pitching
            // note that bit size (8) and channels (1) have to be the same for
            // all pitched
            // buffers
            pitchFormat = new AudioFormat(SAMPLE_RATE, DEFAULT_SAMPLE_SIZE, 1,
                    true, false);
            pitchInfo = new DataLine.Info(Clip.class, pitchFormat);
            pitchBuffers = new byte[NUMBER_PITCHED][];

            for (int i = 0; i < NUMBER_PITCHED; i++) {
                pitchBuffers[i] = createPitched(pitchID, i);
            }
        }

        // get all available mixers
        final Mixer.Info[] mixInfo = AudioSystem.getMixerInfo();
        final List<Mixer> mix = new ArrayList<Mixer>();

        for (int i = 0; i < mixInfo.length; i++) {
            final Mixer mixer = AudioSystem.getMixer(mixInfo[i]);
            final Line.Info clipInfo = new Line.Info(Clip.class);
            final int num = mixer.getMaxLines(clipInfo);

            if (num != 0) {
                mix.add(mixer);
            }
        }

        mixers = new Mixer[mix.size()];
        mixers = mix.toArray(mixers);
    }

    /**
     * Returns number of sounds simultaneously played.
     *
     * @return number of sounds simultaneously played.
     */
    public static int getSimulSounds() {
        synchronized (MONITOR_OBJECT) {
            return simulSounds;
        }
    }

    /**
     * Sets number of sounds simultaneously played.
     *
     * @param numberPlayed number of sounds simultaneously played.
     * @return number of sounds simultaneously played.
     */
    public static int setSimulSounds(final int numberPlayed) {
        synchronized (MONITOR_OBJECT) {
            Sound.simulSounds = numberPlayed;
            return numberPlayed;
        }
    }

    /**
     * Get an array of available mixer names.
     *
     * @return array of available mixer names
     */
    public String[] getMixers() {
        if (mixers == null) {
            return null;
        }
        final String[] s = new String[mixers.length];
        for (int i = 0; i < mixers.length; i++) {
            s[i] = mixers[i].getMixerInfo().getName();
        }
        return s;
    }

    /**
     * Set mixer to be used for sound output.
     *
     * @param idx index of mixer
     */
    public void setMixer(final int idx) {
        if (idx > mixers.length) {
            mixerIdx = 0;
        } else {
            mixerIdx = idx;
        }
    }

    /**
     * Return a data line to play a sample.
     *
     * @param lineInfo line info with requirements
     * @return data line to play a sample
     */
    public Line getLine(final DataLine.Info lineInfo) {
        try {
            return mixers[mixerIdx].getLine(lineInfo);
        } catch (final Exception ex) {
            return null;
        }
    }

    /**
     * Play a given sound.
     *
     * @param idx index of the sound to be played
     */
    public synchronized void play(final int idx) {
        if (!SoundController.isSoundOn()
                || getSimulSounds() >= MAX_SIMUL_SOUNDS /* || clips==null */) {
            return;
        }

        try {
            final Clip c = (Clip) mixers[mixerIdx].getLine(info[idx]);
            // Add a listener for line events
            c.addLineListener(defaultListener);
            c.open(format[idx], soundBuffer[idx], 0, soundBuffer[idx].length);
            setLineGain(c, gain);
            c.start();
            setSimulSounds(getSimulSounds() + 1);
        } catch (final Exception ex) {
            System.out.println(
                    "Error playing sound " + idx + ": " + ex.getMessage());
        }
    }

    /**
     * Convert sampling rate to default sampling rate.
     *
     * @param buffer       byte array containing source sample
     * @param sampleFormat AudioFormat of source sample (only unsigned 8bit PCM
     *                     or signed 16bit PCM supported)
     * @return sample converted to default format (16bit signed PCM 22050Hz)
     *         stored in byte array
     */
    public synchronized byte[] convertToDefault(final byte[] buffer,
            final AudioFormat sampleFormat) {
        // check unsupported formats

        // check if the default format is already OK
        if ((sampleFormat.getFrameRate() == DEFAULT_FREQUENCY)
                && (sampleFormat.getFrameSize() == 2)
                && !sampleFormat.isBigEndian() && (sampleFormat
                        .getEncoding() == AudioFormat.Encoding.PCM_SIGNED)) {
            return buffer;
        }

        final boolean from8bit = (sampleFormat.getFrameSize() == 1);
        final boolean convertEndian = ((sampleFormat.getFrameSize() == 2)
                && sampleFormat.isBigEndian());

        // sample up low frequency files to a DEFAULT_FREQUENCY to work around
        // sound bug
        // in JDK6
        final double scale = DEFAULT_FREQUENCY / sampleFormat.getSampleRate();
        final int sampleNumSrc = buffer.length / (from8bit ? 1 : 2);
        final int sampleNumTrg = (int) (buffer.length * scale)
                / (from8bit ? 1 : 2); // length of target buffer in
                                      // samples
        final byte[] buf = new byte[sampleNumTrg * (from8bit ? 2 : 1)];

        // create scaled buffer
        for (int i = 0; i < sampleNumTrg; i++) {
            int pos = (int) (i / scale);
            final double ofs = i / scale - pos;

            if (pos >= sampleNumSrc) {
                pos = sampleNumSrc - 1;
            }

            int val;
            int val2;

            if (from8bit) {
                if (ofs < ONE_TENTH || pos == sampleNumSrc - 1) {
                    val = buffer[pos] & Constants.EIGHT_BIT_MASK;
                } else {
                    // interpolate between sample points
                    val = (int) ((buffer[pos] & Constants.EIGHT_BIT_MASK)
                            * (1.0 - ofs)
                            + (buffer[pos + 1] & Constants.EIGHT_BIT_MASK)
                                    * ofs);
                }
                // byte order is little endian
                val = (val - HEX_80) << EIGHT;
            } else {
                if (convertEndian) {
                    val = (buffer[2 * pos + 1] & Constants.EIGHT_BIT_MASK)
                            | (buffer[2 * pos] << EIGHT);
                } else {
                    val = (buffer[2 * pos] & Constants.EIGHT_BIT_MASK)
                            | (buffer[2 * pos + 1] << EIGHT);
                }

                if (ofs >= ONE_TENTH && pos < sampleNumSrc - 1) {
                    // interpolate between sample points
                    if (convertEndian) {
                        val2 = (buffer[2 * pos + Constants.THREE]
                                & Constants.EIGHT_BIT_MASK)
                                | (buffer[2 * pos + 2] << EIGHT);
                    } else {
                        val2 = (buffer[2 * pos + 2] & Constants.EIGHT_BIT_MASK)
                                | (buffer[2 * pos + Constants.THREE] << EIGHT);
                    }

                    val = (int) (val * (1.0 - ofs) + val2 * ofs);
                }
            }

            buf[i * 2] = (byte) val;
            buf[i * 2 + 1] = (byte) (val >> EIGHT);
        }

        return buf;
    }

    /**
     * Create a pitched version of a sample.
     *
     * @param idx   index of the sample to be pitched
     * @param pitch pitch value as percent (0..100)
     * @return pitched sample as array of byte
     */
    public synchronized byte[] createPitched(final int idx, final int pitch) {
        // the idea is to sample up to 44KHz
        // then increase the sample rate virtually by creating a buffer which
        // contains
        // only
        // every Nth sample
        if (format[idx].getFrameSize() != 2) {
            return null;
        }

        final double scale = pitchFormat.getSampleRate()
                / format[idx].getSampleRate();
        double dpitch = (1.0 + ((pitch - 1) * PITCH_MULTIPLIER));

        if (dpitch < 1.0) {
            dpitch = 1.0;
        }

        final double fact = dpitch / scale;
        final int len = (int) (soundBuffer[idx].length / (2 * fact)); // length
                                                                      // of
                                                                      // target
                                                                      // buffer
                                                                      // in
                                                                      // samples
        final byte[] buf = new byte[len * 2];
        // create scaled buffer

        for (int i = 0; i < len; i++) {
            int pos = (int) (i * fact + Constants.HALF) * 2;

            if (pos >= soundBuffer[idx].length - 1) {
                pos = soundBuffer[idx].length - 2;
            }

            double val = (soundBuffer[idx][pos] & Constants.EIGHT_BIT_MASK)
                    | (soundBuffer[idx][pos + 1] << EIGHT);

            // fade in
            if (i < PITCH_FADE_IN) {
                val *= 1.0 - (PITCH_FADE_IN - 1 - i) / TEN_POINT_0;
            }

            // byte order is little endian
            final int ival = (int) val;
            buf[i * 2] = (byte) ival;
            buf[i * 2 + 1] = (byte) (ival >> EIGHT);
        }

        return buf;
    }

    /**
     * Play the pitched sample.
     *
     * @param pitch pitch value 0..99
     */
    public synchronized void playPitched(final int pitch) {
        if (!SoundController.isSoundOn()
                || getSimulSounds() >= MAX_SIMUL_SOUNDS) {
            return;
        }

        try {
            final Clip c = (Clip) mixers[mixerIdx].getLine(pitchInfo);
            // Add a listener for line events
            c.addLineListener(defaultListener);
            c.open(pitchFormat, pitchBuffers[pitch], 0,
                    pitchBuffers[pitch].length);
            setLineGain(c, gain);
            c.start();
            setSimulSounds(getSimulSounds() + 1);
        } catch (final Exception ex) {
            System.out.println(
                    "Error playing pitched sample: " + ex.getMessage());
        }
    }

    /**
     * Set gain of a line.
     *
     * @param line line
     * @param gn   gain (1.0 = 100%)
     */
    public void setLineGain(final Line line, final double gn) {
        if (line != null) {
            try {
                double g;
                final FloatControl control = (FloatControl) line
                        .getControl(FloatControl.Type.MASTER_GAIN);
                final double maxGain = Math.pow(10, control.getMaximum() / 20);

                if (gn == 0) {
                    g = MINIMUM_GAIN;
                } else {
                    g = gn;
                }

                final float fgain = 20 * (float) Math.log10(g * maxGain);
                control.setValue(fgain);
            } catch (final IllegalArgumentException ex) {
            }
        }
    }

    /**
     * Get gain.
     *
     * @return gain (1.0 == 100%)
     */
    public double getGain() {
        return gain;
    }

    /**
     * Set gain.
     *
     * @param gn gain (1.0 == 100%)
     */
    public void setGain(final double gn) {
        if (gn > 1.0) {
            gain = 1.0;
        } else if (gn < 0) {
            gain = 0;
        } else {
            gain = gn;
        }
        Core.getProgramProps().set("soundGain", gain);
    }

}

/**
 * Default Line Listener. Called after sample was played.
 *
 * @author Volker Oth
 */
class DefaultListener implements LineListener {
    @Override
    public synchronized void update(final LineEvent event) {
        if (event.getType().equals(LineEvent.Type.STOP)) {
            final Clip c = (Clip) event.getLine();

            if (c.isOpen()) {
                c.close();

                if (Sound.setSimulSounds(Sound.getSimulSounds() - 1) < 0) {
                    Sound.setSimulSounds(0);
                }
            }
        }
    }
}
