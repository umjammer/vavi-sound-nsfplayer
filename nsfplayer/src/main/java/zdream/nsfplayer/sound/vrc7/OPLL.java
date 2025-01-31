package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DEPTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_PG_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_SPEED;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DB2LIN_AMP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DB_MUTE;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DB_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.OPLL_TONE_NUM;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PG_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_AMP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DEPTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_PG_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_SPEED;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.TL_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.TL_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.default_inst;


/**
 * Used only in VRC7. Retains all environment data used by VRC7.
 *
 * @author Zdream
 * @since v0.2.7
 */
public class OPLL {

    /** unsigned */
    int realStep;
    /** unsigned */
    int opllStep;

    // Voice Data
    // 19 x 2 = 38
    /** flag for check patch update */
    final OPLLPatch[] patches = new OPLLPatch[38];

    // Above are the variables of the original project OPLL

    /** Input clock, unsigned */
    int clk = 0;
    /** Sampling rate, unsigned */
    int rate = 0;

    // WaveTable for each envelope amp
    /** unsigned */
    final int[][] waveform = {new int[PG_WIDTH], new int[PG_WIDTH]};
    final int[] fullSinTable = waveform[0];
    final int[] halfSinTable = waveform[1];

    // LFO Table
    /** unsigned */
    final int[] pmTable = new int[PM_PG_WIDTH];
    final int[] amTable = new int[AM_PG_WIDTH];

    // Phase delta for LFO
    /** unsigned */
    int pm_dPhase, am_dPhase;

    // dB to Liner table
    /** int - 16bit */
    final int[] DB2LIN_TABLE = new int[(DB_MUTE + DB_MUTE) * 2];

    // Liner to Log curve conversion table (for Attack rate).
    /** unsigned - 16bit */
    final int[] AR_ADJUST_TABLE = new int[1 << EG_BITS];

    // Basic voice Data
    /**
     * Default patch values. Size: [8][38]
     */
    final OPLLPatch[][] default_patch = new OPLLPatch[OPLL_TONE_NUM][(16 + 3) * 2];

    /** Phase incr table for Attack, unsigned */
    final int[][] dPhaseARTable = new int[16][16];
    /** Phase incr table for Decay and Release, unsigned */
    final int[][] dPhaseDRTable = new int[16][16];

    /** KSL + TL Table, unsigned */
    final int[][][][] tllTable = new int[16][8][1 << TL_BITS][4];
    final int[][][] rksTable = new int[2][8][2];

    /** Phase incr table for PG, unsigned */
    final int[][][] dPhaseTable = new int[512][8][16];

    {
        for (OPLLPatch[] os : default_patch) {
            for (int j = 0; j < os.length; j++) {
                os[j] = new OPLLPatch();
            }
        }
    }

    private static void dump2patch(short[] dump, int offset, OPLLPatch patch0, OPLLPatch patch1) {
        patch0.AM = (dump[offset] & 0x80) != 0;
        patch1.AM = (dump[1 + offset] & 0x80) != 0;
        patch0.PM = (dump[offset] & 0x40) != 0;
        patch1.PM = (dump[1 + offset] & 0x40) != 0;
        patch0.EG = (dump[offset] & 0x20) != 0;
        patch1.EG = (dump[1 + offset] & 0x20) != 0;
        patch0.KR = (dump[offset] & 0x10) != 0;
        patch1.KR = (dump[1 + offset] & 0x10) != 0;
        patch0.ML = (dump[offset]) & 15;
        patch1.ML = (dump[1 + offset]) & 15;
        patch0.KL = (dump[2 + offset] >> 6) & 3;
        patch1.KL = (dump[3 + offset] >> 6) & 3;
        patch0.TL = (dump[2 + offset]) & 63;
        patch0.FB = (dump[3 + offset]) & 7;
        patch0.WF = (dump[3 + offset] >> 3) & 1;
        patch1.WF = (dump[3 + offset] >> 4) & 1;
        patch0.AR = (dump[4 + offset] >> 4) & 15;
        patch1.AR = (dump[5 + offset] >> 4) & 15;
        patch0.DR = (dump[4 + offset]) & 15;
        patch1.DR = (dump[5 + offset]) & 15;
        patch0.SL = (dump[6 + offset] >> 4) & 15;
        patch1.SL = (dump[7 + offset] >> 4) & 15;
        patch0.RR = (dump[6 + offset]) & 15;
        patch1.RR = (dump[7 + offset]) & 15;
    }

    /**
     * Called only on initialization
     */
    private void makeDefaultPatch() {
        for (int i = 0; i < OPLL_TONE_NUM; i++) {
            for (int j = 0; j < 19; j++) {
                dump2patch(default_inst[i], j * 16,
                        default_patch[i][j * 2], default_patch[i][j * 2 + 1]);
            }
        }
    }

    //
    // Initialization
    //

