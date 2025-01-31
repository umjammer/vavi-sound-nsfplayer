package zdream.nsfplayer.ftm.audio;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfChannelCode;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;

import static java.lang.System.getLogger;
import static java.util.Objects.requireNonNull;


/**
 * Operator for FamiTracker data.
 * Each {@link FtmAudio} has and only has one unique FamiTrackerHandler.
 *
 * @author Zdream
 * @version 2018-04-25
 * @since v0.1
 */
public class FamiTrackerHandler implements INsfChannelCode {

    private static final Logger logger = getLogger(FamiTrackerHandler.class.getName());

    public final FtmAudio audio;

    public FamiTrackerHandler(FtmAudio audio) {
        requireNonNull(audio, "audio cannot be null");
        this.audio = audio;
    }

    //
    // parameter setting
    //

    /**
     * Setup system.
     *
     * @param m
     */
    public void setMechine(byte m) {
        if (m != 0 && m != 1) {
            throw new NsfPlayerException("system analysis error: " + m);
        }
        audio.region = (m == 0) ? ERegion.NTSC : ERegion.PAL;
    }

    /**
     * Set the engine refresh rate, default 0.
     * When set to 0, the <b>runtime</b> system determines the refresh rate based on the standard,
     * which is 60 for NTSC and 50 for PAL.
     *
     * @param fps valid value [0, 800]
     */
    public void setFramerate(int fps) {
        if (fps < 0 || fps > 800) {
            throw new NsfPlayerException("Refresh rate data error: " + fps);
        }
        audio.frameRate = fps;
    }

    /**
     * Setting the chip number
     *
     * @param c
     */
    public void setChip(byte c) {
        audio.useVrc6 = (c & 1) > 0;
        audio.useVrc7 = (c & 2) > 0;
        audio.useFds = (c & 4) > 0;
        audio.useMmc5 = (c & 8) > 0;
        audio.useN163 = (c & 16) > 0;
        audio.useS5b = (c & 32) > 0;
        logger.log(Level.INFO, "%s%s%s%s%s%s".formatted(audio.useVrc6 ? "6" : "_", audio.useVrc7 ? "7" : "_", audio.useFds ? "F" : "_", audio.useMmc5 ? "M" : "_", audio.useN163 ? "N" : "_", audio.useS5b ? "S" : "_"));
        new Exception().printStackTrace();
        channelDirt = true;
    }

    public void setVibrato(byte vibrato) {
        if (vibrato != 0 && vibrato != 1) {
            throw new NsfPlayerException("Vibrato mode error: " + vibrato);
        }
        audio.vibrato = vibrato;
    }

    /**
     * Setting the Number of N163 Channels.
     *
     * @param count valid value [0, 8]
     */
    public void setNamcoChannels(int count) {
        if (count < 0 || count > 8) {
            throw new NsfPlayerException("N163 Wrong number of channels: " + count);
        }
        audio.namcoChannels = count;

        channelDirt = true;
    }

    /**
     * Sets the split value for rhythm and tempo {@link FtmAudio#split}
     *
     * @param split
     */
    public void setSplit(int split) {
        audio.split = split;
    }

    /**
     * Sets the default split value for tempo and rhythm {@link FtmAudio#split}.
     *
     * @see FtmAudio#DEFAULT_SPEED_SPLIT
     */
    public void setDefaultSplit() {
        setSplit(FtmAudio.DEFAULT_SPEED_SPLIT);
    }

    //
    // Track
    //

    /**
     * Create a new track.
     *
     * @return New track.
     */
    public FtmTrack createTrack() {
        ArrayList<FtmTrack> tracks = audio.tracks;

        FtmTrack track = new FtmTrack();
        tracks.add(track);

        return track;
    }

    /**
     * Get the track of the specified index. If it doesn't exist, create one.
     *
     * @param index
     * @return
     */
    public FtmTrack getOrCreateTrack(int index) {
        ArrayList<FtmTrack> tracks = audio.tracks;

        if (index <= tracks.size()) {
            FtmTrack track = tracks.get(index);
            if (track == null) {
                track = new FtmTrack();
                tracks.set(index, track);
            }
            return track;
        } else {
            FtmTrack track = new FtmTrack();
            registerT(tracks, track, index);
            return track;
        }
    }

