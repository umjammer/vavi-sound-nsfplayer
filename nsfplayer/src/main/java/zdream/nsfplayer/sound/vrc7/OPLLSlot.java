package zdream.nsfplayer.sound.vrc7;

import java.util.Arrays;

import zdream.nsfplayer.core.IResetable;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.ATTACK;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DB_MUTE;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DB_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DECAY;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DP_BASE_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.EG_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.FINISH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PG_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PG_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_AMP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.RELEASE;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.SETTLE;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.SLOT_AMP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.SL_STEP;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.SUSHOLD;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.SUSTINE;


public class OPLLSlot implements IResetable {

    final OPLL parent;

    public final OPLLPatch patch = new OPLLPatch();

    /** 0 : modulator 1 : carrier */
    public final int type;

    /** OUTPUT */
    public int feedback;
    /** Output value of slot */
    public final int[] output = new int[2];

    // for Phase Generator (PG)
    /** Wave table, unsigned */
    public int[] sinTbl;
    /** Phase, unsigned */
    public int phase;
    /** Phase increment amount, unsigned */
    public int dPhase;
    /** output, unsigned */
    public int pgOut;

    // for Envelope Generator (EG)
    /** F-Number */
    public int fNum;
    /** Block */
    public int block;
    /** Current volume */
    public int volume;
    /** Sustain true = ON, false = OFF */
    public boolean sustain;
    /** Total Level + Key scale level, unsigned */
    public int tll;
    /** Key scale offset (Rks), unsigned */
    public int rks;
    /** Current state */
    public int eg_mode;
    /** Phase, unsigned */
    public int eg_phase;
    /** Phase increment amount, unsigned */
    public int eg_dPhase;
    /** output, unsigned */
    public int egOut;

    /**
     * @param parent OPLL environment
     * @param type   0 : modulator;  1 : carrier
     */
    public OPLLSlot(OPLL parent, int type) {
        super();
        this.type = type;
        this.parent = parent;
    }

    /**
     * Slot key off
     */
    void slotOff() {
        if (eg_mode == ATTACK)
            eg_phase = ((parent.AR_ADJUST_TABLE[((eg_phase) >> (EG_DP_BITS - EG_BITS))]) << ((EG_DP_BITS)-  (EG_BITS)));
        eg_mode = RELEASE;
        eg_dPhase = calc_eg_dphase(); // UPDATE_EG(slot);
    }

    /**
     * Slot key on
     */
    void slotOn() {
        eg_mode = ATTACK;
        eg_phase = 0;
        phase = 0;
        eg_dPhase = calc_eg_dphase(); // UPDATE_EG(slot);
    }

    /**
     * Slot key on without reseting the phase
     */
    void slotOn2() {
        eg_mode = ATTACK;
        eg_phase = 0;
        eg_dPhase = calc_eg_dphase(); // UPDATE_EG(slot);
    }

    void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * PG
     */
    void calc_phase(int lfo) {
        if (patch.PM)
            phase += ((dPhase * lfo) >> PM_AMP_BITS);
        else
            phase += dPhase;

        phase &= (DP_WIDTH - 1);

        pgOut = (phase) >> (DP_BASE_BITS);
    }

