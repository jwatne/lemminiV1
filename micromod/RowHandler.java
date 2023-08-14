package micromod;
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

import lemmini.Constants;

/**
 * Utility class for processing a Micromod's row. Code moved from Micromod by
 * John Watne 08/2023.
 */
public class RowHandler {
    /**
     * Offset for eparam component in buffer.
     */
    private static final int EPARAM_OFFSET = 3;
    /**
     * Mask for 5th bit = 0x10.
     */
    private static final int BIT_5_MASK = 0x10;
    /** Fx set panning. */
    private static final int FX_SET_PANNING = 0x08;
    /** Fx set spos. */
    private static final int FX_SET_SPOS = 0x09;
    /** FX pat jump. */
    private static final int FX_PAT_JUMP = 0x0B;
    /** FX set volume. */
    private static final int FX_SET_VOLUME = 0x0C;
    /** FX pat break. */
    private static final int FX_PAT_BREAK = 0x0D;
    /** FX set speed. */
    private static final int FX_SET_SPEED = 0x0F;
    /**
     * Maximum number of rows.
     */
    private static final int NUM_ROWS = 64;
    /** Ch pat loop row. */
    private static final int CH_PAT_LOOP_ROW = 0x11;
    /** EX fine port up. */
    private static final int EX_FINE_PORT_UP = 0x10;
    /** EX fine port dn. */
    private static final int EX_FINE_PORT_DN = 0x20;
    /** EX set gliss. */
    @SuppressWarnings("unused")
    private static final int EX_SET_GLISS = 0x30;
    /** EX set vibr wav. */
    @SuppressWarnings("unused")
    private static final int EX_SET_VIBR_WAV = 0x40;
    /** EX set finetune. */
    private static final int EX_SET_FINETUNE = 0x50;
    /** EX pat loop. */
    private static final int EX_PAT_LOOP = 0x60;
    /** EX set trem wav. */
    @SuppressWarnings("unused")
    private static final int EX_SET_TREM_WAV = 0x70;
    /** EX set panning. */
    @SuppressWarnings("unused")
    private static final int EX_SET_PANNING = 0x80;
    /** EX fine vol up. */
    private static final int EX_FINE_VOL_UP = 0xA0;
    /** EX fine vol dn. */
    private static final int EX_FINE_VOL_DN = 0xB0;
    /** EX pat delay. */
    private static final int EX_PAT_DELAY = 0xE0;

    /**
     * The owner of this RowHandler.
     */
    private Micromod micromod;
    /** Pattern. */
    private int pat;
    /** N pattern. */
    private int npat;
    /** Row. */
    private int row;
    /** N row. */
    private int nrow;
    /** Loop count. */
    private int loopcount;
    /** Loop channel. */
    private int loopchan;

    /**
     * Constructs a RowHandler for the specified Micromod.
     *
     * @param owner The owner of this RowHandler.
     */
    public RowHandler(final Micromod owner) {
        this.micromod = owner;
    }

    /**
     * Process row.
     *
     * @return <code>true</code> if song is ended.
     */
    public boolean row() {
        // Decide whether to restart.
        boolean songend = isSongEnd();

        // Jump to next row
        pat = npat;
        row = nrow;
        // Decide next row.
        getNextRow();

        // Load channels and process fx
        int fcount = 0;
        micromod.setFcount(fcount);
        final byte[] mod = micromod.getMod();
        final int poffset = mod[Micromod.FIRST_PATTERN_INDEX + pat]
                & Micromod.ONE_HUNDRED_TWENTY_SEVEN_MASK;
        final int numchan = micromod.getNumchan();
        final int roffset = Micromod.MIN_SAMPLE_INDEX
                + (poffset * 64 * numchan * Micromod.FOUR_CHANNELS)
                + (row * numchan * 4);
        processChannelsAtRowOffset(roffset);
        micromod.mixupdate();
        fcount++;
        micromod.setFcount(fcount);

        if (npat >= micromod.getSonglen()) {
            npat = micromod.getRestart();
        }

        if (nrow >= NUM_ROWS) {
            nrow = 0;
        }

        return songend;
    }

