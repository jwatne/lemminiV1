package micromod;

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
     * 7-bit shift.
     */
    private static final int SHIFT_7_BITS = 7;
    /**
     * Mask for 6th bit = 0x20.
     */
    private static final int BIT_SIX_MASK = 0x20;
    /**
     * 5-bit mask = 0x1F.
     */
    private static final int FIVE_BIT_MASK = 0x1F;
    /**
     * Offset for eparam component in buffer.
     */
    private static final int EPARAM_OFFSET = 3;
    /**
     * Mask for 5th bit = 0x10.
     */
    private static final int BIT_5_MASK = 0x10;
    /**
     * Maximum number of rows.
     */
    private static final int NUM_ROWS = 64;
    /**
     * 4-bit shift.
     */
    private static final int SHIFT_4_BITS = 4;
    /**
     * Mask for bits 5-8 = 0xF0.
     */
    private static final int BITS_5_TO_8_MASK = 0xF0;
    /**
     * Number fcount values - used for mod calc.
     */
    private static final int NUM_FCOUNT_VALUES = 3;
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
    private static final int MAX_VOLUME = 64;
    /**
     * Amount to add to index to get vol value in buffer.
     */
    private static final int VOL_OFFSET = 45;
    /**
     * Amount deducted for values > {@link #MAX7}.
     */
    private static final int REDUCTION16 = 16;
    /**
     * Max value = 7.
     */
    private static final int MAX7 = 7;
    /**
     * 4-bit mask = 0x0F.
     */
    private static final int FOUR_BIT_MASK = 0xF;
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
    private static final int MIN_SAMPLE_INDEX = 1084;
    /**
     * Second index of number of channels in buffer.
     */
    private static final int NUM_CHANNEL_INDEX_2 = 1081;
    /**
     * Multiplier of values to be in tens place.
     */
    private static final int TEN = 10;
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
    private static final int FOUR_CHANNELS = 4;
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
     * 8-bit mask.
     */
    private static final int EIGHT_BIT_MASK = 0xff;
    /**
     * Second index of number of Amiga channels in buffer.
     */
    private static final int AMIGA_NUM_CHANNEL_INDEX_2 = 1083;
    /**
     * 8-bit shift.
     */
    private static final int SHIFT_8_BITS = 8;
    /**
     * First index of number of Amiga channels in buffer.
     */
    private static final int AMIGA_NUM_CHANNEL_INDEX_1 = 1082;
    /**
     * Lowest index of patterns in buffer.
     */
    private static final int FIRST_PATTERN_INDEX = 952;
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
    private static final int ONE_HUNDRED_TWENTY_SEVEN_MASK = 0x7F;
    /**
     * Index of songlen in buffer.
     */
    private static final int SONGLEN_INDEX = 950;
    /**
     * Java 32 bits per int.
     */
    private static final int INT_BITS = 32;
    /** FP shift. */
    private static final int FP_SHIFT = 13;
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
    private static final int CH_STRUCT_LEN = 0x16;
    /** Ch spos. */
    private static final int CH_SPOS = 0x00;
    /** Ch step. */
    private static final int CH_STEP = 0x01;
    /** Ch ampl. */
    private static final int CH_AMPL = 0x02;
    /** Ch instrument. */
    private static final int CH_INSTRUMENT = 0x03;
    /** Ch assigned. */
    private static final int CH_ASSIGNED = 0x04;
    /** Ch volume. */
    private static final int CH_VOLUME = 0x05;
    /** Ch finetune. */
    private static final int CH_FINETUNE = 0x06;
    /** Ch period. */
    private static final int CH_PERIOD = 0x07;
    /** Ch porta period. */
    private static final int CH_PORTA_PERIOD = 0x08;
    /** Ch porta param. */
    private static final int CH_PORTA_PARAM = 0x09;
    /** Ch panning. */
    private static final int CH_PANNING = 0x0A;
    /** Ch arpeggio. */
    private static final int CH_ARPEGGIO = 0x0B;
    /** Ch vibr period. */
    private static final int CH_VIBR_PERIOD = 0x0C;
    /** Ch vibr param. */
    private static final int CH_VIBR_PARAM = 0x0D;
    /** Ch vibr count. */
    private static final int CH_VIBR_COUNT = 0x0E;
    /** Ch trem volume. */
    private static final int CH_TREM_VOLUME = 0x0F;
    /** Ch trem param. */
    private static final int CH_TREM_PARAM = 0x10;
    /** Ch pat loop row. */
    private static final int CH_PAT_LOOP_ROW = 0x11;
    /** Ch note period. */
    private static final int CH_NOTE_PERIOD = 0x12;
    /** Ch note instru. */
    private static final int CH_NOTE_INSTRU = 0x13;
    /** Ch note effect. */
    private static final int CH_NOTE_EFFECT = 0x14;
    /** Ch note eparam. */
    private static final int CH_NOTE_EPARAM = 0x15;
    /** Fx arpeggio. */
    private static final int FX_ARPEGGIO = 0x00;
    /** Fx porta up. */
    private static final int FX_PORTA_UP = 0x01;
    /** Fx porta down. */
    private static final int FX_PORTA_DOWN = 0x02;
    /** Fx tone porta. */
    private static final int FX_TONE_PORTA = 0x03;
    /** Fx vibrato. */
    private static final int FX_VIBRATO = 0x04;
    /** Fx tporta vol. */
    private static final int FX_TPORTA_VOL = 0x05;
    /** Fx vibrato vol. */
    private static final int FX_VIBRATO_VOL = 0x06;
    /** Fx tremolo. */
    private static final int FX_TREMOLO = 0x07;
    /** Fx set panning. */
    private static final int FX_SET_PANNING = 0x08;
    /** Fx set spos. */
    private static final int FX_SET_SPOS = 0x09;
    /** FX volume slide. */
    private static final int FX_VOLUME_SLIDE = 0x0A;
    /** FX pat jump. */
    private static final int FX_PAT_JUMP = 0x0B;
    /** FX set volume. */
    private static final int FX_SET_VOLUME = 0x0C;
    /** FX pat break. */
    private static final int FX_PAT_BREAK = 0x0D;
    /** FX extended. */
    private static final int FX_EXTENDED = 0x0E;
    /** FX set speed. */
    private static final int FX_SET_SPEED = 0x0F;
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
    /** EX retrig. */
    private static final int EX_RETRIG = 0x90;
    /** EX fine vol up. */
    private static final int EX_FINE_VOL_UP = 0xA0;
    /** EX fine vol dn. */
    private static final int EX_FINE_VOL_DN = 0xB0;
    /** EX note cut. */
    private static final int EX_NOTE_CUT = 0xC0;
    /** EX note delay. */
    private static final int EX_NOTE_DELAY = 0xD0;
    /** EX pat delay. */
    private static final int EX_PAT_DELAY = 0xE0;
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
    /** Pattern. */
    private int pat;
    /** N pattern. */
    private int npat;
    /** Row. */
    private int row;
    /** N row. */
    private int nrow;
    /** Tick. */
    private int tick;
    /** Temp. */
    private int tempo;
    /** Beats per minute. */
    private int bpm;
    /** F count. */
    private int fcount;
    /** Loop count. */
    private int loopcount;
    /** Loop channel. */
    private int loopchan;
    /** Instruments. */
    private final int[] instruments = new int[IN_STRUCT_LEN * INT_BITS];
    /** Channels. */
    private final int[] channels = new int[CH_STRUCT_LEN * INT_BITS];
    /** Sample rate. */
    private final int samplerate;
    /** Ticks remaining. */
    private int tickremain;

    /**
     * Constructor ( mod - module data ).
     *
     * @param modDataBuffer byte buffer containing MOD data
     * @param sampleRateHz  sample rate in Hertz
     */
    public Micromod(final byte[] modDataBuffer, final int sampleRateHz) {
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

        switch ((modDataBuffer[AMIGA_NUM_CHANNEL_INDEX_1] << SHIFT_8_BITS)
                | modDataBuffer[AMIGA_NUM_CHANNEL_INDEX_2] & EIGHT_BIT_MASK) {
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
                    & FOUR_BIT_MASK;

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
     * Return the song length in samples.
     *
     * @return song length in samples
     */
    public int getlen() {
        reset();
        int len = getticklen();
        while (!tick()) {
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
                        - SHIFT_8_BITS;
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
                                + (spos >> FP_SHIFT)] << SHIFT_8_BITS;
                        l[offset + x] += sample * lamp >> FP_SHIFT;
                        r[offset + x] += sample * ramp >> FP_SHIFT;
                        spos += step;
                    }
                }
                channels[coffset + CH_SPOS] = spos;
            }
            tickremain -= count;
            if (tickremain == 0) {
                tick();
                tickremain = getticklen();
            }
            offset += count;
            len -= count;
        }
    }

    private void reset() {
        npat = 0;
        pat = 0;
        nrow = 0;
        row = 0;
        tempo = DEFAULT_TEMPO;
        tick = DEFAULT_TICK;
        bpm = DEFAULT_BPM;
        loopchan = 0;
        loopcount = 0;

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

        row();
        tickremain = getticklen();
    }

    private boolean tick() {
        tick--;

        if (tick <= 0) {
            tick = tempo;
            return row();
        }

        // Update channel fx
        for (int chan = 0; chan < numchan; chan++) {
            final int coffset = chan * CH_STRUCT_LEN;
            final int effect = channels[coffset + CH_NOTE_EFFECT];
            final int eparam = channels[coffset + CH_NOTE_EPARAM];

            switch (effect) {
            case FX_ARPEGGIO:
                switch (fcount % NUM_FCOUNT_VALUES) {
                case 0:
                    channels[coffset + CH_ARPEGGIO] = 0;
                    break;
                case 1:
                    channels[coffset + CH_ARPEGGIO] = (eparam
                            & BITS_5_TO_8_MASK) >> SHIFT_4_BITS;
                    break;
                case 2:
                    channels[coffset + CH_ARPEGGIO] = eparam & FOUR_BIT_MASK;
                    break;
                default:
                    break;
                }

                break;
            case FX_PORTA_UP:
                channels[coffset + CH_PERIOD] -= eparam;
                break;
            case FX_PORTA_DOWN:
                channels[coffset + CH_PERIOD] += eparam;
                break;
            case FX_TONE_PORTA:
                toneporta(coffset);
                break;
            case FX_VIBRATO:
                vibrato(coffset);
                break;
            case FX_TPORTA_VOL:
                volslide(coffset, eparam);
                toneporta(coffset);
                break;
            case FX_VIBRATO_VOL:
                volslide(coffset, eparam);
                vibrato(coffset);
                break;
            case FX_TREMOLO:
                tremolo(coffset);
                break;
            case FX_VOLUME_SLIDE:
                volslide(coffset, eparam);
                break;
            case FX_EXTENDED:
                switch (eparam & BITS_5_TO_8_MASK) {
                case EX_RETRIG:
                    int rtparam = eparam & FOUR_BIT_MASK;
                    if (rtparam == 0) {
                        rtparam = 1;
                    }
                    if (fcount % rtparam == 0) {
                        channels[coffset + CH_SPOS] = 0;
                    }
                    break;
                case EX_NOTE_CUT:
                    if ((eparam & FOUR_BIT_MASK) == fcount) {
                        channels[coffset + CH_VOLUME] = 0;
                    }
                    break;
                case EX_NOTE_DELAY:
                    if ((eparam & FOUR_BIT_MASK) == fcount) {
                        trigger(coffset);
                    }
                    break;
                default:
                    break;
                }
                break;
            default:
                break;
            }
            channels[coffset + CH_VIBR_COUNT]++;
        }
        mixupdate();
        fcount++;
        return false;
    }

    private boolean row() {
        // Decide whether to restart.
        boolean songend = false;

        if (npat < pat) {
            songend = true;
        }

        if (npat == pat && nrow <= row && loopcount <= 0) {
            songend = true;
        }

        // Jump to next row
        pat = npat;
        row = nrow;
        // Decide next row.
        nrow = row + 1;

        if (nrow == NUM_ROWS) {
            npat = pat + 1;
            nrow = 0;
        }

        // Load channels and process fx
        fcount = 0;
        final int poffset = mod[FIRST_PATTERN_INDEX + pat]
                & ONE_HUNDRED_TWENTY_SEVEN_MASK;
        final int roffset = MIN_SAMPLE_INDEX
                + (poffset * 64 * numchan * FOUR_CHANNELS)
                + (row * numchan * 4);

        for (int chan = 0; chan < numchan; chan++) {
            final int coffset = chan * CH_STRUCT_LEN;
            final int noffset = roffset + (chan * FOUR_CHANNELS);
            channels[coffset
                    + CH_NOTE_PERIOD] = (mod[noffset + 1] & EIGHT_BIT_MASK)
                            | ((mod[noffset] & FOUR_BIT_MASK) << SHIFT_8_BITS);
            channels[coffset + CH_NOTE_INSTRU] = ((mod[noffset + 2]
                    & BITS_5_TO_8_MASK) >> SHIFT_4_BITS)
                    | (mod[noffset] & BIT_5_MASK);
            channels[coffset + CH_NOTE_EFFECT] = mod[noffset + 2]
                    & FOUR_BIT_MASK;
            channels[coffset + CH_NOTE_EPARAM] = mod[noffset + EPARAM_OFFSET]
                    & EIGHT_BIT_MASK;
            final int effect = channels[coffset + CH_NOTE_EFFECT];
            final int eparam = channels[coffset + CH_NOTE_EPARAM];
            if (!(effect == FX_EXTENDED
                    && ((eparam & BITS_5_TO_8_MASK) == EX_NOTE_DELAY))) {
                trigger(coffset);
            }
            channels[coffset + CH_ARPEGGIO] = 0;
            channels[coffset + CH_VIBR_PERIOD] = 0;
            channels[coffset + CH_TREM_VOLUME] = 0;
            switch (effect) {

            case FX_TONE_PORTA:
                if (eparam != 0) {
                    channels[coffset + CH_PORTA_PARAM] = eparam;
                }
                break;
            case FX_VIBRATO:
                if (eparam != 0) {
                    channels[coffset + CH_VIBR_PARAM] = eparam;
                }
                vibrato(coffset);
                break;

            case FX_VIBRATO_VOL:
                vibrato(coffset);
                break;
            case FX_TREMOLO:
                if (eparam != 0) {
                    channels[coffset + CH_TREM_PARAM] = eparam;
                }
                tremolo(coffset);
                break;
            case FX_SET_PANNING:
                if (!amiga) {
                    channels[coffset + CH_PANNING] = eparam;
                }
                break;
            case FX_SET_SPOS:
                channels[coffset + CH_SPOS] = eparam << FP_SHIFT + SHIFT_8_BITS;
                break;

            case FX_PAT_JUMP:
                if (loopcount <= 0) {
                    npat = eparam;
                    nrow = 0;
                }
                break;
            case FX_SET_VOLUME:
                channels[coffset + CH_VOLUME] = (eparam > MAX_VOLUME)
                        ? MAX_VOLUME
                        : eparam;
                break;
            case FX_PAT_BREAK:
                if (loopcount <= 0) {
                    npat = pat + 1;
                    nrow = ((eparam & BITS_5_TO_8_MASK) >> SHIFT_4_BITS) * TEN
                            + (eparam & FOUR_BIT_MASK);
                }

                break;
            case FX_EXTENDED:
                switch (eparam & BITS_5_TO_8_MASK) {
                case EX_FINE_PORT_UP:
                    channels[coffset + CH_PERIOD] -= (eparam & FOUR_BIT_MASK);
                    break;
                case EX_FINE_PORT_DN:
                    channels[coffset + CH_PERIOD] += (eparam & FOUR_BIT_MASK);
                    break;

                case EX_SET_FINETUNE:
                    int ftval = eparam & FOUR_BIT_MASK;

                    if (ftval > MAX7) {
                        ftval -= REDUCTION16;
                    }

                    channels[coffset + CH_FINETUNE] = ftval;
                    break;
                case EX_PAT_LOOP:
                    final int plparam = eparam & FOUR_BIT_MASK;

                    if (plparam == 0) {
                        channels[coffset + CH_PAT_LOOP_ROW] = row;
                    }

                    if (plparam > 0
                            && channels[coffset + CH_PAT_LOOP_ROW] < row) {
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
                    break;

                case EX_FINE_VOL_UP:
                    int fvolup = channels[coffset + CH_VOLUME]
                            + (eparam & FOUR_BIT_MASK);
                    if (fvolup > MAX_VOLUME) {
                        fvolup = MAX_VOLUME;
                    }
                    channels[coffset + CH_VOLUME] = fvolup;
                    break;
                case EX_FINE_VOL_DN:
                    int fvoldn = channels[coffset + CH_VOLUME]
                            - (eparam & FOUR_BIT_MASK);
                    if (fvoldn > MAX_VOLUME) {
                        fvoldn = 0;
                    }
                    channels[coffset + CH_VOLUME] = fvoldn;
                    break;
                case EX_NOTE_CUT:
                    if ((eparam & FOUR_BIT_MASK) == fcount) {
                        channels[coffset + CH_VOLUME] = 0;
                    }

                    break;
                case EX_NOTE_DELAY:
                    if ((eparam & FOUR_BIT_MASK) == fcount) {
                        trigger(coffset);
                    }

                    break;
                case EX_PAT_DELAY:
                    tick = tempo + tempo * (eparam & FOUR_BIT_MASK);
                    break;
                default:
                    break;
                }

                break;
            case FX_SET_SPEED:
                if (eparam < INT_BITS) {
                    tempo = eparam;
                    tick = eparam;
                } else {
                    bpm = eparam;
                }

                break;
            default:
                break;
            }
        }

        mixupdate();
        fcount++;

        if (npat >= songlen) {
            npat = restart;
        }

        if (nrow >= NUM_ROWS) {
            nrow = 0;
        }

        return songend;
    }

    private void trigger(final int coffset) {
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

    private void volslide(final int coffset, final int eparam) {
        int vol = channels[coffset + CH_VOLUME];
        vol += (eparam & BITS_5_TO_8_MASK) >> SHIFT_4_BITS;
        vol -= eparam & FOUR_BIT_MASK;
        if (vol > MAX_VOLUME) {
            vol = MAX_VOLUME;
        }
        if (vol < 0) {
            vol = 0;
        }
        channels[coffset + CH_VOLUME] = vol;
    }

    private void toneporta(final int coffset) {
        int sp = channels[coffset + CH_PERIOD];
        final int dp = channels[coffset + CH_PORTA_PERIOD];
        if (sp < dp) {
            sp += channels[coffset + CH_PORTA_PARAM];
            if (sp > dp) {
                sp = dp;
            }
        }
        if (sp > dp) {
            sp -= channels[coffset + CH_PORTA_PARAM];
            if (sp < dp) {
                sp = dp;
            }
        }
        channels[coffset + CH_PERIOD] = sp;
    }

    private void vibrato(final int coffset) {
        final int vparam = channels[coffset + CH_VIBR_PARAM];
        final int vspeed = (vparam & BITS_5_TO_8_MASK) >> SHIFT_4_BITS;
        final int vdepth = vparam & FOUR_BIT_MASK;
        final int vibpos = vspeed * channels[coffset + CH_VIBR_COUNT];
        int tval = sintable[vibpos & FIVE_BIT_MASK];

        if ((vibpos & BIT_SIX_MASK) > 0) {
            tval = -tval;
        }

        channels[coffset + CH_VIBR_PERIOD] = tval * vdepth >> SHIFT_7_BITS;
    }

    private void tremolo(final int coffset) {
        final int tparam = channels[coffset + CH_TREM_PARAM];
        final int tspeed = (tparam & BITS_5_TO_8_MASK) >> SHIFT_4_BITS;
        final int tdepth = tparam & FOUR_BIT_MASK;
        final int trempos = tspeed * channels[coffset + CH_VIBR_COUNT];
        int tval = sintable[trempos & FIVE_BIT_MASK];

        if ((trempos & BIT_SIX_MASK) > 0) {
            tval = -tval;
        }

        channels[coffset + CH_TREM_VOLUME] = tval * tdepth >> SHIFT_7_BITS;
    }

    private void mixupdate() {
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

            channels[coffset + CH_AMPL] = a << FP_SHIFT - SHIFT_8_BITS;
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
        return ((buf[offset] & EIGHT_BIT_MASK) << SHIFT_8_BITS)
                | (buf[offset + 1] & EIGHT_BIT_MASK);
    }
}
