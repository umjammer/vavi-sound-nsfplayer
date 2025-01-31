package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;


public interface External {

//	protected External(Mixer pMixer) {
//		this.m_pMixer = pMixer;
//	}

    void reset();

    void process(int time);

    void endFrame();

    void write(int address, int value);

    int read(int address);

    boolean isMapped(int address);

    Mixer getMixer();

}
