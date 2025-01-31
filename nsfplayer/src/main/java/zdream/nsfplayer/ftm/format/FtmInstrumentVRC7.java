package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;


public class FtmInstrumentVRC7 extends AbstractFtmInstrument {

    public FtmInstrumentVRC7() {
        patchNum = 0;
        regs[0] = 0x01;
        regs[1] = 0x21;
        regs[2] = 0x00;
        regs[3] = 0x00;
        regs[4] = 0x00;
        regs[5] = 0xF0;
        regs[6] = 0x00;
        regs[7] = 0x0F;
    }

    /**
     * Patch number
     */
    public int patchNum;

    /**
     * Customize patch settings. If {@link #patchNum} is 0, the following data will be used
     * Custom patch settings, unsigned
     */
    public final short[] regs = new short[8];

    @Override
    public FtmChipType instType() {
        return FtmChipType.VRC7;
    }

    @Override
    public String toString() {
        return String.format("VRC7 Instrument #%d %s", seq, name);
    }
}