    /**
     * Get the pattern at the specified index. If it doesn't exist, create one.
     *
     * @param trackIdx   track number (of a song)
     * @param patternIdx pattern Serial number, i.e. order value,
     *                   {@link FtmTrack#orders} The data stored inside the pattern.
     * @param channelIdx channel number
     * @return
     */
    public FtmPattern getOrCreatePattern(int trackIdx, int patternIdx, int channelIdx) {
        if (channelIdx < 0 || channelIdx >= this.channelCount()) {
            throw new ArrayIndexOutOfBoundsException(String.format("channelIdx: %d out of range [0, %d)",
                    channelIdx, this.channelCount()));
        }

        FtmTrack track = getOrCreateTrack(trackIdx);
        return getOrCreatePattern(track, channelIdx, patternIdx);
    }

    /**
     * Get the pattern at the specified index. If it doesn't exist, create one.
     *
     * @param track      Examples of repertoire
     * @param patternIdx pattern Serial number, i.e. order value,
     *                   {@link FtmTrack#orders} The data stored inside the pattern.
     * @param channelIdx channel index
     * @return
     */
    public FtmPattern getOrCreatePattern(FtmTrack track, int patternIdx, int channelIdx) {
        if (track.patterns == null) {
            int newLen = Math.max(track.orders.length, patternIdx + 1);
            track.patterns = new FtmPattern[newLen][channelCount()];
        } else if (patternIdx >= track.patterns.length) {
            // expand capacity
            int newLen = patternIdx + 1;
            FtmPattern[][] oldps = track.patterns;
            track.patterns = new FtmPattern[newLen][];

            System.arraycopy(oldps, 0, track.patterns, 0, oldps.length);
            for (int i = oldps.length; i < newLen; i++) {
                track.patterns[i] = new FtmPattern[channelCount()];
            }
        }

        FtmPattern pattern = null;
        pattern = track.patterns[patternIdx][channelIdx];

        if (pattern == null) {
            track.patterns[patternIdx][channelIdx] = pattern = new FtmPattern();
        }
        return pattern;
    }

    /**
     * Gets the note at the specified index. If it doesn't exist, creates one.
     *
     * @param trackIdx   track number (of a song)
     * @param patternIdx pattern Serial number, i.e. order value, {@link FtmTrack#orders} The data stored inside the pattern.
     * @param channelIdx channel index
     * @param row        line number
     * @return
     */
    public FtmNote getOrCreateNote(
            int trackIdx,
            int patternIdx,
            int channelIdx,
            int row) {
        return getOrCreateNote(getOrCreateTrack(trackIdx), channelIdx, patternIdx, row);
    }

    /**
     * Gets the note at the specified index. If it doesn't exist, creates one.
     *
     * @param track      track number (of a song)
     * @param patternIdx pattern Serial number, i.e. order value, {@link FtmTrack#orders} The data stored inside the pattern.
     * @param channelIdx channel index
     * @param row        line number
     * @return
     */
    public FtmNote getOrCreateNote(
            FtmTrack track,
            int patternIdx,
            int channelIdx,
            int row) {
        FtmPattern pattern = getOrCreatePattern(track, channelIdx, patternIdx);

        return getOrCreateNote(pattern, row, track.length);
    }

    private static FtmNote getOrCreateNote(FtmPattern pattern, int row, int rowMax) {
        FtmNote[] notes = pattern.notes;
        if (pattern.notes == null) {
            notes = pattern.notes = new FtmNote[rowMax];
        }

        if (notes[row] == null) {
            notes[row] = new FtmNote();
        }
        return notes[row];
    }

    /*
     // channel
     */

    /**
     * <p>Whether or not the data related to the number of tracks has been modified.
     * <p>If it has been modified, it is necessary to recalculate the relevant data.
     */
    boolean channelDirt = true;

    /**
     * Total number of cache tracks
     */
    int channelCount = 0;

    /**
     * What is the track number corresponding to the nth track of the cache?
     */
    byte[] channelCode;

