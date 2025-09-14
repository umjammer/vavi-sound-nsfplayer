/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.sampled.nsfz;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import vavi.io.OutputEngine;
import vavi.io.OutputEngineInputStream;
import zdream.nsfplayer.nsf.audio.StreamNsfAudio;
import zdream.nsfplayer.nsf.audio.StreamNsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;

import static java.lang.System.getLogger;


/**
 * Nsf2PcmAudioInputStream.
 * <pre>
 *  property
 *   track = number
 * </pre>
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2025/08/24 umjammer initial version <br>
 */
public class Nsf2PcmAudioInputStream extends AudioInputStream {

    private static final Logger logger = getLogger(Nsf2PcmAudioInputStream.class.getName());

    /** use format's properties */
    public Nsf2PcmAudioInputStream(InputStream stream, AudioFormat format, long length) throws IOException {
        this(stream, format, length, format.properties());
    }

    /** format's properties are ignored */
    public Nsf2PcmAudioInputStream(InputStream stream, AudioFormat format, long length, Map<String, Object> props) throws IOException {
        super(new OutputEngineInputStream(new NSFOutputEngine(stream, props)), format, length);
    }

    /** */
    private static class NSFOutputEngine implements OutputEngine {

        /** */
        private OutputStream out;

        /** */
        private final StreamNsfAudio nsf;

        /** */
        private NsfRenderer<InputStream> renderer;

        private final Map<String, Object> props;

        /** */
        public NSFOutputEngine(InputStream in, Map<String, Object> props) throws IOException {
            StreamNsfAudioFactory factory = new StreamNsfAudioFactory();
            this.nsf = factory.create(in);

            this.props = props;
        }

        private int trackNumber = 1;

        @Override
        public void initialize(OutputStream out) throws IOException {
            if (this.out != null) {
                throw new IOException("Already initialized");
            } else {
                this.out = new BufferedOutputStream(out);
            }

            renderer = new NsfRenderer<>();

            if (props.containsKey("track")) {
                int t = (int) props.get("track");
                if (trackNumber >= 1) {
                    trackNumber = t;
                }
            }

            renderer.ready(nsf, trackNumber);
        }

        static final int BUFFER_SIZE = 1600;

        private byte[] b = new byte[BUFFER_SIZE];

        @Override
        public void execute() throws IOException {
            int r = renderer.renderOneFrame(b, 0, b.length);
            if (r > 0) {
                // The renderer produces 16-bit mono audio data.
                // We convert it to 16-bit stereo by duplicating the mono channel.
                for (int i = 0; i < r; i++) {
                    out.write(b[i]);
                }
            } else {
                out.flush();
                out.close();
            }
        }

        @Override
        public void finish() throws IOException {
logger.log(Level.DEBUG, "engine finish");
        }
    }
}
