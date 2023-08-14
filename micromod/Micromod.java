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
 * Micromod/e fast module player (rev b).
 */
public class Micromod {
    /**
     * 13-bit shift.
     */
    private static final int SHIFT_13_BITS = 13;
    /**
     * 14-bit shift.
     */
    private static final int SHIFT_14_BITS = 14;
    /**
     * Index offset in fttable.
     */
    private static final int FTTABLE_OFFSET = 8;
    /**
     * Clock value for all computers OTHER THAN Commodore Amiga.
     */
    private static final int STANDARD_CLOCK_VALUE = 3579364;
    /**
     * Clock value for Commodore Amiga.
     */
    private static final int AMIGA_CLOCK_VALUE = 3546894;
    /**
     * Minimum value for p for mix update.
     */
    private static final int MIN_P_FOR_MIX_UPDATE = 27;
    /**
     * Mask for 6th bit = 0x20.
     */
    private static final int BIT_SIX_MASK = 0x20;
    /**
     * 5-bit mask = 0x1F.
     */
    private static final int FIVE_BIT_MASK = 0x1F;
    /**
     * Max 2 bit value = 3 (base 10).
     */
    private static final int MAX_2_BIT_VALUE = 3;
    /**
     * 192 value for p.
     */
    private static final int P192 = 192;
    /**
     * 64 value for p.
     */
    private static final int P64 = 64;
    /**
     * 2-bit mask value = 0x3 = 0x03.
     */
    private static final int TWO_BIT_MASK = 0x3;
    /**
     * Default beats per minute.
     */
    private static final int DEFAULT_BPM = 125;
    /**
     * Default tick value.
     */
    private static final int DEFAULT_TICK = 6;
    /**
     * Default tempo value.
     */
    private static final int DEFAULT_TEMPO = 6;
    /**
     * Cutoff value to check llen against for adjustment.
     */
    private static final int LLEN_CUTOFF = 4;
    /**
     * Amount to add to index to get llen value in buffer.
     */
    private static final int LLEN_OFFSET = 48;
    /**
     * Amount to add to index to get lsta value in buffer.
     */
    private static final int LSTA_OFFSET = 46;
    /**
     * Maximum volume value.
     */
    public static final int MAX_VOLUME = 64;
    /**
     * Amount to add to index to get vol value in buffer.
     */
    private static final int VOL_OFFSET = 45;
    /**
     * Amount deducted for values > {@link #MAX7}.
     */
    public static final int REDUCTION16 = 16;
    /**
     * Max value = 7.
     */
    public static final int MAX7 = 7;
    /**
     * Amount to add for fine value.
     */
    private static final int FINE_OFFSET = 44;
    /**
     * Amount to add for slen value.
     */
    private static final int SLEN_OFFSET = 42;
    /**
     * Multiplier of inst indexed values from buffer.
     */
    private static final int INST_MULTIPLIER = 30;
    /**
     * Max inst value.
     */
    private static final int MAX_INST = 31;
    /**
     * Bits per pattern.
     */
    private static final int BITS_PER_PATTERN = 64;
    /**
     * Minimum value for sample index in buffer.
     */
    public static final int MIN_SAMPLE_INDEX = 1084;
    /**
     * Second index of number of channels in buffer.
     */
    private static final int NUM_CHANNEL_INDEX_2 = 1081;
    /**
     * Multiplier of values to be in tens place.
     */
    public static final int TEN = 10;
    /**
     * xxCH.
     */
    private static final int XX_CH = 0x4348;
    /**
     * Amount to subtrace from buffer value to get number of channels.
     */
    private static final int NUM_CHANNEL_REDUCTION = 48;
    /**
     * First index of number of channels in buffer.
     */
    private static final int NUM_CHANNEL_INDEX_1 = 1080;
    /**
     * xCHN.
     */
    private static final int X_CHN = 0x484e;
    /**
     * Number of channels = 4.
     */
    public static final int FOUR_CHANNELS = 4;
    /**
     * FLT4.
     */
    private static final int FLT4 = 0x5434;
    /**
     * M!K!
     */
    private static final int M_BANG_K_BANG = 0x4b21;
    /**
     * M.K.
     */
    private static final int M_DOT_K_DOT = 0x4b2e;
    /**
     * Second index of number of Amiga channels in buffer.
     */
    private static final int AMIGA_NUM_CHANNEL_INDEX_2 = 1083;
    /**
     * First index of number of Amiga channels in buffer.
     */
    private static final int AMIGA_NUM_CHANNEL_INDEX_1 = 1082;
    /**
     * Lowest index of patterns in buffer.
     */
    public static final int FIRST_PATTERN_INDEX = 952;
    /**
     * Maximum number of patterns.
     */
    private static final int MAX_NUM_PATTERNS = 128;
    /**
     * Index of reset in buffer.
     */
    private static final int RESTART_INDEX = 951;
    /**
     * Bit mask for value 127 = 0x7f.
     */
    public static final int ONE_HUNDRED_TWENTY_SEVEN_MASK = 0x7F;
    /**
     * Index of songlen in buffer.
     */
    private static final int SONGLEN_INDEX = 950;
    /**
     * Java 32 bits per int.
     */
    public static final int INT_BITS = 32;
    /** FP shift. */
    public static final int FP_SHIFT = 13;
    /** FP one. */
    private static final int FP_ONE = 1 << FP_SHIFT;
    /** In struct len. */
    private static final int IN_STRUCT_LEN = 0x05;
    /** In sample index. */
    private static final int IN_SAMPLE_INDEX = 0x00;
    /** In loop start. */
    private static final int IN_LOOP_START = 0x01;
    /** In loop end. */
    private static final int IN_LOOP_END = 0x02;
    /** In volume. */
    private static final int IN_VOLUME = 0x03;
    /** In finetune. */
    private static final int IN_FINETUNE = 0x04;
    /** Ch struct len. */
    public static final int CH_STRUCT_LEN = 0x16;
    /** Ch spos. */
    public static final int CH_SPOS = 0x00;
    /** Ch step. */
    private static final int CH_STEP = 0x01;
    /** Ch ampl. */
    private static final int CH_AMPL = 0x02;
    /** Ch instrument. */
    private static final int CH_INSTRUMENT = 0x03;
    /** Ch assigned. */
    private static final int CH_ASSIGNED = 0x04;
    /** Ch volume. */
    public static final int CH_VOLUME = 0x05;
    /** Ch finetune. */
    public static final int CH_FINETUNE = 0x06;
    /** Ch period. */
    public static final int CH_PERIOD = 0x07;
    /** Ch porta period. */
    public static final int CH_PORTA_PERIOD = 0x08;
    /** Ch porta param. */
    public static final int CH_PORTA_PARAM = 0x09;
    /** Ch panning. */
    public static final int CH_PANNING = 0x0A;
    /** Ch arpeggio. */
    public static final int CH_ARPEGGIO = 0x0B;
    /** Ch vibr period. */
    public static final int CH_VIBR_PERIOD = 0x0C;
    /** Ch vibr param. */
    public static final int CH_VIBR_PARAM = 0x0D;
    /** Ch vibr count. */
    public static final int CH_VIBR_COUNT = 0x0E;
    /** Ch trem volume. */
    public static final int CH_TREM_VOLUME = 0x0F;
    /** Ch trem param. */
    public static final int CH_TREM_PARAM = 0x10;
    /** Ch note period. */
    public static final int CH_NOTE_PERIOD = 0x12;
    /** Ch note instru. */
    public static final int CH_NOTE_INSTRU = 0x13;
    /** Ch note effect. */
    public static final int CH_NOTE_EFFECT = 0x14;
    /** Ch note eparam. */
    public static final int CH_NOTE_EPARAM = 0x15;
    /** Fx tone porta. */
    public static final int FX_TONE_PORTA = 0x03;
    /** Fx vibrato. */
    public static final int FX_VIBRATO = 0x04;
    /** Fx tporta vol. */
    public static final int FX_TPORTA_VOL = 0x05;
    /** Fx vibrato vol. */
    public static final int FX_VIBRATO_VOL = 0x06;
    /** Fx tremolo. */
    public static final int FX_TREMOLO = 0x07;
    /** FX extended. */
    public static final int FX_EXTENDED = 0x0E;

