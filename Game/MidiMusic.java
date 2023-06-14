package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

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
 * Class to play Midi music.
 *
 * @author Volker Oth
 */
public class MidiMusic {

    /**
     * Maximum value assignable to a Controller = 127.0.
     */
    private static final double MAX_CONTROLLER_VALUE = 127.0;
    /**
     * MIDI Controller 7.
     */
    private static final int CONTROLLER_7 = 7;
    /**
     * Metamessage type to start MIDI music(?).
     */
    private static final int START_MUSIC = 47;
    /** Midi sequencer. */
    private Sequencer sequencer;
    /** flag: initialization is finished and midi file can be played. */
    private boolean canPlay;

    /**
     * Constructor.
     *
     * @param fName file name
     * @throws ResourceException
     * @throws LemmException
     */
    public MidiMusic(final String fName)
            throws ResourceException, LemmException {
        try (FileInputStream f = new FileInputStream(
                Core.findResource(fName))) {
            canPlay = false;
            sequencer = MidiSystem.getSequencer();

            if (sequencer == null) {
                throw new LemmException("Midi not supported");
            } else {
                // Acquire resources and make operational.
                sequencer.open();
                final Sequence mySeq = MidiSystem.getSequence(f);
                setGain(Music.getGain());
                sequencer.setSequence(mySeq);
                canPlay = true;
                sequencer.addMetaEventListener(new MetaEventListener() {
                    @Override
                    public void meta(final MetaMessage event) {
                        final int type = event.getType();
                        // System.out.println("midi message: "+type+"
                        // "+event.toString());

                        if (type == START_MUSIC) {
                            sequencer.setTickPosition(0);
                            sequencer.start();
                        }
                    }
                });
            }
        } catch (final MidiUnavailableException ex) {
            throw new LemmException("Midi not supported");
        } catch (final InvalidMidiDataException ex) {
            throw new ResourceException(fName + " (Invalid midi data)");
        } catch (final FileNotFoundException ex) {
            throw new ResourceException(fName);
        } catch (final IOException ex) {
            throw new ResourceException(fName + " (IO exception)");
        }
    }

    /**
     * Play current midi file.
     */
    public void play() {
        if (canPlay) {
            sequencer.start();
        }
    }

    /**
     * Stop current midi file.
     */
    public void stop() {
        if (canPlay) {
            sequencer.stop();
        }
    }

    /**
     * Close current midi file.
     */
    public void close() {
        stop();

        if (sequencer != null) {
            sequencer.close();
        }
    }

    /**
     * Set gain (volume) of midi output.
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

        if (sequencer != null && sequencer instanceof Synthesizer) {
            final Synthesizer synthesizer = (Synthesizer) sequencer;
            final MidiChannel[] channels = synthesizer.getChannels();

            for (int i = 0; i < channels.length; i++) {
                channels[i].controlChange(CONTROLLER_7,
                        (int) (gain * MAX_CONTROLLER_VALUE));
            }
        }
    }
}
