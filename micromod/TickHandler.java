package micromod;

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
 * Handler of ticks for a Micromod. Code moved from Micromod by John Watne
 * 08/2023.
 */
public class TickHandler {
    /**
     * Number fcount values - used for mod calc.
     */
    private static final int NUM_FCOUNT_VALUES = 3;
    /** Fx arpeggio. */
    private static final int FX_ARPEGGIO = 0x00;
    /** Fx porta up. */
    private static final int FX_PORTA_UP = 0x01;
    /** Fx porta down. */
    private static final int FX_PORTA_DOWN = 0x02;
    /** FX volume slide. */
    private static final int FX_VOLUME_SLIDE = 0x0A;
    /** EX retrig. */
    private static final int EX_RETRIG = 0x90;
    /** Tick. */
    private int tick;

    /**
     * Micromod owning the instance of TickHandler.
     */
    private final Micromod micromod;

    /**
     * Constructs a TickHandler for the specified Micromod.
     *
     * @param owner the Micromod owning the instance of TickHandler.
     */
    public TickHandler(final Micromod owner) {
        this.micromod = owner;
    }

    /**
     * Returns value for tick.
     *
     * @return value for tick.
     */
    public int getTick() {
        return tick;
    }

    /**
     * Sets value for tick.
     *
     * @param tickValue value for tick.
     */
    public void setTick(final int tickValue) {
        this.tick = tickValue;
    }

    /**
     * Decrements tick and takes the appropriate actions for the new value.
     *
     * @return <code>true</code> if the song is ended.
     */
    public boolean tick() {
        tick--;

        if (tick <= 0) {
            tick = micromod.getTempo();
            return micromod.row();
        }

        final int[] channels = micromod.getChannels();
        final int fcount = micromod.getFcount();

        // Update channel fx
        for (int chan = 0; chan < micromod.getNumchan(); chan++) {
            final int coffset = chan * Micromod.CH_STRUCT_LEN;
            handleEffect(channels, coffset);
            channels[coffset + Micromod.CH_VIBR_COUNT]++;
        }

        micromod.mixupdate();
        micromod.setFcount(fcount + 1);
        return false;
    }

    private void handleEffect(final int[] channels, final int coffset) {
        final int effect = channels[coffset + Micromod.CH_NOTE_EFFECT];
        final int eparam = channels[coffset + Micromod.CH_NOTE_EPARAM];

        switch (effect) {
        case FX_ARPEGGIO:
            handleArpeggioEffects(coffset);
            break;
        case FX_PORTA_UP:
            channels[coffset + Micromod.CH_PERIOD] -= eparam;
            break;
        case FX_PORTA_DOWN:
            channels[coffset + Micromod.CH_PERIOD] += eparam;
            break;
        case Micromod.FX_TONE_PORTA:
            toneporta(coffset);
            break;
        case Micromod.FX_VIBRATO:
            micromod.vibrato(coffset);
            break;
        case Micromod.FX_TPORTA_VOL:
            volslide(coffset, eparam);
            toneporta(coffset);
            break;
        case Micromod.FX_VIBRATO_VOL:
            volslide(coffset, eparam);
            micromod.vibrato(coffset);
            break;
        case Micromod.FX_TREMOLO:
            micromod.tremolo(coffset);
            break;
        case FX_VOLUME_SLIDE:
            volslide(coffset, eparam);
            break;
        case Micromod.FX_EXTENDED:
            handleFxExtended(coffset);
            break;
        default:
            break;
        }
    }

    private void handleArpeggioEffects(final int coffset) {
        final int[] channels = micromod.getChannels();
        final int fcount = micromod.getFcount();
        final int eparam = channels[coffset + Micromod.CH_NOTE_EPARAM];

        switch (fcount % NUM_FCOUNT_VALUES) {
        case 0:
            channels[coffset + Micromod.CH_ARPEGGIO] = 0;
            break;
        case 1:
            channels[coffset + Micromod.CH_ARPEGGIO] = (eparam
                    & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4;
            break;
        case 2:
            channels[coffset + Micromod.CH_ARPEGGIO] = eparam
                    & Constants.FOUR_BIT_MASK;
            break;
        default:
            break;
        }
    }

    /**
     * Handle extended FX effect.
     *
     * @param coffset channel offset.
     */
    private void handleFxExtended(final int coffset) {
        final int[] channels = micromod.getChannels();
        final int fcount = micromod.getFcount();
        final int eparam = channels[coffset + Micromod.CH_NOTE_EPARAM];

        switch (eparam & Constants.BITS_5_TO_8_MASK) {
        case EX_RETRIG:
            int rtparam = eparam & Constants.FOUR_BIT_MASK;

            if (rtparam == 0) {
                rtparam = 1;
            }

            if (fcount % rtparam == 0) {
                channels[coffset + Micromod.CH_SPOS] = 0;
            }

            break;
        case Micromod.EX_NOTE_CUT:
            if ((eparam & Constants.FOUR_BIT_MASK) == fcount) {
                channels[coffset + Micromod.CH_VOLUME] = 0;
            }

            break;
        case Micromod.EX_NOTE_DELAY:
            if ((eparam & Constants.FOUR_BIT_MASK) == fcount) {
                micromod.trigger(coffset);
            }

            break;
        default:
            break;
        }
    }

    private void volslide(final int coffset, final int eparam) {
        final int[] channels = micromod.getChannels();
        int vol = channels[coffset + Micromod.CH_VOLUME];
        vol += (eparam & Constants.BITS_5_TO_8_MASK) >> Constants.SHIFT_4;
        vol -= eparam & Constants.FOUR_BIT_MASK;

        if (vol > Micromod.MAX_VOLUME) {
            vol = Micromod.MAX_VOLUME;
        }

        if (vol < 0) {
            vol = 0;
        }

        channels[coffset + Micromod.CH_VOLUME] = vol;
    }

    private void toneporta(final int coffset) {
        final int[] channels = micromod.getChannels();
        int sp = channels[coffset + Micromod.CH_PERIOD];
        final int dp = channels[coffset + Micromod.CH_PORTA_PERIOD];

        if (sp < dp) {
            sp += channels[coffset + Micromod.CH_PORTA_PARAM];

            if (sp > dp) {
                sp = dp;
            }
        }

        if (sp > dp) {
            sp -= channels[coffset + Micromod.CH_PORTA_PARAM];

            if (sp < dp) {
                sp = dp;
            }
        }

        channels[coffset + Micromod.CH_PERIOD] = sp;
    }
}