    /** EX note cut. */
    public static final int EX_NOTE_CUT = 0xC0;
    /** EX note delay. */
    public static final int EX_NOTE_DELAY = 0xD0;
    /** EX invert loop. */
    @SuppressWarnings("unused")
    private static final int EX_INVERT_LOOP = 0xF0;

    /** Arp table. */
    private final int[] arptable = new int[] {8192, 8679, 9195, 9742, 10321,
            10935, 11585, 12274, 13004, 13777, 14596, 15464, 16384, 17358,
            18390, 19484};

    /** FT table. */
    private final int[] fttable = new int[] {15464, 15576, 15689, 15803, 15918,
            16033, 16149, 16266, 16384, 16503, 16622, 16743, 16864, 16986,
            17109, 17233};

    /** Sin table. */
    private final int[] sintable = new int[] {0, 24, 49, 74, 97, 120, 141, 161,
            180, 197, 212, 224, 235, 244, 250, 253, 255, 253, 250, 244, 235,
            224, 212, 197, 180, 161, 141, 120, 97, 74, 49, 24};

    /** Mod. */
    private final byte[] mod;
    /** Amiga? */
    private boolean amiga;

    /** Number of channels. */
    private int numchan;
    /** Song length. */
    private final int songlen;

    /** Restart. */
    private int restart;
    /** Temp. */
    private int tempo;

