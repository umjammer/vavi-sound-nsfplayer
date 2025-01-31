/*
 * NSFPlay/NFSPlug project by Brezza.
 *
 * https://web.archive.org/web/20160301201825/http://www.pokipoki.org/dsa/
 */

package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundDPCM;
import zdream.nsfplayer.sound.SoundEnvelopeNoise;
import zdream.nsfplayer.sound.SoundLinearTriangle;
import zdream.nsfplayer.sound.SoundPulse;


/**
 * A 2A03 audio device that manages the audio output of Triangle, Noise and DPCM channels.
 *
 * @author Zdream
 * @since v0.2.4
 */
public class NesDMC extends AbstractSoundChip {

    private final SoundLinearTriangle triangle;
    private final SoundEnvelopeNoise noise;
    private final SoundDPCM dpcm;

    /**
     * Parameters for record placement
     */
    private final byte[] mem = new byte[12];
    private byte mem4015 = 0;

    public NesDMC(NsfRuntime runtime) {
        super(runtime);
        triangle = new SoundLinearTriangle();
        noise = new SoundEnvelopeNoise();
        dpcm = new SoundDPCM();
    }

    @Override
    public boolean write(int adr, int val, int id) {
        /*
         * APU The addresses to be received here are:
         * [0x4000, 0x4007], 0x4015, 0x4017
         */
        switch (adr) {
            case 0x4008:
            case 0x4009:
            case 0x400A:
            case 0x400B: {
                // triangle
                writeToTriangle(adr & 3, val);
                mem[adr & 3] = (byte) val;
            }
            break;
            case 0x400C:
            case 0x400D:
            case 0x400E:
            case 0x400F: {
                // noise
                writeToNoise(adr & 3, val);
                mem[(adr & 3) + 4] = (byte) val;
            }
            break;
            case 0x4010:
            case 0x4011:
            case 0x4012:
            case 0x4013: {
                // dpcm
                writeToDPCM(adr & 3, val);
                mem[(adr & 3) + 8] = (byte) val;
            }
            break;
            case 0x4015: {
                // enable
                mem4015 = (byte) val;
                handleEnable();
            }
            break;

            default:
                return false;
        }

        return true;
    }

    private void writeToTriangle(int adr, int value) {
        switch (adr) {
            case 0:
                triangle.looping = (value >> 7) != 0;
                triangle.linearLoad = (value & 0x7F);
                break;

            // Ignore position 1.

            case 2: {
                int period = (triangle.period & 0xFF00) + value;
                triangle.period = period;
            }
            break;

            case 3: {
                int period = (triangle.period & 0xFF) + ((value & 7) << 8);
                triangle.period = period;
                triangle.lengthCounter = SoundPulse.LENGTH_TABLE[(value & 0xF8) >> 3];
                triangle.onEnvelopeUpdated();
            }
            break;

        }
    }

    private void writeToNoise(int adr, int value) {
        switch (adr) {
            case 0:
                noise.envelopeLoop = (value & 0x20) != 0;
                noise.envelopeDisable = (value & 0x10) != 0;
                noise.fixedVolume = (value & 0xF);
                break;

            // Ignore position 1.

            case 2: {
                noise.periodIndex = (value & 0xF);
                noise.dutySampleRate = ((value & 0x80) != 0) ?
                        SoundEnvelopeNoise.DUTY_SAMPLE_RATE1 : SoundEnvelopeNoise.DUTY_SAMPLE_RATE0;
            }
            break;

            case 3: {
                noise.lengthCounter = SoundPulse.LENGTH_TABLE[(value & 0xF8) >> 3];
                noise.onEnvelopeUpdated();
            }
            break;

        }
    }

    private void writeToDPCM(int adr, int value) {
        switch (adr) {
            case 0:
                dpcm.loop = (value & 0x40) != 0;
                dpcm.periodIndex = (value & 0xF);
                break;

            case 1:
                dpcm.deltaCounter = (value & 0x7F);
                break;

            case 2: {
                dpcm.offsetAddress = value * 64;
            }
            break;

            case 3: {
                dpcm.length = value * 16;
            }
            break;

        }
    }

    /**
     * <p>The main issue here is to deal with the DPCM reading samples
     * <p>and enable switches for other speakers
     * </p>
     */
    private void handleEnable() {
        if ((mem4015 & 16) == 0) {
            dpcm.setEnable(false);
            dpcm.sample = null; // not active
        } else {
            dpcm.setEnable(true);
            if (dpcm.sample == null) {
                // Need to prepare to read
                FtmDPCMSample sample = new FtmDPCMSample();
                int address = (0xC000 | (dpcm.offsetAddress));
                if (dpcm.length == 0) {
                    return;
                }

                int length = (dpcm.length + 1);
                sample.data = new byte[length];

                getRuntime().manager.stack.read(sample.data, 0, length, address);
                dpcm.sample = sample;
                dpcm.offsetAddress = 0; // TODO This is the place I use to force a reset.
                dpcm.reload(); // This should not be called during the reset phase
            }
        }

        triangle.setEnable((mem4015 & 4) != 0);
        noise.setEnable((mem4015 & 8) != 0);

        if (!triangle.isEnable()) {
            triangle.lengthCounter = 0;
        }
        if (!noise.isEnable()) {
            noise.lengthCounter = 0;
        }
    }

    @Override
    public boolean read(int adr, IntHolder val, int id) {
        if (adr >= 0x4008 && adr < 0x4014) {
            val.val = mem[adr - 0x4008] & 0xFF;
            return true;
        } else if (adr == 0x4015) {
            {
                // Amendments to mem4015
                if (dpcm.isFinish()) {
                    mem4015 = 0;
                } else {
                    mem4015 = 16;
                }

                mem4015 |= triangle.isEnable() ? 4 : 0;
                mem4015 |= noise.isEnable() ? 8 : 0;
            }
            val.val |= mem4015;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        triangle.reset();
        noise.reset();
        triangle.setSequenceStep(
                getRuntime().manager.getRegion() == ERegion.PAL ?
                        SoundEnvelopeNoise.SEQUENCE_STEP_PAL : SoundEnvelopeNoise.SEQUENCE_STEP_NTSC);
        noise.setSequenceStep(
                getRuntime().manager.getRegion() == ERegion.PAL ?
                        SoundEnvelopeNoise.SEQUENCE_STEP_PAL : SoundEnvelopeNoise.SEQUENCE_STEP_NTSC);
        dpcm.reset();

        Arrays.fill(mem, (byte) 0);
        mem4015 = 0x7F;
    }

    @Override
    public AbstractNsfSound getSound(byte code) {
        return switch (code) {
            case CHANNEL_2A03_TRIANGLE -> triangle;
            case CHANNEL_2A03_NOISE -> noise;
            case CHANNEL_2A03_DPCM -> dpcm;
            default -> null;
        };
    }

    @Override
    public byte[] getAllChannelCodes() {
        return new byte[] {CHANNEL_2A03_TRIANGLE, CHANNEL_2A03_NOISE, CHANNEL_2A03_DPCM};
    }
}
