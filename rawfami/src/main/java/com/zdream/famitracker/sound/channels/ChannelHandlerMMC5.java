package com.zdream.famitracker.sound.channels;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.Instrument2A03;

import static com.zdream.famitracker.FamitrackerTypes.EF_DUTY_CYCLE;
import static com.zdream.famitracker.FamitrackerTypes.EF_NONE;
import static com.zdream.famitracker.FamitrackerTypes.EF_SLIDE_DOWN;
import static com.zdream.famitracker.FamitrackerTypes.EF_SLIDE_UP;
import static com.zdream.famitracker.FamitrackerTypes.EF_VOLUME;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_HALT;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_NONE;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_RELEASE;
import static com.zdream.famitracker.FamitrackerTypes.SEQ_ARPEGGIO;
import static com.zdream.famitracker.FamitrackerTypes.SEQ_DUTYCYCLE;
import static com.zdream.famitracker.FamitrackerTypes.SEQ_HIPITCH;
import static com.zdream.famitracker.FamitrackerTypes.SEQ_PITCH;
import static com.zdream.famitracker.FamitrackerTypes.SEQ_VOLUME;
import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_NONE;


/**
 * MMC5 track parent class
 *
 * @author Zdream
 */
public abstract class ChannelHandlerMMC5 extends ChannelHandler {

    public ChannelHandlerMMC5() {
        super(0x7FF, 0x0F);
    }

    @Override
    protected void handleNoteData(StChanNote pNoteData, int EffColumns) {
        m_iPostEffect = 0;
        m_iPostEffectParam = 0;
        m_bManualVolume = false;
        m_iInitVolume = 0x0F;

        super.handleNoteData(pNoteData, EffColumns);

        if (pNoteData.note != NOTE_NONE && pNoteData.note != NOTE_HALT && pNoteData.note != NOTE_RELEASE) {
            if (m_iPostEffect != 0 && (m_iEffect == EF_SLIDE_UP || m_iEffect == EF_SLIDE_DOWN))
                setupSlide(m_iPostEffect, m_iPostEffectParam);
            else if (m_iEffect == EF_SLIDE_DOWN || m_iEffect == EF_SLIDE_UP)
                m_iEffect = EF_NONE;
        }
    }

    @Override
    protected void handleCustomEffects(int effNum, int effParam) {
        if (!checkCommonEffects(effNum, effParam)) {
            switch (effNum) {
                case EF_VOLUME:
                    m_iInitVolume = effParam;
                    m_bManualVolume = true;
                    break;
                case EF_DUTY_CYCLE:
                    m_iDefaultDuty = m_iDutyPeriod = (byte) effParam;
                    break;
                case EF_SLIDE_UP:
                case EF_SLIDE_DOWN:
                    m_iPostEffect = effNum;
                    m_iPostEffectParam = effParam;
                    setupSlide(effNum, effParam);
                    break;
            }
        }
    }

    @Override
    protected boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument) {
        FamiTrackerDoc pDocument = m_pSoundGen.getDocument();

        // After modification here, the InstrumentContainer class is cancelled
        Instrument inst = pDocument.getInstrument(instrument);
        if (inst == null || !(inst instanceof Instrument2A03 pInstrument)) {
            return false;
        }

        for (int i = 0; i < Instrument2A03.SEQUENCE_COUNT; ++i) {
            Sequence pSequence = pDocument.getSequence(SNDCHIP_NONE, pInstrument.getSeqIndex(i), i);
            if (trigger || !isSequenceEqual(i, pSequence) || pInstrument.getSeqEnable(i) > getSequenceState(i)) {
                if (pInstrument.getSeqEnable(i) == 1)
                    setupSequence(i, pSequence);
                else
                    clearSequence(i);
            }
        }

        return true;
    }

    @Override
    protected void handleEmptyNote() {
        // do nothing
    }

    @Override
    protected void handleCut() {
        cutNote();
    }

    @Override
    protected void handleRelease() {
        if (!m_bRelease) {
            releaseNote();
            releaseSequences();
        }
    }

    @Override
    protected void handleNote(int note, int octave) {
        m_iNote = runNote(octave, note);
        m_iDutyPeriod = m_iDefaultDuty;
        m_iSeqVolume = m_iInitVolume;
    }

    @Override
    public void processChannel() {
        // FamiTrackerDoc pDocument = m_pSoundGen.getDocument();

        // Default effects
        super.processChannel();

        // Sequences
        for (int i = 0; i < SEQUENCES; ++i)
            runSequence(i);
    }

    protected static final int SEQUENCES = 5;
    protected static final int[] SEQ_TYPES = {SEQ_VOLUME, SEQ_ARPEGGIO, SEQ_PITCH, SEQ_HIPITCH, SEQ_DUTYCYCLE};

    /**
     * TODO Unknown meaning
     */
    protected int m_iPostEffect;

    /**
     * TODO Unknown meaning
     */
    protected int m_iPostEffectParam;

    /**
     * TODO Unknown meaning
     */
    protected int m_iInitVolume;

    /**
     * TODO Unknown meaning
     */
    protected boolean m_bManualVolume;

}
