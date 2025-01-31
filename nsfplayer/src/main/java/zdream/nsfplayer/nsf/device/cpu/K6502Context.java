/*
 * Portable 6502/65c02/HuC6280 emulator 'KM6502'
 *
 * License PDS
 */

package zdream.nsfplayer.nsf.device.cpu;

import java.lang.System.Logger;

import static java.lang.System.getLogger;


public final class K6502Context {

    private static final Logger logger = getLogger(K6502Context.class.getName());

    /**
     * Accumulator
     */
    public int a;
    /**
     * Status register
     */
    public int p;
    /**
     * X register
     */
    public int x;
    /**
     * Y register
     */
    public int y;
    /**
     * Stack pointer
     */
    public int s;
    /**
     * Program Counter
     */
    public int pc;
    /**
     * interrupt request
     */
    public int iRequest;
    /**
     * interrupt mask
     */
    public int iMask;
    /**
     * (incremental) cycle counter
     */
    public int clock;
    public int lastCode;

    public int illegal;

    @Override
    public String toString() {
        return "K6502Context [a=" + a + ", p=" + p + ", x=" + x + ", y=" + y + ", s=" + s + ", pc=" + pc + ", iRequest="
                + iRequest + ", iMask=" + (iMask & 0x7fff_ffff) + ", clock=" + clock + ", lastCode=" + lastCode
                + ", illegal=" + (illegal == 1) + "]";
    }

    public interface ReadHandler {

        int handler(int adr);
    }

    public interface WriteHandler {

        void handler(int adr, int value);
    }

    public ReadHandler readByte;
    public WriteHandler writeByte;

    /**
     * enum K65C02_FLAGS
     */
    public static final int
            T_FLAG = 0,
            C_FLAG = 0x01,
            Z_FLAG = 0x02,
            I_FLAG = 0x04,
            D_FLAG = 0x08,
            B_FLAG = 0x10,
            R_FLAG = 0x20,
            V_FLAG = 0x40,
            N_FLAG = 0x80;

    /**
     * enum K65C02_IRQ
     */
    public static final int
            K6502_INIT = 1,
            K6502_RESET = 2,
            K6502_NMI = 4,
            K6502_BRK = 8,
            K6502_INT = 16;

    public static final int VEC_RESET = 0xfffc,
            VEC_NMI = 0xfffa,
            VEC_INT = 0xfffe;

