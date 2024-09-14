package org.jge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IO {
    
    public static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        byte[] buf = new byte[1024];
        int n;

        try {
            output = new ByteArrayOutputStream(1024);
            while((n = input.read(buf)) >= 0) {
                if(n > 0) {
                    output.write(buf, 0, n);
                }
            }
            return output.toByteArray();
        } finally {
            if(output != null) {
                output.close();
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
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if(output != null) {
                output.close();
            }
        }
    }

    public static void appendAllBytes(byte[] bytes, File file) throws IOException {
        if(!file.exists()) {
            writeAllBytes(bytes, file);
        } else {
            FileOutputStream output = null;
            byte[] b = readAllBytes(file);

            try {
                output = new FileOutputStream(file);
                output.write(b);
                output.write(bytes);
            } finally {
                if(output != null) {
                    output.close();
                }
            }
        }
    }

    public static File file(String path) {
        return new File(path.replace('\\', File.separatorChar).replace('/', File.separatorChar));
    }

    public static File file(File file, String path) {
        return new File(file, IO.file(path).getPath());
    }

    public static String getFilenameWithoutExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');

        if(i >= 0) {
            name = name.substring(0, i);
        }
        return name;
    }

    public static String getExtension(File file) {
        String name = file.getName();
        String extension = "";
        int i = name.lastIndexOf('.');

        if(i >= 0) {
            extension = name.substring(i);
        }
        return extension;
    }
}
