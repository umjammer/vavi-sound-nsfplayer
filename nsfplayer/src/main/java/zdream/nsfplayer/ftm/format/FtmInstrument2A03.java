package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;

import static zdream.nsfplayer.ftm.format.FtmStatic.OCTAVE_RANGE;


/**
 * 2A03 Instrumental Part
 *
 * @author Zdream
 */
public final class FtmInstrument2A03 extends AbstractFtmInstrument {

    @Override
    public FtmChipType instType() {
        return FtmChipType._2A03;
    }

    public int vol = -1;
    public int arp = -1;
    public int pit = -1;
    public int hip = -1;
    public int dut = -1;

    // Sampling related data
    public final FtmDPCMSample[][] samples = new FtmDPCMSample[OCTAVE_RANGE][12];
    public final byte[][] samplePitches = new byte[OCTAVE_RANGE][12];
    public final byte[][] sampleDeltas = new byte[OCTAVE_RANGE][12];

    /**
     * Set up sampling data
     */
    public void setSample(int octave, int pitchOfOctave, FtmDPCMSample sample, byte pitch, byte delta) {
        samples[octave][pitchOfOctave] = sample;
        samplePitches[octave][pitchOfOctave] = pitch;
        sampleDeltas[octave][pitchOfOctave] = delta;
    }

    /**
     * Set empty sampling
     */
    public void setEmptySample(int octave, int pitchOfOctave) {
        setSample(octave, pitchOfOctave, null, (byte) 0, (byte) -1);
    }

    /**
     * Get DMA Sample Instance
     *
     * @param octave
     * @param pitchOfOctave
     * @return
     */
    public FtmDPCMSample getSample(int octave, int pitchOfOctave) {
        return samples[octave][pitchOfOctave];
    }

    /**
     * Get DMA Sample Instance
     *
     * @param pitch range [1, 96]
     * @return
     */
    public FtmDPCMSample getSample(int pitch) {
        int i = pitch - 1;
        int octave = i / 12;
        int pitchOfOctave = i % 12;
        return getSample(octave, pitchOfOctave);
    }

    @Override
    public String toString() {
        return String.format("2A03 Instrument #%d %s", seq, name);
    }

    public int getSamplePitch(int pitch) {
        int i = pitch - 1;
        int octave = i / 12;
        int pitchOfOctave = i % 12;
        return samplePitches[octave][pitchOfOctave];
    }

    public int getSampleDelta(int pitch) {
        int i = pitch - 1;
        int octave = i / 12;
        int pitchOfOctave = i % 12;
        return sampleDeltas[octave][pitchOfOctave];
    }
}