    public void exec() {
        if (iRequest != 0) {
            if ((iRequest & K6502_INIT) != 0) {
                a = 0;
                x = 0;
                y = 0;
                s = 0xff;
                p = Z_FLAG | R_FLAG | I_FLAG;
                iRequest = 0;
                iMask = ~0;
                KI_ADDCLOCK(7);
                return;
            } else if ((iRequest & K6502_RESET) != 0) {
                a = 0;
                x = 0;
                y = 0;
                s = 0xff;
                p = Z_FLAG | R_FLAG | I_FLAG;
                pc = KI_READWORD(VEC_RESET);
                iRequest = 0;
                iMask = ~0;
            } else if ((iRequest & K6502_NMI) != 0) {
                KM_PUSH(0xff & (pc >> 8));
                KM_PUSH(0xff & pc);
                KM_PUSH(p | R_FLAG | B_FLAG);
                p = (p & ~T_FLAG) | I_FLAG; // 6502 bug
                iRequest &= ~(K6502_NMI | K6502_BRK);
                pc = KI_READWORD(VEC_NMI);
                KI_ADDCLOCK(7);
            } else if ((iRequest & K6502_BRK) != 0) {
                KM_PUSH(0xff & (pc >> 8));
                KM_PUSH(0xff & pc);
                KM_PUSH(p | R_FLAG | B_FLAG);
                p = (p & ~T_FLAG) | I_FLAG; // 6502 bug
                iRequest &= ~K6502_BRK;
                pc = KI_READWORD(VEC_INT);
                KI_ADDCLOCK(7);
            } else if ((p & I_FLAG) != 0) {
                // interrupt disabled
            } else if ((iMask & iRequest & K6502_INT) != 0) {
                KM_PUSH(0xff & (pc >> 8));
                KM_PUSH(0xff & pc);
                KM_PUSH((p | R_FLAG) & ~B_FLAG);
                p = (p & ~T_FLAG) | I_FLAG; // 6502 bug
                iRequest &= ~K6502_INT;
                pc = KI_READWORD(VEC_INT);
                KI_ADDCLOCK(7);
            }
        }
        K_OPEXEC();
    }

//int CC;
    // km6502ot.h
    void K_OPEXEC() {
        int opCode = lastCode = K_READ(KAI_IMM());
        KI_ADDCLOCK(cl_table[opCode]);
//if (CC++ < 300) { logger.log(Level.DEBUG, "pc: %04x: op: %02x".formatted(pc, opCode)); }
//else { System.exit(0); }

        switch (opCode) {
            case 0x00:
                opCode00();
                break;
            case 0x01:
                opCode01();
                break;
            case 0x02:
                opCode02();
                illegal = 1;
                break;
            case 0x03:
                opCode03();
                illegal = 1;
                break;
            case 0x04:
                opCode04();
                illegal = 1;
                break;
            case 0x05:
                opCode05();
                break;
            case 0x06:
                opCode06();
                break;
            case 0x07:
                opCode07();
                illegal = 1;
                break;
            case 0x08:
                opCode08();
                break;
            case 0x09:
                opCode09();
                break;
            case 0x0A:
                opCode0A();
                break;
            case 0x0B:
                opCode0B();
                illegal = 1;
                break;
            case 0x0C:
                opCode0C();
                illegal = 1;
                break;
            case 0x0D:
                opCode0D();
                break;
            case 0x0E:
                opCode0E();
                break;
            case 0x0F:
                opCode0F();
                illegal = 1;
                break;

            case 0x10:
                opCode10();
                break;
            case 0x11:
                opCode11();
                break;
            case 0x12:
                opCode12();
                illegal = 1;
                break;
            case 0x13:
                opCode13();
                illegal = 1;
                break;
            case 0x14:
                opCode14();
                illegal = 1;
                break;
            case 0x15:
                opCode15();
                break;
            case 0x16:
                opCode16();
                break;
            case 0x17:
                opCode17();
                illegal = 1;
                break;
            case 0x18:
                opCode18();
                break;
            case 0x19:
                opCode19();
                break;
            case 0x1A:
                opCode1A();
                illegal = 1;
                break;
            case 0x1B:
                opCode1B();
                illegal = 1;
                break;
            case 0x1C:
                opCode1C();
                illegal = 1;
                break;
            case 0x1D:
                opCode1D();
                break;
            case 0x1E:
                opCode1E();
                break;
            case 0x1F:
                opCode1F();
                illegal = 1;
                break;

            case 0x20:
                opCode20();
                break;
            case 0x21:
                opCode21();
                break;
            case 0x22:
                opCode22();
                illegal = 1;
                break;
            case 0x23:
                opCode23();
                illegal = 1;
                break;
            case 0x24:
                opCode24();
                break;
            case 0x25:
                opCode25();
                break;
            case 0x26:
                opCode26();
                break;
            case 0x27:
                opCode27();
                illegal = 1;
                break;
            case 0x28:
                opCode28();
                break;
            case 0x29:
                opCode29();
                break;
            case 0x2A:
                opCode2A();
                break;
            case 0x2B:
                opCode2B();
                illegal = 1;
                break;
            case 0x2C:
                opCode2C();
                break;
            case 0x2D:
                opCode2D();
                break;
            case 0x2E:
                opCode2E();
                break;
            case 0x2F:
                opCode2F();
                illegal = 1;
                break;

            case 0x30:
                opCode30();
                break;
            case 0x31:
                opCode31();
                break;
            case 0x32:
                opCode32();
                illegal = 1;
                break;
            case 0x33:
                opCode33();
                illegal = 1;
                break;
            case 0x34:
                opCode34();
                illegal = 1;
                break;
            case 0x35:
                opCode35();
                break;
            case 0x36:
                opCode36();
                break;
            case 0x37:
                opCode37();
                illegal = 1;
                break;
            case 0x38:
                opCode38();
                break;
            case 0x39:
                opCode39();
                break;
            case 0x3A:
                opCode3A();
                illegal = 1;
                break;
            case 0x3B:
                opCode3B();
                illegal = 1;
                break;
            case 0x3C:
                opCode3C();
                illegal = 1;
                break;
            case 0x3D:
                opCode3D();
                break;
            case 0x3E:
                opCode3E();
                break;
            case 0x3F:
                opCode3F();
                illegal = 1;
                break;

            case 0x40:
                opCode40();
                break;
            case 0x41:
                opCode41();
                break;
            case 0x42:
                opCode42();
                illegal = 1;
                break;
            case 0x43:
                opCode43();
                illegal = 1;
                break;
            case 0x44:
                opCode44();
                illegal = 1;
                break;
            case 0x45:
                opCode45();
                break;
            case 0x46:
                opCode46();
                break;
            case 0x47:
                opCode47();
                illegal = 1;
                break;
            case 0x48:
                opCode48();
                break;
            case 0x49:
                opCode49();
                break;
            case 0x4A:
                opCode4A();
                break;
            case 0x4B:
                opCode4B();
                illegal = 1;
                break;
            case 0x4C:
                opCode4C();
                break;
            case 0x4D:
                opCode4D();
                break;
            case 0x4E:
                opCode4E();
                break;
            case 0x4F:
                opCode4F();
                illegal = 1;
                break;

            case 0x50:
                opCode50();
                break;
            case 0x51:
                opCode51();
                break;
            case 0x52:
                opCode52();
                illegal = 1;
                break;
            case 0x53:
                opCode53();
                illegal = 1;
                break;
            case 0x54:
                opCode54();
                illegal = 1;
                break;
            case 0x55:
                opCode55();
                break;
            case 0x56:
                opCode56();
                break;
            case 0x57:
                opCode57();
                illegal = 1;
                break;
            case 0x58:
                opCode58();
                break;
            case 0x59:
                opCode59();
                break;
            case 0x5A:
                opCode5A();
                illegal = 1;
                break;
            case 0x5B:
                opCode5B();
                illegal = 1;
                break;
            case 0x5C:
                opCode5C();
                illegal = 1;
                break;
            case 0x5D:
                opCode5D();
                break;
            case 0x5E:
                opCode5E();
                break;
            case 0x5F:
                opCode5F();
                illegal = 1;
                break;

            case 0x60:
                opCode60();
                break;
            case 0x61:
                opCode61();
                break;
            case 0x62:
                opCode62();
                illegal = 1;
                break;
            case 0x63:
                opCode63();
                illegal = 1;
                break;
            case 0x64:
                opCode64();
                illegal = 1;
                break;
            case 0x65:
                opCode65();
                break;
            case 0x66:
                opCode66();
                break;
            case 0x67:
                opCode67();
                illegal = 1;
                break;
            case 0x68:
                opCode68();
                break;
            case 0x69:
                opCode69();
                break;
            case 0x6A:
                opCode6A();
                break;
            case 0x6B:
                opCode6B();
                illegal = 1;
                break;
            case 0x6C:
                opCode6C();
                break;
            case 0x6D:
                opCode6D();
                break;
            case 0x6E:
                opCode6E();
                break;
            case 0x6F:
                opCode6F();
                illegal = 1;
                break;

            case 0x70:
                opCode70();
                break;
            case 0x71:
                opCode71();
                break;
            case 0x72:
                opCode72();
                illegal = 1;
                break;
            case 0x73:
                opCode73();
                illegal = 1;
                break;
            case 0x74:
                opCode74();
                illegal = 1;
                break;
            case 0x75:
                opCode75();
                break;
            case 0x76:
                opCode76();
                break;
            case 0x77:
                opCode77();
                illegal = 1;
                break;
            case 0x78:
                opCode78();
                break;
            case 0x79:
                opCode79();
                break;
            case 0x7A:
                opCode7A();
                illegal = 1;
                break;
            case 0x7B:
                opCode7B();
                illegal = 1;
                break;
            case 0x7C:
                opCode7C();
                illegal = 1;
                break;
            case 0x7D:
                opCode7D();
                break;
            case 0x7E:
                opCode7E();
                break;
            case 0x7F:
                opCode7F();
                illegal = 1;
                break;

            case 0x80:
                opCode80();
                illegal = 1;
                break;
            case 0x81:
                opCode81();
                break;
            case 0x82:
                opCode82();
                illegal = 1;
                break;
            case 0x83:
                opCode83();
                illegal = 1;
                break;
            case 0x84:
                opCode84();
                break;
            case 0x85:
                opCode85();
                break;
            case 0x86:
                opCode86();
                break;
            case 0x87:
                opCode87();
                illegal = 1;
                break;
            case 0x88:
                opCode88();
                break;
            case 0x89:
                opCode89();
                illegal = 1;
                break;
            case 0x8A:
                opCode8A();
                break;
            case 0x8B:
                opCode8B();
                illegal = 1;
                break;
            case 0x8C:
                opCode8C();
                break;
            case 0x8D:
                opCode8D();
                break;
            case 0x8E:
                opCode8E();
                break;
            case 0x8F:
                opCode8F();
                illegal = 1;
                break;

            case 0x90:
                opCode90();
                break;
            case 0x91:
                opCode91();
                break;
            case 0x92:
                opCode92();
                illegal = 1;
                break;
            case 0x93:
                opCode93();
                illegal = 1;
                break;
            case 0x94:
                opCode94();
                break;
            case 0x95:
                opCode95();
                break;
            case 0x96:
                opCode96();
                break;
            case 0x97:
                opCode97();
                illegal = 1;
                break;
            case 0x98:
                opCode98();
                break;
            case 0x99:
                opCode99();
                break;
            case 0x9A:
                opCode9A();
                break;
            case 0x9B:
                opCode9B();
                illegal = 1;
                break;
            case 0x9C:
                opCode9C();
                illegal = 1;
                break;
            case 0x9D:
                opCode9D();
                break;
            case 0x9E:
                opCode9E();
                illegal = 1;
                break;
            case 0x9F:
                opCode9F();
                illegal = 1;
                break;

            case 0xA0:
                opCodeA0();
                break;
            case 0xA1:
                opCodeA1();
                break;
            case 0xA2:
                opCodeA2();
                break;
            case 0xA3:
                opCodeA3();
                illegal = 1;
                break;
            case 0xA4:
                opCodeA4();
                break;
            case 0xA5:
                opCodeA5();
                break;
            case 0xA6:
                opCodeA6();
                break;
            case 0xA7:
                opCodeA7();
                illegal = 1;
                break;
            case 0xA8:
                opCodeA8();
                break;
            case 0xA9:
                opCodeA9();
                break;
            case 0xAA:
                opCodeAA();
                break;
            case 0xAB:
                opCodeAB();
                illegal = 1;
                break;
            case 0xAC:
                opCodeAC();
                break;
            case 0xAD:
                opCodeAD();
                break;
            case 0xAE:
                opCodeAE();
                break;
            case 0xAF:
                opCodeAF();
                illegal = 1;
                break;

            case 0xB0:
                opCodeB0();
                break;
            case 0xB1:
                opCodeB1();
                break;
            case 0xB2:
                opCodeB2();
                illegal = 1;
                break;
            case 0xB3:
                opCodeB3();
                illegal = 1;
                break;
            case 0xB4:
                opCodeB4();
                break;
            case 0xB5:
                opCodeB5();
                break;
            case 0xB6:
                opCodeB6();
                break;
            case 0xB7:
                opCodeB7();
                illegal = 1;
                break;
            case 0xB8:
                opCodeB8();
                break;
            case 0xB9:
                opCodeB9();
                break;
            case 0xBA:
                opCodeBA();
                break;
            case 0xBB:
                opCodeBB();
                illegal = 1;
                break;
            case 0xBC:
                opCodeBC();
                break;
            case 0xBD:
                opCodeBD();
                break;
            case 0xBE:
                opCodeBE();
                break;
            case 0xBF:
                opCodeBF();
                illegal = 1;
                break;

            case 0xC0:
                opCodeC0();
                break;
            case 0xC1:
                opCodeC1();
                break;
            case 0xC2:
                opCodeC2();
                illegal = 1;
                break;
            case 0xC3:
                opCodeC3();
                illegal = 1;
                break;
            case 0xC4:
                opCodeC4();
                break;
            case 0xC5:
                opCodeC5();
                break;
            case 0xC6:
                opCodeC6();
                break;
            case 0xC7:
                opCodeC7();
                illegal = 1;
                break;
            case 0xC8:
                opCodeC8();
                break;
            case 0xC9:
                opCodeC9();
                break;
            case 0xCA:
                opCodeCA();
                break;
            case 0xCB:
                opCodeCB();
                illegal = 1;
                break;
            case 0xCC:
                opCodeCC();
                break;
            case 0xCD:
                opCodeCD();
                break;
            case 0xCE:
                opCodeCE();
                break;
            case 0xCF:
                opCodeCF();
                illegal = 1;
                break;

            case 0xD0:
                opCodeD0();
                break;
            case 0xD1:
                opCodeD1();
                break;
            case 0xD2:
                opCodeD2();
                illegal = 1;
                break;
            case 0xD3:
                opCodeD3();
                illegal = 1;
                break;
            case 0xD4:
                opCodeD4();
                illegal = 1;
                break;
            case 0xD5:
                opCodeD5();
                break;
            case 0xD6:
                opCodeD6();
                break;
            case 0xD7:
                opCodeD7();
                illegal = 1;
                break;
            case 0xD8:
                opCodeD8();
                break;
            case 0xD9:
                opCodeD9();
                break;
            case 0xDA:
                opCodeDA();
                illegal = 1;
                break;
            case 0xDB:
                opCodeDB();
                illegal = 1;
                break;
            case 0xDC:
                opCodeDC();
                illegal = 1;
                break;
            case 0xDD:
                opCodeDD();
                break;
            case 0xDE:
                opCodeDE();
                break;
            case 0xDF:
                opCodeDF();
                illegal = 1;
                break;

            case 0xE0:
                opCodeE0();
                break;
            case 0xE1:
                opCodeE1();
                break;
            case 0xE2:
                opCodeE2();
                illegal = 1;
                break;
            case 0xE3:
                opCodeE3();
                illegal = 1;
                break;
            case 0xE4:
                opCodeE4();
                break;
            case 0xE5:
                opCodeE5();
                break;
            case 0xE6:
                opCodeE6();
                break;
            case 0xE7:
                opCodeE7();
                illegal = 1;
                break;
            case 0xE8:
                opCodeE8();
                break;
            case 0xE9:
                opCodeE9();
                break;
            case 0xEA:
                opCodeEA();
                break;
            case 0xEB:
                opCodeEB();
                illegal = 1;
                break;
            case 0xEC:
                opCodeEC();
                break;
            case 0xED:
                opCodeED();
                break;
            case 0xEE:
                opCodeEE();
                break;
            case 0xEF:
                opCodeEF();
                illegal = 1;
                break;

            case 0xF0:
                opCodeF0();
                break;
            case 0xF1:
                opCodeF1();
                break;
            case 0xF2:
                opCodeF2();
                illegal = 1;
                break;
            case 0xF3:
                opCodeF3();
                illegal = 1;
                break;
            case 0xF4:
                opCodeF4();
                illegal = 1;
                break;
            case 0xF5:
                opCodeF5();
                break;
            case 0xF6:
                opCodeF6();
                break;
            case 0xF7:
                opCodeF7();
                illegal = 1;
                break;
            case 0xF8:
                opCodeF8();
                break;
            case 0xF9:
                opCodeF9();
                break;
            case 0xFA:
                opCodeFA();
                illegal = 1;
                break;
            case 0xFB:
                opCodeFB();
                illegal = 1;
                break;
            case 0xFC:
                opCodeFC();
                illegal = 1;
                break;
            case 0xFD:
                opCodeFD();
                break;
            case 0xFE:
                opCodeFE();
                break;
            case 0xff:
                opCodeFF();
                illegal = 1;
                break;
        }
    }

