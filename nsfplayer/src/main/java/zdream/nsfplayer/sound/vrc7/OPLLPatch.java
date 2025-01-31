package zdream.nsfplayer.sound.vrc7;

import zdream.nsfplayer.core.IResetable;


public class OPLLPatch implements IResetable {

    public boolean AM, PM, EG, KR;

    /** unsigned */
    public int TL, FB, ML, AR, DR, SL, RR, KL, WF;

    public void copyFrom(OPLLPatch o) {
        this.AM = o.AM;
        this.PM = o.PM;
        this.EG = o.EG;
        this.KR = o.KR;

        this.TL = o.TL;
        this.FB = o.FB;
        this.ML = o.ML;
        this.AR = o.AR;
        this.DR = o.DR;
        this.SL = o.SL;
        this.RR = o.RR;
        this.KL = o.KL;
        this.WF = o.WF;
    }

    @Override
    public String toString() {

        String b = "AM:" + (AM ? 1 : 0) + ',' +
                "PM:" + (PM ? 1 : 0) + ',' +
                "EG:" + (EG ? 1 : 0) + ',' +
                "KR:" + (KR ? 1 : 0) + ',' +
                "TL:" + TL + ',' +
                "FB:" + FB + ',' +
                "ML:" + ML + ',' +
                "AR:" + AR + ',' +
                "DR:" + DR + ',' +
                "SL:" + SL + ',' +
                "RR:" + RR + ',' +
                "KL:" + KL + ',' +
                "WF:" + WF;
        return b;
    }

    @Override
    public void reset() {
        this.AM = false;
        this.PM = false;
        this.EG = false;
        this.KR = false;

        this.TL = 0;
        this.FB = 0;
        this.ML = 0;
        this.AR = 0;
        this.DR = 0;
        this.SL = 0;
        this.RR = 0;
        this.KL = 0;
        this.WF = 0;
    }
}
