package com.zdream.famitracker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.zdream.famitracker.components.Settings;
import com.zdream.famitracker.sound.SoundGen;


public class FamiTrackerApp {

    private static final FamiTrackerApp app;

    public static final Charset defCharset;

    static {
        defCharset = StandardCharsets.UTF_8;
        app = new FamiTrackerApp();
    }

    public static FamiTrackerApp getInstance() {
        return app;
    }

    public FamiTrackerApp() {
        doc = new FamiTrackerDoc();
        m_pSoundGenerator = new SoundGen();
        m_pSoundGenerator.assignDocument(doc);

        m_pSettings = Settings.getInstance();
        // TODO
    }

    // TODO

    /**
     * Sound synth & player
     */
    SoundGen m_pSoundGenerator;

    public final SoundGen getSoundGenerator() {
        return m_pSoundGenerator;
    }

    // TODO

    /**
     * Here's what I mean.
     */
    FamiTrackerDoc doc;
    Settings m_pSettings;

    public static FamiTrackerDoc getDoc() {
        return getInstance().doc;
    }

    public Settings getSettings() {
        return m_pSettings;
    }

    public boolean isPlaying() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * This is the method I defined to load the FamiTrackerDoc file.
     *
     * @param filename
     */
    public boolean open(String filename) {
        return doc.onOpenDocument(filename);
    }

    /**
     * <p>This is the method I defined to play it. It is called
     * if you have already loaded FamiTrackerDoc.
     * <p>It's definitely a blocking method.
     */
    public void play(int track) {
        m_pSoundGenerator.ready(doc, track, 0);

        while (m_pSoundGenerator.isPlaying()) {
            m_pSoundGenerator.checkFinish();
            m_pSoundGenerator.playFrame();
        }
    }
}
