package org.jge;

import java.awt.Window;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;


public class Utils {

    public static void copy(Object src, Object dst) throws Exception {
        Field[] fields = src.getClass().getFields();

        for(Field field : fields) {
            Class<?> cls = field.getType();
            int m = field.getModifiers();

            if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
                if(
                    int.class.isAssignableFrom(cls) ||
                    float.class.isAssignableFrom(cls) ||
                    boolean.class.isAssignableFrom(cls) ||
                    String.class.isAssignableFrom(cls) || 
                    cls.isEnum()
                ) {
                    field.set(dst, field.get(src));
                } else if(Vector2f.class.isAssignableFrom(cls)) {
                    Vector2f v1 = (Vector2f)field.get(src);
                    Vector2f v2 = (Vector2f)field.get(dst);

                    v2.set(v1);
                } else if(Vector3f.class.isAssignableFrom(cls)) {
                    Vector3f v1 = (Vector3f)field.get(src);
                    Vector3f v2 = (Vector3f)field.get(dst);

                    v2.set(v1);
                } else if(Vector4f.class.isAssignableFrom(cls)) {
                    Vector4f v1 = (Vector4f)field.get(src);
                    Vector4f v2 = (Vector4f)field.get(dst);

                    v2.set(v1);
                } else if(Matrix4f.class.isAssignableFrom(cls)) {
                    Matrix4f m1 = (Matrix4f)field.get(src);
                    Matrix4f m2 = (Matrix4f)field.get(dst);

                   m2.set(m1);
                }
            }
        }
    }

    public static String toString(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getField(fieldName);
        Class<?> cls = field.getType();
        int m = field.getModifiers();
        String s = null;

        if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
            if(
                int.class.isAssignableFrom(cls) ||
                float.class.isAssignableFrom(cls) ||
                boolean.class.isAssignableFrom(cls) ||
                String.class.isAssignableFrom(cls)
            ) {
                s = field.get(obj).toString();
            } else if(Vector2f.class.isAssignableFrom(cls)) {
                Vector2f v = (Vector2f)field.get(obj);

                s = v.x + " " + v.y;
            } else if(Vector3f.class.isAssignableFrom(cls)) {
                Vector3f v = (Vector3f)field.get(obj);

                s = v.x + " " + v.y + " " + v.z;
            } else if(Vector4f.class.isAssignableFrom(cls)) {
                Vector4f v = (Vector4f)field.get(obj);

                s = v.x + " " + v.y + " " + v.z + " " + v.w;
            } else if(Matrix4f.class.isAssignableFrom(cls)) {
                Matrix4f mat = (Matrix4f)field.get(obj);

                s = 
                mat.m00() + " " + mat.m01() + " " + mat.m02() + " " + mat.m03() + " " +
                mat.m10() + " " + mat.m11() + " " + mat.m12() + " " + mat.m13() + " " +
                mat.m20() + " " + mat.m21() + " " + mat.m22() + " " + mat.m23() + " " +
                mat.m30() + " " + mat.m31() + " " + mat.m32() + " " + mat.m33();
            } else if(cls.isEnum()) {
                s = field.get(obj).toString();
            }
        }
        return s;
    }
    
    public static void parse(Object obj, String fieldName, String text) throws Exception {
        Field field = obj.getClass().getField(fieldName);
        Class<?> cls = field.getType();
        int m = field.getModifiers();

        if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
            if(int.class.isAssignableFrom(cls)) {
                field.set(obj, Integer.parseInt(text));
            } else if(float.class.isAssignableFrom(cls)) {
                field.set(obj, Float.parseFloat(text));
            } else if(boolean.class.isAssignableFrom(cls)) {
                field.set(obj, Boolean.parseBoolean(text));
            } else if(String.class.isAssignableFrom(cls)) {
                field.set(obj, text);
            } else if(Vector2f.class.isAssignableFrom(cls)) {
                String[] tokens = text.split("\\s+");

                if(tokens.length >= 2) {
                    Vector2f v = (Vector2f)field.get(obj);

                    v.x = Float.parseFloat(tokens[0]);
                    v.y = Float.parseFloat(tokens[1]);
                }
            } else if(Vector3f.class.isAssignableFrom(cls)) {
                String[] tokens = text.split("\\s+");

                if(tokens.length >= 3) {
                    Vector3f v = (Vector3f)field.get(obj);

                    v.x = Float.parseFloat(tokens[0]);
                    v.y = Float.parseFloat(tokens[1]);
                    v.z = Float.parseFloat(tokens[2]);
                }
            } else if(Vector4f.class.isAssignableFrom(cls)) {
                String[] tokens = text.split("\\s+");

                if(tokens.length >= 4) {
                    Vector4f v = (Vector4f)field.get(obj);

                    v.x = Float.parseFloat(tokens[0]);
                    v.y = Float.parseFloat(tokens[1]);
                    v.z = Float.parseFloat(tokens[2]);
                    v.w = Float.parseFloat(tokens[3]);
                }
            } else if(Matrix4f.class.isAssignableFrom(cls)) {
                String[] tokens = text.split("\\s+");

                if(tokens.length >= 16) {
                    Matrix4f mat = (Matrix4f)field.get(obj);

                    mat.set(
                        Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]),
                        Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5]), Float.parseFloat(tokens[6]), Float.parseFloat(tokens[7]),
                        Float.parseFloat(tokens[8]), Float.parseFloat(tokens[9]), Float.parseFloat(tokens[10]), Float.parseFloat(tokens[11]),
                        Float.parseFloat(tokens[12]), Float.parseFloat(tokens[13]), Float.parseFloat(tokens[14]), Float.parseFloat(tokens[15])
                    );
                }
            } else if(cls.isEnum()) {
                Object[] constants = cls.getEnumConstants();
                for(int i = 0; i != constants.length; i++) {
                    String ename = constants[i].toString();
                    if(ename.equals(text)) {
                        field.set(obj, constants[i]);
                        break;
                    }
                }
            }
        }
    }

    private static class DarkTheme extends DefaultMetalTheme {

        public DarkTheme() {

            UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Color.BLACK));
        }

        public String getName() {
            return "DarkTheme";
        }

        @Override
        public ColorUIResource getControl() {
             return new ColorUIResource(new Color(50, 50, 50));
        }

        @Override
        public ColorUIResource getControlDarkShadow() {
            return new ColorUIResource(new Color(10, 10, 10));
        }

        @Override
        public ColorUIResource getControlShadow() {
            return new ColorUIResource(new Color(25, 25, 25));
        }

        @Override
        public ColorUIResource getControlHighlight() {
            return new ColorUIResource(new Color(75, 75, 75));
        }

        @Override
        public ColorUIResource getControlDisabled() {
            return new ColorUIResource(new Color(60, 60, 60));
        }

        @Override
        public ColorUIResource getWindowBackground() {
            return new ColorUIResource(new Color(75, 75, 75));
        }

        @Override
        public ColorUIResource getUserTextColor() {
            return new ColorUIResource(new Color(150, 150, 150));
        }

        @Override
        public ColorUIResource getControlTextColor() {
            return new ColorUIResource(new Color(150, 150, 150));
        }

        @Override
        public ColorUIResource getSystemTextColor() {
            return new ColorUIResource(new Color(150, 150, 150));
        }
    }


    public static void setLookAndFeel() {
        try {
            MetalLookAndFeel.setCurrentTheme(new DarkTheme());
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    public static File selectFile(Window parent, File directory, String extension) {
        Vector<Object> paths = new Vector<>();

        appendFiles(directory, extension, paths);

        if(paths.size() == 0) {
            return null;
        }

        paths.sort((a, b) -> ((String)a).compareTo((String)b));

        Object r = JOptionPane.showInputDialog(parent, "Select", "Select", JOptionPane.INFORMATION_MESSAGE, null, paths.toArray(), paths.get(0));

        if(r != null) {
            return IO.file((String)r);
        }
        return null;
    }

    private static void appendFiles(File directory, String extension, Vector<Object> paths) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                if(file.isFile() && IO.getExtension(file).equals(extension)) {
                    paths.add(file.getPath());
                }
            }
            for(File file : files) {
                if(file.isDirectory()) {
                    appendFiles(file, extension, paths);
                }
            }
        }
    }
}