    private void processChannelsAtRowOffset(final int roffset) {
        final byte[] mod = micromod.getMod();
        final int numchan = micromod.getNumchan();
        final int[] channels = micromod.getChannels();

        for (int chan = 0; chan < numchan; chan++) {
            final int coffset = chan * Micromod.CH_STRUCT_LEN;
            final int noffset = roffset + (chan * Micromod.FOUR_CHANNELS);
            channels[coffset + Micromod.CH_NOTE_PERIOD] = (mod[noffset + 1]
                    & Constants.EIGHT_BIT_MASK)
                    | ((mod[noffset]
                            & Constants.FOUR_BIT_MASK) << Constants.SHIFT_8);
            channels[coffset + Micromod.CH_NOTE_INSTRU] = ((mod[noffset + 2]
                    & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4)
                    | (mod[noffset] & BIT_5_MASK);
            channels[coffset + Micromod.CH_NOTE_EFFECT] = mod[noffset + 2]
                    & Constants.FOUR_BIT_MASK;
            channels[coffset
                    + Micromod.CH_NOTE_EPARAM] = mod[noffset + EPARAM_OFFSET]
                            & Constants.EIGHT_BIT_MASK;
            final int effect = channels[coffset + Micromod.CH_NOTE_EFFECT];
            final int eparam = channels[coffset + Micromod.CH_NOTE_EPARAM];

            if (!(effect == Micromod.FX_EXTENDED && ((eparam
                    & Constants.BITS_5_TO_8_MASK) == Micromod.EX_NOTE_DELAY))) {
                micromod.trigger(coffset);
            }

            channels[coffset + Micromod.CH_ARPEGGIO] = 0;
            channels[coffset + Micromod.CH_VIBR_PERIOD] = 0;
            channels[coffset + Micromod.CH_TREM_VOLUME] = 0;
            handleEffectForChannel(chan);
        }
    }

    private void handleEffectForChannel(final int chan) {
        final int[] channels = micromod.getChannels();
        final int coffset = chan * Micromod.CH_STRUCT_LEN;
        final int effect = channels[coffset + Micromod.CH_NOTE_EFFECT];
        final int eparam = channels[coffset + Micromod.CH_NOTE_EPARAM];

        switch (effect) {
        case Micromod.FX_TONE_PORTA:
            handleFXTonePortA(coffset, eparam);
            break;
        case Micromod.FX_VIBRATO:
            handleFXVibrato(coffset, eparam);
            break;
        case Micromod.FX_VIBRATO_VOL:
            micromod.vibrato(coffset);
            break;
        case Micromod.FX_TREMOLO:
            handleFXTremolo(coffset, eparam);
            break;
        case FX_SET_PANNING:
            handleFXSetpanning(coffset, eparam);
            break;
        case FX_SET_SPOS:
            channels[coffset + Micromod.CH_SPOS] = eparam << Micromod.FP_SHIFT
                    + Constants.SHIFT_8;
            break;
        case FX_PAT_JUMP:
            handleFXPatJump(eparam);
            break;
        case FX_SET_VOLUME:
            channels[coffset
                    + Micromod.CH_VOLUME] = (eparam > Micromod.MAX_VOLUME)
                            ? Micromod.MAX_VOLUME
                            : eparam;
            break;
        case FX_PAT_BREAK:
            handleFXPatBreak(eparam);
            break;
        case Micromod.FX_EXTENDED:
            handleFXExtended(chan, coffset, eparam);
            break;
        case FX_SET_SPEED:
            handleFXSetSpeed(eparam);
            break;
        default:
            break;
        }
    }

    private void handleFXSetSpeed(final int eparam) {
        if (eparam < Micromod.INT_BITS) {
            micromod.setTempo(eparam);
            micromod.getTickHandler().setTick(eparam);
        } else {
            micromod.setBpm(eparam);
        }
    }

    private void handleFXExtended(final int chan, final int coffset,
            final int eparam) {
        final int[] channels = micromod.getChannels();

        switch (eparam & Constants.BITS_5_TO_8_MASK) {
        case EX_FINE_PORT_UP:
            channels[coffset + Micromod.CH_PERIOD] -= (eparam
                    & Constants.FOUR_BIT_MASK);
            break;
        case EX_FINE_PORT_DN:
            channels[coffset + Micromod.CH_PERIOD] += (eparam
                    & Constants.FOUR_BIT_MASK);
            break;
        case EX_SET_FINETUNE:
            handleExSetFinetune(coffset, eparam);
            break;
        case EX_PAT_LOOP:
            handleExPatLoop(chan, coffset, eparam);
            break;
        case EX_FINE_VOL_UP:
            handleExFineVolUp(coffset, eparam);
            break;
        case EX_FINE_VOL_DN:
            handleExFineVolDn(coffset, eparam);
            break;
        case Micromod.EX_NOTE_CUT:
            handleExNoteCut(coffset, eparam);
            break;
        case Micromod.EX_NOTE_DELAY:
            handleExNoteDelay(coffset, eparam);
            break;
        case EX_PAT_DELAY:
            handleExPatDelay(eparam);
            break;
        default:
            break;
        }
    }

    private void handleExSetFinetune(final int coffset, final int eparam) {
        final int[] channels = micromod.getChannels();
        int ftval = eparam & Constants.FOUR_BIT_MASK;

        if (ftval > Micromod.MAX7) {
            ftval -= Micromod.REDUCTION16;
        }

        channels[coffset + Micromod.CH_FINETUNE] = ftval;
    }

    private void handleExPatLoop(final int chan, final int coffset,
            final int eparam) {
        final int[] channels = micromod.getChannels();
        final int plparam = eparam & Constants.FOUR_BIT_MASK;

        if (plparam == 0) {
            channels[coffset + CH_PAT_LOOP_ROW] = row;
        }

        if (plparam > 0 && channels[coffset + CH_PAT_LOOP_ROW] < row) {
            if (loopcount <= 0) {
                loopcount = plparam;
                loopchan = chan;
                nrow = channels[coffset + CH_PAT_LOOP_ROW];
                npat = pat;
            } else if (loopchan == chan) {
                if (loopcount == 1) {
                    channels[coffset + CH_PAT_LOOP_ROW] = row + 1;
                } else {
                    nrow = channels[coffset + CH_PAT_LOOP_ROW];
                    npat = pat;
                }
                loopcount--;
            }
        }
    }

    private void handleExFineVolUp(final int coffset, final int eparam) {
        final int[] channels = micromod.getChannels();
        int fvolup = channels[coffset + Micromod.CH_VOLUME]
                + (eparam & Constants.FOUR_BIT_MASK);

        if (fvolup > Micromod.MAX_VOLUME) {
            fvolup = Micromod.MAX_VOLUME;
        }

        channels[coffset + Micromod.CH_VOLUME] = fvolup;
    }

    private void handleExFineVolDn(final int coffset, final int eparam) {
        final int[] channels = micromod.getChannels();
        int fvoldn = channels[coffset + Micromod.CH_VOLUME]
                - (eparam & Constants.FOUR_BIT_MASK);

        if (fvoldn > Micromod.MAX_VOLUME) {
            fvoldn = 0;
        }

        channels[coffset + Micromod.CH_VOLUME] = fvoldn;
    }

    private void handleExNoteCut(final int coffset, final int eparam) {
        final int[] channels = micromod.getChannels();

        if ((eparam & Constants.FOUR_BIT_MASK) == micromod.getFcount()) {
            channels[coffset + Micromod.CH_VOLUME] = 0;
        }
    }

    private void handleExNoteDelay(final int coffset, final int eparam) {
        if ((eparam & Constants.FOUR_BIT_MASK) == micromod.getFcount()) {
            micromod.trigger(coffset);
        }
    }

    private void handleExPatDelay(final int eparam) {
        final int tempo = micromod.getTempo();
        micromod.getTickHandler()
                .setTick(tempo + tempo * (eparam & Constants.FOUR_BIT_MASK));
    }

    private void handleFXPatBreak(final int eparam) {
        if (loopcount <= 0) {
            npat = pat + 1;
            nrow = ((eparam & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4)
                    * Micromod.TEN + (eparam & Constants.FOUR_BIT_MASK);
        }
    }

    private void handleFXPatJump(final int eparam) {
        if (loopcount <= 0) {
            npat = eparam;
            nrow = 0;
        }
    }

    private void handleFXSetpanning(final int coffset, final int eparam) {
        if (!micromod.isAmiga()) {
            micromod.getChannels()[coffset + Micromod.CH_PANNING] = eparam;
        }
    }

    private void handleFXTremolo(final int coffset, final int eparam) {
        if (eparam != 0) {
            micromod.getChannels()[coffset + Micromod.CH_TREM_PARAM] = eparam;
        }

        micromod.tremolo(coffset);
    }

    private void handleFXVibrato(final int coffset, final int eparam) {
        if (eparam != 0) {
            micromod.getChannels()[coffset + Micromod.CH_VIBR_PARAM] = eparam;
        }

        micromod.vibrato(coffset);
    }

    private void handleFXTonePortA(final int coffset, final int eparam) {
        if (eparam != 0) {
            micromod.getChannels()[coffset + Micromod.CH_PORTA_PARAM] = eparam;
        }
    }

    private void getNextRow() {
        nrow = row + 1;

        if (nrow == NUM_ROWS) {
            npat = pat + 1;
            nrow = 0;
        }
    }

    private boolean isSongEnd() {
        boolean songend = false;

        if (npat < pat) {
            songend = true;
        }

        if (npat == pat && nrow <= row && loopcount <= 0) {
            songend = true;
        }
        return songend;
    }

    /**
     * Resets loop variables for the RowHandler.
     */
    public void reset() {
        npat = 0;
        pat = 0;
        nrow = 0;
        row = 0;
        loopchan = 0;
        loopcount = 0;
    }

}