    /**
     * Initializing
     * Initialization requires rebuilding all the tables and patches you need.
     */
    private void internal_refresh() {

        //makeDphaseTable()
        {
            int fnum, block, ML;
            int[] mltable = {1, 1 * 2, 2 * 2, 3 * 2, 4 * 2, 5 * 2, 6 * 2, 7 * 2, 8 * 2, 9 * 2, 10 * 2, 10 * 2, 12 * 2,
                    12 * 2, 15 * 2, 15 * 2};

            for (fnum = 0; fnum < 512; fnum++) {
                for (block = 0; block < 8; block++) {
                    //#define RATE_ADJUST(x) (rate==49716?x:(e_uint32)((double)(x)*clk/72/rate + 0.5))
                    for (ML = 0; ML < 16; ML++) {
                        int x = ((fnum * mltable[ML]) << block) >> (20 - DP_BITS);
                        dPhaseTable[fnum][block][ML] = (rate == 49716 ? x
                                : (int) ((double) (x) * clk / 72 / rate + 0.5));
                    }
                }
            }
        }

        //makeDphaseARTable();
        // Rate Table for Attack
        {
            int AR, Rks, RM, RL;
            for (AR = 0; AR < 16; AR++) {
                for (Rks = 0; Rks < 16; Rks++) {
                    RM = AR + (Rks >> 2);
                    RL = Rks & 3;
                    if (RM > 15)
                        RM = 15;
                    switch (AR) {
                        case 0:
                            dPhaseARTable[AR][Rks] = 0;
                            break;
                        case 15:
                            dPhaseARTable[AR][Rks] = 0; // EG_DP_WIDTH
                            break;
                        default:
                            int x = 3 * (RL + 4) << (RM + 1);
                            dPhaseARTable[AR][Rks] = (rate == 49716 ? x : (int) ((double) (x) * clk / 72 / rate + 0.5));
                            break;
                    }
                }
            }
        }

        //makeDphaseDRTable()
        {
            int DR, Rks, RM, RL;

            for (DR = 0; DR < 16; DR++) {
                for (Rks = 0; Rks < 16; Rks++) {
                    RM = DR + (Rks >> 2);
                    RL = Rks & 3;
                    if (RM > 15)
                        RM = 15;
                    switch (DR) {
                        case 0:
                            dPhaseDRTable[DR][Rks] = 0;
                            break;
                        default: {
                            int x = (RL + 4) << (RM - 1);
                            dPhaseDRTable[DR][Rks] = (rate == 49716 ? x : (int) ((double) (x) * clk / 72 / rate + 0.5));
                        }
                        break;
                    }
                }
            }
        }

        double x = (PM_SPEED * PM_DP_WIDTH / (clk / 72));
        pm_dPhase = (rate == 49716 ? (int) x : (int) (x * clk / 72 / rate + 0.5)) & 0x7FFFFFFF;
        x = AM_SPEED * AM_DP_WIDTH / (clk / 72);
        am_dPhase = (rate == 49716 ? (int) x : (int) (x * clk / 72 / rate + 0.5)) & 0x7FFFFFFF;
    }