    /** Beats per minute. */
    private int bpm;

    /** F count. */
    private int fcount;
    /** Instruments. */
    private final int[] instruments = new int[IN_STRUCT_LEN * INT_BITS];
    /** Channels. */
    private final int[] channels = new int[CH_STRUCT_LEN * INT_BITS];
    /** Sample rate. */
    private final int samplerate;
    /** Ticks remaining. */
    private int tickremain;
    /**
     * Handles ticks for the Micromod instance.
     */
    private TickHandler tickHandler;

    /**
     * Processes rows for the Micromod instance.
     */
    private RowHandler rowHandler;

    /**
     * Constructor ( mod - module data ).
     *
     * @param modDataBuffer byte buffer containing MOD data
     * @param sampleRateHz  sample rate in Hertz
     */
    public Micromod(final byte[] modDataBuffer, final int sampleRateHz) {
        this.tickHandler = new TickHandler(this);
        this.rowHandler = new RowHandler(this);
        this.mod = modDataBuffer;
        this.samplerate = sampleRateHz;
        // System.out.println( "micromod/e (c)2005 mumart@gmail.com" );
        songlen = modDataBuffer[SONGLEN_INDEX] & ONE_HUNDRED_TWENTY_SEVEN_MASK;
        restart = modDataBuffer[RESTART_INDEX] & ONE_HUNDRED_TWENTY_SEVEN_MASK;

        if (restart >= songlen) {
            restart = 0;
        }

        int numpatterns = 0;

        for (int n = 0; n < MAX_NUM_PATTERNS; n++) {
            final int pattern = modDataBuffer[FIRST_PATTERN_INDEX + n]
                    & ONE_HUNDRED_TWENTY_SEVEN_MASK;

            if (pattern >= numpatterns) {
                numpatterns = pattern + 1;
            }
        }

        switch ((modDataBuffer[AMIGA_NUM_CHANNEL_INDEX_1] << Constants.SHIFT_8)
                | modDataBuffer[AMIGA_NUM_CHANNEL_INDEX_2]
                        & Constants.EIGHT_BIT_MASK) {
        case M_DOT_K_DOT: // M.K.
        case M_BANG_K_BANG: // M!K!
        case FLT4: // FLT4
            numchan = FOUR_CHANNELS;
            amiga = true;
            break;
        case X_CHN: // xCHN
            numchan = modDataBuffer[NUM_CHANNEL_INDEX_1]
                    - NUM_CHANNEL_REDUCTION;
            amiga = false;
            break;
        case XX_CH: // xxCH
            numchan = ((modDataBuffer[NUM_CHANNEL_INDEX_1]
                    - NUM_CHANNEL_REDUCTION) * TEN)
                    + (modDataBuffer[NUM_CHANNEL_INDEX_2]
                            - NUM_CHANNEL_REDUCTION);
            amiga = false;
            break;
        default:
            throw new IllegalArgumentException("MOD Format not recognised!");
        }

        int sampleidx = MIN_SAMPLE_INDEX
                + FOUR_CHANNELS * numchan * BITS_PER_PATTERN * numpatterns;

        for (int inst = 0; inst < MAX_INST; inst++) {
            int slen = ushortbe(modDataBuffer,
                    inst * INST_MULTIPLIER + SLEN_OFFSET) << 1;
            int fine = modDataBuffer[inst * INST_MULTIPLIER + FINE_OFFSET]
                    & Constants.FOUR_BIT_MASK;

            if (fine > MAX7) {
                fine -= REDUCTION16;
            }

            int vol = modDataBuffer[inst * INST_MULTIPLIER + VOL_OFFSET]
                    & ONE_HUNDRED_TWENTY_SEVEN_MASK;

            if (vol > MAX_VOLUME) {
                vol = MAX_VOLUME;
            }

            int lsta = ushortbe(modDataBuffer,
                    inst * INST_MULTIPLIER + LSTA_OFFSET) << 1;
            int llen = ushortbe(modDataBuffer,
                    inst * INST_MULTIPLIER + LLEN_OFFSET) << 1;

            if (sampleidx + slen - 1 >= modDataBuffer.length) {
                System.out.println("Module is truncated!");
                slen = modDataBuffer.length - sampleidx;

                if (slen < 0) {
                    slen = 0;
                }
            }

            if (llen < LLEN_CUTOFF || lsta >= slen) {
                lsta = slen - 1;
                llen = 1;
            }

            int lend = lsta + llen - 1;

            if (lend >= slen) {
                lend = slen - 1;
            }

            final int ioffset = (inst + 1) * IN_STRUCT_LEN;
            instruments[ioffset + IN_SAMPLE_INDEX] = sampleidx;
            instruments[ioffset + IN_LOOP_START] = lsta;
            instruments[ioffset + IN_LOOP_END] = lend;
            instruments[ioffset + IN_FINETUNE] = fine;
            instruments[ioffset + IN_VOLUME] = vol;
            sampleidx += slen;
        }

        reset();
    }

