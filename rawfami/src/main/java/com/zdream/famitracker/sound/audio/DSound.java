package com.zdream.famitracker.sound.audio;

/**
 * The original project uses Direct Sound, which needs to be changed to javax here
 *
 * @author Zdream
 */
public class DSound {

    public static final byte
            BUFFER_NONE = 0,
            BUFFER_CUSTOM_EVENT = 1,
            BUFFER_TIMEOUT = 2,
            BUFFER_IN_SYNC = 3,
            BUFFER_OUT_OF_SYNC = 4;

    public DSound() {
        // do nothing
    }

    int m_iDevices;

    /**
     * If it returns false, please throw an exception
     *
     * @param iDevice
     */
    public void setupDevice(int iDevice) throws RuntimeException {
        if (iDevice > m_iDevices)
            iDevice = 0;
		
		/*if (m_lpDirectSound) {
			m_lpDirectSound->Release();
			m_lpDirectSound = NULL;
		}

		if (FAILED(DirectSoundCreate((LPCGUID)m_pGUIDs[iDevice], &m_lpDirectSound, NULL))) {
			m_lpDirectSound = NULL;
			return false;
		}

		if (FAILED(m_lpDirectSound->SetCooperativeLevel(m_hWndTarget, DSSCL_PRIORITY))) {
			m_lpDirectSound = NULL;
			return false;
		}

		return true;*/
    }

    public void closeChannel(DSoundChannel m_pDSoundChannel) {
        System.out.println("A method that has not been completed is called: ");
        System.out.println(Thread.currentThread().getStackTrace()[1]);
    }

    public int getDeviceCount() {
        return m_iDevices;
    }

    public DSoundChannel openChannel(int sampleRate, int sampleSize, int i, int bufferLen, int iBlocks) {
        System.out.println("A method that has not been completed is called: ");
        System.out.println(Thread.currentThread().getStackTrace()[1]);
        return null;
    }

}
