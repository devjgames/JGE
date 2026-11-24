package org.game;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOHelper {
    
    public static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream out = null;
        byte[] b = new byte[1000];
        int n;

        try {
            out = new ByteArrayOutputStream(1000);
            while((n = input.read(b)) >= 0) {
                if(n > 0) {
                    out.write(b, 0, n);
                }
            }
            return out.toByteArray();
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    public static byte[] readAllBytes(File file) throws IOException {
        FileInputStream input = null;

        try {
            return readAllBytes(input = new FileInputStream(file));
        } finally {
            if(input != null) {
                input.close();
            }
        }
    }

    public static byte[] readAllBytes(Class<?> cls, String name) throws IOException {
        InputStream input = null;

        try {
            return readAllBytes(input = cls.getResourceAsStream(name));
        } finally {
            if(input != null) {
                input.close();
            }
        }
    }

    public static void writeAllBytes(byte[] bytes, File file) throws IOException {
        FileOutputStream output = null;

        try {
            (output = new FileOutputStream(file)).write(bytes);
        } finally {
            if(output != null) {
                output.close();
            }
        }
    }
}