    /**
     * EG
     */
    void calc_envelope(int lfo) {
        int[] SL = new int[16];
        for (int i = 0; i < SL.length; i++) {
            SL[i] = (int) ((3.0 * i / SL_STEP) * (int) (SL_STEP / EG_STEP)) << (EG_DP_BITS - EG_BITS);
        }

        int egout; // unsigned

        switch (this.eg_mode) {
            case ATTACK:
                egout = parent.AR_ADJUST_TABLE[(this.eg_phase) >> (EG_DP_BITS - EG_BITS)];
                this.eg_phase += this.eg_dPhase;
                if ((EG_DP_WIDTH & this.eg_phase) != 0 || (this.patch.AR == 15)) {
                    egout = 0;
                    this.eg_phase = 0;
                    this.eg_mode = DECAY;
                    this.eg_dPhase = this.calc_eg_dphase();
                }
                break;

            case DECAY:
                egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
                this.eg_phase += this.eg_dPhase;
                if (this.eg_phase >= SL[this.patch.SL]) {
                    if (this.patch.EG) {
                        this.eg_phase = SL[this.patch.SL];
                        this.eg_mode = SUSHOLD;
                        this.eg_dPhase = this.calc_eg_dphase();
                    } else {
                        this.eg_phase = SL[this.patch.SL];
                        this.eg_mode = SUSTINE;
                        this.eg_dPhase = this.calc_eg_dphase();
                    }
                }
                break;

            case SUSHOLD:
                egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
                if (!this.patch.EG) {
                    this.eg_mode = SUSTINE;
                    this.eg_dPhase = this.calc_eg_dphase();
                }
                break;

            case SUSTINE:
            case RELEASE:
                egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
                this.eg_phase += this.eg_dPhase;
                if (egout >= (1 << EG_BITS)) {
                    this.eg_mode = FINISH;
                    egout = (1 << EG_BITS) - 1;
                }
                break;

            case SETTLE:
                egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
                this.eg_phase += this.eg_dPhase;
                if (egout >= (1 << EG_BITS)) {
                    this.eg_mode = ATTACK;
                    egout = (1 << EG_BITS) - 1;
                    this.eg_dPhase = this.calc_eg_dphase();
                }
                break;

            case FINISH:
                egout = (1 << EG_BITS) - 1;
                break;

            default:
                egout = (1 << EG_BITS) - 1;
                break;
        }

        if (this.patch.AM) {
            egout = ((egout + this.tll) * (int) (EG_STEP / DB_STEP)) + lfo;
        } else {
            egout = ((egout + this.tll) * (int) (EG_STEP / DB_STEP));
        }

        if (egout >= DB_MUTE)
            egout = DB_MUTE - 1;

        this.egOut = egout | 3;
    }

    /**
     * Calculation parameters
     *
     * @return
     */
    int calc_eg_dphase() {
        switch (eg_mode) {
            case ATTACK:
                return parent.dPhaseARTable[patch.AR][rks];

            case DECAY:
                return parent.dPhaseDRTable[patch.DR][rks];

            case SUSHOLD:
                return 0;

            case SUSTINE:
                return parent.dPhaseDRTable[patch.RR][rks];

            case RELEASE:
                if (sustain)
                    return parent.dPhaseDRTable[5][rks];
                else if (patch.EG)
                    return parent.dPhaseDRTable[patch.RR][rks];
                else
                    return parent.dPhaseDRTable[7][rks];

            case SETTLE:
                return parent.dPhaseDRTable[15][0];

            case FINISH:
                return 0;

            default:
                return 0;
        }
    }

    /**
     * CARRIER
     */
    int calc_slot_car(int fm) {
        if (egOut >= (DB_MUTE - 1)) {
            output[0] = 0;
        } else { // #define wave2_8pi(e) ( (e) << ( 2 + PG_BITS - SLOT_AMP_BITS ))
            output[0] = parent.DB2LIN_TABLE[sinTbl[(pgOut + ((fm) << (2 + PG_BITS - SLOT_AMP_BITS)))
                    & (PG_WIDTH - 1)] + egOut];
        }

        output[1] = (output[1] + output[0]) >> 1;
        return output[1];
    }

    /**
     * MODULATOR
     */
    int calc_slot_mod() {
        int fm;

        output[1] = output[0];

        if (egOut >= (DB_MUTE - 1)) {
            output[0] = 0;
        } else if (patch.FB != 0) {
            fm = ((feedback) << (1 + PG_BITS - SLOT_AMP_BITS)) >> (7 - patch.FB);
            output[0] = parent.DB2LIN_TABLE[sinTbl[(pgOut + fm) & (PG_WIDTH - 1)] + egOut];
        } else {
            output[0] = parent.DB2LIN_TABLE[sinTbl[pgOut] + egOut];
        }

        feedback = (output[1] + output[0]) >> 1;

        return feedback;

    }

    /**
     * TOM
     */
    int calc_slot_tom() {
        if (egOut >= (DB_MUTE - 1))
            return 0;

        return parent.DB2LIN_TABLE[sinTbl[pgOut] + egOut];
    }

    /**
     * SNARE
     *
     * @param noise unsigned
     */
    int calc_slot_snare(int noise) {
        if (egOut >= (DB_MUTE - 1))
            return 0;

        if (((pgOut >> 7) & 1) != 0)
            return parent.DB2LIN_TABLE[(noise != 0 ? 0 : (int) ((15.0) / DB_STEP)) + egOut];
        else
            return parent.DB2LIN_TABLE[(noise != 0 ? DB_MUTE + DB_MUTE : DB_MUTE + DB_MUTE + (int) (15.0 / DB_STEP))
                    + egOut];
    }

