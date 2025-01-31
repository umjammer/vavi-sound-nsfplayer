package zdream.nsfplayer.ftm.executor.context;

import java.util.Map;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.executor.effect.*;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

import static zdream.nsfplayer.core.FtmChipType.FDS;
import static zdream.nsfplayer.core.FtmChipType.N163;
import static zdream.nsfplayer.core.FtmChipType.S5B;
import static zdream.nsfplayer.core.FtmChipType.VRC6;
import static zdream.nsfplayer.core.FtmChipType.VRC7;
import static zdream.nsfplayer.core.FtmChipType._2A03;
import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;
import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;
import static zdream.nsfplayer.ftm.format.FtmNote.*;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_EFFECT_COLUMNS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;


/**
 * Default Ftm effect converter interface.
 * <p>In the default playback environment, a {@link FamiTrackerRenderer} has only one of these converters.
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */
public class DefaultFtmEffectConverter implements IFtmEffectConverter {

    //
    // conversions
    //

    @Override
    public void convert(
            FtmNote note,
            byte channelType,
            Map<FtmEffectType, IFtmEffect> effects,
            Map<FtmEffectType, IFtmEffect> geffects,
            FamiTrackerQuerier querier) {

        if (note == null) {
            return;
        }

        if (note.note != NOTE_NONE) {
            if (channelType == CHANNEL_TYPE_NOISE) {
                handleNoise(note, effects);
            } else {
                handleNote(channelType, note, effects);
            }
        }

        if (note.vol != MAX_VOLUME && channelType != CHANNEL_TYPE_TRIANGLE) {
            // Triangle wave tracks ignore the volume column
            putEffect(effects, VolumeEffect.of(note.vol));
        }

        if (note.instrument != MAX_INSTRUMENTS) {
            // If note is a release or a halt, it won't work either.
            if (note.note != NOTE_RELEASE && note.note != NOTE_HALT)
                handleInst(channelType, note, effects, querier);
        }

        // Other effects
        handleEffect(channelType, note, effects, geffects, querier);
    }

    /**
     * Putting in the effect
     *
     * @param effects
     * @param effect
     */
    private void putEffect(Map<FtmEffectType, IFtmEffect> effects, IFtmEffect effect) {
        if (effect == null) {
            return;
        }

        effects.put(effect.type(), effect);
    }

    /**
     * Put in global effect
     *
     * @param effect
     */
    private void putGlobalEffect(IFtmEffect effect, Map<FtmEffectType, IFtmEffect> geffects) {
        geffects.put(effect.type(), effect);
    }

    /**
     * Processing Note Parts (Noise Tracks)
     *
     * @param note
     * @param effects
     */
    private void handleNoise(FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
        if (note.note == NOTE_RELEASE) {
            putEffect(effects, NoteReleaseEffect.of());
        } else if (note.note == NOTE_HALT) {
            putEffect(effects, NoteHaltEffect.of());
        } else {
            int noise = (note.octave * 12 + note.note) & 0xF;
            if (noise == 0) {
                noise = 16;
            }
            putEffect(effects, NoiseEffect.of(noise));
        }
    }

    /**
     * Processing of note parts (non-noise tracks)
     *
     * @param channelType channel type
     * @param note
     * @param effects
     */
    private void handleNote(byte channelType, FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
        if (note.note == NOTE_RELEASE) {
            putEffect(effects, NoteReleaseEffect.of());
        } else if (note.note == NOTE_HALT) {
            putEffect(effects, NoteHaltEffect.of());
        } else {
            putEffect(effects, NoteEffect.of(note.octave, note.note));
        }
    }

    /**
     * Handling of instrumental parts
     *
     * @param channelType channel type
     * @param note
     * @param effects
     * @param querier
     */
    private void handleInst(
            byte channelType,
            FtmNote note,
            Map<FtmEffectType, IFtmEffect> effects,
            FamiTrackerQuerier querier) {
        int inst = note.instrument;
        if (inst < 0) {
            return;
        }

        // Check if the chip the channel belongs to can apply the instrument
        boolean valid = false;
        FtmChipType type = querier.getInstrumentType(inst);
        valid = switch (chipOfChannel(channelType)) {
            case CHIP_2A03, CHIP_2A07, CHIP_MMC5 -> type == _2A03;
            case CHIP_VRC6 -> type == VRC6;
            case CHIP_FDS -> type == FDS;
            case CHIP_N163 -> type == N163;
            case CHIP_VRC7 -> type == VRC7;
            case CHIP_S5B -> type == S5B;
            default -> valid;
        };

        if (valid) {
            putEffect(effects, InstrumentEffect.of(inst));
        } else {
            putEffect(effects, NoteHaltEffect.of());
        }
    }