    /**
     * Returns the row handler for the Micromod instance.
     *
     * @return the row handler for the Micromod instance.
     */
    public RowHandler getRowHandler() {
        return rowHandler;
    }

    /**
     * Indicates whether the computer running the program is an Amiga.
     *
     * @return <code>true</code> if the computer running the program is an
     *         Amiga.
     */
    public boolean isAmiga() {
        return amiga;
    }

    /**
     * Sets whether the computer running the program is an Amiga.
     *
     * @param isAmiga <code>true</code> if the computer running the program is
     *                an Amiga.
     */
    public void setAmiga(final boolean isAmiga) {
        this.amiga = isAmiga;
    }

    /**
     * Returns beats per minute.
     *
     * @return beats per minute.
     */
    public int getBpm() {
        return bpm;
    }

    /**
     * Sets beats per minute.
     *
     * @param beatsPerMinute beats per minute.
     */
    public void setBpm(final int beatsPerMinute) {
        this.bpm = beatsPerMinute;
    }

    /**
     * Sets tick handler for Micromod instance.
     *
     * @return tick handler for Micromod instance.
     */
    public TickHandler getTickHandler() {
        return tickHandler;
    }

    /**
     * Returns restart.
     *
     * @return restart.
     */
    public int getRestart() {
        return restart;
    }

    /**
     * Sets restart.
     *
     * @param restartValue restart.
     */
    public void setRestart(final int restartValue) {
        this.restart = restartValue;
    }

    /**
     * Returns song length.
     *
     * @return song length.
     */
    public int getSonglen() {
        return songlen;
    }

    /**
     * Returns mod.
     *
     * @return mod.
     */
    public byte[] getMod() {
        return mod;
    }

    /**
     * Returns F count.
     *
     * @return F count.
     */
    public int getFcount() {
        return fcount;
    }

    /**
     * Sets F count.
     *
     * @param fs F count.
     */
    public void setFcount(final int fs) {
        this.fcount = fs;
    }

    /**
     * Returns channels.
     *
     * @return channels.
     */
    public int[] getChannels() {
        return channels;
    }

