package com.zdream.famitracker.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import com.zdream.famitracker.document.StChanNote;


public class FamitrackerLogger {

    public static final FamitrackerLogger instance = new FamitrackerLogger();

    final boolean muteToDo = true;
    final boolean muteNote = true;
    final boolean muteWriteAddress = true;
    final boolean muteValue = false;

    final HashSet<String> muteAddressSet = new HashSet<>();

    /**
     * 现在是第几帧
     */
    int frame;
    int pattern;
    int row;

    File file;
    FileWriter writer;

    public void createFile(String path) throws IOException {
        file = new File(path);
        writer = new FileWriter(file);
    }

    void writeFile(String s) {
        if (writer != null) {
            try {
                writer.write(s);
                writer.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeFile() throws IOException {
        if (writer == null)
            return;

        writer.flush();
        writer.close();
    }

    private FamitrackerLogger() {
    }

    public void ready() {

    }

    public void logNote(StChanNote note, int channel, int frame, int row) {
        if (muteNote)
            return;

//		if (channel != 3)
//			return;

        String l = "note" + ':' + '[' + channel + ']' + ' ' +
                frame + ':' + row + ' ' +
                note;
        System.out.println(l);
        writeFile(l);
    }

    public void addMuteAddressName(String name) {
        muteAddressSet.add(name);
    }

    public void logWriteAddress(String name, int address, int value) {
        if (muteWriteAddress)
            return;

        if (muteAddressSet.contains(name)) {
            return;
        }

        value = value & 0xFF;

        String l = "write" + ':' + name + ' ' +
                Integer.toHexString(address) + " -> " + value +
                '(' + Integer.toHexString(value) + ',' +
                Integer.toBinaryString(value) + ')';
        System.out.println(l);
        writeFile(l);
    }

    public void logToDo(String msg) {
        if (muteToDo)
            return;

        String l = msg + " | " + Thread.currentThread().getStackTrace()[2];
        System.out.println(l);
        writeFile(l);
    }

    public void notifyFrame(int f, int pattern, int row) {
        this.frame = f;
        this.pattern = pattern;
        this.row = row;
    }

    public void logValue(String s) {
        if (muteValue) {
            return;
        }
		
		/*if (frame == 833) {
			System.out.println(833);
		}*/

        String l = "[" + frame + ',' + Integer.toHexString(pattern) +
                ':' + row + ']' + s;
        System.out.println(l);
        writeFile(l);
    }

}
