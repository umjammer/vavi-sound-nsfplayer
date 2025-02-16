package zdream.nsfplayer.nsf.device.cpu;

import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static org.junit.jupiter.api.Assertions.*;


class K6502ContextTest {

    @Test
    void test1() throws Exception {
        K6502Context cpu = new K6502Context();
        int r = cpu.KM_DEC(100);
Debug.println(r);
        assertEquals(99, r);
    }
}