    /**
     * Recalculate the data related to the channel's sound source.
     */
    private void reScanChannel() {
        // Calculate the total number of channels
        channelCount = 5; // 2A03 + 2A07
        if (audio.useVrc6) {
            channelCount += 3;
        }
        if (audio.useVrc7) {
            channelCount += 6;
        }
        if (audio.useFds) {
            channelCount += 1;
        }
        if (audio.useMmc5) {
            channelCount += 2;
        }
        if (audio.useN163) {
            channelCount += audio.namcoChannels;
        }
        if (audio.useS5b) {
            channelCount += 3;
        }

        // Supplementary channel number
        channelCode = new byte[channelCount];
        int codePtr = 0;
        channelCode[codePtr++] = CHANNEL_2A03_PULSE1;
        channelCode[codePtr++] = CHANNEL_2A03_PULSE2;
        channelCode[codePtr++] = CHANNEL_2A03_TRIANGLE;
        channelCode[codePtr++] = CHANNEL_2A03_NOISE;
        channelCode[codePtr++] = CHANNEL_2A03_DPCM;

        if (audio.useVrc6) {
            channelCode[codePtr++] = CHANNEL_VRC6_PULSE1;
            channelCode[codePtr++] = CHANNEL_VRC6_PULSE2;
            channelCode[codePtr++] = CHANNEL_VRC6_SAWTOOTH;
        }
        if (audio.useMmc5) {
            channelCode[codePtr++] = CHANNEL_MMC5_PULSE1;
            channelCode[codePtr++] = CHANNEL_MMC5_PULSE2;
        }
        if (audio.useN163) {
            byte[] cs = new byte[] {
                    CHANNEL_N163_1,
                    CHANNEL_N163_2,
                    CHANNEL_N163_3,
                    CHANNEL_N163_4,
                    CHANNEL_N163_5,
                    CHANNEL_N163_6,
                    CHANNEL_N163_7,
                    CHANNEL_N163_8
            };
            int length = audio.namcoChannels;
            for (int i = 0; i < length; i++) {
                channelCode[codePtr++] = cs[i];
            }
        }
        if (audio.useFds) {
            channelCode[codePtr++] = CHANNEL_FDS;
        }
        if (audio.useVrc7) {
            channelCode[codePtr++] = CHANNEL_VRC7_FM1;
            channelCode[codePtr++] = CHANNEL_VRC7_FM2;
            channelCode[codePtr++] = CHANNEL_VRC7_FM3;
            channelCode[codePtr++] = CHANNEL_VRC7_FM4;
            channelCode[codePtr++] = CHANNEL_VRC7_FM5;
            channelCode[codePtr++] = CHANNEL_VRC7_FM6;
        }
        if (audio.useS5b) {
            channelCode[codePtr++] = CHANNEL_S5B_SQUARE1;
            channelCode[codePtr++] = CHANNEL_S5B_SQUARE2;
            channelCode[codePtr++] = CHANNEL_S5B_SQUARE3;
        }

        // close
        channelDirt = false;
    }

    /**
     * Calculate the sum of the orbital numbers
     *
     * @return
     */
    public int channelCount() {
        if (channelDirt) {
            reScanChannel();
        }

        return channelCount;
    }

    /**
     * Check the track number of the first orbit.
     *
     * @return
     */
    public byte channelCode(int channel) {
        if (channelDirt) {
            reScanChannel();
        }

        return channelCode[channel];
    }

    /**
     * Check the chip number of the track number of the first {@code channel}.
     *
     * @return The chip number to which the track belongs. If the track does not exist, return -1
     * @see NsfChannelCode#chipOfChannel(byte)
     * @since v0.2.7
     */
    public byte channelChip(int channel) {
        byte code = channelCode(channel);
        return NsfChannelCode.chipOfChannel(code);
    }

    /**
     * <p>Set the total number of tracks.
     * <p>if {@code size > original number of tracks} : add blank tracks, until the number of tracks is size.
     * <br>If {@code size <= number of original tracks} : do not operate
     */
    public void allocateTrack(int size) {
        int i = size - audio.getTrackCount();
        if (i > 0) {
            for (; i > 0; i--) {
                audio.tracks.add(new FtmTrack());
            }
        }
    }

    /*
     * Instrument Sequence
     */