    /**
     * TOP-CYM
     *
     * @param pgout_hh unsigned
     */
    int calc_slot_cym(int pgout_hh) {
        int dbout;

        if (egOut >= (DB_MUTE - 1))
            return 0;
        else if
            // the same as fmopl.c
        ((((pgout_hh >> (PG_BITS - 8)) & 1) ^ ((pgout_hh >> (PG_BITS - 1)) & 1) | ((pgout_hh >> (PG_BITS - 7)) & 1) ^
                        // different from fmopl.c
                        (((pgOut >> (PG_BITS - 7)) & 1) & (((pgOut >> (PG_BITS - 5)) & 1) == 0 ? 1 : 0))) != 0)
            dbout = (int) (DB_MUTE + DB_MUTE + (3.0) / DB_STEP);
        else
            dbout = (int) ((3.0) / DB_STEP);

        return parent.DB2LIN_TABLE[dbout + egOut];
    }

    /**
     * HI-HAT
     *
     * @param noise unsigned
     */
    int calc_slot_hat(int pgout_cym, int noise) {
        int dbout;

        if (egOut >= (DB_MUTE - 1))
            return 0;
        else if ((
                // the same as fmopl.c
                ((((pgOut >> (PG_BITS - 8)) & 1) ^ ((pgOut >> (PG_BITS - 1)) & 1))
                        | ((pgOut >> (PG_BITS - 7)) & 1)) ^
                        // different from fmopl.c
                        (((pgout_cym >> (PG_BITS - 7)) & 1) & (((pgout_cym >> (PG_BITS - 5)) & 1) == 1 ? 0 : 1))) != 0) {
            if (noise != 0)
                dbout = (int) (DB_MUTE + DB_MUTE + (12.0) / DB_STEP);
            else
                dbout = (int) (DB_MUTE + DB_MUTE + (24.0) / DB_STEP);
        } else {
            if (noise != 0)
                dbout = (int) ((12.0) / DB_STEP);
            else
                dbout = (int) ((24.0) / DB_STEP);
        }

        return parent.DB2LIN_TABLE[dbout + egOut];
    }

    /**
     * Force Refresh (When external program changes some parameters).
     */
    public void forceRefresh() {
        this.dPhase = parent.dPhaseTable[this.fNum][this.block][this.patch.ML];
        this.rks = parent.rksTable[(this.fNum) >> 8][this.block][this.patch.KR ? 1 : 0];

        if (this.type == 0) {
            this.tll = parent.tllTable[(this.fNum) >> 5][this.block][this.patch.TL][this.patch.KL];
        } else {
            this.tll = parent.tllTable[(this.fNum) >> 5][this.block][this.volume][this.patch.KL];
        }
        this.sinTbl = parent.waveform[this.patch.WF];
        this.eg_dPhase = this.calc_eg_dphase();
    }

    /**
     * initialization
     */
    @Override
    public void reset() {
        sinTbl = parent.waveform[0];
        phase = 0;
        dPhase = 0;
        output[0] = 0;
        output[1] = 0;
        feedback = 0;
        eg_mode = FINISH;
        eg_phase = EG_DP_WIDTH;
        eg_dPhase = 0;
        rks = 0;
        tll = 0;
        sustain = false;
        fNum = 0;
        block = 0;
        volume = 0;
        pgOut = 0;
        egOut = 0;
        patch.reset();
    }

    @Override
    public String toString() {
        return "[patch=" + patch + ", type=" + type + ", feedback=" + feedback
                + ", output=" + Arrays.toString(output) + ", sinTbl=" + Arrays.toString(sinTbl) + ", phase=" + phase
                + ", dPhase=" + dPhase + ", pgOut=" + pgOut + ", fNum=" + fNum + ", block=" + block + ", volume="
                + volume + ", sustain=" + sustain + ", tll=" + tll + ", rks=" + rks + ", eg_mode=" + eg_mode
                + ", eg_phase=" + eg_phase + ", eg_dPhase=" + eg_dPhase + ", egOut=" + egOut + "]";
    }
}
