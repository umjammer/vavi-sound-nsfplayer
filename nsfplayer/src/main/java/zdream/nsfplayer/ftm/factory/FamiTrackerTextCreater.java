package zdream.nsfplayer.ftm.factory;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.utils.common.TextReader;

import static java.lang.System.getLogger;
import static zdream.nsfplayer.core.FtmChipType.N163;
import static zdream.nsfplayer.core.FtmChipType.VRC6;
import static zdream.nsfplayer.core.FtmChipType._2A03;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_DPCM;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_NOISE;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_TYPE_FDS;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_TYPE_PULSE;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_TYPE_S5B;
import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;
import static zdream.nsfplayer.ftm.format.FtmNote.*;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.ARPEGGIO;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.DUTY;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.HI_PITCH;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.PITCH;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.VOLUME;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_VOLUMN;
import static zdream.utils.common.CodeSpliter.extract;
import static zdream.utils.common.CodeSpliter.split;


/**
 * <p>Used to populate {@link FtmAudio} data from the exported text file (.txt) of
 * FamiTracker using {@link FamiTrackerHandler}
 * <p>An instance of this creator can only be populated with data for one {@link FtmAudio}.
 * If you want to populate more {@link FtmAudio}, please create more instances of this creator.
 * </p>
 *
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerTextCreater extends AbstractFamiTrackerCreater<TextReader> {

    private static final Logger logger = getLogger(FamiTrackerTextCreater.class.getName());

    /**
     * Text Reader
     */
    TextReader reader;

    // Cache Values

    /**
     * The track number being parsed
     */
    int curTrackIdx = -1;

    /**
     * Tracks being parsed
     */
    FtmTrack curTrack;

    /**
     * The number of effect columns for each column
     */
    int[] columns;

    /**
     * The column numbers in txt correspond to the track numbers in FtmAudio.
     * When N163 tracks are included, FTM will ignore the namco track number parameter and
     * directly write the data of 8 tracks. So this mapping is needed
     *
     * @since v0.2.6
     */
    int[] channelIndexs;

    /**
     * order List
     */
    ArrayList<int[]> orders;

    /**
     * The pattern part
     */
    int patternIdx = -1;

    /**
     * Number of rows
     */
    int rowIdx = 0;

    /**
     * Others are 0
     * 1 when parsing ORDER
     * 2 when parse PATTERN(ROW)
     * 3 when parsing TRACK
     * -1 at the end
     */
    int status = 0;

    /**
     * pattern group number - a group of pattern
     */
    HashMap<Integer, FtmPattern[]> patterns;

    /**
     * The maximum pattern group number
     */
    int maxPatternIdx;

    /**
     * The current pattern group
     */
    FtmPattern[] curPatternGroup;

    /*
     * DPCM data read temporary status
     *
     * Sequence: DPCM DEF -> DPCM -> DPCM ... DPCM -> Others
     */

    /**
     * The sampling data of DPCM currently read
     */
    byte[] dpcmBytes;

    /**
     * The position of the DPCM currently being read, equivalent to the index pointing to dpcmBytes
     */
    int dpcmOffset;

    public FamiTrackerTextCreater() {
        // do nothing
    }

    /**
     * Generate {@link FtmAudio} based on text content
     */
    @Override
    public void doCreate(TextReader reader, FamiTrackerHandler doc) throws FamiTrackerFormatException {
        this.reader = reader;

        if (!reader.isFinished()) {
            while (reader.toNextValidLine() > 0) {
                handleLine(reader, doc);
            }
        }

        statusChange(-1, doc);
        reader.close();
    }

    private void handleLine(TextReader reader, FamiTrackerHandler doc) {
        String[] strs = split(reader.thisLine());

        switch (strs[0]) {

            // Song information
            case "TITLE": {
                doc.audio.title = strs[1];
            }
            break;

            case "AUTHOR": {
                doc.audio.author = strs[1];
            }
            break;

            case "COPYRIGHT": {
                doc.audio.copyright = strs[1];
            }
            break;

            // Global settings
            case "MACHINE": {
                doc.setMechine(Byte.parseByte(strs[1]));
            }
            break;

            case "FRAMERATE": {
                doc.setFramerate(Integer.parseInt(strs[1]));
            }
            break;

            case "EXPANSION": {
                doc.setChip(Byte.parseByte(strs[1]));
            }
            break;

            case "VIBRATO": {
                doc.setVibrato(Byte.parseByte(strs[1]));
            }
            break;

            case "SPLIT": {
                doc.setSplit(Integer.parseInt(strs[1]));
            }
            break;

            case "N163CHANNELS": {
                doc.setNamcoChannels(Integer.parseInt(strs[1]));
            }
            break;

            // Macros
            case "MACRO": { // It is sequence
                parseMacro(reader, doc, strs);
            }
            break;

            case "MACROVRC6": {
                parseMacroVRC6(reader, doc, strs);
            }
            break;

            case "MACRON163": {
                parseMacroN163(reader, doc, strs);
            }
            break;

            // DPCM samples
            case "DPCMDEF": {
                parseDPCMDefine(reader, doc, strs);
            }
            break;

            case "DPCM": {
                parseDPCM(reader, doc, strs);
            }
            break;

            // Instruments
            case "INST2A03": {
                parseInst2A03(reader, doc, strs);
            }
            break;

            case "KEYDPCM": {
                parseInstKeyDPCM(reader, doc, strs);
            }
            break;

            case "INSTVRC6": {
                parseInstVRC6(reader, doc, strs);
            }
            break;

            case "FDSWAVE": {
                parseFDSWave(reader, doc, strs);
            }
            break;

            case "FDSMOD": {
                parseFDSMod(reader, doc, strs);
            }
            break;

            case "FDSMACRO": {
                parseFDSMacro(reader, doc, strs);
            }
            break;

            case "INSTN163": {
                parseInstN163(reader, doc, strs);
            }
            break;

            case "N163WAVE": {
                parseN163Wave(reader, doc, strs);
            }
            break;

            case "INSTVRC7": {
                parseInstVRC7(reader, doc, strs);
            }
            break;

            // Tracks
            case "TRACK": {
                parseTrack(reader, doc, strs);
            }
            break;

            case "COLUMNS": {
                parseColumns(reader, doc, strs);
            }
            break;

            case "ORDER": {
                parseOrder(reader, doc, strs);
            }
            break;

            case "PATTERN": {
                parsePattern(reader, doc, strs);
            }
            break;

            case "ROW": {
                parseRow(reader, doc, strs);
            }
            break;

            default:
logger.log(Level.WARNING, "unhandled: " + strs[0]);
                break;
        }
    }

    /**
     * <p>Parse the Macro part, that is, the sequence part
     * <p>Example:
     * <blockquote><pre>
     *     MACRO       0   4  -1   3   0 : 9 5 3 2 1 1 0
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     MACRO &lt;Type&gt; &lt;Order Number&gt; &lt;Loop Point Position&gt; &lt;Release Point Position&gt; &lt;Auxiliary Parameters&gt; : &lt;Sequence&gt; ...
     * </pre></blockquote>
     * Auxiliary parameters, see {@link FtmSequence#settings}
     * </p>
     *
     * @since v0.2.5
     */
    private void parseMacro(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length < 7) {
            handleException(reader, EX_MACRO_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[6])) {
            handleException(reader, EX_MACRO_WRONG_TOKEN);
        }

        parseMacro0(reader, doc, strs, _2A03);
    }

    /**
     * <p>Parse the MacroVRC6 part, that is, the VRC6 sequence part
     * <p>Example:
     * <blockquote><pre>
     *     MACROVRC6   0   1  -1   1   0 : 11 9 5 2 0
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     MACRO &lt;Type&gt; &lt;Order Number&gt; &lt;Loop Point Position&gt; &lt;Release Point Position&gt; &lt;Auxiliary Parameters&gt; : &lt;Sequence&gt; ...
     * </pre></blockquote>
     * Auxiliary parameters, see {@link FtmSequence#settings}
     * <p>As you can see, there is basically no difference from Macro
     * </p>
     *
     * @since v0.2.5
     */
    private void parseMacroVRC6(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length < 7) {
            handleException(reader, EX_MACROVRC6_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[6])) {
            handleException(reader, EX_MACROVRC6_WRONG_TOKEN);
        }

        parseMacro0(reader, doc, strs, VRC6);
    }

    /**
     * <p>Parse the MacroN163 part, that is, the N163 sequence part
     * <p>Example:
     * <blockquote><pre>
     *     MACRON163   0   4  -1  -1   0 : 15 13 12
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     MACRON163 &lt;Type&gt; &lt;Serial number&gt; &lt;Loop point position&gt; &lt;Release point position&gt; &lt;Auxiliary parameters&gt; : &lt;Sequence&gt; ...
     * </pre></blockquote>
     * Auxiliary parameters, see {@link FtmSequence#settings}
     * <p>As you can see, there is basically no difference from Macro
     * </p>
     *
     * @since v0.2.6
     */
    private void parseMacroN163(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length < 7) {
            handleException(reader, EX_MACRON163_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[6])) {
            handleException(reader, EX_MACRON163_WRONG_TOKEN);
        }

        parseMacro0(reader, doc, strs, N163);
    }

    private void parseMacro0(TextReader reader, FamiTrackerHandler doc, String[] strs, FtmChipType chip) {
        int type = Integer.parseInt(strs[1]);
        int index = Integer.parseInt(strs[2]);
        int loop = Integer.parseInt(strs[3]);
        int release = Integer.parseInt(strs[4]);
        int settings = Integer.parseInt(strs[5]);

        FtmSequence seq = doc.getOrCreateSequence(chip, FtmSequenceType.get(type), index);
        seq.loopPoint = loop;
        seq.releasePoint = release;
        seq.settings = (byte) settings;

        // Sequence Storage
        int length = strs.length - 7;
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = Byte.parseByte(strs[i + 7]);
        }
        seq.data = data;
    }

    /**
     * <p>Parse the DPCMDEF part, which is the DPCM define part
     * <p>Example:
     * <blockquote><pre>
     *     DPCMDEF   0   897 "fsharp"
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     DPCMDEF &lt;Sequence Number&gt; &lt;Data Size&gt; &lt;Name&gt;
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.5
     */
    private void parseDPCMDefine(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 4) {
            handleException(reader, EX_DPCMDEF_WRONG_ITEMS, strs.length);
        }

        int index = Integer.parseInt(strs[1]);
        int length = Integer.parseInt(strs[2]);

        FtmDPCMSample dsample = doc.getOrCreateDPCMSample(index);
        dsample.data = this.dpcmBytes = new byte[length];
        dsample.name = extract(strs[3]);
        dpcmOffset = 0;
    }

    /**
     * <p>Parsing DPCM part, that is, DPCM sampling data part
     * <p>Example:
     * <blockquote><pre>
     *     DPCM : D5 FF FD 00 00 FF 01 1C 01 F0 E7 0F 00 FE FF 03 00 80 57 FF F1 FD 0F 00 40 FC FF FF 01 FE 08 80
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     DPCM : &lt;Sampling Data&gt; ...
     * </pre></blockquote>
     * The number of sampled data is in the range of [1, 32], and is presented in the form of hexadecimal text.
     * </p>
     *
     * @since v0.2.5
     */
    private void parseDPCM(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (!":".equals(strs[1])) {
            handleException(reader, EX_DPCM_WRONG_TOKEN, strs.length);
        }

        for (int i = 2; i < strs.length; i++) {
            this.dpcmBytes[this.dpcmOffset++] = (byte) Integer.parseInt(strs[i], 16);
        }
    }

    private void parseInst2A03(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 8) {
            handleException(reader, EX_INST2A03_WRONG_ITEMS, strs.length);
        }

        FtmInstrument2A03 inst = new FtmInstrument2A03();
        inst.seq = Integer.parseInt(strs[1]);
        inst.name = extract(strs[7]);

        inst.vol = Integer.parseInt(strs[2]);
        if (inst.vol != -1) {
            doc.getOrCreateSequence(_2A03, VOLUME, inst.vol);
        }

        inst.arp = Integer.parseInt(strs[3]);
        if (inst.arp != -1) {
            doc.getOrCreateSequence(_2A03, ARPEGGIO, inst.arp);
        }

        inst.pit = Integer.parseInt(strs[4]);
        if (inst.pit != -1) {
            doc.getOrCreateSequence(_2A03, PITCH, inst.pit);
        }

        inst.hip = Integer.parseInt(strs[5]);
        if (inst.hip != -1) {
            doc.getOrCreateSequence(_2A03, HI_PITCH, inst.hip);
        }

        inst.dut = Integer.parseInt(strs[6]);
        if (inst.dut != -1) {
            doc.getOrCreateSequence(_2A03, DUTY, inst.dut);
        }

        doc.registerInstrument(inst);
    }

    /**
     * <p>Parse the KEYDPCM part, which is the DPCM parameter part of the 2A03 Instrument
     * <p>Example:
     * <blockquote><pre>
     *     KEYDPCM   2   3   2     3  15   0     0  -1
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     KEYDPCM &lt;Instrument number&gt; &lt;Scale&gt; &lt;Pitch&gt; &lt;DPCM sample number&gt;
     *     &lt;Sample pitch&gt; &lt;Whether to loop&gt; &lt;Position parameter&gt; &lt;Sample delta value&gt;
     * </pre></blockquote>
     * Pitch refers to pitchOfOctave;
     * <br>Whether to loop, 1 for loop, 0 for no loop
     * <br>Sample pitch, see {@link FtmInstrument2A03#samplePitches}
     * <br>Sample delta values, see {@link FtmInstrument2A03#sampleDeltas}
     * </p>
     *
     * @since v0.2.5
     */
    private void parseInstKeyDPCM(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 9) {
            handleException(reader, EX_KEYDPCM_WRONG_ITEMS, strs.length);
        }

        int index = Integer.parseInt(strs[1]);
        int octave = Integer.parseInt(strs[2]);
        int pitchOfOctave = Integer.parseInt(strs[3]);
        int sIndex = Integer.parseInt(strs[4]);
        byte samplePitch = Byte.parseByte(strs[5]);
        boolean loop = Integer.parseInt(strs[6]) != 0;
        // strs[7] ignored
        byte sampleDelta = Byte.parseByte(strs[8]);

        FtmInstrument2A03 inst = (FtmInstrument2A03) doc.audio.getInstrument(index);
        FtmDPCMSample sample = doc.getOrCreateDPCMSample(sIndex);

        inst.samples[octave][pitchOfOctave] = sample;
        inst.samplePitches[octave][pitchOfOctave] = (byte) (samplePitch | (loop ? 0x80 : 0));
        inst.sampleDeltas[octave][pitchOfOctave] = sampleDelta;
    }

    /**
     * <p>Analyze the InstVRC6 part, which is the VRC6 instrument part
     * <p>Example:
     * <blockquote><pre>
     *     INSTVRC6   7     1  -1  -1  -1   0 "VRC6 lead 1"
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     INSTVRC6 &lt;Serial Number&gt; &lt;Volume Serial Number&gt; &lt;Arpeggio Serial Number&gt; &lt;Pitch Serial Number&gt;
     *     &lt;Large Pitch Serial Number&gt; &lt;Tone Serial Number&gt; &lt;Instrument Name&gt;
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.5
     */
    private void parseInstVRC6(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 8) {
            handleException(reader, EX_INSTVRC6_WRONG_ITEMS, strs.length);
        }

        FtmInstrumentVRC6 inst = new FtmInstrumentVRC6();
        inst.seq = Integer.parseInt(strs[1]);
        inst.name = strs[7];

        inst.vol = Integer.parseInt(strs[2]);
        if (inst.vol != -1) {
            doc.getOrCreateSequence(VRC6, VOLUME, inst.vol);
        }

        inst.arp = Integer.parseInt(strs[3]);
        if (inst.arp != -1) {
            doc.getOrCreateSequence(VRC6, ARPEGGIO, inst.arp);
        }

        inst.pit = Integer.parseInt(strs[4]);
        if (inst.pit != -1) {
            doc.getOrCreateSequence(VRC6, PITCH, inst.pit);
        }

        inst.hip = Integer.parseInt(strs[5]);
        if (inst.hip != -1) {
            doc.getOrCreateSequence(VRC6, HI_PITCH, inst.hip);
        }

        inst.dut = Integer.parseInt(strs[6]);
        if (inst.dut != -1) {
            doc.getOrCreateSequence(VRC6, DUTY, inst.dut);
        }

        doc.registerInstrument(inst);
    }

    /**
     * <p>Parsing the FDSWave part, that is, the FDS volume envelope data part
     * <p>Example:
     * <blockquote><pre>
     *     FDSWAVE    2 : 35 39 42 41 39 45 48 45 42 45 46 ...
     * </pre></blockquote>
     * Note: The number of digits after the colon is fixed at 64.
     * <br>The meaning of each parameter is:
     * <blockquote><pre>
     *     FDSWAVE &lt;instrument number&gt; : &lt;64 envelope values&gt;
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.5
     */
    private void parseFDSWave(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 67) {
            handleException(reader, EX_FDSWAVE_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[2])) {
            handleException(reader, EX_FDSWAVE_WRONG_TOKEN);
        }

        int index = Integer.parseInt(strs[1]);
        FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);
        byte[] bs = instfds.samples;

        for (int i = 0; i < bs.length; i++) {
            bs[i] = Byte.parseByte(strs[i + 3]);
        }
    }

    /**
     * <p>Parse the FDSMod part, which is the FDS modulation data part
     * <p>Example:
     * <blockquote><pre>
     *     FDSMOD    2 : 35 39 42 41 39 45 48 45 42 45 46 ...
     * </pre></blockquote>
     * Note: The number of digits after the colon is fixed at 32.
     * <br>The meaning of each parameter is:
     * <blockquote><pre>
     *     FDSMOD &lt;instrument number&gt; : &lt;32 modulation values&gt;
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.5
     */
    private void parseFDSMod(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 35) {
            handleException(reader, EX_FDSMOD_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[2])) {
            handleException(reader, EX_FDSMOD_WRONG_TOKEN);
        }

        int index = Integer.parseInt(strs[1]);
        FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);
        byte[] bs = instfds.modulation;

        for (int i = 0; i < bs.length; i++) {
            bs[i] = Byte.parseByte(strs[i + 3]);
        }
    }

    /**
     * <p>Parse the FDSMacro part, that is, the FDS sequence part
     * <p>Example:
     * <blockquote><pre>
     *     FDSMACRO   2   0  -1  -1   0 : 24 18 16 14 13 12
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     MACRO &lt;Type&gt; &lt;Order Number&gt; &lt;Loop Point Position&gt; &lt;Release Point Position&gt; &lt;Auxiliary Parameters&gt; : &lt;Sequence&gt; ...
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.5
     */
    private void parseFDSMacro(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (!":".equals(strs[6])) {
            handleException(reader, EX_FDSMACRO_WRONG_TOKEN);
        }

        int index = Integer.parseInt(strs[1]);
        FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);

        int type = Integer.parseInt(strs[2]);
        FtmSequence seq = new FtmSequence(FtmSequenceType.get(type));
        seq.loopPoint = Integer.parseInt(strs[3]);
        seq.releasePoint = Integer.parseInt(strs[4]);
        seq.settings = Byte.parseByte(strs[5]);

        // Sequence Storage
        int length = strs.length - 7;
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = Byte.parseByte(strs[i + 7]);
        }
        seq.data = data;

        switch (type) {
            case 0:
                instfds.seqVolume = seq;
                break;
            case 1:
                instfds.seqArpeggio = seq;
                break;
            case 2:
                instfds.seqPitch = seq;
                break;
        }
    }

    /**
     * <p>Parsing INSTN163 part, which is the instrument part of N163
     * <p>Example:
     * <blockquote><pre>
     *     INSTN163   6     0  -1  -1  -1  -1  32  96   1 "Choir"
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     INSTN163 &lt;Serial Number&gt;
     *     &lt;Volume Serial Number&gt; &lt;Arpeggio Serial Number&gt; &lt;Pitch Serial Number&gt; &lt;Large Pitch Serial Number&gt; &lt;Tone Serial Number&gt;
     *     &lt;Waveform length&gt; &lt;Waveform position&gt; &lt;Number of waveforms&gt; &lt;Instrument name&gt;
     * </pre></blockquote>
     * </p>
     *
     * @since v0.2.6
     */
    private void parseInstN163(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 11) {
            handleException(reader, EX_INSTN163_WRONG_ITEMS, strs.length);
        }

        int seq = Integer.parseInt(strs[1]);
        FtmInstrumentN163 inst = doc.getOrCreateInstrumentN163(seq);
        inst.name = strs[10];

        inst.vol = Integer.parseInt(strs[2]);
        if (inst.vol != -1) {
            doc.getOrCreateSequence(N163, VOLUME, inst.vol);
        }

        inst.arp = Integer.parseInt(strs[3]);
        if (inst.arp != -1) {
            doc.getOrCreateSequence(N163, ARPEGGIO, inst.arp);
        }

        inst.pit = Integer.parseInt(strs[4]);
        if (inst.pit != -1) {
            doc.getOrCreateSequence(N163, PITCH, inst.pit);
        }

        inst.hip = Integer.parseInt(strs[5]);
        if (inst.hip != -1) {
            doc.getOrCreateSequence(N163, HI_PITCH, inst.hip);
        }

        inst.dut = Integer.parseInt(strs[6]);
        if (inst.dut != -1) {
            doc.getOrCreateSequence(N163, DUTY, inst.dut);
        }

        int waveSize = Integer.parseInt(strs[7]);
        int waveCount = Integer.parseInt(strs[9]);
        inst.wavePos = Integer.parseInt(strs[8]);

        inst.waves = new byte[waveCount][waveSize];
        doc.registerInstrument(inst);
    }

    /**
     * <p>Parsing N163WAVE part, that is, N163 waveform (envelope) data part
     * <p>Example:
     * <blockquote><pre>
     *     N163WAVE   6   0 : 4 2 2 2 1 1 0 0 1 5 8 11 13 14 15 15 15 14 13 10 7 3 1 1 0 0 0 0 1 2 4 5
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     N163WAVE &lt;Instrument Number&gt; &lt;Waveform Number&gt; : &lt;Waveform Data&gt; ...
     * </pre></blockquote>
     * The number of waveform data varies, usually 32
     * </p>
     *
     * @since v0.2.6
     */
    private void parseN163Wave(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
        if (strs.length < 5) {
            handleException(reader, EX_N163WAVE_WRONG_ITEMS, strs.length);
        }
        if (!":".equals(strs[3])) {
            handleException(reader, EX_FDSMACRO_WRONG_TOKEN);
        }

        int instSeq = Integer.parseInt(strs[1]);
        int waveSeq = Integer.parseInt(strs[2]);

        FtmInstrumentN163 inst = doc.getOrCreateInstrumentN163(instSeq);

        // Before this function is run, parseInstN163 must have been run, so inst.waves must not be null
        byte[] waves = inst.waves[waveSeq];
        for (int i = 0; i < waves.length; i++) {
            waves[i] = Byte.parseByte(strs[i + 4]);
        }
    }

    /**
     * <p>Parse the INSTVRC7 part, which is the VRC7 instrument data part
     * <p>Example:
     * <blockquote><pre>
     *     INSTVRC7   5     0 01 00 70 1A 12 20 8F F0 "intro"
     * </pre></blockquote>
     * The meaning of each parameter is:
     * <blockquote><pre>
     *     INSTVRC7 &lt;Instrument number&gt; &lt;patch number&gt; &lt;8 instrument parameters&gt; &lt;Instrument name&gt;
     * </pre></blockquote>
     * There are 8 instrument parameters, written in hexadecimal format.
     * </p>
     *
     * @since v0.2.7
     */
    private void parseInstVRC7(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 12) {
            handleException(reader, EX_INSTVRC7_WRONG_ITEMS, strs.length);
        }

        FtmInstrumentVRC7 inst = new FtmInstrumentVRC7();
        inst.seq = Integer.parseInt(strs[1]);
        inst.name = strs[11];
        inst.patchNum = Integer.parseInt(strs[2]);

        for (int i = 0; i < 8; i++) {
            inst.regs[i] = Short.parseShort(strs[3 + i], 16);
        }

        doc.registerInstrument(inst);
    }

    private void parseTrack(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 5) {
            handleException(reader, EX_TRACK_WRONG_ITEMS, strs.length);
        }

        statusChange(3, doc);

        this.curTrackIdx++;
        FtmTrack track = doc.createTrack();
        curTrack = track;

        track.length = Integer.parseInt(strs[1]);
        track.speed = Integer.parseInt(strs[2]);
        track.tempo = Integer.parseInt(strs[3]);
        track.name = strs[4];

        orders = new ArrayList<>();
        patternIdx = -1;
    }

    private void parseColumns(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (!":".equals(strs[1])) {
            handleException(reader, EX_COLUMNS_WRONG_TOKEN);
        }

        columns = new int[strs.length - 2];
        int length = strs.length - 2;
        for (int i = 0; i < length; i++) {
            columns[i] = Integer.parseInt(strs[i + 2]);
        }

        // N163
        // If the txt file contains the N163 chip, all 8 tracks will be written.
        // Therefore, some processing is required here.
        channelIndexs = new int[columns.length];
        if (doc.audio.isUseN163()) {
            int namcoCount = doc.audio.getNamcoChannels();
            int namco1Index = -1;
            int i = 5;
            for (; i < columns.length; i++) {
                if (doc.channelCode(i) == CHANNEL_N163_1) {
                    namco1Index = i;
                    break;
                }
            }

            int end = namco1Index + namcoCount;
            for (i = 0; i < end; i++) {
                channelIndexs[i] = i;
            }
            int nextVal = i; // i = end

            end = namco1Index + 8;
            for (; i < end; i++) {
                channelIndexs[i] = -1;
            }
            for (; i < channelIndexs.length; i++, nextVal++) {
                channelIndexs[i] = nextVal;
            }
        } else {
            for (int i = 0; i < channelIndexs.length; i++) {
                channelIndexs[i] = i;
            }
        }
    }

    private void parseOrder(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 3 + columns.length) {
            handleException(reader, EX_OREDR_WRONG_ITEMS, 3 + columns.length, strs.length);
        }
        if (!":".equals(strs[2])) {
            handleException(reader, EX_OREDR_WRONG_TOKEN);
        }

        int index = Integer.parseInt(strs[1], 16);
        if (index != orders.size()) {
            handleException(reader, EX_OREDR_WRONG_OREDR_NO, index, orders.size());
        }

        statusChange(1, doc);

        int[] order = new int[columns.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = Integer.parseInt(strs[3 + i], 16);
        }
        orders.add(order);
    }

    private void parsePattern(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        if (strs.length != 2) {
            handleException(reader, EX_PATTERN_WRONG_TOKEN);
        }

        statusChange(2, doc);

        this.patternIdx = Integer.parseInt(strs[1], 16);
        if (patternIdx > maxPatternIdx) {
            maxPatternIdx = patternIdx;
        }
        // Package this PATTERN group
        this.patterns.put(this.patternIdx, curPatternGroup);

        rowIdx = 0; // Rows are counted starting from 0.
    }

    private void parseRow(TextReader reader, FamiTrackerHandler doc, String[] strs) {
        // Estimated array length of strs
        int lenExp = 2;
        for (int j : columns) {
            lenExp += (4 + j);
        }

        if (strs.length != lenExp) {
            handleException(reader, EX_ROW_WRONG_ITEMS, lenExp, strs.length);
        }

        // Number of rows
        int row = Integer.parseInt(strs[1], 16);
        if (row != rowIdx) {
            handleException(reader, EX_ROW_WRONG_ROW_NO, row, rowIdx);
        }

        // Analysis
        int offset = 2;
        for (int column = 0; column < columns.length; column++) {
            int length = 4 + columns[column];

            // realColume is the number of columns in FtmAudio. When there is an N163 track,
            // realColume may not be equal to column
            int realColume = channelIndexs[column];
            if (realColume != -1) {
                parseColumnInRow(reader, doc, strs, realColume, offset, length);
            }

            offset += length;
        }

        rowIdx++;
    }

    /**
     * Parse a note section, generate {@link FtmNote} and save it in the doc section
     *
     * @param reader
     * @param doc
     * @param strs
     * @param column Column
     * @param offset The strs input into this column starts from which element
     * @param length The length of strs belonging to this column, at least 5
     *              <br>(the first is ':', the second is the pitch, the third is the instrument,
     *              the fourth is the volume, and the fifth and subsequent ones are effects)
     */
    private void parseColumnInRow(
            TextReader reader,
            FamiTrackerHandler doc,
            String[] strs,
            int column,
            int offset,
            int length) {
        // The first element must be ":"
        if (!":".equals(strs[offset])) {
            handleException(reader, EX_ROW_WRONG_TOKEN_IN_COLUMN, column);
        }

        boolean empty = true;
        FtmNote note = new FtmNote();

        // Tone Part
        String t = strs[offset + 1];

        if ("...".equals(t)) {
            note.note = FtmNote.NOTE_NONE;
            note.octave = 0;
        } else if ("---".equals(t)) {
            note.note = FtmNote.NOTE_HALT;
            note.octave = 0;
            empty = false;
        } else if ("===".equals(t)) {
            note.note = FtmNote.NOTE_RELEASE;
            note.octave = 0;
            empty = false;
        } else {
            if (doc.channelCode(column) == CHANNEL_2A03_NOISE) {
                parseNoiseNote(t, note);
            } else {
                parseAudioNote(t, note);
            }
            empty = false;
        }

        // Instrumental Part
        t = strs[offset + 2];
        if ("..".equals(t)) {
            note.instrument = MAX_INSTRUMENTS;
        } else {
            note.instrument = Integer.parseInt(t, 16);
            empty = false;
        }

        // Volume section
        t = strs[offset + 3];
        if (".".equals(t)) {
            note.vol = MAX_VOLUMN;
        } else {
            note.vol = Byte.parseByte(t, 16);
            empty = false;
        }

        // Effect part
        byte channelCode = doc.channelCode(column);
        for (int idx = 4; idx < length; idx++) {
            t = strs[offset + idx];
            empty &= parseEffect(t, note, idx - 4, channelCode);
        }

        if (!empty)
            this.curPatternGroup[column].notes[rowIdx] = note;
    }

    /**
     * Analysis of the Tone and Scale of the Non-Noise Part
     */
    private void parseAudioNote(String text, FtmNote note) {
        String tt = text.substring(0, 2);
        switch (tt) {
            case "C-":
                note.note = FtmNote.NOTE_C;
                break;
            case "C#":
                note.note = FtmNote.NOTE_CS;
                break;
            case "D-":
                note.note = FtmNote.NOTE_D;
                break;
            case "D#":
                note.note = FtmNote.NOTE_DS;
                break;
            case "E-":
                note.note = FtmNote.NOTE_E;
                break;
            case "F-":
                note.note = FtmNote.NOTE_F;
                break;
            case "F#":
                note.note = FtmNote.NOTE_FS;
                break;
            case "G-":
                note.note = FtmNote.NOTE_G;
                break;
            case "G#":
                note.note = FtmNote.NOTE_GS;
                break;
            case "A-":
                note.note = FtmNote.NOTE_A;
                break;
            case "A#":
                note.note = FtmNote.NOTE_AS;
                break;
            case "B-":
                note.note = FtmNote.NOTE_B;
                break;

            default:
                handleException(reader, EX_ROW_WRONG_NOTE, tt);
        }

        // scale
        byte octave = Byte.parseByte(text.substring(2));
        if (octave < 0 || octave > 9) {
            handleException(reader, EX_ROW_WRONG_OCTAVE, octave);
        }
        note.octave = octave;
    }

    /**
     * Tone analysis of the noise part
     */
    private void parseNoiseNote(String text, FtmNote note) {
        switch (text) {
            case "0-#":
                note.note = FtmNote.NOTE_C;
                note.octave = 0;
                break;
            case "1-#":
                note.note = FtmNote.NOTE_CS;
                note.octave = 0;
                break;
            case "2-#":
                note.note = FtmNote.NOTE_D;
                note.octave = 0;
                break;
            case "3-#":
                note.note = FtmNote.NOTE_DS;
                note.octave = 0;
                break;
            case "4-#":
                note.note = FtmNote.NOTE_E;
                note.octave = 0;
                break;
            case "5-#":
                note.note = FtmNote.NOTE_F;
                note.octave = 0;
                break;
            case "6-#":
                note.note = FtmNote.NOTE_FS;
                note.octave = 0;
                break;
            case "7-#":
                note.note = FtmNote.NOTE_G;
                note.octave = 0;
                break;
            case "8-#":
                note.note = FtmNote.NOTE_GS;
                note.octave = 0;
                break;
            case "9-#":
                note.note = FtmNote.NOTE_A;
                note.octave = 0;
                break;
            case "A-#":
                note.note = FtmNote.NOTE_AS;
                note.octave = 0;
                break;
            case "B-#":
                note.note = FtmNote.NOTE_B;
                note.octave = 0;
                break;
            case "C-#":
                note.note = FtmNote.NOTE_C;
                note.octave = 1;
                break;
            case "D-#":
                note.note = FtmNote.NOTE_CS;
                note.octave = 1;
                break;
            case "E-#":
                note.note = FtmNote.NOTE_D;
                note.octave = 1;
                break;
            case "F-#":
                note.note = FtmNote.NOTE_DS;
                note.octave = 1;
                break;

            default:
                handleException(reader, EX_ROW_WRONG_NOISE, text);
        }
    }

    /**
     * Effect analysis
     *
     * @param channelCode The track number currently being processed (not the track sequence number)
     */
    private boolean parseEffect(String text, FtmNote note, int index, byte channelCode) {
        if ("...".equals(text)) {
            return true;
        }

        char head = text.charAt(0);
        byte eff = this.conventEffectToCode(head, channelCode);

        if (eff == -1) {
            return true; // If you can't find what the effect is, you can choose to report an error, but this is not done here.
        }

        note.effNumber[index] = eff;
        note.effParam[index] = Short.parseShort(text.substring(1), 16);
        return false;
    }

    /**
     * State Transition
     */
    private void statusChange(int status, FamiTrackerHandler doc) {
        switch (status) {
            case 1:

                break;
            case 2:
                if (this.status == 2) {

                } else if (this.status == 1) {
                    // First go to PATTERN
                    this.patterns = new HashMap<>();
                    maxPatternIdx = 0;

                    // Pack the order data and send it to curTrack
                    int len = orders.size();
                    curTrack.orders = new int[len][];
                    for (int i = 0; i < len; i++) {
                        curTrack.orders[i] = orders.get(i);
                    }
                }
                curPatternGroup = new FtmPattern[columns.length];
                for (int i = 0; i < curPatternGroup.length; i++) {
                    curPatternGroup[i] = new FtmPattern();
                    curPatternGroup[i].notes = new FtmNote[curTrack.length];
                }
                break;

            case 3:
                if (this.status == 0) {
                    // Now it's track 0 (the first track)

                } else {
                    // This isn't the first one.

                    // All patterns of the previous track need to be packaged
                    packPattern();
                }
                break;

            case -1:
                // All patterns of the previous track need to be packaged
                packPattern();

                break;

            default:
                break;
        }

        this.status = status;
    }

    /**
     * Package all patterns of the previous track
     */
    private void packPattern() {
        curTrack.patterns = new FtmPattern[maxPatternIdx + 1][];

        for (Entry<Integer, FtmPattern[]> entry : this.patterns.entrySet()) {
            curTrack.patterns[entry.getKey()] = entry.getValue();
        }
    }

    //
    // tool
    //
	
	/*
	 * Below are all the effect characters and their corresponding explanations.
	'.',	// None
	'F',	// Speed
	'B',	// Jump 
	'D',	// Skip 
	'C',	// Halt
	'E',	// Volume
	'3',	// Porta on
	 0,		// Porta off // Deprecated
	'H',	// Sweep up
	'I',	// Sweep down
	'0',	// Arpeggio Arpeggios
	'4',	// Vibrato
	'7',	// Tremolo
	'P',	// Pitch
	'G',	// Note delay
	'Z',	// DAC setting
	'1',	// Portamento up
	'2',	// Portamento down
	'V',	// Duty cycle
	'Y',	// Sample offset
	'Q',	// Slide up
	'R',	// Slide down
	'A',	// Volume slide
	'S',	// Note cut
	'X',	// DPCM retrigger
	 0,		// Deprecated
	'H',	// FDS modulation depth
	'I',	// FDS modulation speed hi
	'J',	// FDS modulation speed lo
	'W',	// DPCM Pitch
	'H',	// Sunsoft envelope low
	'I',	// Sunsoft envelope high
	'J',	// Sunsoft envelope type
	'9',	// Targeted volume slide
	'H',	// VRC7 modulator
	'I',	// VRC7 carrier
	'J',	// VRC7 modulator/feedback level
	 */

    /**
     * Convert the character of the given effect into an effect code
     *
     * @param ch          The characters with the given effects written in the txt file
     * @param channelCode Track number
     * @return The effect code. This code is defined in {@link FtmNote}.
     * If no corresponding effect code is found, return -1.
     * @since v0.2.5
     */
    private byte conventEffectToCode(char ch, byte channelCode) {
        byte channelType = typeOfChannel(channelCode);

        switch (ch) {
            // Global Effects
            case 'F':
                return EF_SPEED;
            case 'B':
                return EF_JUMP;
            case 'D':
                return EF_SKIP;
            case 'C':
                return EF_HALT;

            // Universal Track Effects
            case 'E':
                return EF_VOLUME; // Deprecated
            case '3':
                return EF_PORTAMENTO;
            case '0':
                return EF_ARPEGGIO;
            case '4':
                return EF_VIBRATO;
            case '7':
                return EF_TREMOLO;
            case 'P':
                return EF_PITCH;
            case 'G':
                return EF_DELAY;
            case '1':
                return EF_PORTA_UP;
            case '2':
                return EF_PORTA_DOWN;
            case 'V':
                return EF_DUTY_CYCLE;
            case 'Q':
                return EF_SLIDE_UP;
            case 'R':
                return EF_SLIDE_DOWN;
            case 'A':
                return EF_VOLUME_SLIDE;
            case 'S':
                return EF_NOTE_CUT;
//            case '9': return ?; // Targeted volume slide, this effect is not implemented in FTM project
            // OCC new effect contains T, O, L, M, the effect is not yet clear

            // DPCM track-specific effects
            case 'Z':
                if (channelCode == CHANNEL_2A03_DPCM) return EF_DAC;
                break;
            case 'Y':
                if (channelCode == CHANNEL_2A03_DPCM) return EF_SAMPLE_OFFSET;
                break;
            case 'X':
                if (channelCode == CHANNEL_2A03_DPCM) return EF_RETRIGGER;
                break;
            case 'W':
                if (channelCode == CHANNEL_2A03_DPCM) return EF_DPCM_PITCH;
                break;

            // Special track effects
            case 'H': {
                // 2A03 sweep
                // FDS modulation depth
                // Sunsoft envelope low
                // TODO VRC7 modulator, 'H' effect not implemented
                switch (channelType) {
                    case CHANNEL_TYPE_PULSE:
                        return EF_SWEEPUP;
                    case CHANNEL_TYPE_FDS:
                        return EF_FDS_MOD_DEPTH;
                    case CHANNEL_TYPE_S5B:
                        return EF_SUNSOFT_ENV_LO;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
                }
            }
            break;
            case 'I': {
                // 2A03 sweep
                // FDS modulation speed hi
                // Sunsoft envelope high
                // TODO VR C7 modulator, 'H effect not implemented
                switch (channelType) {
                    case CHANNEL_TYPE_PULSE:
                        return EF_SWEEPDOWN;
                    case CHANNEL_TYPE_FDS:
                        return EF_FDS_MOD_SPEED_HI;
                    case CHANNEL_TYPE_S5B:
                        return EF_SUNSOFT_ENV_HI;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
                }
            }
            break;
            case 'J': {
                // FDS modulation speed lo
                // Sunsoft envelope type
                // TODO VRC7 modulator/feedback level, 'I' effect not implemented
                switch (channelType) {
                    case CHANNEL_TYPE_FDS:
                        return EF_FDS_MOD_SPEED_LO;
                    case CHANNEL_TYPE_S5B:
                        return EF_SUNSOFT_ENV_TYPE;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
                }
            }
            break;

        }
        return -1;
    }

    /*
     * Error handling
     */

    /*
     * List of generated message errors
     */
    static final String EX_INST2A03_WRONG_ITEMS = "Instrument parsing error, 2A03 The instrument format specifies 8 items, but there are only %d";
    static final String EX_KEYDPCM_WRONG_ITEMS = "Instrument parsing error, KEYDPCM instrument format specifies 9 items, but there are only %d";
    static final String EX_INSTVRC6_WRONG_ITEMS = "Instrument parsing error, VRC6 instrument format specifies 8 items, but there are only %d";
    static final String EX_FDSWAVE_WRONG_ITEMS = "Instrument parsing error, FDSWAVE format specifies 67 items, but there are only %d";
    static final String EX_FDSWAVE_WRONG_TOKEN = "FDSWAVE partial parsing error";
    static final String EX_FDSMOD_WRONG_ITEMS = "Instrument parsing error, FDSMOD format specifies 67 items, but there are only %d";
    static final String EX_FDSMOD_WRONG_TOKEN = "FDSMOD partial parsing error";
    static final String EX_FDSMACRO_WRONG_TOKEN = "FDSMACRO partial parsing error";
    static final String EX_INSTN163_WRONG_ITEMS = "Instrument parsing error, N163 The instrument format specifies 11 items, but there are only %d";
    static final String EX_N163WAVE_WRONG_ITEMS = "Instrument parsing error, N163 Waveform data format requires at least 5 items, but there are only %d";
    static final String EX_N163WAVE_WRONG_TOKEN = " N163WAVE waveform data parsing error";
    static final String EX_INSTVRC7_WRONG_ITEMS = "Instrument parsing error, VRC7 instrument format specifies 12 items, but there are only %d";
    static final String EX_DPCMDEF_WRONG_ITEMS = "Track parsing error, DPCMDEF format specifies 4 items, but there are only %d";
    static final String EX_DPCM_WRONG_TOKEN = "MACRO partial parsing error";
    static final String EX_MACRO_WRONG_ITEMS = "Error parsing the track part. The MACRO format requires at least 8 items, but there are only %d items here.";
    static final String EX_MACRO_WRONG_TOKEN = "MACRO partial parsing error";
    static final String EX_MACROVRC6_WRONG_ITEMS = "Error parsing the track part. The MACROVRC6 format requires at least 8 items, but there are only %d items here.";
    static final String EX_MACROVRC6_WRONG_TOKEN = "MACROVRC6 Partial parsing error";
    static final String EX_MACRON163_WRONG_ITEMS = "Error parsing the track part. The MACRON163 format requires at least 8 items, but there are only %d items here.";
    static final String EX_MACRON163_WRONG_TOKEN = "MACRON163 Partial parsing error";
    static final String EX_TRACK_WRONG_ITEMS = "Track parsing error, TRACK format specifies 5 items, but there are only %d items.";
    static final String EX_COLUMNS_WRONG_TOKEN = "COLUMNS partial parse error";
    static final String EX_OREDR_WRONG_ITEMS = "Track parsing error, ORDER format specifies %d items, but there are only %d items";
    static final String EX_OREDR_WRONG_TOKEN = "OREDR Partial parsing error";
    static final String EX_OREDR_WRONG_OREDR_NO = "OREDR sequence number mismatch, value is %d, expected value is %d";
    static final String EX_PATTERN_WRONG_TOKEN = "PATTERN partial parsing error";
    static final String EX_ROW_WRONG_ITEMS = "Track parsing error, ROW format specifies %d items, but there are only %d";
        static final String EX_ROW_WRONG_ROW_NO = "ROW number mismatch, value is %d, expected value is %d";
    static final String EX_ROW_WRONG_TOKEN_IN_COLUMN = "ROW section parsing error for channel number %d";
    static final String EX_ROW_WRONG_NOTE = "Error parsing part of the track, note '%s' in ROW format cannot be parsed";
    static final String EX_ROW_WRONG_OCTAVE = "Error parsing part of the track, wrong scale %d in ROW format";
    static final String EX_ROW_WRONG_NOISE = "Track parsing error, '%s' in the noise track in ROW format cannot be parsed";

    @Override
    protected void handleException(TextReader reader, String msg) throws FamiTrackerFormatException {
        String msg0 = String.format("Error found at line number %d: %s", reader.line(), msg);

        throw new FamiTrackerFormatException(msg0);
    }

    protected void handleException(TextReader reader, String msg, Object... args) throws FamiTrackerFormatException {
        handleException(reader, String.format(msg, args));
    }
}