    /**
     * Called only on initialization
     *
     * @param c unsigned
     * @param r unsigned, 48000
     */
    private void makeTables(int c, int r) {
        clk = c;

        //makePmTable();
        {
            for (int i = 0; i < PM_PG_WIDTH; i++) {
                double phase = 2.0 * Math.PI * i / PM_PG_WIDTH, d;
                // saw begin - inline
                if (phase <= Math.PI / 2)
                    d = phase * 2 / Math.PI;
                else if (phase <= Math.PI * 3 / 2)
                    d = 2.0 - (phase * 2 / Math.PI);
                else
                    d = -4.0 + phase * 2 / Math.PI;
                // saw end - inline
                pmTable[i] = (int) ((double) PM_AMP * Math.pow(2, PM_DEPTH * d / 1200));
            }
        }
        //makeAmTable();
        {
            for (int i = 0; i < AM_PG_WIDTH; i++) {
                double phase = 2.0 * Math.PI * i / PM_PG_WIDTH, d;
                // saw begin - inline
                if (phase <= Math.PI / 2)
                    d = phase * 2 / Math.PI;
                else if (phase <= Math.PI * 3 / 2)
                    d = 2.0 - (phase * 2 / Math.PI);
                else
                    d = -4.0 + phase * 2 / Math.PI;
                // saw end - inline
                amTable[i] = (int) (AM_DEPTH / 2 / DB_STEP * (1.0 + d));
            }
        }
        //makeDB2LinTable();
        {
            for (int i = 0; i < DB_MUTE + DB_MUTE; i++) {
                DB2LIN_TABLE[i] = (int) ((double) ((1 << DB2LIN_AMP_BITS) - 1)
                        * Math.pow(10, -(double) i * DB_STEP / 20));
                if (i >= DB_MUTE)
                    DB2LIN_TABLE[i] = 0;
                DB2LIN_TABLE[i + DB_MUTE + DB_MUTE] = -DB2LIN_TABLE[i];
            }
        }
        //makeAdjustTable();
        {
            AR_ADJUST_TABLE[0] = (1 << EG_BITS) - 1;
            for (int i = 1; i < (1 << EG_BITS); i++) {
                AR_ADJUST_TABLE[i] = (int) ((double) (1 << EG_BITS) - 1
                        - ((1 << EG_BITS) - 1) * Math.log(i) / Math.log(127));
            }
        }
        //makeTllTable();
        {
            double[] kltable = {
                    0.00, 18.00, 24.00, 27.75, 30.00, 32.25, 33.75, 35.25,
                    36.00, 37.50, 38.25, 39.00, 39.75, 40.50, 41.25, 42.00
            };

            int tmp;
            for (int fnum = 0; fnum < 16; fnum++)
                for (int block = 0; block < 8; block++)
                    for (int TL = 0; TL < 64; TL++)
                        for (int KL = 0; KL < 4; KL++) {
                            if (KL == 0) {
                                tllTable[fnum][block][TL][KL] = (TL * (int) (TL_STEP / EG_STEP));
                            } else {
                                tmp = (int) (kltable[fnum] - (6.00) * (7 - block));
                                if (tmp <= 0)
                                    tllTable[fnum][block][TL][KL] = (TL) * (int) (TL_STEP / EG_STEP);
                                else
                                    tllTable[fnum][block][TL][KL] = (int) ((tmp >> (3 - KL)) / EG_STEP)
                                            + (TL) * (int) (TL_STEP / EG_STEP);
                            }
                        }
        }
        //makeRksTable();
        {
            int fnum8, block, KR;

            for (fnum8 = 0; fnum8 < 2; fnum8++)
                for (block = 0; block < 8; block++)
                    for (KR = 0; KR < 2; KR++) {
                        if (KR != 0)
                            rksTable[fnum8][block][KR] = (block << 1) + fnum8;
                        else
                            rksTable[fnum8][block][KR] = block >> 1;
                    }
        }
        //makeSinTable();
        {
            int i;

            for (i = 0; i < PG_WIDTH / 4; i++) {
                double d = Math.sin(2.0 * Math.PI * i / PG_WIDTH), v;
                // lin2db begin - inline
                if (d == 0)
                    v = (DB_MUTE - 1);
                else
                    v = Math.min(-(int) (20.0 * Math.log10(d) / DB_STEP), DB_MUTE - 1); /* 0 -- 127 */
                // lin2db end
                fullSinTable[i] = (int) v;
            }

            for (i = 0; i < PG_WIDTH / 4; i++) {
                fullSinTable[PG_WIDTH / 2 - 1 - i] = fullSinTable[i];
            }

            for (i = 0; i < PG_WIDTH / 2; i++) {
                fullSinTable[PG_WIDTH / 2 + i] = DB_MUTE + DB_MUTE + fullSinTable[i];
            }

            for (i = 0; i < PG_WIDTH / 2; i++)
                halfSinTable[i] = fullSinTable[i];
            for (i = PG_WIDTH / 2; i < PG_WIDTH; i++)
                halfSinTable[i] = fullSinTable[0];
        }

        makeDefaultPatch();

        rate = r;
        internal_refresh();
    }

    public OPLL() {
        this(3579545, 49716); // default
    }

    /**
     * init OPLL
     *
     * @param clk  unsigned
     * @param rate unsigned
     * @return
     */
    public OPLL(int clk, int rate) {
        initSound();

        int i = 0;

        for (i = 0; i < patches.length; i++) {
            patches[i] = new OPLLPatch();
        }
        makeTables(clk, rate);

        reset();
        reset_patch(0);

    }

    /**
     * Reset patch datas by system default.
     */
    public void reset_patch(int type) {
        type = type % OPLL_TONE_NUM;
        int i;

        for (i = 0; i < this.patches.length; i++) {
            this.patches[i].copyFrom(default_patch[type][i]);
        }
    }

    /**
     * Reset whole of OPLL except patch datas.
     */
    public void reset() {
        int i;

        for (i = 0; i < sounds.length; i++) {
            sounds[i].setPatch(0);
            sounds[i].reset();
        }

        long factor = 1L << 31;
        realStep = (int) (factor / rate);
        if (realStep < 0) {
            System.err.println("opll.realStep < 0");
        }
        opllStep = (int) (factor / (clk / 72));
    }

    //
    // I/O Ctrl
    //

    /**
     * Get patch 0
     *
     * @return
     */
    public OPLLPatch getCustomModPatch() {
        return patches[0];
    }

    public OPLLPatch getCustomCarPatch() {
        return patches[1];
    }

    //
    // sound device
    //

    final SoundVRC7[] sounds = new SoundVRC7[6];

    private void initSound() {
        for (int i = 0; i < sounds.length; i++) {
            sounds[i] = new SoundVRC7(this, i);
        }
    }

    /**
     * Example of obtaining a loudspeaker
     *
     * @param index range [0, 5]
     * @return
     */
    public SoundVRC7 getSound(int index) {
        return sounds[index];
    }
}
