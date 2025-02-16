/*
 * NSFPlay/NFSPlug project by Brezza.
 *
 * https://web.archive.org/web/20160301201825/http://www.pokipoki.org/dsa/
 */

package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.SoundN163;


/**
 * N163 audio chip, manages the audio output of 1 to 8 N163 channel
 *
 * @author Zdream
 * @since v0.2.6
 */
public class NesN163 extends AbstractSoundChip {

    private final SoundN163[] n163s = new SoundN163[8];

    /**
     * Is the upper N163 speaker on?
     */
    private final boolean[] ons = new boolean[8];

    /**
     * The volume envelope of the N163 speaker above is read at the index in reg[]
     */
    private final int[] offsets = new int[8];

    /**
     * Recording of actual data for relevant components.
     * The data for the 8 tracks in n163s are placed upside down,
     * with the first track at the end of the array.
     */
    private final byte[] reg = new byte[0x80];
    /**
     * Master switch for 8 channels
     */
    private boolean masterDisable;

    private int regSelect;
    private boolean regAdvance;

    public boolean isMasterDisable() {
        return masterDisable;
    }

    public NesN163(NsfRuntime runtime) {
        super(runtime);

        // In any case, the first orbit of N163 must be there.
        n163s[0] = new SoundN163();
        n163s[0].step = 15;
        ons[0] = true;
    }

int CC=0;
    @Override
    public boolean write(int adr, int val, int id) {
//if (CC++ < 300) { System.err.printf("%04x, %d, %d\n", adr, val, id); }
//else { System.exit(1); }
        if (adr == 0xE000) { // Main disable parameter
            masterDisable = ((val & 0x40) != 0);
            return true;
        } else if (adr == 0xF800) { // option
            regSelect = (val & 0x7F);
            regAdvance = (val & 0x80) != 0;
            return true;
        } else if (adr == 0x4800) { // write
            handleWrite(val);
            if (regAdvance)
                regSelect = (regSelect + 1) & 0x7F;
            return true;
        }
        return false;
    }

