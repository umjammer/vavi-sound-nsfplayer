package zdream.nsfplayer.ftm.executor.effect;

/**
 * Ftm Effect Enumeration
 *
 * @author Zdream
 * @since 0.2.1
 */
public enum FtmEffectType {

    /**
     * <p>Fxx
     * <p>Speed adjustment
     * </p>
     *
     * @see SpeedEffect
     */
    SPEED,

    /**
     * <p>Fxx
     * <p>adjusting tempo
     * </p>
     *
     * @see TempoEffect
     */
    TEMPO,

    /**
     * <p>Bxx
     * <p>Jump to the specified section
     * </p>
     *
     * @see JumpEffect
     */
    JUMP,

    /**
     * <p>Dxx
     * <p>Jump to the specified line of the next paragraph
     * </p>
     *
     * @see SkipEffect
     */
    SKIP,

    /**
     * <p>Cxx (Corresponds to the EF_HALT effect)
     * <p>Stop playback directly
     * </p>
     *
     * @see StopEffect
     */
    STOP,

    /**
     * <p>Exx Or modify volume directly
     * to adjust the volume.
     * <p>DPCM, Triangle is invalid
     * </p>
     *
     * @see VolumeEffect
     */
    VOLUME,

    /**
     * <p>Directly modify the pitch or key
     * </p>
     *
     * @see NoteEffect
     * @see NoiseEffect
     */
    NOTE,

    /**
     * <p>Stop the current note playing directly and mute the track
     * </p>
     *
     * @see NoteHaltEffect
     */
    HALT,

    /**
     * <p>Directly release the current key. If the instrument corresponding to
     * the key has a release part, play the release part
     * </p>
     *
     * @see NoteReleaseEffect
     */
    RELEASE,

    /**
     * <p>Modify instruments directly
     * </p>
     *
     * @see InstrumentEffect
     */
    INSTRUMENT,

    /**
     * <p>3xx Continuous Portamento
     * <p>DPCM invalid
     * </p>
     *
     * @see PortamentoOnEffect
     */
    PORTAMENTO,

    /**
     * <p>Hxy (sharp), Ixy (flat), sweep
     * <p>2A03 Pulse unique
     * </p>
     *
     * @see PulseSweepEffect
     */
    SWEEP,

    /**
     * <p>0xx Arpeggios
     * <p>DPCM invalid
     * </p>
     *
     * @see ArpeggioEffect
     */
    ARPEGGIO,

    /**
     * <p>4xy vibrato
     * <p>DPCM invalid
     * </p>
     *
     * @see VibratoEffect
     */
    VIBRATO,

    /**
     * <p>7xy Volume Vibrato
     * <p>DPCM, Triangle is invalid
     * </p>
     *
     * @see TremoloEffect
     */
    TREMOLO,

    /**
     * <p>Pxx Modify the pitch
     * <p>DPCM invalid
     * </p>
     *
     * @see PitchEffect
     */
    PITCH,

    /**
     * <p>Gxx Delay Effect
     * </p>
     *
     * @see DelayEffect
     */
    DELAY,

    /**
     * <p>1xx Portamento up,
     * 2xx Portamento down,
     * <p>DPCM invalid
     * </p>
     *
     * @see PortamentoEffect
     */
    PORTA,

    /**
     * <p>Vxx Modify the sound
     * <p>DPCM invalid
     * </p>
     *
     * @see DutyEffect
     */
    DUTY_CYCLE,

    /**
     * <p>Qxy Slide up, Slide up to the specified key
     * <p>Rxy Slide down, Swipe down to the specified key
     * <p>DPCM invalid
     * </p>
     *
     * @see NoteSlideEffect
     */
    SLIDE,

    /**
     * <p>Axx Volume changes linearly over time
     * <p>Triangle, DPCM invalid
     * </p>
     *
     * @see VolumeSlideEffect
     * @see VRC7VolumeSlideEffect
     */
    VOLUME_SLIDE,

    /**
     * <p>Sxx Cutting effect
     * </p>
     *
     * @see CutEffect
     */
    NOTE_CUT,

    /**
     * <p>Zxx, Setting the DAC Value
     * <p>DPCM Unique
     * </p>
     *
     * @see DPCM_DACSettingEffect
     */
    DAC,

    /**
     * <p>Yxx, Set the start reading position offset.
     * <p>DPCM Unique
     * </p>
     *
     * @see DPCMSampleOffsetEffect
     */
    SAMPLE_OFFSET,

    /**
     * <p>Xxx, Re-trigger sampling, i.e. cycle once.
     * <p>DPCM Unique
     * </p>
     *
     * @see DPCMRetriggerEffect
     */
    RETRIGGER,

    /**
     * Wxx, Set the pitch of the DPCM samples
     * <p>DPCM Unique
     * </p>
     *
     * @see DPCMPitchEffect
     */
    DPCM_PITCH,

    /**
     * Hxx, Set the source depth of the FDS frequency modulator
     * <p>FDS Unique
     * </p>
     *
     * @see FDSModDepthEffect
     */
    FDS_MOD_DEPTH,

    /**
     * Ixx, Set FDS modulator frequency high 4 bits
     * <p>FDS Unique
     * </p>
     *
     * @see FDSModSpeedHighEffect
     */
    FDS_MOD_SPEED_HIGH,

    /**
     * Jxx, Set FDS modulator frequency low 8 bits
     * <p>FDS Unique
     * </p>
     *
     * @see FDSModSpeedLowEffect
     */
    FDS_MOD_SPEED_LOW,

    /**
     * Hxx
     * TODO
     */
    SUNSOFT_ENV_LO,

    /**
     * Ixx
     * TODO
     */
    SUNSOFT_ENV_HI,

    /**
     * Jxx
     * TODO
     */
    SUNSOFT_ENV_TYPE
}
