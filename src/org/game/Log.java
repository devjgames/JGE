package org.game;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Log {
    
    private static Log instance = null;

    static {
        new Log();
    }

    public static Log getInstance() {
        return instance;
    }

    private int level = 1;

    public Log() {
        instance = this;
    }

    public final int getLevel() {
        return level;
    }

    public final void setLevel(int level) {
        this.level = level;
    }

    public void put(int level, Object data) {
        if(level <= this.level) {
            System.out.println(toString(data));
        }
    }

    protected String toString(Object data) {
        try {
            if(data == null) {
                return "null";
            } else if(data instanceof Exception || data instanceof Error) {
                ByteArrayOutputStream out = null;

                try {
                    out = new ByteArrayOutputStream(1000);

                    PrintStream s = null;

                    try {
                        s = new PrintStream(out);
                        if(data instanceof Exception) {
                            ((Exception)data).printStackTrace(s);
                        } else {
                            ((Error)data).printStackTrace(s);
                        }
                    } finally {
                        if(s != null) {
                            s.close();
                        }
                    }
                    return new String(out.toByteArray());
                } finally {
                    if(out != null) {
                        out.close();
                    }
                }
            } else {
                return data.toString();
            }
        } catch(Exception | Error ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