    /**
     * Returns number of channels.
     *
     * @return number of channels.
     */
    public int getNumchan() {
        return numchan;
    }

    /**
     * Sets number of channels.
     *
     * @param numberOfChannels number of channels.
     */
    public void setNumchan(final int numberOfChannels) {
        this.numchan = numberOfChannels;
    }

    /**
     * Returns tempo value.
     *
     * @return tempo value.
     */
    public int getTempo() {
        return tempo;
    }

    /**
     * Sets tempo value.
     *
     * @param tempoValue tempo value.
     */
    public void setTempo(final int tempoValue) {
        this.tempo = tempoValue;
    }

    /**
     * Return the song length in samples.
     *
     * @return song length in samples
     */
    public int getlen() {
        reset();
        int len = getticklen();

        while (!tickHandler.tick()) {
            len += getticklen();
        }

        reset();
        return len;
    }

    /**
     * Mix 16 bit stereo audio into the buffers.
     *
     * @param l      left
     * @param r      right
     * @param ofs    offset
     * @param length length
     */
    public void mix(final int[] l, final int[] r, final int ofs,
            final int length) {
        int len = length;
        int offset = ofs;

        while (len > 0) {
            int count = tickremain;

            if (count > len) {
                count = len;
            }

            for (int chan = 0; chan < numchan; chan++) {
                final int coffset = chan * CH_STRUCT_LEN;
                final int ampl = channels[coffset + CH_AMPL];
                final int pann = channels[coffset + CH_PANNING] << FP_SHIFT
                        - Constants.SHIFT_8;
                final int lamp = ampl * (FP_ONE - pann) >> FP_SHIFT;
                final int ramp = ampl * pann >> FP_SHIFT;
                final int inst = channels[coffset + CH_INSTRUMENT];
                final int ioffset = inst * IN_STRUCT_LEN;
                final int sidx = instruments[ioffset + IN_SAMPLE_INDEX];
                final int lsta = instruments[ioffset
                        + IN_LOOP_START] << FP_SHIFT;
                final int lep1 = instruments[ioffset + IN_LOOP_END]
                        + 1 << FP_SHIFT;
                int spos = channels[coffset + CH_SPOS];
                final int step = channels[coffset + CH_STEP];
                final int llen = lep1 - lsta;
                final boolean dontmix = llen <= FP_ONE && spos >= lsta;

                if (!dontmix) {
                    for (int x = 0; x < count; x++) {
                        while (spos >= lep1) {
                            spos -= llen;
                        }

                        final int sample = mod[sidx
                                + (spos >> FP_SHIFT)] << Constants.SHIFT_8;
                        l[offset + x] += sample * lamp >> FP_SHIFT;
                        r[offset + x] += sample * ramp >> FP_SHIFT;
                        spos += step;
                    }
                }

                channels[coffset + CH_SPOS] = spos;
            }

            tickremain -= count;

            if (tickremain == 0) {
                tickHandler.tick();
                tickremain = getticklen();
            }

            offset += count;
            len -= count;
        }
    }

    private void reset() {
        rowHandler.reset();
        tempo = DEFAULT_TEMPO;
        tickHandler.setTick(DEFAULT_TICK);
        bpm = DEFAULT_BPM;

        for (int n = 0; n < channels.length; n++) {
            channels[n] = 0;
        }

        for (int chan = 0; chan < numchan; chan++) {
            int p = MAX_NUM_PATTERNS;

            switch (chan & TWO_BIT_MASK) {
            case 0:
                p = P64;
                break;
            case 1:
                p = P192;
                break;
            case 2:
                p = P192;
                break;
            case MAX_2_BIT_VALUE:
                p = P64;
                break;
            default:
                break;
            }

            channels[chan * CH_STRUCT_LEN + CH_PANNING] = p;
        }

        rowHandler.row();
        tickremain = getticklen();
    }