    /**
     * The clock cycles used by the m6502 for each operation are recorded here.
     * m6502 clock cycle table
     * <p>
     * (n)		undefined OP-code
     * +n		 +1 by page boundary case
     * BRK(#$00)  +7 by interrupt
     * <p>
     * BS - corrected NOP timings for undefined opCodes
     */
    final static byte[] cl_table = {
            /* L 0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F	  H */
            0, 6, (2), (8), (3), 3, 5, (5), 3, 2, 2, (2), (4), 4, 6, (6), /* 0 */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* 1 */
            6, 6, (2), (8), 3, 3, 5, (5), 4, 2, 2, (2), 4, 4, 6, (6), /* 2 */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* 3 */
            6, 6, (2), (8), (3), 3, 5, (5), 3, 2, 2, (2), 3, 4, 6, (6), /* 4 */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* 5 */
            6, 6, (2), (8), (3), 3, 5, (5), 4, 2, 2, (2), 5, 4, 6, (6), /* 6 */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* 7 */
            (2), 6, (2), (6), 3, 3, 3, (3), 2, (2), 2, (2), 4, 4, 4, (4), /* 8 */
            2, 6, (2), (6), 4, 4, 4, (4), 2, 5, 2, (5), (5), 5, (5), (5), /* 9 */
            2, 6, 2, (6), 3, 3, 3, (3), 2, 2, 2, (2), 4, 4, 4, (4), /* A */
            2, +5, (2), (5), 4, 4, 4, (4), 2, +4, 2, (4), +4, +4, +4, (4), /* B */
            2, 6, (2), (8), 3, 3, 5, (5), 2, 2, 2, (2), 4, 4, 6, (6), /* C */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* D */
            2, 6, (2), (8), 3, 3, 5, (5), 2, 2, 2, (2), 4, 4, 6, (6), /* E */
            2, +5, (2), (8), (4), 4, 6, (6), 2, +4, (2), (7), (4), +4, 7, (7), /* F */
    };

