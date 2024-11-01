package org.jge;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextAreaStream extends OutputStream {

    public static boolean consolePaused = false;

    private JTextArea textArea;
    private final File logFile = IO.file("log.txt");

    public TextAreaStream(JTextArea textArea) {
        this.textArea = textArea;

        if(logFile.exists()) {
            logFile.delete();
        }
        consolePaused = false;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if(consolePaused) {
            return;
        }
        String s = new String(b, off, len);

        textArea.append(s);
        textArea.setCaretPosition(textArea.getText().length());

        try {
            IO.appendAllBytes(s.getBytes(), logFile);
        } catch(Exception ex) {
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }
}
