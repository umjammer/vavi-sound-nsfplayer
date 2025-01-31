package com.zdream.famitracker.components;

/**
 * TODO
 *
 * @author Zdream
 */
public class Settings {

    private Settings() {
    }

    private static final Settings instance = new Settings();

    public static Settings getInstance() {
        return instance;
    }

    public static class General {

        public final boolean bNoDPCMReset = false;
    }

    public static class Sound {

        public int iDevice = 0;
        public final int iSampleRate = 48000;
        public final int iSampleSize = 16;
        public final int iBufferLength = 40;
        public final int iBassFilter = 30;
        public final int iTrebleFilter = 12000;
        public final int iTrebleDamping = 24;
        public final int iMixVolume = 100;
    }

    /**
     * The default is all zeros.
     */
    public static class ChipLevels {

        public int iLevelAPU1;
        public int iLevelAPU2;
        public int iLevelVRC6;
        public int iLevelVRC7;
        public int iLevelMMC5;
        public int iLevelFDS;
        public int iLevelN163;
        public int iLevelS5B;
    }

    public final General general = new General();
    public final Sound sound = new Sound();
    public final ChipLevels chipLevels = new ChipLevels();
}