    /**
     * Process a trigger for the given column offset value.
     *
     * @param coffset the column offset.
     */
    public void trigger(final int coffset) {
        final int period = channels[coffset + CH_NOTE_PERIOD];
        final int instru = channels[coffset + CH_NOTE_INSTRU];
        final int effect = channels[coffset + CH_NOTE_EFFECT];

        if (instru != 0) {
            channels[coffset + CH_ASSIGNED] = instru;
            final int ioffset = instru * IN_STRUCT_LEN;
            channels[coffset + CH_VOLUME] = instruments[ioffset + IN_VOLUME];
            channels[coffset + CH_FINETUNE] = instruments[ioffset
                    + IN_FINETUNE];

            if (amiga) {
                final int atlsta = instruments[ioffset + IN_LOOP_START];
                final int atlend = instruments[ioffset + IN_LOOP_END];

                if (atlend > atlsta) {
                    channels[coffset + CH_INSTRUMENT] = instru;
                }
            }
        }

        if (period != 0) {
            channels[coffset + CH_INSTRUMENT] = channels[coffset + CH_ASSIGNED];
            channels[coffset + CH_PORTA_PERIOD] = period;

            if (effect != FX_TONE_PORTA && effect != FX_TPORTA_VOL) {
                channels[coffset + CH_PERIOD] = period;
                channels[coffset + CH_SPOS] = 0;
            }

            channels[coffset + CH_VIBR_COUNT] = 0;
        }
    }

    /**
     * Processes vibrato for the given channel offset value.
     *
     * @param coffset the channel offset.
     */
    public void vibrato(final int coffset) {
        final int vparam = channels[coffset + CH_VIBR_PARAM];
        final int vspeed = (vparam
                & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4;
        final int vdepth = vparam & Constants.FOUR_BIT_MASK;
        final int vibpos = vspeed * channels[coffset + CH_VIBR_COUNT];
        int tval = sintable[vibpos & FIVE_BIT_MASK];

        if ((vibpos & BIT_SIX_MASK) > 0) {
            tval = -tval;
        }

        channels[coffset + CH_VIBR_PERIOD] = tval * vdepth >> Constants.SHIFT_7;
    }

    /**
     * Processes tremolo for the given channel offset value.
     *
     * @param coffset the channel offset.
     */
    public void tremolo(final int coffset) {
        final int tparam = channels[coffset + CH_TREM_PARAM];
        final int tspeed = (tparam
                & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4;
        final int tdepth = tparam & Constants.FOUR_BIT_MASK;
        final int trempos = tspeed * channels[coffset + CH_VIBR_COUNT];
        int tval = sintable[trempos & FIVE_BIT_MASK];

        if ((trempos & BIT_SIX_MASK) > 0) {
            tval = -tval;
        }

        channels[coffset + CH_TREM_VOLUME] = tval * tdepth >> Constants.SHIFT_7;
    }

    /**
     * Updates the mix.
     */
    public void mixupdate() {
        for (int chan = 0; chan < numchan; chan++) {
            final int coffset = chan * CH_STRUCT_LEN;
            int a = channels[coffset + CH_VOLUME]
                    + channels[coffset + CH_TREM_VOLUME];

            if (a < 0) {
                a = 0;
            }

            if (a > MAX_VOLUME) {
                a = MAX_VOLUME;
            }

            channels[coffset + CH_AMPL] = a << FP_SHIFT - Constants.SHIFT_8;
            int p = channels[coffset + CH_PERIOD]
                    + channels[coffset + CH_VIBR_PERIOD];

            if (p < MIN_P_FOR_MIX_UPDATE) {
                p = MIN_P_FOR_MIX_UPDATE;
            }

            final int clk = amiga ? AMIGA_CLOCK_VALUE : STANDARD_CLOCK_VALUE;
            int s = (clk / p << FP_SHIFT) / samplerate;
            s = s * fttable[channels[coffset + CH_FINETUNE]
                    + FTTABLE_OFFSET] >> SHIFT_14_BITS;
            s = s * arptable[channels[coffset + CH_ARPEGGIO]] >> SHIFT_13_BITS;
            channels[coffset + CH_STEP] = s;
        }
    }

    private int getticklen() {
        return ((samplerate << 1) + (samplerate >> 1)) / bpm;
    }

    private int ushortbe(final byte[] buf, final int offset) {
        return ((buf[offset] & Constants.EIGHT_BIT_MASK) << Constants.SHIFT_8)
                | (buf[offset + 1] & Constants.EIGHT_BIT_MASK);
    }
}
