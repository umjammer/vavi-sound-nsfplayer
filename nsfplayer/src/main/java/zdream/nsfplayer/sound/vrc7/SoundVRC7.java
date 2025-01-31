package zdream.nsfplayer.sound.vrc7;

import zdream.nsfplayer.sound.AbstractNsfSound;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_PG_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.FINISH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_PG_BITS;


/**
 * <p>VRC7 track sounders. There are six such tracks
 * <p>This speaker is dependent on the OPLL environment,
 * and its creation requires the creation of an {@link OPLL} instance.
 * Please get the sound via {@link OPLL#getSound(int)}.
 * </p>
 *
 * @author Zdream
 * @since v0.2.7
 */
public class SoundVRC7 extends AbstractNsfSound {

    final OPLL opll;
    final int index;

    SoundVRC7(OPLL opll, int index) {
        this.opll = opll;
        this.index = index;

        this.modulatorSlot = new OPLLSlot(opll, 0);
        this.carrierSlot = new OPLLSlot(opll, 1);
    }

    //
    // parameters
    //

    /*
     * (Indirect) Original recording parameters
     *
     * Most of the data is in the modulatorSlot and the carrierSlot.
     */

    /**
     * modulatorSlot, No. 0
     * carrierSlot, No. 1
     */
    public final OPLLSlot modulatorSlot, carrierSlot;

    /**
     * Flags for whether modulatorSlot and carrierSlot are open.
     */
    public boolean modOn, carOn;

    /**
     * The patch number used. 0 is customized
     */
    public int patchNum;

    /*
     * Auxiliary parameters
     */

    /**
     * The state of the audio changes every {@link #step} clock,
     * and the audio value needs to be output to an external source.
     * Record the number of clocks remaining until the next step trigger point.
     */
    private int counter = 36;

    /**
     * Phase Parameters of the AM Unit (Amp Modulator)
     */
    private int am_phase;

    /**
     * Phase Parameters of the PM Unit (Pitch Modulator)
     */
    private int pm_phase;

    //
    // Processing Writes
    //

    /**
     * Ask if the current vocalizer is using a custom patch
     */
    public boolean useCustomPatch() {
        return patchNum == 0;
    }

    /**
     * <p>In the NES process, if the [0x10, 0x40) position of the VRC7 chip has been written to,
     * the corresponding slot needs a data reset. That's what this function is supposed to do
     * <p>Re-read the data of the custom patch, and reset your own slot with the patch data.
     * <p>If the speaker is using a custom patch, i.e. <code>patchNum == 0</code>,
     * then when the data of the custom patch is modified externally,
     * the current speaker needs to synchronize the data.
     * </p>
     */
    public void rebuildAll() {
        rebuildModDphase();
        rebuildModTll();
        rebuildModSintbl();
        recalcModDphase();

        rebuildCarDphase();
        rebuildCarTll();
        rebuildCarSintbl();
        recalcCarDphase();
    }

    public void rebuildModDphase() {
        modulatorSlot.dPhase = opll.dPhaseTable[modulatorSlot.fNum][modulatorSlot.block][modulatorSlot.patch.ML];
        modulatorSlot.rks = opll.rksTable[(modulatorSlot.fNum) >> 8][modulatorSlot.block][modulatorSlot.patch.KR ? 1 : 0];
    }

    public void rebuildCarDphase() {
        carrierSlot.dPhase = opll.dPhaseTable[carrierSlot.fNum][carrierSlot.block][carrierSlot.patch.ML];
        carrierSlot.rks = opll.rksTable[(carrierSlot.fNum) >> 8][carrierSlot.block][carrierSlot.patch.KR ? 1 : 0];
    }

    public void rebuildModTll() {
        modulatorSlot.tll = opll.tllTable[(modulatorSlot.fNum) >> 5][modulatorSlot.block][modulatorSlot.patch.TL][modulatorSlot.patch.KL];
    }

    public void rebuildCarTll() {
        carrierSlot.tll = opll.tllTable[(carrierSlot.fNum) >> 5][carrierSlot.block][carrierSlot.volume][carrierSlot.patch.KL];
    }

    public void rebuildModSintbl() {
        modulatorSlot.sinTbl = opll.waveform[modulatorSlot.patch.WF];
    }

    public void rebuildCarSintbl() {
        carrierSlot.sinTbl = opll.waveform[carrierSlot.patch.WF];
    }

    public void recalcModDphase() {
        modulatorSlot.eg_dPhase = modulatorSlot.calc_eg_dphase();
    }

    public void recalcCarDphase() {
        carrierSlot.eg_dPhase = carrierSlot.calc_eg_dphase();
    }

    /**
     * Modify Tone
     * Change a voice
     */
    public void setPatch(int num) {
        patchNum = num;
        modulatorSlot.patch.copyFrom(opll.patches[num * 2]);
        carrierSlot.patch.copyFrom(opll.patches[num * 2 + 1]);
    }

    /**
     * Channel key on
     */
    public void keyOn() {
        if (!modOn) {
            modulatorSlot.slotOn();
            modOn = true;
        }
        if (!carOn) {
            carrierSlot.slotOn();
            carOn = true;
        }
    }

    /**
     * Channel key off
     * <p>Unlike enable, which is a complete shutdown, keyOff is an end state, much like the RELEASE state of seq.
     * </p>
     */
    public void keyOff() {
        if (carOn) {
            carrierSlot.slotOff();
            carOn = false;
        }
        modOn = false;
    }

    //
    // Public method
    //

    @Override
    public void reset() {
        counter = 36;

        // OPLL
        pm_phase = 0;
        am_phase = 0;

        modulatorSlot.reset();
        carrierSlot.reset();

        super.reset();
    }

    @Override
    protected void onProcess(int time) {
        int value;

        while (time >= counter) {
            time -= counter;
            this.time += counter;
            counter = 36;

            value = this.renderStep();
            mix(value);
        }

        this.time += time;
        counter -= time;
    }

    private int renderStep() {
        // The original project's opll.update_ampm();
        // Update AM, PM unit
        pm_phase = (pm_phase + opll.pm_dPhase) & (PM_DP_WIDTH - 1);
        am_phase = (am_phase + opll.am_dPhase) & (AM_DP_WIDTH - 1);
        int lfo_am = opll.amTable[(am_phase) >> (AM_DP_BITS - AM_PG_BITS)];
        int lfo_pm = opll.pmTable[(pm_phase) >> (PM_DP_BITS - PM_PG_BITS)];

        carrierSlot.calc_phase(lfo_pm);
        carrierSlot.calc_envelope(lfo_am);
        modulatorSlot.calc_phase(lfo_pm);
        modulatorSlot.calc_envelope(lfo_am);

        if (carrierSlot.eg_mode != FINISH) {
            carrierSlot.calc_slot_car(modulatorSlot.calc_slot_mod());
        }

        return carrierSlot.output[1];
    }
}