    /**
     * Handling of other effects sections
     *
     * @param channelType channel type
     * @param note
     * @param effects
     * @param geffects
     */
    private void handleEffect(
            byte channelType,
            FtmNote note,
            Map<FtmEffectType, IFtmEffect> effects,
            Map<FtmEffectType, IFtmEffect> geffects,
            FamiTrackerQuerier querier) {
        short delay = 0;

        for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
            switch (note.effNumber[i]) {
                case EF_NONE:
                    continue;

                    // overall situation

                case EF_JUMP:
                    putGlobalEffect(JumpEffect.of(note.effParam[i]), geffects);
                    break;

                case EF_SKIP:
                    putGlobalEffect(SkipEffect.of(note.effParam[i]), geffects);
                    break;

                case EF_SPEED: {
                    int speed = note.effParam[i];
                    if (speed >= querier.audio.getSplit()) {
                        putGlobalEffect(TempoEffect.of(speed), geffects);
                    } else {
                        putGlobalEffect(SpeedEffect.of(speed), geffects);
                    }
                }
                break;

                case EF_HALT:
                    putGlobalEffect(StopEffect.of(), geffects);
                    break;

                // channels

                case EF_PITCH: // Pxx
                    if (channelType != CHANNEL_TYPE_DPCM)
                        putEffect(effects, PitchEffect.of(note.effParam[i] - 0x80));
                    break;

                case EF_DELAY: // Gxx
                    delay = note.effParam[i]; // Deferred processing
                    break;

                case EF_DUTY_CYCLE: // Vxx
                    if (channelType != CHANNEL_TYPE_DPCM)
                        putEffect(effects, DutyEffect.of(note.effParam[i]));
                    break;

                case EF_VOLUME_SLIDE: // Axx
                    if (channelType == CHANNEL_TYPE_DPCM || channelType == CHANNEL_TYPE_TRIANGLE) {
                        break;
                    }

                {
                    int param = note.effParam[i];
                    if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
                        if ((param & 0xF) != 0) { // up to 0
                            putEffect(effects, VRC7VolumeSlideEffect.of((param & 0xF)));
                        } else { // down
                            putEffect(effects, VRC7VolumeSlideEffect.of((param >> 4) * -1));
                        }
                    } else {
                        if ((param & 0xF) != 0) { // down to 0
                            putEffect(effects, VolumeSlideEffect.of((param & 0xF) * -2));
                        } else { // up
                            putEffect(effects, VolumeSlideEffect.of((param >> 4) * 2));
                        }
                    }
                }
                break;

                case EF_SLIDE_UP:
                case EF_SLIDE_DOWN: // Qxy Rxy
                {
                    if (channelType == CHANNEL_TYPE_DPCM) {
                        break;
                    }

                    short param = note.effParam[i];
                    int delta = param & 0xF;

                    param >>= 4;
                    boolean up = note.effNumber[i] == EF_SLIDE_UP;
                    int speed;
                    if (channelType == CHANNEL_TYPE_NOISE) {
                        speed = (param == 0) ? 1 : param;
                    } else if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
                        speed = (param << 3) + 1;
                    } else {
                        speed = (param << 1) + 1;
                    }

                    if (!up) {
                        delta *= -1;
                    }

                    putEffect(effects, NoteSlideEffect.of(delta, speed));
                }
                break;

                case EF_VIBRATO: // 4xy
                    if (channelType != CHANNEL_TYPE_DPCM) {
                        int param = note.effParam[i];
                        putEffect(effects, VibratoEffect.of(param >> 4, param & 0xF));
                    }
                    break;

                case EF_TREMOLO: // 7xy
                    if (channelType != CHANNEL_TYPE_DPCM && channelType != CHANNEL_TYPE_TRIANGLE) {
                        int param = note.effParam[i];
                        putEffect(effects, TremoloEffect.of(param >> 4, param & 0xF));
                    }
                    break;