    final static byte[] fl_table = {
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 00
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 01
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 02
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 03
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 04
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 05
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 06
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 07
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,

            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 08
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 09
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0A
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0B
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0C
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0D
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0E
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, // 0F
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80,

            (byte) 0x03, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 10
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 11
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 12
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 13
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 14
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 15
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 16
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // 17
            (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,

            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 18
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 19
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1A
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1B
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1C
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1D
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1E
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, // 1F
            (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
    };

    // km6502m.h

    /**
     * #define K_READ K_READ
     *
     * @param adr
     * @return
     */
    int K_READ(int adr) {
        return readByte.handler(adr);
    }

    /**
     * #define K_WRITENP K_WRITE
     *
     * @param adr
     * @param value
     */
    void K_WRITE(int adr, int value) {
        writeByte.handler(adr, value);
    }

    // km6502cd.h

    public static final int BASE_OF_ZERO = 0x0000;

    void KI_ADDCLOCK(int cycle) {
        this.clock += cycle;
    }

    int KI_READWORD(int adr) {
        int ret = K_READ(adr);
        return (ret + (K_READ((adr + 1) & 0xffff) << 8)) & 0xffff;
    }

    int KI_READWORDZP(int adr) {
        int ret = K_READ(BASE_OF_ZERO + adr);
        return (ret + (K_READ(BASE_OF_ZERO + ((adr + 1) & 0xff)) << 8)) & 0xffff;
    }

    int KAI_IMM() {
        int ret = pc;
        pc = (pc + 1) & 0xffff;
        return ret;
    }

    int KAI_IMM16() {
        int ret = pc;
        pc = (pc + 2) & 0xffff;
        return ret;
    }

    int KAI_ABS() {
        return KI_READWORD(KAI_IMM16());
    }

    int KAI_ABSX() {
        return (KAI_ABS() + x) & 0xffff;
    }

    int KAI_ABSY() {
        return (KAI_ABS() + y) & 0xffff;
    }

    int KAI_ZP() {
        return K_READ(KAI_IMM());
    }

    int KAI_ZPX() {
        return (KAI_ZP() + x) & 0xff;
    }

    int KAI_INDY() {
        return (KI_READWORDZP(KAI_ZP()) + y) & 0xffff;
    }

    int KA_IMM() {
        int ret = pc;
        pc = (pc + 1) & 0xffff;
        return ret;
    }

    int KA_IMM16() {
        int ret = pc;
        pc = (pc + 2) & 0xffff;
        return ret;
    }

    int KA_ABS() {
        return KI_READWORD(KAI_IMM16());
    }

    int KA_ABSX() {
        return (KAI_ABS() + x) & 0xffff;
    }

    int KA_ABSY() {
        return (KAI_ABS() + y) & 0xffff;
    }

    int KA_ZP() {
        return BASE_OF_ZERO + K_READ(KAI_IMM());
    }

    int KA_ZPX() {
        return BASE_OF_ZERO + ((KAI_ZP() + x) & 0xff);
    }

    int KA_ZPY() {
        return BASE_OF_ZERO + ((KAI_ZP() + y) & 0xff);
    }

    int KA_INDX() {
        return KI_READWORDZP(KAI_ZPX());
    }

    int KA_INDY() {
        return (KI_READWORDZP(KAI_ZP()) + y) & 0xffff;
    }

    int KI_READWORDBUG(int adr) {
        int ret = K_READ(adr);
        return ret + (K_READ((adr & 0xff00) + ((adr + 1) & 0xff)) << 8);
    }

    int KA_ABSX_() {
        if ((pc & 0xff) == 0xff) KI_ADDCLOCK(1); // page break
        return KAI_ABSX();
    }

    int KA_ABSY_() {
        if ((pc & 0xff) == 0xff) KI_ADDCLOCK(1); // page break
        return KAI_ABSY();
    }

    int KA_INDY_() {
        int adr = KAI_INDY();
        if ((adr & 0xff) == 0xff) KI_ADDCLOCK(1); // page break
        return adr;
    }

    void KM_ALUADDER(int src) {
        int w = a + src + (p & C_FLAG);
        p &= ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0x01ff] + ((((~a ^ src) & (a ^ w)) >> 1) & V_FLAG);
        p &= 0xff;
        a = (w & 0xff);
    }

    void KM_ALUADDER_D(int src) {
        int wl = (a & 0x0F) + (src & 0x0F) + (p & C_FLAG);
        int w = a + src + (p & C_FLAG);
        p &= ~C_FLAG;
        if (wl > 0x9) w += 0x6;
        if (w > 0x9F) {
            p += C_FLAG;
            w += 0x60;
        }
        p &= 0xff;
        a = (w & 0xff);
        KI_ADDCLOCK(1);
    }

    void KMI_ADC(int src) {
        KM_ALUADDER(src);
    }

    void KMI_ADC_D(int src) {
        KM_ALUADDER_D(src);
    }

    void KMI_SBC(int src) {
        KM_ALUADDER(src ^ 0xff);
    }

    void KMI_SBC_D(int src) {
        KM_ALUADDER_D(0xff & ((src ^ 0xff) + (0x100 - 0x66)));
    }

    void KM_CMP(int src) {
        int w = a + (src ^ 0xff) + 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0x01ff];
        p &= 0xff;
    }