    /**
     * Obtain 2A03 sequence data
     */
    public FtmSequence getSequence2A03(FtmSequenceType type, int index) {
        int key = FtmChipType._2A03.ordinal() * FtmSequenceType.values().length + type.ordinal();
        ArrayList<FtmSequence> list = audio.seqs.get(key);
        if (list == null) {
            return null;
        }

        if (index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * Obtain VRC6 sequence data
     */
    public FtmSequence getSequenceVRC6(FtmSequenceType type, int index) {
        int key = FtmChipType.VRC6.ordinal() * FtmSequenceType.values().length + type.ordinal();
        ArrayList<FtmSequence> list = audio.seqs.get(key);
        if (list == null) {
            return null;
        }

        if (index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * <p>Get the sequence. If it doesn't exist, create one.
     * <p>Support for 2A03, VRC6 and N163 chips
     * </p>
     */
    public FtmSequence getOrCreateSequence(FtmChipType chip, FtmSequenceType type, int index) {
        return switch (chip) {
            case _2A03, VRC6, N163 -> getOrCreateSequence0(chip, type, index);
            default -> null;
        };
    }

    private FtmSequence getOrCreateSequence0(FtmChipType chip, FtmSequenceType type, int index) {
        int key = chip.ordinal() * FtmSequenceType.values().length + type.ordinal();
        ArrayList<FtmSequence> list = audio.seqs.get(key);
        if (list == null) {
            list = new ArrayList<>();
            audio.seqs.put(key, list);
        }

        FtmSequence seq = null;
        if (index < list.size()) {
            seq = list.get(index);
        }

        if (seq == null) {
            seq = new FtmSequence(type);
            registerT(list, seq, index);
        }
        return seq;
    }

    /**
     * Registered Musical Instruments
     */
    public void registerInstrument(AbstractFtmInstrument inst) {
        int index = inst.seq;
        ArrayList<AbstractFtmInstrument> list = audio.insts;
        registerT(list, inst, index);
    }

    /**
     * <p>Get the FDS instrument with the specified instrument serial number.
     * <li>If there is no instrument instance for this instrument serial number, create one and return;
     * if there is no instrument instance for this instrument serial number, then create one.
     * <li>Returns null if the instrument instance for which this instrument number exists is not an FDS.
     * </li>
     * </p>
     *
     * @param index Instrument number. This value needs to be a non-negative number
     * @return FDS Instrument instance, or null
     * @since v0.2.5
     */
    public FtmInstrumentFDS getOrCreateInstrumentFDS(int index) {
        ArrayList<AbstractFtmInstrument> list = audio.insts;
        AbstractFtmInstrument inst;
        if (index < list.size()) {
            inst = list.get(index);
        } else {
            inst = null;
        }

        if (inst != null) {
            return (inst.instType() == FtmChipType.FDS) ? (FtmInstrumentFDS) inst : null;
        }

        FtmInstrumentFDS instfds = new FtmInstrumentFDS();
        instfds.seq = index;
        registerInstrument(instfds);

        return instfds;
    }

    /**
     * <p>Get the N163 instrument with the specified instrument serial number.
     * <li>If there is no instrument instance for this instrument serial number, create one and return;
     * if there is no instrument instance for this instrument serial number, then create one.
     * <li>Returns null if the instrument instance for which this instrument number exists is not N163.
     * </li>
     * </p>
     *
     * @param index Instrument number. This value needs to be a non-negative number
     * @return N163 Instrument instance, or null
     * @since v0.2.6
     */
    public FtmInstrumentN163 getOrCreateInstrumentN163(int index) {
        ArrayList<AbstractFtmInstrument> list = audio.insts;
        AbstractFtmInstrument inst;
        if (index < list.size()) {
            inst = list.get(index);
        } else {
            inst = null;
        }

        if (inst != null) {
            return (inst.instType() == FtmChipType.N163) ? (FtmInstrumentN163) inst : null;
        }

        FtmInstrumentN163 instNamco = new FtmInstrumentN163();
        instNamco.seq = index;
        registerInstrument(instNamco);

        return instNamco;
    }

    /**
     * Registered Sampling
     *
     * @param index
     * @return
     */
    public FtmDPCMSample getOrCreateDPCMSample(int index) {
        ArrayList<FtmDPCMSample> list = audio.samples;
        FtmDPCMSample sample = null;

        if (list.size() > index) {
            sample = list.get(index);
        }

        if (sample == null) {
            sample = new FtmDPCMSample();
            registerT(list, sample, index);
        }

        return sample;
    }

    private <T> void registerT(ArrayList<T> list, T t, int index) {
        int size = list.size();
        int d = index - size;
        if (d < 0) {
            list.set(index, t);
            return;
        }

        // Swings the sequence to the position specified by index, with null in between.
        while (d > 0) {
            list.add(null);
            d--;
        }
        list.add(t);
    }
}