                case EF_PORTA_UP: // 1xx
                    if (channelType != CHANNEL_TYPE_DPCM) {
                        int speed;
                        if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
                            // N163 modifies the reciprocal of the wavelength, is increasing,
                            // and the absolute value of the data is high.
                            speed = (note.effParam[i] << 2);
                        } else if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
                            // VRC7 modifies the reciprocal of the wavelength, which is increased.
                            speed = note.effParam[i];
                        } else {
                            // The other orbital modifications are wavelengths. The wavelengths are reduced.
                            speed = -note.effParam[i];
                        }
                        putEffect(effects, PortamentoEffect.of(speed));
                    }
                    break;

                case EF_PORTA_DOWN: // 2xx
                    if (channelType != CHANNEL_TYPE_DPCM) {
                        int speed;
                        if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
                            // N163 modifies the reciprocal of the wavelength, is decreasing,
                            // and the absolute value of the data is high.
                            speed = -(note.effParam[i] << 2);
                        } else if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
                            // VRC7 modifies the reciprocal of the wavelength, which is increased.
                            speed = -note.effParam[i];
                        } else {
                            // The other orbits modify the wavelength. The wavelength is increased.
                            speed = note.effParam[i];
                        }
                        putEffect(effects, PortamentoEffect.of(speed));
                    }
                    break;

                case EF_NOTE_CUT: // Sxx
                    putEffect(effects, CutEffect.of(note.effParam[i]));
                    break;

                case EF_ARPEGGIO: // 0xy
                    if (channelType != CHANNEL_TYPE_DPCM) {
                        int param = note.effParam[i];
                        putEffect(effects, ArpeggioEffect.of(param >> 4, param & 0xF));
                    }
                    break;

                case EF_PORTAMENTO: // 3xx
                    if (channelType != CHANNEL_TYPE_DPCM) {
                        int speed;
                        if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
                            speed = (note.effParam[i] << 2);
                        } else {
                            speed = note.effParam[i];
                        }
                        putEffect(effects, PortamentoOnEffect.of(speed));
                    }
                    break;

                // 2A03 Pulse channel

                case EF_SWEEPUP:
                    if (channelType == CHANNEL_TYPE_PULSE) {
                        short param = note.effParam[i];
                        putEffect(effects, PulseSweepEffect.of((param >> 4) & 7, param & 7, true));
                    }
                    break;

                case EF_SWEEPDOWN:
                    if (channelType == CHANNEL_TYPE_PULSE) {
                        short param = note.effParam[i];
                        putEffect(effects, PulseSweepEffect.of((param >> 4) & 7, param & 7, false));
                    }
                    break;

                // DPCM channel

                case EF_DAC: // Zxx
                    if (channelType == CHANNEL_TYPE_DPCM) {
                        putEffect(effects, DPCM_DACSettingEffect.of(note.effParam[i] & 0x7F));
                    }
                    break;

                case EF_SAMPLE_OFFSET: // Yxx
                    if (channelType == CHANNEL_TYPE_DPCM) {
                        putEffect(effects, DPCMSampleOffsetEffect.of(note.effParam[i] * 64));
                    }
                    break;

                case EF_RETRIGGER: // Xxx
                    if (channelType == CHANNEL_TYPE_DPCM) {
                        putEffect(effects, DPCMRetriggerEffect.of(note.effParam[i]));
                    }
                    break;

                case EF_DPCM_PITCH:
                    if (channelType == CHANNEL_TYPE_DPCM) {
                        putEffect(effects, DPCMPitchEffect.of(note.effParam[i] & 0xF));
                    }
                    break;

                // FDS channel

                case EF_FDS_MOD_DEPTH: // Hxx
                    if (channelType == CHANNEL_TYPE_FDS) {
                        putEffect(effects, FDSModDepthEffect.of(note.effParam[i] & 0x3F));
                    }
                    break;

                case EF_FDS_MOD_SPEED_HI: // Ixx
                    if (channelType == CHANNEL_TYPE_FDS) {
                        putEffect(effects, FDSModSpeedHighEffect.of(note.effParam[i] & 15));
                    }
                    break;

                case EF_FDS_MOD_SPEED_LO: // Jxx
                    if (channelType == CHANNEL_TYPE_FDS) {
                        putEffect(effects, FDSModSpeedLowEffect.of(note.effParam[i] & 0xFF));
                    }
                    break;

                // TODO Other effects

                default:
                    break;
            }
        }

        // Processing of delayed parts Gxx
        if (delay != 0) {
            DelayEffect d = DelayEffect.of(delay, effects.values());
            effects.clear();
            putEffect(effects, d);
        }
    }
}