    void KM_CPX(int src) {
        int w = x + (src ^ 0xff) + 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0x01ff];
        p &= 0xff;
    }

    void KM_CPY(int src) {
        int w = y + (src ^ 0xff) + 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0x01ff];
        p &= 0xff;
    }

    void KM_BIT(int src) {
        int w = a & src;
        p &= ~(N_FLAG | V_FLAG | Z_FLAG);
        p += (src & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
        p &= 0xff;
    }

    void KM_AND(int src) {
        a &= src;
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[a & 0xff];
        p &= 0xff;
    }

    void KM_ORA(int src) {
        a |= src;
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[a & 0xff];
        p &= 0xff;
    }

    void KM_EOR(int src) {
        a ^= src;
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[a & 0xff];
        p &= 0xff;
    }

    int KM_DEC(int des) {
        int w = des - 1;
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[w & 0xff];
        p &= 0xff;
        return w & 0xff;
    }

    int KM_INC(int des) {
        int w = des + 1;
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[w & 0xff];
        p &= 0xff;
        return w & 0xff;
    }

    int KM_ASL(int des) {
        int w = des << 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0xff] + ((des >> 7) /* & C_FLAG */);
        p &= 0xff;
        return (w & 0xff);
    }

    int KM_LSR(int des) {
        int w = des >> 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0xff] + (des & C_FLAG);
        p &= 0xff;
        return w;
    }

    int KM_LD(int src) {
        p &= ~(N_FLAG | Z_FLAG);
        p += fl_table[src & 0xff];
        p &= 0xff;
        return src;
    }

    int KM_ROL(int des) {
        int w = (des << 1) + (p & C_FLAG);
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0xff] + ((des >> 7) /* & C_FLAG */);
        p &= 0xff;
        return w & 0xff;
    }

    int KM_ROR(int des) {
        int w = (des >> 1) + ((p & C_FLAG) << 7);
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p += fl_table[w & 0xff] + (des & C_FLAG);
        p &= 0xff;
        return w & 0xff;
    }

    void KM_BRA(int rel) {
        int oldPage = pc & 0xff00;
        pc = (pc + (rel ^ 0x80) - 0x80) & 0xffff;
        KI_ADDCLOCK(1 + ((oldPage != (pc & 0xff00)) ? 1 : 0));
    }

    void KM_PUSH(int src) {
        K_WRITE(BASE_OF_ZERO + 0x100 + s, src);
        s = (s - 1) & 0xff;
    }

    int KM_POP() {
        s = (s + 1) & 0xff;
        return K_READ(BASE_OF_ZERO + 0x100 + s);
    }

    // ADC part

    /**
     * 61 - ADC - (Indirect,X)
     */
    void opCode61() {
        KMI_ADC(K_READ(KA_INDX()));
    }

    void D_Opco61() {
        KMI_ADC_D(K_READ(KA_INDX()));
    }

    /**
     * 65 - ADC - Zero Page
     */
    void opCode65() {
        KMI_ADC(K_READ(KA_ZP()));
    }

    void D_Opco65() {
        KMI_ADC_D(K_READ(KA_ZP()));
    }

    /**
     * 69 - ADC - Immediate
     */
    void opCode69() {
        KMI_ADC(K_READ(KA_IMM()));
    }

    void D_Opco69() {
        KMI_ADC_D(K_READ(KA_IMM()));
    }

    /**
     * 6D - ADC - Absolute
     */
    void opCode6D() {
        KMI_ADC(K_READ(KA_ABS()));
    }

    void D_Opco6D() {
        KMI_ADC_D(K_READ(KA_ABS()));
    }

    /**
     * 71 - ADC - (Indirect),Y
     */
    void opCode71() {
        KMI_ADC(K_READ(KA_INDY_()));
    }

    void D_Opco71() {
        KMI_ADC_D(K_READ(KA_INDY_()));
    }

    /**
     * 75 - ADC - Zero Page,X
     */
    void opCode75() {
        KMI_ADC(K_READ(KA_ZPX()));
    }

    void D_Opco75() {
        KMI_ADC_D(K_READ(KA_ZPX()));
    }

    /**
     * 79 - ADC - Absolute,Y
     */
    void opCode79() {
        KMI_ADC(K_READ(KA_ABSY_()));
    }

    void D_Opco79() {
        KMI_ADC_D(K_READ(KA_ABSY_()));
    }

    /**
     * 7D - ADC - Absolute,X
     */
    void opCode7D() {
        KMI_ADC(K_READ(KA_ABSX_()));
    }

    void D_Opco7D() {
        KMI_ADC_D(K_READ(KA_ABSX_()));
    }

    // AND part

    /**
     * 21 - AND - (Indirect,X)
     */
    void opCode21() {
        KM_AND(K_READ(KA_INDX()));
    }

    /**
     * 25 - AND - Zero Page
     */
    void opCode25() {
        KM_AND(K_READ(KA_ZP()));
    }

    /**
     * 29 - AND - Immediate
     */
    void opCode29() {
        KM_AND(K_READ(KA_IMM()));
    }

    /**
     * 2D - AND - Absolute
     */
    void opCode2D() {
        KM_AND(K_READ(KA_ABS()));
    }

    /**
     * 31 - AND - (Indirect),Y
     */
    void opCode31() {
        KM_AND(K_READ(KA_INDY_()));
    }

    /**
     * 35 - AND - Zero Page,X
     */
    void opCode35() {
        KM_AND(K_READ(KA_ZPX()));
    }

    /**
     * 39 - AND - Absolute,Y
     */
    void opCode39() {
        KM_AND(K_READ(KA_ABSY_()));
    }

    /**
     * 3D - AND - Absolute,X
     */
    void opCode3D() {
        KM_AND(K_READ(KA_ABSX_()));
    }

    // ASL part

    /**
     * 06 - ASL - Zero Page
     */
    void opCode06() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_ASL(K_READ(adr)));
    }

    /**
     * 0E - ASL - Absolute
     */
    void opCode0E() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_ASL(K_READ(adr)));
    }

    /**
     * 16 - ASL - Zero Page,X
     */
    void opCode16() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_ASL(K_READ(adr)));
    }

    /**
     * 1E - ASL - Absolute,X
     */
    void opCode1E() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_ASL(K_READ(adr)));
    }

    /**
     * 0A - ASL - Accumulator
     */
    void opCode0A() {
        a = KM_ASL(a);
    }

    // BIT part

    /**
     * 24 - BIT - Zero Page
     */
    void opCode24() {
        KM_BIT(K_READ(KA_ZP()));
    }

    /**
     * 2C - BIT - Absolute
     */
    void opCode2C() {
        KM_BIT(K_READ(KA_ABS()));
    }

    // Bcc part

    /** 10 - BPL */
    void opCode10() {
        int rel = K_READ(KA_IMM());
        if ((p & N_FLAG) == 0) KM_BRA(rel);
    }

    /** 30 - BMI */
    void opCode30() {
        int rel = K_READ(KA_IMM());
        if ((p & N_FLAG) != 0) KM_BRA(rel);
    }

    /** 50 - BVC */
    void opCode50() {
        int rel = K_READ(KA_IMM());
        if ((p & V_FLAG) == 0) KM_BRA(rel);
    }

    /** 70 - BVS */
    void opCode70() {
        int rel = K_READ(KA_IMM());
        if ((p & V_FLAG) != 0) KM_BRA(rel);
    }

    /** 90 - BCC */
    void opCode90() {
        int rel = K_READ(KA_IMM());
        if ((p & C_FLAG) == 0) KM_BRA(rel);
    }

    /** B0 - BCS */
    void opCodeB0() {
        int rel = K_READ(KA_IMM());
        if ((p & C_FLAG) != 0) KM_BRA(rel);
    }

    /** D0 - BNE */
    void opCodeD0() {
        int rel = K_READ(KA_IMM());
        if ((p & Z_FLAG) == 0) KM_BRA(rel);
    }

    /** F0 - BEQ */
    void opCodeF0() {
        int rel = K_READ(KA_IMM());
        if ((p & Z_FLAG) != 0) KM_BRA(rel);
    }

    // BRK part

    /** 00 - BRK */
    void opCode00() {
        pc = (pc + 1) & 0xffff;
        iRequest |= K6502_BRK;
    }

    /** 18 - CLC */
    void opCode18() {
        p &= ~C_FLAG;
    }

    /** D8 - CLD */
    void opCodeD8() {
        p &= ~D_FLAG;
    }

    /** 58 - CLI */
    void opCode58() {
        p &= ~I_FLAG;
    }

    /** B8 - CLV */
    void opCodeB8() {
        p &= ~V_FLAG;
    }

    // CMP part

    /** C1 - CMP - (Indirect,X) */
    void opCodeC1() {
        KM_CMP(K_READ(KA_INDX()));
    }

    /** C5 - CMP - Zero Page */
    void opCodeC5() {
        KM_CMP(K_READ(KA_ZP()));
    }

    /** C9 - CMP - Immediate */
    void opCodeC9() {
        KM_CMP(K_READ(KA_IMM()));
    }

    /** CD - CMP - Absolute */
    void opCodeCD() {
        KM_CMP(K_READ(KA_ABS()));
    }

    /** D1 - CMP - (Indirect),Y */
    void opCodeD1() {
        KM_CMP(K_READ(KA_INDY_()));
    }

    /** D5 - CMP - Zero Page,X */
    void opCodeD5() {
        KM_CMP(K_READ(KA_ZPX()));
    }

    /** D9 - CMP - Absolute,Y */
    void opCodeD9() {
        KM_CMP(K_READ(KA_ABSY_()));
    }

    /** DD - CMP - Absolute,X */
    void opCodeDD() {
        KM_CMP(K_READ(KA_ABSX_()));
    }

    // CPX part

    /** E0 - CPX - Immediate */
    void opCodeE0() {
        KM_CPX(K_READ(KA_IMM()));
    }

    /** E4 - CPX - Zero Page */
    void opCodeE4() {
        KM_CPX(K_READ(KA_ZP()));
    }

    /** EC - CPX - Absolute */
    void opCodeEC() {
        KM_CPX(K_READ(KA_ABS()));
    }

    // CPY part

    /** C0 - CPY - Immediate */
    void opCodeC0() {
        KM_CPY(K_READ(KA_IMM()));
    }

    /** C4 - CPY - Zero Page */
    void opCodeC4() {
        KM_CPY(K_READ(KA_ZP()));
    }

    /** CC - CPY - Absolute */
    void opCodeCC() {
        KM_CPY(K_READ(KA_ABS()));
    }

    // DEC part

    /** C6 - DEC - Zero Page */
    void opCodeC6() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_DEC(K_READ(adr)));
    }

    /** CE - DEC - Absolute */
    void opCodeCE() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_DEC(K_READ(adr)));
    }

    /** D6 - DEC - Zero Page,X */
    void opCodeD6() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_DEC(K_READ(adr)));
    }

    /** DE - DEC - Absolute,X */
    void opCodeDE() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_DEC(K_READ(adr)));
    }

    /** CA - DEX */
    void opCodeCA() {
        x = KM_DEC(x);
    }

    /** 88 - DEY */
    void opCode88() {
        y = KM_DEC(y);
    }

    // EOR part

    /** 41 - EOR - (Indirect,X) */
    void opCode41() {
        KM_EOR(K_READ(KA_INDX()));
    }

    /** 45 - EOR - Zero Page */
    void opCode45() {
        KM_EOR(K_READ(KA_ZP()));
    }

    /** 49 - EOR - Immediate */
    void opCode49() {
        KM_EOR(K_READ(KA_IMM()));
    }

    /** 4D - EOR - Absolute */
    void opCode4D() {
        KM_EOR(K_READ(KA_ABS()));
    }

    /** 51 - EOR - (Indirect),Y */
    void opCode51() {
        KM_EOR(K_READ(KA_INDY_()));
    }

    /** 55 - EOR - Zero Page,X */
    void opCode55() {
        KM_EOR(K_READ(KA_ZPX()));
    }

    /** 59 - EOR - Absolute,Y */
    void opCode59() {
        KM_EOR(K_READ(KA_ABSY_()));
    }

    /** 5D - EOR - Absolute,X */
    void opCode5D() {
        KM_EOR(K_READ(KA_ABSX_()));
    }

    // INC part

    /** E6 - INC - Zero Page */
    void opCodeE6() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_INC(K_READ(adr)));
    }

    /** EE - INC - Absolute */
    void opCodeEE() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_INC(K_READ(adr)));
    }

    /** F6 - INC - Zero Page,X */
    void opCodeF6() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_INC(K_READ(adr)));
    }

    /** FE - INC - Absolute,X */
    void opCodeFE() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_INC(K_READ(adr)));
    }

    /** E8 - INX */
    void opCodeE8() {
        x = KM_INC(x);
    }

    /** C8 - INY */
    void opCodeC8() {
        y = KM_INC(y);
    }

    // JMP part

    /** 4C - JMP - Immediate */
    void opCode4C() {
        pc = KI_READWORD(KA_IMM16());
    }

    /** 6C - JMP - Absolute */
    void opCode6C() {
        pc = KI_READWORDBUG(KA_ABS());
    }

    // JSR part

    /** 20 - JSR */
    void opCode20() {
        int adr = KA_IMM();
        KM_PUSH((pc >> 8) & 0xff);    /* !!! PC = NEXT - 1; !!! */
        KM_PUSH((pc) & 0xff);
        pc = KI_READWORD(adr);
    }

    // LDA part

    /** A1 - LDA - (Indirect,X) */
    void opCodeA1() {
        a = KM_LD(K_READ(KA_INDX()));
    }

    /** A5 - LDA - Zero Page */
    void opCodeA5() {
        a = KM_LD(K_READ(KA_ZP()));
    }

    /** A9 - LDA - Immediate */
    void opCodeA9() {
        a = KM_LD(K_READ(KA_IMM()));
    }

    /** AD - LDA - Absolute */
    void opCodeAD() {
        a = KM_LD(K_READ(KA_ABS()));
    }

    /** B1 - LDA - (Indirect),Y */
    void opCodeB1() {
        a = KM_LD(K_READ(KA_INDY_()));
    }

    /** B5 - LDA - Zero Page,X */
    void opCodeB5() {
        a = KM_LD(K_READ(KA_ZPX()));
    }

    /** B9 - LDA - Absolute,Y */
    void opCodeB9() {
        a = KM_LD(K_READ(KA_ABSY_()));
    }

    /** BD - LDA - Absolute,X */
    void opCodeBD() {
        a = KM_LD(K_READ(KA_ABSX_()));
    }

    // LDX part

    /** A2 - LDX - Immediate */
    void opCodeA2() {
        x = KM_LD(K_READ(KA_IMM()));
    }

    /** A6 - LDX - Zero Page */
    void opCodeA6() {
        x = KM_LD(K_READ(KA_ZP()));
    }

    /** AE - LDX - Absolute */
    void opCodeAE() {
        x = KM_LD(K_READ(KA_ABS()));
    }

    /** B6 - LDX - Zero Page,Y */
    void opCodeB6() {
        x = KM_LD(K_READ(KA_ZPY()));
    }

    /** BE - LDX - Absolute,Y */
    void opCodeBE() {
        x = KM_LD(K_READ(KA_ABSY_()));
    }

    // LDY part

    /** A0 - LDY - Immediate */
    void opCodeA0() {
        y = KM_LD(K_READ(KA_IMM()));
    }

    /** A4 - LDY - Zero Page */
    void opCodeA4() {
        y = KM_LD(K_READ(KA_ZP()));
    }

    /** AC - LDY - Absolute */
    void opCodeAC() {
        y = KM_LD(K_READ(KA_ABS()));
    }

    /** B4 - LDY - Zero Page,X */
    void opCodeB4() {
        y = KM_LD(K_READ(KA_ZPX()));
    }

    /** BC - LDY - Absolute,X */
    void opCodeBC() {
        y = KM_LD(K_READ(KA_ABSX_()));
    }

    // LSR part

    /** 46 - LSR - Zero Page */
    void opCode46() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_LSR(K_READ(adr)));
    }

    /** 4E - LSR - Absolute */
    void opCode4E() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_LSR(K_READ(adr)));
    }

    /** 56 - LSR - Zero Page,X */
    void opCode56() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_LSR(K_READ(adr)));
    }

    /** 5E - LSR - Absolute,X */
    void opCode5E() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_LSR(K_READ(adr)));
    }

    /** 4A - LSR - Accumulator */
    void opCode4A() {
        a = KM_LSR(a);
    }

    // NOP part

    /** EA - NOP */
    void opCodeEA() {
    }

    // ORA part

    /** 01 - ORA - (Indirect,X) */
    void opCode01() {
        KM_ORA(K_READ(KA_INDX()));
    }

    /** 05 - ORA - Zero Page */
    void opCode05() {
        KM_ORA(K_READ(KA_ZP()));
    }

    /** 09 - ORA - Immediate */
    void opCode09() {
        KM_ORA(K_READ(KA_IMM()));
    }

    /** 0D - ORA - Absolute */
    void opCode0D() {
        KM_ORA(K_READ(KA_ABS()));
    }

    /** 11 - ORA - (Indirect),Y */
    void opCode11() {
        KM_ORA(K_READ(KA_INDY_()));
    }

    /** 15 - ORA - Zero Page,X */
    void opCode15() {
        KM_ORA(K_READ(KA_ZPX()));
    }

    /** 19 - ORA - Absolute,Y */
    void opCode19() {
        KM_ORA(K_READ(KA_ABSY_()));
    }

    /** 1D - ORA - Absolute,X */
    void opCode1D() {
        KM_ORA(K_READ(KA_ABSX_()));
    }

    // PHr PLr part

    /** 48 - PHA */
    void opCode48() {
        KM_PUSH(a);
    }

    /** 08 - PHP */
    void opCode08() {
        KM_PUSH((p | B_FLAG | R_FLAG) & ~T_FLAG);
    }

    /** 68 - PLA */
    void opCode68() {
        a = KM_LD(KM_POP());
    }

    /** 28 - PLP */
    void opCode28() {
        p = KM_POP() & ~T_FLAG;
    }

    // ROL part

    /** 26 - ROL - Zero Page */
    void opCode26() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_ROL(K_READ(adr)));
    }

    /** 2E - ROL - Absolute */
    void opCode2E() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_ROL(K_READ(adr)));
    }

    /** 36 - ROL - Zero Page,X */
    void opCode36() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_ROL(K_READ(adr)));
    }

    /** 3E - ROL - Absolute,X */
    void opCode3E() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_ROL(K_READ(adr)));
    }

    /** 2A - ROL - Accumulator */
    void opCode2A() {
        a = KM_ROL(a);
    }

    // ROR part

    /** 66 - ROR - Zero Page */
    void opCode66() {
        int adr = KA_ZP();
        K_WRITE(adr, KM_ROR(K_READ(adr)));
    }

    /** 6E - ROR - Absolute */
    void opCode6E() {
        int adr = KA_ABS();
        K_WRITE(adr, KM_ROR(K_READ(adr)));
    }

    /** 76 - ROR - Zero Page,X */
    void opCode76() {
        int adr = KA_ZPX();
        K_WRITE(adr, KM_ROR(K_READ(adr)));
    }

    /** 7E - ROR - Absolute,X */
    void opCode7E() {
        int adr = KA_ABSX();
        K_WRITE(adr, KM_ROR(K_READ(adr)));
    }

    /** 6A - ROR - Accumulator */
    void opCode6A() {
        a = KM_ROR(a);
    }

    /** 40 - RTI */
    void opCode40() {
        p = KM_POP();
        pc = KM_POP();
        pc += KM_POP() << 8;
    }

    /** 60 - RTS */
    void opCode60() {
        pc = KM_POP();
        pc += KM_POP() << 8;
        pc = (pc + 1) & 0xffff;
    }

    // SBC part

    /** E1 - SBC - (Indirect,X) */
    void opCodeE1() {
        KMI_SBC(K_READ(KA_INDX()));
    }

    void D_OpcoE1() {
        KMI_SBC_D(K_READ(KA_INDX()));
    }

    /** E5 - SBC - Zero Page */
    void opCodeE5() {
        KMI_SBC(K_READ(KA_ZP()));
    }

    void D_OpcoE5() {
        KMI_SBC_D(K_READ(KA_ZP()));
    }

    /** E9 - SBC - Immediate */
    void opCodeE9() {
        KMI_SBC(K_READ(KA_IMM()));
    }

    void D_OpcoE9() {
        KMI_SBC_D(K_READ(KA_IMM()));
    }

    /** ED - SBC - Absolute */
    void opCodeED() {
        KMI_SBC(K_READ(KA_ABS()));
    }

    void D_OpcoED() {
        KMI_SBC_D(K_READ(KA_ABS()));
    }

    /** F1 - SBC - (Indirect),Y */
    void opCodeF1() {
        KMI_SBC(K_READ(KA_INDY_()));
    }

    void D_OpcoF1() {
        KMI_SBC_D(K_READ(KA_INDY_()));
    }

    /** F5 - SBC - Zero Page,X */
    void opCodeF5() {
        KMI_SBC(K_READ(KA_ZPX()));
    }

    void D_OpcoF5() {
        KMI_SBC_D(K_READ(KA_ZPX()));
    }

    /** F9 - SBC - Absolute,Y */
    void opCodeF9() {
        KMI_SBC(K_READ(KA_ABSY_()));
    }

    void D_OpcoF9() {
        KMI_SBC_D(K_READ(KA_ABSY_()));
    }

    /** FD - SBC - Absolute,X */
    void opCodeFD() {
        KMI_SBC(K_READ(KA_ABSX_()));
    }

    void D_OpcoFD() {
        KMI_SBC_D(K_READ(KA_ABSX_()));
    }

    /** 38 - SEC */
    void opCode38() {
        p |= C_FLAG;
    }

    /** F8 - SED */
    void opCodeF8() {
        p |= D_FLAG;
    }

    /** 78 - SEI */
    void opCode78() {
        p |= I_FLAG;
    }

    // STA part

    /** 81 - STA - (Indirect,X) */
    void opCode81() {
        K_WRITE(KA_INDX(), a);
    }

    /** 85 - STA - Zero Page */
    void opCode85() {
        K_WRITE(KA_ZP(), a);
    }

    /** 8D - STA - Absolute */
    void opCode8D() {
        K_WRITE(KA_ABS(), a);
    }

    /** 91 - STA - (Indirect),Y */
    void opCode91() {
        K_WRITE(KA_INDY(), a);
    }

    /** 95 - STA - Zero Page,X */
    void opCode95() {
        K_WRITE(KA_ZPX(), a);
    }

    /** 99 - STA - Absolute,Y */
    void opCode99() {
        K_WRITE(KA_ABSY(), a);
    }

    /** 9D - STA - Absolute,X */
    void opCode9D() {
        K_WRITE(KA_ABSX(), a);
    }

    // STX part

    /** 86 - STX - Zero Page */
    void opCode86() {
        K_WRITE(KA_ZP(), x);
    }

    /** 8E - STX - Absolute */
    void opCode8E() {
        K_WRITE(KA_ABS(), x);
    }

    /** 96 - STX - Zero Page,Y */
    void opCode96() {
        K_WRITE(KA_ZPY(), x);
    }

    // STY part

    /** 84 - STY - Zero Page */
    void opCode84() {
        K_WRITE(KA_ZP(), y);
    }

    /** 8C - STY - Absolute */
    void opCode8C() {
        K_WRITE(KA_ABS(), y);
    }

    /** 94 - STY - Zero Page,X */
    void opCode94() {
        K_WRITE(KA_ZPX(), y);
    }

    /** AA - TAX */
    void opCodeAA() {
        x = KM_LD(a);
    }

    /** A8 - TAY */
    void opCodeA8() {
        y = KM_LD(a);
    }

    /** BA - TSX */
    void opCodeBA() {
        x = KM_LD(s);
    }

    /** 8A - TXA */
    void opCode8A() {
        a = KM_LD(x);
    }

    /** 9A - TXS */
    void opCode9A() {
        s = x;
    }

    /** 98 - TYA */
    void opCode98() {
        a = KM_LD(y);
    }

    // KIL part

    /** 02 - KIL - halts CPU */
    void opCode02() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 12 - KIL - halts CPU */
    void opCode12() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 22 - KIL - halts CPU */
    void opCode22() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 32 - KIL - halts CPU */
    void opCode32() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 42 - KIL - halts CPU */
    void opCode42() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 52 - KIL - halts CPU */
    void opCode52() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 62 - KIL - halts CPU */
    void opCode62() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 72 - KIL - halts CPU */
    void opCode72() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** 92 - KIL - halts CPU */
    void opCode92() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** B2 - KIL - halts CPU */
    void opCodeB2() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** D2 - KIL - halts CPU */
    void opCodeD2() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    /** F2 - KIL - halts CPU */
    void opCodeF2() {
        pc = (pc - 1) & 0xffff;
        p |= I_FLAG; /* disable interrupt */
    }

    // NOP part

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode80() {
        KAI_IMM();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode82() {
        KAI_IMM();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeC2() {
        KAI_IMM();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeE2() {
        KAI_IMM();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode04() {
        KAI_ZP();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode14() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode34() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode44() {
        KAI_ZP();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode54() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode64() {
        KAI_ZP();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode74() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeD4() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeF4() {
        KAI_ZPX();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode89() {
        KAI_IMM();
    }

    /** does nothing */
    void opCode1A() {
    }

    /** does nothing */
    void opCode3A() {
    }

    /** does nothing */
    void opCode5A() {
    }

    /** does nothing */
    void opCode7A() {
    }

    /** does nothing */
    void opCodeDA() {
    }

    /** does nothing */
    void opCodeFA() {
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode0C() {
        KAI_ABS();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode1C() {
        KA_ABSX_();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode3C() {
        KA_ABSX_();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode5C() {
        KA_ABSX_();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCode7C() {
        KA_ABSX_();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeDC() {
        KA_ABSX_();
    }

    /** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
    void opCodeFC() {
        KA_ABSX_();
    }

    // SLO part

    /** shift left, OR result */
    int KM_SLO(int src) {
        int w = (src << 1) & 0xff;
        a |= w;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= (fl_table[a & 0xff]);
        p |= (src >> 7) & C_FLAG;
        return w;
    }

    /** macro - opCodes */
    void opCode03() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode13() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode07() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode17() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode1B() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode0F() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    /** macro - opCodes */
    void opCode1F() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SLO(src));
    }

    // RLA part

    /** rotate left, AND result */
    int KM_RLA(int src) {
        int w = ((src << 1) | (p & C_FLAG)) & 0xff;
        a &= w;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= (src >> 7) & C_FLAG;
        return w;
    }

    /** macro - opCodes */
    void opCode23() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode33() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode27() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode37() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode3B() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode2F() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    /** macro - opCodes */
    void opCode3F() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RLA(src));
    }

    // SRE part

    /** shift right, EOR result */
    int KM_SRE(int src) {
        int w = (src >> 1) & 0xff;
        a ^= w;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= src & C_FLAG;
        return w;
    }

    /** macro - opCodes SRE */
    void opCode43() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode53() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode47() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode57() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode5B() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode4F() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    /** macro - opCodes SRE */
    void opCode5F() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_SRE(src));
    }

    // RRA part
    int KM_RRA(int src) {
        int w = ((src >> 1) | ((p & C_FLAG) << 7)) & 0xff;
        p &= ~(C_FLAG);
        p |= src & C_FLAG;
        KMI_ADC(w);
        return w;
    }

    /** macro - opCodes RRA */
    void opCode63() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode73() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode67() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode77() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode7B() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode6F() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    /** macro - opCodes RRA */
    void opCode7F() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_RRA(src));
    }

    // DCP part

    /** decrement, CMP */
    int KM_DCP(int src) {
        int w = (src - 1) & 0xff;
        KM_CMP(w);
        return w;
    }

    /** macro - opCodes DCP */
    void opCodeC3() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeD3() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeC7() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeD7() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeDB() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeCF() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    /** macro - opCodes DCP */
    void opCodeDF() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_DCP(src));
    }

    // ISC part

    /** increment, SBC */
    int KM_ISC(int src) {
        int w = (src + 1) & 0xff;
        KMI_SBC(w);
        return w;
    }

    /** macro - opCodes ISC */
    void opCodeE3() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeF3() {
        int adr = KA_INDY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeE7() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeF7() {
        int adr = KA_ZPX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeFB() {
        int adr = KA_ABSY();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeEF() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    /** macro - opCodes ISC */
    void opCodeFF() {
        int adr = KA_ABSX();
        int src = K_READ(adr);
        K_WRITE(adr, KM_ISC(src));
    }

    // LAX part

    /** load A and X */
    void KM_LAX(int src) {
        a = src;
        x = src;
        p &= ~(N_FLAG | Z_FLAG);
        p |= fl_table[src & 0xff];
    }

    /** macro - opCodes LAX */
    void opCodeA3() {
        int adr = KA_INDX();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX */
    void opCodeB3() {
        int adr = KA_INDY_();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX */
    void opCodeA7() {
        int adr = KA_ZP();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX */
    void opCodeB7() {
        int adr = KA_ZPY();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX | this one is unstable on hardware */
    void opCodeAB() {
        int adr = KA_IMM();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX */
    void opCodeAF() {
        int adr = KA_ABS();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    /** macro - opCodes LAX */
    void opCodeBF() {
        int adr = KA_ABSY_();
        int src = K_READ(adr);
        KM_LAX(src);
    }

    // SAX part

    /** SAX - store A AND X */
    void opCode83() {
        K_WRITE(KA_INDX(), (a & x));
    }

    /** SAX - store A AND X */
    void opCode87() {
        K_WRITE(KA_ZP(), (a & x));
    }

    /** SAX - store A AND X */
    void opCode97() {
        K_WRITE(KA_ZPY(), (a & x));
    }

    /** SAX - store A AND X */
    void opCode8F() {
        K_WRITE(KA_ABS(), (a & x));
    }

    // AHX part

    /** AHX - store A AND X AND high address (somewhat unstable) */
    void opCode93() {
        int adr = KA_ZPY();
        K_WRITE(adr, 0xff & (a & x & ((adr >> 8) + 1)));
    }

    /** AHX - store A AND X AND high address (somewhat unstable) */
    void opCode9F() {
        int adr = KA_ABSY();
        K_WRITE(adr, 0xff & (a & x & ((adr >> 8) + 1)));
    }

    // TAS part

    /** transfer A AND X to S, store A AND X AND high address */
    void opCode9B() {
        int adr = KA_ABSY();
        s = a & x;
        K_WRITE(adr, 0xff & (s & ((adr >> 8) + 1)));
    }

    // SHY part

    /** store Y AND high address (somewhat unstable) */
    void opCode9C() {
        int adr = KA_ABSX();
        K_WRITE(adr, 0xff & (y & ((adr >> 8) + 1)));
    }

    // SHX part

    /** store X AND high address (somewhat unstable) */
    void opCode9E() {
        int adr = KA_ABSY();
        K_WRITE(adr, 0xff & (x & ((adr >> 8) + 1)));
    }

    // ANC part

    /** a = A AND immediate */
    void opCode0B() {
        int adr = KA_IMM();
        a = 0xff & (a & K_READ(adr));
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= (a >> 7); /* C_FLAG */
    }

    void opCode2B() {
        int adr = KA_IMM();
        a = 0xff & (a & K_READ(adr));
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= (a >> 7) & C_FLAG;
    }

    // XAA part

    /** a = X AND immediate (unstable) */
    void opCode8B() {
        int adr = KA_IMM();
        a = 0xff & (x & K_READ(adr));
        p &= ~(N_FLAG | Z_FLAG);
        p |= fl_table[a & 0xff];
    }

    // ALR part

    /** A AND immediate (unstable), shift right */
    void opCode4B() {
        int adr = KA_IMM();
        int res = 0xff & (a & K_READ(adr));
        a = res >> 1;
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= (res & C_FLAG);
    }

    // ARR part

    /** A AND immediate (unstable), rotate right, weird carry */
    void opCode6B() {
        int adr = KA_IMM();
        int res = 0xff & (a & K_READ(adr));
        a = (res >> 1) + ((p & C_FLAG) << 7);
        p &= ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[a & 0xff];
        p |= (res ^ (res >> 1)) & V_FLAG;
        p |= (res >> 7) & C_FLAG;
    }

    // LAS part

    /** stack AND immediate, copy to A and X */
    void opCodeBB() {
        int adr = KA_ABSY_();
        s &= 0xff & (K_READ(adr));
        a = s;
        x = s;
        p &= ~(N_FLAG | Z_FLAG);
        p |= fl_table[a & 0xff];
    }

    // AXS part

    /** (A & X) - immediate, result in X */
    void opCodeCB() {
        int adr = KA_IMM();
        int res = (a & x) - (0xff & K_READ(adr)); // vavi: op '-' is prior than '&'
        x = 0xff & (res);
        p &= ~(N_FLAG | Z_FLAG | C_FLAG);
        p |= fl_table[x & 0xff];
        p |= (res <= 0xff) ? C_FLAG : 0;
    }

    // SBC part

    /** EB is alternate opCode for SBC E9 */
    void opCodeEB() {
        opCodeE9();
    }
}
