package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import lemmini.Constants;
import micromod.Micromod;

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
 * Class to play MOD music.
 *
 * @author Volker Oth
 */
public class ModMusic implements Runnable {
    /**
     * Local copy of {@link Constants#SHIFT_8} used to avoid line-too-long
     * Checkstyle errors.
     */
    private static final int SHIFT8 = Constants.SHIFT_8;

    /**
     * Thread sleep time = 40 ms.
     */
    private static final int SLEEP_40MS = 40;

    /**
     * 3 added to base index within array.
     */
    private static final int OFFSET_3 = 3;

    /** sample frequency. */
    private static final int SAMPLE_RATE = 44100;

    /** object to play MODs. */
    private Micromod micromod;
    /** flag: loop the song. */
    private boolean songloop;
    /** flag: currently playing. */
    private boolean play;
    /** thread for playing. */
    private Thread mmThread;
    /** data line used to play samples. */
    private SourceDataLine line;

    /**
     * Load MOD file, initialize player.
     *
     * @param fn file name
     * @throws ResourceException
     */
    public void load(final String fn) throws ResourceException {
        if (mmThread != null) {
            close();
        }

        final String fName = Core.findResource(fn);
        final int datalen = (int) (new File(fName).length());

        if (datalen < 0) {
            throw new ResourceException(fName);
        }

        try (FileInputStream f = new FileInputStream(fName)) {
            final byte[] songdata = new byte[datalen];

            if (f.read(songdata) < 1) {
                System.out.println("No bytes read from file " + fName);
            }

            micromod = new Micromod(songdata, SAMPLE_RATE);
            setloop(true);
        } catch (final FileNotFoundException ex) {
            throw new ResourceException(fName);
        } catch (final IOException ex) {
            throw new ResourceException(fName + " (IO exception)");
        }

        mmThread = new Thread(this);
        mmThread.start();
    }

    /**
     * Set whether the song is to loop continuously or not. The default is to
     * loop.
     *
     * @param loop true: loop, false: playe only once
     */
    public void setloop(final boolean loop) {
        songloop = loop;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     *
     * Begin playback. This method will return once the song has finished, or
     * stop has been called.
     */
    @Override
    public final void run() {
        final int buflen = 2048;
        final int[] lbuf = new int[buflen];
        final int[] rbuf = new int[buflen];
        final byte[] obuf = new byte[buflen << 2];

        try {
            final AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 2, true,
                    false);
            final DataLine.Info lineInfo = new DataLine.Info(
                    SourceDataLine.class, af);
            line = (SourceDataLine) SoundController.getSound()
                    .getLine(lineInfo);
            line.open();
            line.start();
            setGain(Music.getGain());
            final int songlen = micromod.getlen();
            int remain = songlen;

            while (remain > 0 && Thread.currentThread() == mmThread) {
                if (play) {
                    int count = buflen;

                    if (count > remain) {
                        count = remain;
                    }

                    micromod.mix(lbuf, rbuf, 0, count);

                    for (int i = 0; i < count; i++) {
                        final int ox = i << 2;
                        obuf[ox] = (byte) (lbuf[i] & Constants.EIGHT_BIT_MASK);
                        obuf[ox + 1] = (byte) (lbuf[i] >> SHIFT8);
                        obuf[ox + 2] = (byte) (rbuf[i]
                                & Constants.EIGHT_BIT_MASK);
                        obuf[ox + OFFSET_3] = (byte) (rbuf[i] >> SHIFT8);
                        rbuf[i] = 0;
                        lbuf[i] = 0;
                    }

                    line.write(obuf, 0, count << 2);
                    remain -= count;

                    if (remain == 0 && songloop) {
                        remain = songlen;
                    }

                    Thread.yield();
                } else {
                    try {
                        line.flush();
                        Thread.sleep(SLEEP_40MS);
                    } catch (final InterruptedException ex) {
                    }
                }
            }

            line.flush();
            line.close();
        } catch (final LineUnavailableException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Instruct the run() method to finish playing and return.
     */
    public void stop() {
        if (mmThread != null) {
            mmThread.interrupt();
        }
        play = false;
    }

    /**
     * Instruct the run() method to resume playing.
     */
    public void play() {
        if (mmThread != null) {
            mmThread.interrupt();
        }
        play = true;
    }

    /**
     * Kills the thread.
     */
    public void close() {
        final Thread moribund = mmThread;
        mmThread = null;
        try {
            moribund.interrupt();
            moribund.join();
        } catch (final InterruptedException ex) {
        }
    }

    /**
     * Set gain (volume) of MOD output.
     *
     * @param gn gain factor: 0.0 (off) .. 1.0 (full volume)
     */
    public void setGain(final double gn) {
        double gain;

        if (gn > 1.0) {
            gain = 1.0;
        } else if (gn < 0) {
            gain = 0;
        } else {
            gain = gn;
        }

        if (line != null) {
            SoundController.getSound().setLineGain(line, gain);
        }
    }

}
