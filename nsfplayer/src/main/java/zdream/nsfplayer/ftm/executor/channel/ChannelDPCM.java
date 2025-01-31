package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.sound.SoundDPCM;


/**
 * <p>DPCM Track
 *
 * <p>The five parameters of this track, instrument,
 * instrumentUpdated, masterNote, masterPitch, and curPeriod, are still available.
 * <li>masterPitch default value -1, Wxx effect, reset at the beginning of each frame.
 * Allowed range [0, 15]
 * </li>
 * </p>
 *
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelDPCM extends AbstractFtmChannel {

    public ChannelDPCM() {
        super(CHANNEL_2A03_DPCM);
    }

    @Override
    public void playNote() {
        super.playNote();

        /*
         * Announces to the outside world whether this sounder is working. Has no practical effect.
         * Because the outside world wants to know whether a sound is being made,
         * it can only sense it through isplaying and volume.
         * The modification of curVolume here is just to let the outside world know
         *  the situation and facilitate debugging.
         */
        if (!sound.isFinish()) {
            curVolume = 1;
        }
    }

    /*
     * DMA sampling
     */

    /**
     * Sampling Data
     */
    FtmDPCMSample sample;

    /**
     * m_cDAC
     */
    private int deltaCounter = -1;
    private boolean loop;

    /**
     * The starting position of the read position, unit 1 byte
     */
    private int offset;

    /**
     * Unit: 1 bytes
     */
    private int sampleLength;

    /**
     * Unit: 1 bytes
     */
    private int loopOffset;
    private int loopLength;

    /**
     * Flag, whether the sample and related data should be reloaded.
     * When the key or instrument is modified, needReload is true for that frame
     */
    private boolean needReload;

    /**
     * Xxx effect temporary parameters
     */
    private int retrigger, retriggerCtrl;
    private boolean needTrigger;

//	private int customPitch; // Transfer masterPitch

    /**
     * Sets the original DAC value. Zxx effect triggers
     *
     * @param deltaCounter
     */
    public void setDeltaCounter(int deltaCounter) {
        this.deltaCounter = deltaCounter & 0x7f;
    }

    /**
     * Set the starting reading position of the sample. Yxx effect trigger.
     * This value will be cleared to zero after being used once.
     *
     * @param offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Set the duration of the loop. After calling this method once,
     * the loop is triggered only once, and the Xxx effect is triggered.
     *
     * @param retrigger retrigger + 1 indicates the actual recorded duration, unit: frame
     */
    public void setRetrigger(int retrigger) {
        this.retrigger = retrigger + 1;
        if (retriggerCtrl == 0)
            retriggerCtrl = this.retrigger;
    }

    @Override
    public void reset() {
        deltaCounter = -1;
        loop = false;
        offset = 0;

        super.reset();

        // The sounder also needs to be reset
        sound.reset();
    }

    @Override
    protected void startFrame() {
        masterPitch = -1;
        retrigger = 0;
        offset = 0; // Supplementary

        super.startFrame();
    }

    @Override
    public void doHalt() {
        super.doHalt();
        sound.sample = null;
    }

    @Override
    public void doRelease() {
        this.doHalt();
    }

    @Override
    public void setMasterNote(int note) {
        needReload = true;
        super.setMasterNote(note);
    }

    @Override
    public void setInstrument(int instrument) {
        needReload = true;
        super.setInstrument(instrument);
    }

    public void reload() {
        FtmInstrument2A03 inst = getRuntime().querier.get2A03Instrument(instrument);
        if (inst == null || masterNote == 0) {
            return;
        }

        FtmDPCMSample sample = inst.getSample(masterNote);
        if (sample != null) {
            int pitch = inst.getSamplePitch(masterNote);
            this.loop = (pitch & 0x80) != 0;

            if (masterPitch != -1) {
                // If there is a Wxx effect, that effect will prevail.
                pitch = masterPitch;
            }

            // After checking, it was found that the original program did not set SampleLoopOffset.
            // loopOffset = inst.getSampleLoopOffset(masterNote);
            this.loopOffset = 0;

            int sampleSize = sample.size(); // Total bytes

            if (sampleSize > 0) {
                this.sample = sample;
                this.curPeriod = pitch & 0xF;
                this.sampleLength = sampleSize - this.offset;
                this.loopLength = sampleSize - this.loopOffset;
                this.needTrigger = true;

                // Initial delta counter value
                int delta = inst.getSampleDelta(masterNote);

                if (delta != -1 && deltaCounter == -1)
                    deltaCounter = delta & 0xFF;

                retriggerCtrl = retrigger;
            }
        } else {
            playing = false;
        }
    }

    /*
     * sound
     */

    final SoundDPCM sound = new SoundDPCM();

    @Override
    public SoundDPCM getSound() {
        return sound;
    }

    @Override
    public void writeToSound() {
        if (deltaCounter != -1) {
            sound.deltaCounter = deltaCounter;
            deltaCounter = -1;
        }

        // Xxx Effect
        if (retrigger != 0) {
            retriggerCtrl--;
            if (retriggerCtrl == 0) {
                retriggerCtrl = retrigger;
                needTrigger = true;
            }
        }

        if (needReload) {
            this.reload();
            needReload = false;
        }

        if (!playing) {
            return;
        }

        if (needTrigger) {
            // Start playing the sample
            sound.loop = loop;
            sound.periodIndex = curPeriod;
            sound.offsetAddress = offset;
            sound.length = this.sampleLength;
            sound.sample = this.sample;

            // Loop offset
            if (loopOffset > 0) {
                sound.offsetAddress = loopOffset;
                sound.length = this.loopLength;
            }

            sound.reload();

            needTrigger = false;
        }
    }
}
