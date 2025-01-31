package zdream.nsfplayer.ftm.factory;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
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
import zdream.utils.common.BytesReader;

import static zdream.nsfplayer.core.FtmChipType.FDS;
import static zdream.nsfplayer.core.FtmChipType.N163;
import static zdream.nsfplayer.core.FtmChipType.S5B;
import static zdream.nsfplayer.core.FtmChipType.VRC6;
import static zdream.nsfplayer.core.FtmChipType.VRC7;
import static zdream.nsfplayer.core.FtmChipType._2A03;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PITCH;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PORTA_DOWN;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PORTA_UP;
import static zdream.nsfplayer.ftm.format.FtmSequence.SEQUENCE_COUNT;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_DSAMPLES;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_EFFECT_COLUMNS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_PATTERNS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_PATTERN_LENGTH;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_SECTIONS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_SEQUENCES;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_TEMPO;
import static zdream.nsfplayer.ftm.format.FtmStatic.OCTAVE_RANGE;


/**
 * <p>Used to transfer FamiTracker files (.ftm) to {@link FamiTrackerHandler}
 * Fill {@link FtmAudio} with data
 * <p>An instance of this creator can only be populated with data for one {@link FtmAudio}.
 * If you want to populate more {@link FtmAudio}, please create more instances of this creator.
 * </p>
 *
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerCreater extends AbstractFamiTrackerCreater<BytesReader> {

    /*
     * The ID of each block of FTM is used to identify the content of this block.
     */

    /**
     * FTM header identifier of the entire file
     */
    public static final String FILE_HEADER_ID = "FamiTracker Module";
    public static final String FILE_BLOCK_PARAMS = "PARAMS";
    public static final String FILE_BLOCK_INFO = "INFO";
    public static final String FILE_BLOCK_INSTRUMENTS = "INSTRUMENTS";
    public static final String FILE_BLOCK_SEQUENCES = "SEQUENCES";
    public static final String FILE_BLOCK_FRAMES = "FRAMES";
    public static final String FILE_BLOCK_PATTERNS = "PATTERNS";
    public static final String FILE_BLOCK_DSAMPLES = "DPCM SAMPLES";
    public static final String FILE_BLOCK_HEADER = "HEADER";
    public static final String FILE_BLOCK_COMMENTS = "COMMENTS";

    public static final String FILE_BLOCK_SEQUENCES_VRC6 = "SEQUENCES_VRC6";
    // Not yet used
    public static final String FILE_BLOCK_SEQUENCES_N163 = "SEQUENCES_N163";
    public static final String FILE_BLOCK_SEQUENCES_N106 = "SEQUENCES_N106";
    public static final String FILE_BLOCK_SEQUENCES_S5B = "SEQUENCES_S5B";
    /**
     * FTM The end mark of the entire file
     */
    public static final String FILE_END_ID = "END";

    /*
     * Cached data
     */

    /**
     * The total number of tracks
     */
    private int trackCount;

    /**
     * Number of effect columns per track
     * [trackIdx track number] [channelNo track number]
     */
    private int[][] effColumnCounts;

    /**
     * Default track playback speed
     */
    public static final int DEFAULT_SPEED = 6;

    /**
     * <p>This value is caused by the compatibility issue of FamiTracker version.
     * <p>In the early version (unclear version number) of FDS's ftm file, due to the original
     * FamiTracker developer's reasons, its scale data will differ from the actual scale data by 2.
     * <p>When this problem is detected, the value will be set to true, causing subsequent processing.
     * </p>
     */
    private boolean needAdjustFDSArpeggio;

    private void init() {
        trackCount = 0;
        effColumnCounts = null;
        needAdjustFDSArpeggio = false;
    }

    /**
     * Minimum file version that can be opened, v0.1.
     * Files below this version are no longer compatible
     */
    public static final int COMPATIBLE_VER = 0x0200;

    @Override
    public void doCreate(BytesReader reader, FamiTrackerHandler doc) {
        init();

        validateHeader(reader);

        int version;
        version = reader.readAsCInt();

        if (version < 0x0200) {
            // Read lower version files
            handleException(reader, EX_GENERAL_LOW_VERSION, version);
        } else if (version >= 0x0200) {
            try {
                doCreateNew(doc, reader, version);
            } catch (RuntimeException e) {
                handleException(reader, e);
            }
        }
    }

    private void doCreateNew(FamiTrackerHandler doc, BytesReader reader, int version) {
        doc.allocateTrack(1);

        /*
         * Famitracker produces files consisting of multiple blocks.
         * The reading process requires block-by-block processing.
         */
        while (!reader.isFinished()) LOOP:{
            Block block = nextBlock(reader);
            if (block.id == null) {
                // It's finished reading.
                break;
            }

            switch (block.id) {

                case FILE_END_ID: // It's finished reading.
                    break LOOP;

                case FILE_BLOCK_PARAMS:
                    readBlockParameters(doc, block, version);
                    break;

                case FILE_BLOCK_INFO: {
                    readBlockInfo(doc, block);
                }
                break;

                case FILE_BLOCK_HEADER: {
                    readBlockHeader(doc, block);
                }
                break;

                case FILE_BLOCK_INSTRUMENTS: {
                    readBlockInstruments(doc, block);
                }
                break;

                case FILE_BLOCK_SEQUENCES: {
                    readBlockSequences(doc, block);
                }
                break;

                case FILE_BLOCK_FRAMES: {
                    readBlockFrames(doc, block);
                }
                break;

                case FILE_BLOCK_PATTERNS: {
                    readBlockPatterns(doc, block, version);
                }
                break;

                case FILE_BLOCK_DSAMPLES: {
                    readBlockDSamples(doc, block);
                }
                break;

                case FILE_BLOCK_COMMENTS: {
                    // Ignore directly
                }
                break;

                case FILE_BLOCK_SEQUENCES_VRC6: {
                    readBlockSequencesVRC6(doc, block);
                }
                break;

                // FILE_BLOCK_SEQUENCES_N106 is for backward compatibility purposes.
                case FILE_BLOCK_SEQUENCES_N163:
                case FILE_BLOCK_SEQUENCES_N106: {
                    readBlockSequencesN163(doc, block);
                }
                break;

                case FILE_BLOCK_SEQUENCES_S5B: {
                    // TODO Temporarily unable to process S5B part
                }
                break;

                default:
                    handleException(block, EX_BLOCK_UNKNOWED_ID);
                    break;
            }
        }

        // Once the doc has been created, proceed to the checking section.
        revise(doc);
    }

    /**
     * <p>Processing parameter items.
     * <br>Determine the format of the file inside {@code block} based on the param's
     * block version number as specified in the file:
     *
     * <p>When <b>block version 1</b>.
     * <li>Speed of channel[0]
     * <li>Number of channel used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * </li>
     *
     * <p>When <b>block version 2 </b>.
     * <li>Extended Chip Code
     * <li>Number of channel used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * </li>
     *
     * <p>When <b>block version 3</b>.
     * <li>Extended Chip Code
     * <li>Number of channels used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * <li>Vibration mode (ignored)
     * </li>
     *
     * <p>When <b>block version 4</b>.
     * <li>Extended Chip Code
     * <li>Number of channels used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * <li>Vibration mode (ignored)
     * <li>Bar spacing (ignore)
     * <li>Shooting Interval (Ignore)
     * </li>
     *
     * <p>When <b>block version 5</b>.
     * <li>Extended Chip Code
     * <li>Number of channels used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * <li>Vibration mode (ignored)
     * <li>Bar spacing (ignore)
     * <li>Shooting Interval (Ignore)
     * <li>Number of Namco channels (present when the Extended Chip Code above says the music uses a Namco sound source)
     * </li>
     *
     * <p>When <b>block version 6 or higher</b>.
     * <li>Extended Chip Code
     * <li>Number of channels used
     * <li>standard (format, e.g. PAL or SECAM for TV signal)
     * <li>refresh rate
     * <li>Vibration mode (ignored)
     * <li>Bar spacing (ignore)
     * <li>Shooting Interval (Ignore)
     * <li>Number of Namco channels (present when the Extended Chip Code above says the music uses a Namco sound source)
     * <li>Split values for rhythm and tempo
     * </li>
     *
     * <p><b>Note</b>.
     * <li>Number of channels. 5 for 2A03, 8 for 2A03+VRC6, and so on.
     * <br>Since the number of channels used can be calculated statistically, the number of channels used is not recorded.
     * <li>standard (format, e.g. PAL or SECAM for TV signal) includes NTSC and PAL
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     * @param fileVersion Version number of the entire document
     */
    private void readBlockParameters(FamiTrackerHandler doc, Block block, int fileVersion) {
        int version = block.version;
        if (version < 1) {
            handleException(block, EX_PARAM_LOW_VERSION, version);
        }

        FtmTrack track = doc.audio.getTrack(0);

        switch (version) {
            case 1:
                track.speed = block.readAsCInt();
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                doc.setDefaultSplit();

                break;

            case 2:
                doc.setChip(block.readByte());
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                doc.setDefaultSplit();

                break;

            case 3:
                doc.setChip(block.readByte());
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                block.skip(4); // Vibration Mode, Ignore
                doc.setDefaultSplit();

                break;

            case 4:
                doc.setChip(block.readByte());
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                block.skip(4); // Vibration Mode, Ignore
                block.skip(4); // Bar spacing Ignore
                block.skip(4); // Interval, Ignore
                doc.setDefaultSplit();

                break;

            case 5:
                doc.setChip(block.readByte());
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                block.skip(4); // Vibration Mode, Ignore
                block.skip(4); // Bar spacing Ignore
                block.skip(4); // Interval, Ignore
                if (doc.audio.isUseN163()) {
                    doc.setNamcoChannels(block.readAsCInt());
                }
                doc.setDefaultSplit();

                break;

            case 6:
            default:
                doc.setChip(block.readByte());
                block.skip(4); // Number of channels, ignore
                doc.setMechine((byte) block.readAsCInt());
                doc.setFramerate(block.readAsCInt());
                block.skip(4); // Vibration Mode, Ignore
                block.skip(4); // Bar spacing Ignore
                block.skip(4); // Interval, Ignore
                if (doc.audio.isUseN163()) {
                    doc.setNamcoChannels(block.readAsCInt());
                }
                doc.setSplit(block.readAsCInt());

                break;
        }

        if (fileVersion == 0x0200) {
            int speed = track.speed;
            if (speed < 20)
                track.speed = speed + 1;
        }

        if (version == 1) {
            if (track.speed > 19) {
                track.tempo = track.speed;
                track.speed = 6;
            } else {
                track.tempo = (doc.audio.getRegion() == ERegion.NTSC) ?
                        FtmTrack.DEFAULT_NTSC_TEMPO : FtmTrack.DEFAULT_PAL_TEMPO;
            }
        }
    }

    /**
     * Generate music messages, including titles, authors, copyright notices, etc.
     *
     * @param doc
     * @param block
     */
    private void readBlockInfo(FamiTrackerHandler doc, Block block) {
        doc.audio.title = block.readAsString(32);
        doc.audio.author = block.readAsString(32);
        doc.audio.copyright = block.readAsString(32);
    }

    /**
     * <p>Processing of logo headers.
     * <br>Determine the format of the file inside {@code block}
     * based on the block version number of the header specified in the file:
     *
     * <p>When <b>block version 1</b>.
     * <li>Number of effect columns per track
     * </li>
     *
     * <p>When <b>block version 2 </b>.
     * <li>(Total number of pieces - 1)
     * <li>Various pieces of music, Number of effect columns per track
     * </li>
     *
     * <p>When <b>block version 3 and above</b>.
     * <li>(Total number of pieces - 1)
     * <li>Name of each piece
     * <li>Various pieces of music, Number of effect columns per track
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     */
    private void readBlockHeader(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        if (version < 1) {
            handleException(block, EX_HEADER_LOW_VERSION, version);
        }

        if (version == 1) {
            // Version 1 Single song only
            trackCount = 1;

            int channelCount = doc.channelCount();
            effColumnCounts = new int[trackCount][channelCount];
            for (int i = 0; i < channelCount; ++i) {
                block.skip(1); // channelType ignore
                effColumnCounts[0][i] = block.readByte();
            }
        } else {
            trackCount = block.readUnsignedByte() + 1;  // 0 means only 1 song.
            doc.allocateTrack(trackCount);

            int channelCount = doc.channelCount();
            effColumnCounts = new int[trackCount][channelCount];

            // Track name
            if (version >= 3) {
                for (int i = 0; i < trackCount; ++i) {
                    doc.audio.getTrack(i).name = block.readAsString();
                }
            }

            // Effect Column Count
            for (int i = 0; i < channelCount; ++i) {
                block.skip(1); // channelType ignore
                for (int j = 0; j < trackCount; ++j) {
                    effColumnCounts[j][i] = block.readByte();
                }
            }

            // Highlight ignore
        }
    }

    /**
     * <p>Handling of musical instruments.
     * <p>The contents of the data inside are:
     * <li>Instrument number
     * <li>The serial number of the contained sequence of the instrument (similar to a pointer).
     * <li>Name of instrument
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     */
    private void readBlockInstruments(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        if (version < 1) {
            handleException(block, EX_INSTS_LOW_VERSION, version);
        }

        // The value with the largest serial number in the instrument + 1
        int max = block.readAsCInt();

        for (int i = 0; i < max; ++i) {
            // Instrument number
            int index = block.readAsCInt();

            // Creating Instrument Instances
            byte type = block.readByte();
            AbstractFtmInstrument inst = createInstrument(ofInstrumentType(type), doc, block);
            inst.seq = index;

            // Read instrument name
            int size = block.readAsCInt();
            inst.name = block.readAsString(size);

            // Saving Instruments to FtmAudio
            doc.registerInstrument(inst);
        }
    }

    /**
     * <p>Processing sequences (2A03 & MMC5).
     * <br>Determine the format of the file inside {@code block} based on the block
     * version number of the sequences specified in the file: {@code block}:
     *
     * @param doc
     * @param block
     */
    private void readBlockSequences(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        int count = block.readAsCInt();

        if (version >= 3) {
            int[] indices = new int[MAX_SEQUENCES * SEQUENCE_COUNT];
            int[] types = new int[MAX_SEQUENCES * SEQUENCE_COUNT];

            for (int i = 0; i < count; ++i) {
                int index = block.readAsCInt();
                int type = block.readAsCInt();
                int seqCount = block.readUnsignedByte(); // Array length of the sequence
                int loopPoint = block.readAsCInt(); // Position of the point of the Loop

                // Work-around for some older files
                if (loopPoint == seqCount)
                    loopPoint = -1;

                indices[i] = index;
                types[i] = type;

                FtmSequence seq = doc.getOrCreateSequence(_2A03, FtmSequenceType.get(type), index);

                seq.clear();
                seq.loopPoint = loopPoint;

                /*
                 * Release points and setting data items have been added since version 4.
                 */
                if (version == 4) {
                    seq.releasePoint = block.readAsCInt();
                    seq.settings = (byte) block.readAsCInt();
                }

                byte[] data = new byte[seqCount];
                block.read(data);
                seq.data = data;
            }

            if (version == 5) {
                /*
                 * According to the source code, there was a problem with saving release
                 * points in version 5, which was fixed in version 6.
                 */
                for (int i = 0; i < MAX_SEQUENCES; ++i) {
                    for (int j = 0; j < SEQUENCE_COUNT; ++j) {
                        int releasePoint = block.readAsCInt();
                        int settings = block.readAsCInt();

                        FtmSequence seq = doc.getSequence2A03(FtmSequenceType.get(j), i);
                        if (seq == null) {
                            continue;
                        }

                        seq.releasePoint = releasePoint;
                        seq.settings = (byte) settings;
                    }
                }
            } else if (version >= 6) {
                // Read release points correctly stored
                /*
                 * Version 6 release point data is stored correctly.
                 */
                for (int i = 0; i < count; ++i) {
                    int releasePoint = block.readAsCInt();
                    int settings = block.readAsCInt();
                    int index = indices[i];
                    int type = types[i];

                    FtmSequence seq = doc.getSequence2A03(FtmSequenceType.get(type), index);
                    seq.releasePoint = releasePoint;
                    seq.settings = (byte) settings;
                }
            }
        } else {
            handleException(block, EX_SEQS_LOW_VERSION, version);
        }
    }

    private void readBlockSequencesVRC6(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        int count = block.readAsCInt();

        if (version < 4) {
            for (int i = 0; i < count; ++i) {
                int index = block.readAsCInt();
                int type = block.readAsCInt();
                int seqCount = block.readUnsignedByte();
                int loopPoint = block.readAsCInt();

                FtmSequence seq = doc.getOrCreateSequence(VRC6, FtmSequenceType.get(type), index);
                seq.clear();

                seq.loopPoint = loopPoint;

                byte[] bs = new byte[seqCount];
                block.read(bs);
                seq.data = bs;
            }
        } else {
            int[] indices = new int[MAX_SEQUENCES];
            int[] types = new int[MAX_SEQUENCES];
            int releasePoint = -1, settings = 0;

            for (int i = 0; i < count; ++i) {
                int index = block.readAsCInt();
                int type = block.readAsCInt();
                int seqCount = block.readUnsignedByte();
                int loopPoint = block.readAsCInt();

                indices[i] = index;
                types[i] = type;

                // Check index
                if (index >= MAX_SEQUENCES) {
                    handleException(block, EX_SEQSVRC6_MAX_SEQUENCES, index);
                }

                FtmSequence seq = doc.getOrCreateSequence(VRC6, FtmSequenceType.get(type), index);
                seq.clear();

                seq.loopPoint = loopPoint;

                if (version == 4) {
                    seq.releasePoint = block.readAsCInt();
                    seq.settings = (byte) block.readAsCInt();
                }

                byte[] data = new byte[seqCount];
                block.read(data);
                seq.data = data;
            }

            if (version == 5) {
                // According to the source code, there was a problem with saving release
                // points in version 5, which was fixed in version 6.
                for (int i = 0; i < MAX_SEQUENCES; ++i) {
                    for (int j = 0; j < SEQUENCE_COUNT; ++j) {
                        releasePoint = block.readAsCInt();
                        settings = block.readAsCInt();

                        FtmSequence seq = doc.getSequenceVRC6(FtmSequenceType.get(j), i);
                        if (seq == null) {
                            continue;
                        }

                        seq.releasePoint = releasePoint;
                        seq.settings = (byte) settings;
                    }
                }
            } else if (version >= 6) {
                for (int i = 0; i < count; ++i) {
                    releasePoint = block.readAsCInt();
                    settings = block.readAsCInt();
                    int index = indices[i];
                    int type = types[i];

                    FtmSequence seq = doc.getOrCreateSequence(VRC6, FtmSequenceType.get(type), index);

                    seq.releasePoint = releasePoint;
                    seq.settings = (byte) settings;
                }
            }
        }
    }

    /**
     * <p>Processing segment Sequences N163
     * </p>
     *
     * @param doc
     * @param block
     * @since v0.2.6
     */
    private void readBlockSequencesN163(FamiTrackerHandler doc, Block block) {
        // int version = block.version;

        int count = block.readAsCInt();

        for (int i = 0; i < count; i++) {
            int index = block.readAsCInt();
            int type = block.readAsCInt();
            int seqCount = block.readUnsignedByte();
            int loopPoint = block.readAsCInt();
            int releasePoint = block.readAsCInt();
            int setting = block.readAsCInt();

            if (index >= MAX_SEQUENCES) {
                handleException(block, EX_SEQSN163_MAX_SEQUENCES, index);
            }
            if (type >= 5) {
                handleException(block, EX_SEQSN163_WRONG_TYPE, type);
            }

            FtmSequence seq = doc.getOrCreateSequence(N163, FtmSequenceType.get(type), index);

            seq.clear();
            seq.data = new byte[seqCount];

            seq.loopPoint = loopPoint;
            seq.releasePoint = releasePoint;
            seq.settings = (byte) setting;

            for (int j = 0; j < seqCount; ++j) {
                byte value = block.readByte();
                seq.data[j] = value;
            }
        }
    }

    /**
     * <p>Processing paragraph Frames.
     * <br>Determine the format of the file inside {@code block} from the FRAMES block
     * version number specified in the file.
     *
     * <p>Not supported when <b>block version is 1 </b>.
     *
     * <p>When <b>block version 2</b>, each track has the following information.
     * <li>Number of passages in the repertoire
     * <li>Track playback speed
     * <li>Maximum number of lines in a paragraph
     * <li>Order per track, per segment
     * </li>
     *
     * <p>When <b>block version 3</b>, each track has the following information.
     * <li>Number of passages in the repertoireTrack playback speed
     * <li>Track playback speed
     * <li>Track playback tempo value tempo
     * <li>Maximum number of lines in a paragraph
     * <li>Order per track, per segment
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     */
    private void readBlockFrames(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        if (version <= 1) {
            handleException(block, EX_FRAMES_LOW_VERSION, version);
        }

        int trackIdx = 0;

        for (; trackIdx < trackCount; ++trackIdx) {
            // All segments of the repertoire Frame
            int frameCount = block.readAsCInt();
            if (frameCount <= 0 || frameCount > MAX_SECTIONS) {
                handleException(block, EX_FRAMES_WRONG_FRAME_COUNT, trackIdx, frameCount);
            }

            int speed = block.readAsCInt();
            if (speed <= 0) {
                handleException(block, EX_FRAMES_WRONG_SPEED, trackIdx, speed);
            }

            FtmTrack track = doc.getOrCreateTrack(trackIdx);

            if (version == 3) {
                int tempo = block.readAsCInt();
                if (tempo <= 0 || tempo > MAX_TEMPO) {
                    handleException(block, EX_FRAMES_WRONG_TEMPO, trackIdx, tempo);
                }
                track.tempo = tempo;
                track.speed = speed;

            } else {
                if (speed < 20) {
                    int tempo = (doc.audio.getRegion() == ERegion.NTSC) ?
                            FtmTrack.DEFAULT_NTSC_TEMPO : FtmTrack.DEFAULT_PAL_TEMPO;
                    track.tempo = tempo;
                    track.speed = speed;
                } else {
                    if (speed > MAX_TEMPO) {
                        handleException(block, EX_FRAMES_WRONG_SPEED, trackIdx, speed);
                    }
                    track.tempo = speed;
                    track.speed = DEFAULT_SPEED;
                }
            }

            // Number of lines per paragraph
            int rowCount = block.readAsCInt();
            if (rowCount <= 0 || rowCount > MAX_PATTERN_LENGTH) {
                handleException(block, EX_FRAMES_WRONG_ROW_NO, trackIdx, rowCount);
            }

            track.length = rowCount;
            int channelsCount = doc.channelCount();
            track.orders = new int[frameCount][channelsCount];

            for (int frameIdx = 0; frameIdx < frameCount; ++frameIdx) {
                for (int channelIdx = 0; channelIdx < channelsCount; ++channelIdx) {
                    // order is like an index pointer, telling you what passage
                    // should be played for passage x of a track.
                    int order = block.readUnsignedByte();
                    if (order < 0 || order >= MAX_PATTERNS) {
                        handleException(block, EX_FRAMES_WRONG_ORDER_NO,
                                trackIdx, frameIdx, channelIdx, order);
                    }

                    track.orders[frameIdx][channelIdx] = order;
                }
            }
        }
    }

    /**
     * <p>Processing Pattern.
     * <br>Determine the format of the file inside {@code block} based on the
     * block version number of PATTERNS specified in the file.
     * <p>Not supported when <b>block version 1</b>.
     *
     * <p>Each pattern contains the following data:
     * <li>channel number
     * <li>pattern
     * <li>note count
     * <li>All key data for this mode
     * </li>
     *
     * <p>Each key (note) contains the following data:
     * <li>line number
     * <li>pitch
     * <li>scale
     * <li>Serial number of the instrument used
     * <li>volume
     * <li>Effect items and effect parameters (1 or 4, depending on fileVersion)
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     * @param fileVersion Version number of the document
     */
    private void readBlockPatterns(FamiTrackerHandler doc, Block block, int fileVersion) {
        int version = block.version;
        if (version <= 1) {
            handleException(block, EX_PAT_LOW_VERSION, version);
        }

        while (!block.isFinished()) {
            int trackIdx = block.readAsCInt();

            int channelIdx = block.readAsCInt();
            int patternIdx = block.readAsCInt();

            /*
             * Number of valid data.
             * On a track, if the data on a line is not completely empty, and there are data such as tune,
             * volume, instruments, effects, etc., it is considered to be valid data.
             */
            int items = block.readAsCInt();

            if (channelIdx < 0) {
                handleException(block, EX_PAT_WRONG_CHANNEL_NO, trackIdx, channelIdx);
            }
            if (patternIdx < 0 || patternIdx >= MAX_PATTERNS) {
                handleException(block, EX_PAT_WRONG_PATTERN_NO, trackIdx, patternIdx);
            }
            if (items <= 0 || items > MAX_PATTERN_LENGTH) {
                handleException(block, EX_PAT_WRONG_NOTE_AMOUNT, trackIdx, items);
            }

            for (int i = 0; i < items; ++i) {
                int row;
                if (fileVersion == 0x0200)
                    row = block.readUnsignedByte();
                else
                    row = block.readAsCInt();

                FtmNote note;
                if (row >= MAX_PATTERN_LENGTH) {
                    handleException(block, EX_PAT_WRONG_ROW_NO, trackIdx, patternIdx, channelIdx, i, row);
                }

                if (row >= doc.audio.getTrack(trackIdx).length) {
                    // This note will not be added to the doc
                    note = new FtmNote();
                } else {
                    note = doc.getOrCreateNote(trackIdx, patternIdx, channelIdx, row);
                }

                note.note = block.readByte();
                note.octave = block.readByte();
                note.instrument = block.readUnsignedByte();
                note.vol = block.readByte();

                if (fileVersion == 0x0200) {
                    byte effectNumber;
                    short effectParam;
                    effectNumber = block.readByte();
                    effectParam = (short) block.readUnsignedByte();
                    if (version < 3) {
                        if (effectNumber == FtmNote.EF_PORTAOFF) {
                            effectNumber = FtmNote.EF_PORTAMENTO;
                            effectParam = 0;
                        } else if (effectNumber == FtmNote.EF_PORTAMENTO) {
                            if (effectParam < 0xFF)
                                effectParam++;
                        }
                    }

                    note.effNumber[0] = effectNumber;
                    note.effParam[0] = effectParam;
                } else {
                    int effColumnCount = effColumnCounts[trackIdx][channelIdx] + 1; // By default, there is 1 column
                    for (int n = 0; n < effColumnCount; ++n) {
                        byte effectNumber;
                        int effectParam;
                        effectNumber = block.readByte();
                        effectParam = block.readUnsignedByte();

                        if (version < 3) {
                            if (effectNumber == FtmNote.EF_PORTAOFF) {
                                effectNumber = FtmNote.EF_PORTAMENTO;
                                effectParam = 0;
                            } else if (effectNumber == FtmNote.EF_PORTAMENTO) {
                                if (effectParam < 0xFF)
                                    effectParam++;
                            }
                        }

                        note.effNumber[n] = effectNumber;
                        note.effParam[n] = (short) (effectParam & 0xFF);
                    }
                }

                if (note.vol > FtmNote.MAX_VOLUME) {
                    handleException(block, EX_PAT_WRONG_VOLUME, trackIdx, patternIdx, channelIdx, row, note.vol);
                }

                // Specific for version 2.0
                if (fileVersion == 0x0200) {
                    if (note.effNumber[0] == FtmNote.EF_SPEED && note.effParam[0] < 20)
                        note.effParam[0]++;

                    if (note.vol == 0)
                        note.vol = FtmNote.MAX_VOLUME;
                    else {
                        note.vol--;
                        note.vol &= 0x0F;
                    }

                    if (note.note == 0)
                        note.instrument = MAX_INSTRUMENTS;
                }

                if (version == 3) {
                    // Fix for VRC7 portamento
                    // In FamiTracker version 3, the 1xx and 2xx effects of the VRC7 track were reversed.
                    // This bug has been fixed in later versions.
                    if (doc.audio.isUseVrc7() && doc.channelChip(channelIdx) == INsfChannelCode.CHIP_VRC7) {
                        for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
                            switch (note.effNumber[n]) {
                                case EF_PORTA_DOWN:
                                    note.effNumber[n] = EF_PORTA_UP;
                                    break;
                                case EF_PORTA_UP:
                                    note.effNumber[n] = EF_PORTA_DOWN;
                                    break;
                            }
                        }
                    }
                    // If the track is an FDS channel
                    else if (doc.audio.isUseFds()
                            && doc.channelCode(channelIdx) == INsfChannelCode.CHANNEL_FDS) {
                        for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
                            if (note.effNumber[n] == EF_PITCH) {
                                if (note.effParam[n] != 0x80)
                                    note.effParam[n] = (short) ((0x100 - note.effParam[n]) & 0xFF);
                            }
                        }
                    }

                }

                if (version < 5) {
                    // FDS scales in previous versions were two values lower
                    if (doc.audio.isUseFds() && doc.channelCode(channelIdx) == INsfChannelCode.CHANNEL_FDS
                            && note.octave < 6) {
                        note.octave += 2;
                        needAdjustFDSArpeggio = true;
                    }
                }
            }
        }
    }

    /**
     * <p>Processing DSamples.
     * <br>Determine the format of the file inside {@code block} based on the block
     * version number of DSAMPLES specified in the file: {@code block}:
     *
     * <p>Each sample (note) contains the following data:
     * <li>serial number
     * <li>name
     * <li>digit
     * </li>
     * </p>
     *
     * @param doc
     * @param block
     */
    private void readBlockDSamples(FamiTrackerHandler doc, Block block) {
        int count = block.readUnsignedByte();
        if (count < 0 || count >= MAX_DSAMPLES) {
            handleException(block, EX_DSMP_WRONG_AMOUNT, count);
        }

        for (int i = 0; i < count; ++i) {
            int index = block.readUnsignedByte();
            if (index < 0 || index >= MAX_DSAMPLES) {
                handleException(block, EX_DSMP_WRONG_INDEX, i, index);
            }

            FtmDPCMSample sample = doc.getOrCreateDPCMSample(index);
            int len = block.readAsCInt();

            // name
            sample.name = block.readAsString(len);

            // digit
            int size = block.readAsCInt();
            if (size < 0 || size >= 0x8000) {
                handleException(block, EX_DSMP_WRONG_SIZE, i, size);
            }
            byte[] bs = new byte[size];
            int relSize = block.read(bs);
            if (relSize != size) {
                handleException(block, EX_DSMP_NOT_REACH_END, i, size, relSize);
            }
            sample.data = bs;
        }
    }

    /**
     * Creating a musical instrument
     *
     * @param type
     * @param doc
     * @param block
     * @return
     */
    private AbstractFtmInstrument createInstrument(FtmChipType type, FamiTrackerHandler doc, Block block) {
        return switch (type) {
            case _2A03, _2A07 -> create2A03Instrument(doc, block);
            case VRC6 -> createVRC6Instrument(doc, block);
            case VRC7 -> createVRC7Instrument(doc, block);
            case FDS -> createFDSInstrument(doc, block);
            case N163 -> createN163Instrument(doc, block);

            // TODO Other chips S5B

            default -> null;
        };
    }

    private FtmInstrument2A03 create2A03Instrument(FamiTrackerHandler doc, Block block) {
        int version = block.version;
        FtmInstrument2A03 inst = new FtmInstrument2A03();

        int seqCount = block.readAsCInt();

        for (byte type = 0; type < seqCount; ++type) {
            boolean enable = block.readByte() != 0;
            int index = block.readUnsignedByte();

            if (!enable) {
                continue;
            }

            doc.getOrCreateSequence(FtmChipType._2A03, FtmSequenceType.get(type), index);
            switch (type) {
                case 0:
                    inst.vol = index;
                    break;
                case 1:
                    inst.arp = index;
                    break;
                case 2:
                    inst.pit = index;
                    break;
                case 3:
                    inst.hip = index;
                    break;
                case 4:
                    inst.dut = index;
                    break;

                default:
                    break;
            }
        }

        // DPCM section
        int octaves = (version == 1) ? 6 : OCTAVE_RANGE;

        for (int i = 0; i < octaves; ++i) {
            for (int j = 0; j < 12; ++j) {
                int index = block.readUnsignedByte();
                /*
                 * For those who did not use DPCM, the index is 0.
                 * If it detects that the adopted DPCM is 0, it will be skipped.
                 */
                if (index == 0) {
                    inst.setEmptySample(i, j);
                    block.skip((version > 5) ? 2 : 1);
                    continue;
                }

                FtmDPCMSample sample = doc.getOrCreateDPCMSample(index - 1);
                byte pitch = block.readByte();
                byte delta;
                if (version > 5) {
                    delta = block.readByte();
                    if (delta < 0) {
                        delta = -1;
                    }
                } else {
                    delta = -1;
                }
                inst.setSample(i, j, sample, pitch, delta);
            }
        }

        return inst;
    }

    private FtmInstrumentVRC6 createVRC6Instrument(FamiTrackerHandler doc, Block block) {
        FtmInstrumentVRC6 inst = new FtmInstrumentVRC6();

        int seqCount = block.readAsCInt();

        for (byte type = 0; type < seqCount; ++type) {
            boolean enable = block.readByte() != 0;
            int index = block.readUnsignedByte();

            if (!enable) {
                continue;
            }

            doc.getOrCreateSequence(FtmChipType.VRC6, FtmSequenceType.get(type), index);
            switch (type) {
                case 0:
                    inst.vol = index;
                    break;
                case 1:
                    inst.arp = index;
                    break;
                case 2:
                    inst.pit = index;
                    break;
                case 3:
                    inst.hip = index;
                    break;
                case 4:
                    inst.dut = index;
                    break;

                default:
                    break;
            }
        }
        return inst;
    }

    /**
     * <p>Creating a VRC7 Instrument
     * </p>
     *
     * @return VRC7 instruments
     * @since v0.2.7
     */
    private FtmInstrumentVRC7 createVRC7Instrument(FamiTrackerHandler doc, Block block) {
        FtmInstrumentVRC7 inst = new FtmInstrumentVRC7();

        inst.patchNum = block.readAsCInt();

        int length = inst.regs.length;
        for (int i = 0; i < length; i++) {
            inst.regs[i] = (short) block.readUnsignedByte();
        }

        return inst;
    }

    /**
     * <p>Create N163 Instrument
     * </p>
     *
     * @return N163 instruments
     * @since v0.2.6
     */
    private FtmInstrumentN163 createN163Instrument(FamiTrackerHandler doc, Block block) {
        FtmInstrumentN163 inst = new FtmInstrumentN163();

        int seqCount = block.readAsCInt();
        if (seqCount >= (SEQUENCE_COUNT + 1)) {
            handleException(block, EX_INSTN163_WRONG_SEQ_AMOUNT, seqCount);
        }

        seqCount = SEQUENCE_COUNT;

        for (int type = 0; type < seqCount; ++type) {
            boolean seqEnable = (block.readByte() != 0);
            int index = block.readUnsignedByte();

            if (!seqEnable) {
                continue;
            }

            if (index >= MAX_SEQUENCES) {
                handleException(block, EX_INSTN163_WRONG_SEQ_NO, index);
            }
            switch (type) {
                case 0:
                    inst.vol = index;
                    break;
                case 1:
                    inst.arp = index;
                    break;
                case 2:
                    inst.pit = index;
                    break;
                case 3:
                    inst.hip = index;
                    break;
                case 4:
                    inst.dut = index;
                    break;

                default:
                    break;
            }
        }

        int waveSize = block.readAsCInt();
        if (waveSize < 0 || waveSize > 128) {
            handleException(block, EX_INSTN163_WRONG_WAVE_SIZE, waveSize);
        }

        inst.wavePos = block.readAsCInt();
        if (inst.wavePos < 0 || inst.wavePos >= 128) {
            handleException(block, EX_INSTN163_WRONG_WAVE_POS, inst.wavePos);
        }

        int waveCount = block.readAsCInt();
        if (waveCount < 1 || waveCount > 16) {
            handleException(block, EX_INSTN163_WRONG_WAVE_AMOUNT, waveCount);
        }

        // Up to 16 waves, each wave up to 128 segments long
        inst.waves = new byte[waveCount][waveSize];
        for (int i = 0; i < waveCount; ++i) {
            for (int j = 0; j < waveSize; ++j) {
                byte waveSample = block.readByte();
                if (waveSample < 0 || waveSample >= 16) {
                    handleException(block, EX_INSTN163_WRONG_WAVE_VALUE, i, j, waveSample);
                }
                inst.waves[i][j] = waveSample;
            }
        }

        return inst;
    }

    private FtmInstrumentFDS createFDSInstrument(FamiTrackerHandler doc, Block block) {
        FtmInstrumentFDS inst = new FtmInstrumentFDS();

        for (int i = 0; i < FtmInstrumentFDS.SAMPLE_LENGTH; ++i) {
            inst.samples[i] = block.readByte();
        }

        for (int i = 0; i < FtmInstrumentFDS.MODULATION_LENGTH; ++i) {
            inst.modulation[i] = block.readByte();
        }

        inst.modulationSpeed = block.readAsCInt();
        inst.modulationDepth = block.readAsCInt();
        inst.modulationDelay = block.readAsCInt();

        /*
         * Here the latter data are used to infer which part is below
         */
        int a = block.readAsCInt() & 0x7FFFFFFF; // unsigned
        int b = block.readAsCInt() & 0x7FFFFFFF; // unsigned
        block.rollback(8);

        if (a < 256 && (b & 0xFF) != 0x00) {
            // Nothing.
        } else {
            inst.seqVolume = createFDSSequence(doc, block, FtmSequenceType.VOLUME);
            inst.seqArpeggio = createFDSSequence(doc, block, FtmSequenceType.ARPEGGIO);
            //
            // The following text is from the original FamiTracker project.
            // Note: Remove this line when files are unable to load
            // (if a file contains FDS instruments but FDS is disabled)
            // this was a problem in an earlier version.
            //
            if (block.version > 2) {
                inst.seqPitch = createFDSSequence(doc, block, FtmSequenceType.PITCH);
            } else {
                inst.seqPitch = createEmptySequence(FtmSequenceType.PITCH);
            }
        }

        // The original version had a volume range of [0, 15], now it's [0, 31].
        // Older files was 0-15, new is 0-31
        if (block.version <= 3) {
            for (int i = 0; i < inst.seqVolume.length(); ++i) {
                inst.seqVolume.data[i] *= 2;
            }
        }

        return inst;
    }

    private FtmSequence createFDSSequence(FamiTrackerHandler doc, Block block, FtmSequenceType type) {
        // The following four values in the original project are all unsigned
        // But I think loopPoint and releasePoint have an illegal value of -1.
        // So here these two values are not forced to be converted to unsigned
        int seqCount = block.readUnsignedByte();
        int loopPoint = block.readAsCInt();
        int releasePoint = block.readAsCInt();
        int settings = block.readAsCInt(); // For use with Arpeggio sequences only

        if (seqCount > MAX_SEQUENCES) {
            handleException(block, EX_INSTS_WRONG_SEQ_AMOUNT, seqCount);
        }
        FtmSequence seq = new FtmSequence(type);

        // seq.setItemCount(seqCount);
        seq.loopPoint = loopPoint;
        seq.releasePoint = releasePoint;
        seq.settings = (byte) settings;

        seq.data = new byte[seqCount];
        for (int i = 0; i < seqCount; ++i) {
            int value = block.readUnsignedByte();
            seq.data[i] = (byte) value;
        }

        return seq;
    }

    private FtmSequence createEmptySequence(FtmSequenceType type) {
        FtmSequence seq = new FtmSequence(type);
        seq.clear();
        return seq;
    }

    //
    // checks
    //

    /**
     * Checking and trying to fix
     */
    void revise(FamiTrackerHandler doc) {
        reviseNotes(doc);
        reviseInsts(doc);

        // FDS Instrument Compatibility Issues
        if (needAdjustFDSArpeggio) {
            adjustFDSArpeggio(doc);
        }

        // TODO Other tests
    }

    private void adjustFDSArpeggio(FamiTrackerHandler doc) {
        for (int i = 0; i < doc.audio.instrumentCount(); ++i) {
            AbstractFtmInstrument inst = doc.audio.getInstrument(i);
            if (inst == null || inst.instType() != FtmChipType.FDS) {
                continue;
            }

            FtmInstrumentFDS instfds = (FtmInstrumentFDS) inst;
            FtmSequence seq = instfds.seqArpeggio;

            if (seq.length() > 0 && seq.settings == FtmSequence.ARP_SETTING_FIXED) {
                int length = seq.length();
                for (int j = 0; j < length; ++j) {
                    seq.data[j] += 24;
                }
            }
        }
    }

    /**
     * Check instruments, set seq values for all instruments
     */
    private void reviseInsts(FamiTrackerHandler doc) {
        FtmAudio audio = doc.audio;
        int instMax = audio.instrumentCount();

        for (int i = 0; i < instMax; i++) {
            AbstractFtmInstrument inst = audio.getInstrument(i);

            if (inst != null) {
                inst.seq = i;
            }
        }
    }

    /**
     * Check that the instrument used for each key is in the correct range.
     * Fix: Change instrument numbers that are not in the correct range to a flat value of -1
     *
     * @param doc
     */
    private void reviseNotes(FamiTrackerHandler doc) {
        FtmAudio audio = doc.audio;
        int instMax = audio.instrumentCount();

        int trackLen = audio.getTrackCount();
        for (int i = 0; i < trackLen; i++) {
            FtmTrack track = audio.getTrack(i);
            FtmPattern[][] ps = track.patterns;
            if (ps == null) {
                track.patterns = new FtmPattern[1][doc.channelCount()];
                continue;
            }

            for (FtmPattern[] ys : ps) {
                if (ys == null) {
                    continue;
                }

                for (FtmPattern p : ys) {
                    if (p == null) {
                        continue;
                    }

                    FtmNote[] notes = p.notes;
                    for (FtmNote note : notes) {
                        reviseNote(note, instMax);
                    }
                }
            }

        }
    }

    private void reviseNote(FtmNote note, int instMax) {
        if (note == null) {
            return;
        }

        if (note.instrument < 0 || note.instrument >= instMax) {
            note.instrument = -1;
        }
    }

    //
    // Error handling
    //

    //
    // List of message errors generated
    //
    static final String EX_GENERAL_WRONG_HEAD = "FTM file format is incorrect, header mismatch";
    static final String EX_GENERAL_LOW_VERSION = "FTM file version %x too low to parse";
    static final String EX_BLOCK_UNKNOWED_ID = "Unknown Block ID";
    // Parameters
    static final String EX_PARAM_LOW_VERSION = "FTM Parameters block version %d too low to parse";
    // Header
    static final String EX_HEADER_LOW_VERSION = "FTM Header block version %d too low to parse";
    // Instruments
    static final String EX_INSTS_LOW_VERSION = "FTM Instruments block version %d too low to parse";
    static final String EX_INSTS_WRONG_SEQ_AMOUNT = "Number of instrument sequences: %d Error";
    // Inst N163
    static final String EX_INSTN163_WRONG_SEQ_AMOUNT = "N163 Number of instrument sequences: %d Error";
    static final String EX_INSTN163_WRONG_SEQ_NO = "N163 Instrument Serial Number: %d Error";
    static final String EX_INSTN163_WRONG_WAVE_SIZE = "N163 Instrument Waveform Size: %d Errors";
    static final String EX_INSTN163_WRONG_WAVE_POS = "N163 Instrument waveform position: %d error";
    static final String EX_INSTN163_WRONG_WAVE_AMOUNT = "N163 Number of instrument waveforms: %d Error";
    static final String EX_INSTN163_WRONG_WAVE_VALUE = "N163 Instrument waveform number: %d, index: %d, value: %d error";
    // Sequences
    static final String EX_SEQS_LOW_VERSION = "FTM Sequences block version %d is too low to parse.";
    // Seq VRC6
    static final String EX_SEQSVRC6_MAX_SEQUENCES = "Serial number of VRC6 sequence %d Exception";
    // Seq N163
    static final String EX_SEQSN163_MAX_SEQUENCES = "N163 Serial number of sequence %d Exception";
    static final String EX_SEQSN163_WRONG_TYPE = "N163 Type number of sequence type: %d Exception";
    // FRAMES
    static final String EX_FRAMES_LOW_VERSION = "FTM Frames block version %d too low to parse";
    static final String EX_FRAMES_WRONG_FRAME_COUNT = "Number of Segment Frames for track %d: %d is incorrect.";
    static final String EX_FRAMES_WRONG_SPEED = "Speed value for track %d speed: %d is wrong";
    static final String EX_FRAMES_WRONG_TEMPO = "Rhythmic value of track %d tempo: %d is wrong.";
    static final String EX_FRAMES_WRONG_ROW_NO = "Line number of track %d: %d is incorrect.";
    static final String EX_FRAMES_WRONG_ORDER_NO = "Track %d, Frame %d, Track number %d order: %d is wrong";
    // PATTERNS
    static final String EX_PAT_LOW_VERSION = "FTM Patterns block version %d is too low to parse.";
    static final String EX_PAT_WRONG_CHANNEL_NO = "Track number of track %d: %d is incorrect.";
    static final String EX_PAT_WRONG_PATTERN_NO = "Pattern number of track %d: %d is incorrect.";
    static final String EX_PAT_WRONG_NOTE_AMOUNT = "Number of keys for track %d: %d is incorrect.";
    static final String EX_PAT_WRONG_ROW_NO = "Track %d, Pattern %d, Track number %d, Line number of %d key: %d error";
    static final String EX_PAT_WRONG_VOLUME = "Volume of keys for track %d, mode %d, track number %d, line number %d: %d error";
    // DSamples
    static final String EX_DSMP_WRONG_AMOUNT = "Number of DPCM samples: %d Errors";
    static final String EX_DSMP_WRONG_INDEX = "%d sample number: %d error";
    static final String EX_DSMP_WRONG_SIZE = "Data length of %dth sample: %d error";
    static final String EX_DSMP_NOT_REACH_END = "Data length of the %dth sample: %d is not legal, only %d can be read.";

    /**
     * @param block Current error block
     * @param msg   Error message content
     * @throws FamiTrackerFormatException
     * @since v0.2.5
     */
    protected void handleException(Block block, String msg) throws FamiTrackerFormatException {
        String msg0 = String.format("Location 0x%x [0x%x + 0x%x] (%s) Version %d, error found: %s",
                block.getOffset() + block.blockOffset, block.getOffset(), block.blockOffset,
                block.id, block.version, msg);

        throw new FamiTrackerFormatException(msg0);
    }

    protected void handleException(Block block, String msg, Object... args) throws FamiTrackerFormatException {
        handleException(block, String.format(msg, args));
    }

    /**
     * @param reader Current data carrier of byte[]
     * @param msg    Error message content
     * @throws FamiTrackerFormatException
     * @since v0.2.5
     */
    @Override
    protected void handleException(BytesReader reader, String msg) throws FamiTrackerFormatException {
        String msg0 = String.format("Location 0x%x Error found: %s", reader.getOffset(), msg);

        throw new FamiTrackerFormatException(msg0);
    }

    protected void handleException(BytesReader reader, String msg, Object... args) throws FamiTrackerFormatException {
        handleException(reader, String.format(msg, args));
    }

    protected void handleException(BytesReader reader, RuntimeException exp)
            throws FamiTrackerFormatException, RuntimeException {
        if (exp instanceof FamiTrackerFormatException) {
            throw exp;
        } else {
            String msg0 = String.format("Location $%x Error found %s: %s", reader.getOffset(),
                    exp.getClass().getSimpleName(), exp.getMessage());
            throw new FamiTrackerFormatException(msg0, exp);
        }
    }

    //
    // other than
    //

    /**
     * Check the header ID. Throw {@link FamiTrackerFormatException} if there is a problem.
     *
     * @param reader
     */
    void validateHeader(BytesReader reader) {
        int len = FILE_HEADER_ID.length();
        byte[] bs_head = new byte[len];
        int i = reader.read(bs_head);

        if (i != len) {
            handleException(reader, EX_GENERAL_WRONG_HEAD);
        }

        byte[] id_head = FILE_HEADER_ID.getBytes();
        for (int j = 0; j < bs_head.length; j++) {
            if (id_head[j] != bs_head[j]) {
                handleException(reader, EX_GENERAL_WRONG_HEAD);
            }
        }
    }

    /**
     * If there is a next block.
     * <p>All blocks, except for the last ending block (END), are defined by:
     * <li>16-byte block identifier
     * <li>4-byte unsigned number, indicating the block version number</li>
     * <li>4-byte unsigned number, indicates block size</li>
     * <li>Data of any size</li>
     */
    public Block nextBlock(BytesReader reader) {
        Block block = new Block();

        byte[] bs = new byte[16];
        int bytesRead = reader.read(bs);

        if (bytesRead == 0) {
            // No data read, means the file has been read.
            return block; // This block is the one that doesn't have an id.
        }

        block.setId(bs);
        if (FILE_END_ID.equals(block.id)) {
            return block; // End identifier, no version, size, data
        }

        block.version = reader.readAsCInt();
        block.setSize(reader.readAsCInt());
        block.blockOffset = reader.getOffset();

        // TODO The original program determines the legitimacy of
        //  version and size, but it is skipped here.

        bytesRead = reader.read(block.bytes());
        if (bytesRead != block.size) {
            throw new FamiTrackerFormatException("Block: " + block.id + " size " + block.size + " but can only read " + bytesRead + " bytes.\n" +
                    "but can only read " + bytesRead + " bytes. The file seems to be corrupted");
        }

        return block;
    }

    /**
     * Use the native type number given in the FTM file to determine what chip it is for the Nsf
     *
     * @param type
     * @return
     */
    public FtmChipType ofInstrumentType(int type) {
        return switch (type) {
            case 1 -> _2A03;
            case 2 -> VRC6;
            case 3 -> VRC7;
            case 4 -> FDS;
            case 5 -> N163;
            case 6 -> S5B;
            default -> null;
        };
    }
}
