package zdream.nsfplayer.ftm.executor.context;

import java.util.Map;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.executor.effect.FtmEffectType;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.format.FtmNote;


/**
 * Ftm effect converter interface, used to convert {@link FtmNote} to {@link IFtmEffect} collection.
 * It is a tool needed in an FTM runtime environment.
 *
 * @author Zdream
 * @version v0.2.9
 * Redefines the class as a tool rather than a component, so that subclasses that implement
 * the interface are no longer forced to implement the IFtmRuntimeHolder interface.
 * @since v0.2.1
 */
public interface IFtmEffectConverter extends INsfChannelCode {

    /**
     * To implement a transformation, transform the effect as {@link IFtmEffect}
     * and put it into effects or geffects.
     *
     * @param note        Examples of keys
     * @param channelType The type of the channel the key is on. See {@link INsfChannelCode}
     *                    for byte constants starting with CHANNEL_TYPE_.
     * @param effects     The track's effect set
     * @param geffects    Effect sets for global tracks
     * @param querier     checker
     * @since v0.2.9
     */
    void convert(
            FtmNote note,
            byte channelType,
            Map<FtmEffectType, IFtmEffect> effects,
            Map<FtmEffectType, IFtmEffect> geffects,
            FamiTrackerQuerier querier);
}