    @Override
    public boolean read(int adr, IntHolder val, int id) {
        if (adr == 0x4800) { // Selective Reading
            val.val = handleRead();
            if (regAdvance)
                regSelect = (regSelect + 1) & 0x7F;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        for (int i = 0; i < n163s.length; i++) {
            SoundN163 sound = n163s[i];
            if (sound != null && ons[i]) {
                sound.reset();
            }
        }
        Arrays.fill(reg, (byte) 0);
        Arrays.fill(offsets, 0);
    }

    /**
     * An external (typically {@link DeviceManager}) call to force an update of
     * the number of channels in the N163.
     * Does not automatically handle connections to the mixer
     *
     * @param num Total number of channels, range [1, 8]
     */
    public void forceChannelCount(int num) {
        this.writeChannelCount(num, false);
    }

    @Override
    public SoundN163 getSound(byte channelCode) {
        switch (channelCode) {
            case CHANNEL_N163_1:
            case CHANNEL_N163_2:
            case CHANNEL_N163_3:
            case CHANNEL_N163_4:
            case CHANNEL_N163_5:
            case CHANNEL_N163_6:
            case CHANNEL_N163_7:
            case CHANNEL_N163_8:
                int index = channelCode - CHANNEL_N163_1;
                if (ons[index]) {
                    return n163s[index];
                }
        }
        return null;
    }

    @Override
    public byte[] getAllChannelCodes() {
        byte[] bs = new byte[8];
        int count = 0;
        for (int i = 0; i < bs.length; i++) {
            SoundN163 sound = n163s[i];
            if (sound != null && ons[i]) {
                bs[count++] = (byte) (CHANNEL_N163_1 + i);
            }
        }

        if (count == 8) {
            return bs;
        }

        return Arrays.copyOf(bs, count);
    }

    //
    // Processing Writes
    //

    /**
     * Processing the results of writes
     *
     * @param val value, range [0, 0xFF]
     */
    private void handleWrite(int val) {
        if (regSelect <= 0x3F) {
            // Modify the envelope section
            writeEnvelop(regSelect, (byte) val);
        } else {
            // Modify the parameter section

            // 0x7F is where the volume of the first track is set, and where the total number of tracks is set.
            if (regSelect == 0x7F) {
                writeChannelCount(((val >> 4) & 0x07) + 1, true);
            }
            // As mentioned above, in reg[], the tracks are numbered from highest to lowest.
            int x = 7 - ((regSelect - 0x40) >> 3);
            writeParamToSound(x, regSelect & 7, val);
        }

        reg[regSelect] = (byte) val;
    }

    /**
     * Setting the total number of channels
     *
     * @param num Total number of channels, range [1, 8]
     * @param b   Whether the connection to the mixer needs to be processed, updated or not
     */
    private void writeChannelCount(int num, boolean b) {
        int step = num * 15;

        for (int i = 0; i < n163s.length; i++) {
            boolean on = i < num;
            ons[i] = on;
            SoundN163 sound = n163s[i];
            if (on) {
                if (sound == null) {
                    sound = n163s[i] = new SoundN163();
                }
                sound.step = step;
            } else {
                if (sound != null) {
                    sound.step = 0;
                    sound.reset();
                }
            }
        }

        if (b) {
            // Report the current number of tracks to the DeviceManager,
            // and have it connect the sound to the mixer.
            getRuntime().manager.reattachN163(num);
        }
    }

    /**
     * Setting the parameters of each microphone
     *
     * @param x       Speaker number, from 0, range [0, 7]
     * @param address address, range [0, 7]
     * @param value   value, range [0, 0xFF]
     */
    private void writeParamToSound(int x, int address, int value) {
        SoundN163 sound = n163s[x];
        if (sound == null) {
            return;
        }

        switch (address) {
            case 0:
                sound.period = (sound.period & 0xf_ff00) | (value & 0xff);
                break;
            case 1:
                sound.phase = (sound.phase & 0xff_ff00) | (value & 0xff);
                break;
            case 2:
                sound.period = (sound.period & 0xf_00ff) | ((value & 0xFF) << 8);
                break;
            case 3:
                sound.phase = (sound.phase & 0xff_00ff) | ((value & 0xff) << 8);
                break;
            case 4:
                sound.period = (sound.period & 0xffff) | ((value & 0x3) << 16);
                sound.length = 256 - (value & 0xFC);
                copyEnvelop(sound, this.offsets[x]);
                break;
            case 5:
                sound.phase = (sound.phase & 0xffff) | ((value & 0xff) << 16);
                break;
            case 6:
                int offset = value & 0xff;
                this.offsets[x] = offset;
                copyEnvelop(sound, offset);
                break;
            case 7:
                sound.volume = (value & 0xF);
                break;
        }
    }

    /**
     * Copy the volume envelope data of reg[] in its entirety into sound.wave
     */
    private void copyEnvelop(SoundN163 sound, int offset) {
        int len = Math.min(sound.length, (this.reg.length - offset) * 2);
        if (len > sound.wave.length) {
            len = sound.wave.length;
        }

        // TODO offset is an odd number, which doesn't usually happen,
        //  but it may be a normal situation, and it's not done.
        int index = offset / 2; // Points to this.reg

        // len Must be a multiple of 2
        for (int i = 0; i < len; ) {
            sound.wave[i++] = (byte) (this.reg[index] & 0xf); // lows
            sound.wave[i++] = (byte) ((this.reg[index] >> 4) & 0xf); // highs
            index++;
        }
    }

    /**
     * Modify the envelope, and also modify the corresponding envelope in each sound
     * that contains this envelope data {@link SoundN163#wave}
     */
    private void writeEnvelop(int address, byte value) {
        for (int i = 0; i < n163s.length; i++) {
            SoundN163 sound = n163s[i];
            if (sound == null || !ons[i]) {
                continue;
            }

            // TODO If the offset is an odd number, which doesn't usually happen,
            //  it may be a normal case, and it's not done.
            int offset = this.offsets[i] / 2;

            int length = sound.length;
            int index = (address - offset) * 2;

            if (index < length && index >= 0) {
                sound.wave[index++] = (byte) (value & 0xf); // lows
                sound.wave[index] = (byte) ((value >> 4) & 0xf); // highs
            }
        }
    }

    //
    // Processing Read
    //

    /**
     * Processing of read results
     *
     * @return read
     */
    private int handleRead() {
        if (regSelect > 0x3F) {
            int x = 7 - ((regSelect - 0x40) >> 3);
            readParamFromSound(x, regSelect & 7);
        }

        return reg[regSelect] & 0xff;
    }

    /**
     * Read data from the specified speaker and write it back to the reg array.
     *
     * @param x       Speaker number, from 0, range [0, 7]
     * @param address address, range [0, 7]
     */
    private void readParamFromSound(int x, int address) {
        SoundN163 sound = n163s[x];
        if (sound == null) {
            return;
        }

        int value = switch (address) {
            case 1 -> (sound.phase & 0xff);
            case 3 -> (sound.phase & 0xff00);
            case 5 -> (sound.phase & 0xff_0000);
            default -> 0;
        };

        reg[regSelect] = (byte) value;
    }
}
