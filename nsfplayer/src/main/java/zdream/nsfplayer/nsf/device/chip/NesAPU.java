/*
 * NSFPlay/NFSPlug project by Brezza.
 *
 * https://web.archive.org/web/20160301201825/http://www.pokipoki.org/dsa/
 */

package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundEnvelopeNoise;
import zdream.nsfplayer.sound.SoundPulse;
import zdream.nsfplayer.sound.SoundSweepPulse;


/**
 * APU audio device, manages audio output for Pulse1 and Pulse2 channels
 *
 * @author Zdream
 * @since v0.2.4
 */
public class NesAPU extends AbstractSoundChip {

    private final SoundSweepPulse pulse1;
    private final SoundSweepPulse pulse2;

    /**
     * Parameters for record placement
     */
    private final byte[] mem = new byte[8];
    private byte mem4015, mem4017 = 0;

    public NesAPU(NsfRuntime runtime) {
        super(runtime);
        pulse1 = new SoundSweepPulse(true);
        pulse2 = new SoundSweepPulse(false);
    }

    @Override
    public boolean write(int adr, int val, int id) {
        /*
         * APU The addresses to be received here are:
         * [0x4000, 0x4007], 0x4015, 0x4017
         */
        switch (adr) {
            case 0x4000:
            case 0x4001:
            case 0x4002:
            case 0x4003: {
                writeToPulse(adr & 3, val, pulse1);
                mem[adr & 3] = (byte) val;
            }
            break;
            case 0x4004:
            case 0x4005:
            case 0x4006:
            case 0x4007: {
                writeToPulse(adr & 3, val, pulse2);
                mem[(adr & 3) + 4] = (byte) val;
            }
            break;
            case 0x4015: {
                // enable
                mem4015 = (byte) val;
                handleEnable();
            }
            break;
            case 0x4017: {
                // unknown
                mem4017 = (byte) val;
            }
            break;

            default:
                return false;
        }

        return true;
    }

    public void writeToPulse(int adr, int value, SoundSweepPulse pulse) {
        switch (adr) {
            case 0:
                pulse.dutyLength = (value >> 6);
                pulse.envelopeLoop = (value & 0x20) != 0;
                pulse.envelopeFix = (value & 0x10) != 0;
                pulse.fixedVolume = (value & 0xF);
                break;

            case 1:
                pulse.sweepEnabled = (value >> 7) != 0;
                pulse.sweepPeriod = (value & 0x70) >> 4;
                pulse.sweepMode = (value & 8) != 0;
                pulse.sweepShift = value & 7;
                pulse.onSweepUpdated();
                break;

            case 2: {
                int period = (pulse.period & 0xff00) + value;
                pulse.period = period;
                pulse.onSweepUpdated();
            }
            break;

            case 3: {
                int period = (pulse.period & 0xff) + ((value & 7) << 8);
                pulse.period = period;
                pulse.lengthCounter = SoundPulse.LENGTH_TABLE[(value & 0xf8) >> 3];
                pulse.onEnvelopeUpdated();
                pulse.onSweepUpdated();
            }
            break;
        }
    }

    /**
     * <p>This deals mainly with the enable switch for the transmitter
     * </p>
     */
    private void handleEnable() {
        pulse1.setEnable((mem4015 & 1) != 0);
        pulse2.setEnable((mem4015 & 2) != 0);

        if (!pulse1.isEnable()) {
            pulse1.lengthCounter = 0;
        }
        if (!pulse2.isEnable()) {
            pulse2.lengthCounter = 0;
        }
    }

    @Override
    public boolean read(int adr, IntHolder val, int id) {
        if (adr >= 0x4000 && adr < 0x4008) {
            val.val = mem[adr & 0x7] & 0xFF;
            return true;
        } else if (adr == 0x4015) {
            int m = pulse1.isEnable() ? 1 : 0;
            m |= pulse2.isEnable() ? 1 : 0;
            val.val = m;
            return true;
        } else if (adr == 0x4017) {
            val.val = mem4017 & 0xFF;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        pulse1.reset();
        pulse2.reset();
        pulse1.setSequenceStep(
                getRuntime().manager.getRegion() == ERegion.PAL ?
                        SoundEnvelopeNoise.SEQUENCE_STEP_PAL : SoundEnvelopeNoise.SEQUENCE_STEP_NTSC);
        pulse2.setSequenceStep(
                getRuntime().manager.getRegion() == ERegion.PAL ?
                        SoundEnvelopeNoise.SEQUENCE_STEP_PAL : SoundEnvelopeNoise.SEQUENCE_STEP_NTSC);

        Arrays.fill(mem, (byte) 0);
        mem4015 = 0x7F;
        mem4017 = 0;
    }

    @Override
    public AbstractNsfSound getSound(byte code) {
        return switch (code) {
            case CHANNEL_2A03_PULSE1 -> pulse1;
            case CHANNEL_2A03_PULSE2 -> pulse2;
            default -> null;
        };

    }

    @Override
    public byte[] getAllChannelCodes() {
        return new byte[] {CHANNEL_2A03_PULSE1, CHANNEL_2A03_PULSE2};
    }
}
