package org.game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThreadHelper {
    
    public static void startOnFirstThread(String[] args, String jarFileName) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            boolean isRelaunched = "true".equals(System.getProperty("macRelaunched"));

            if (!isRelaunched) {
                Log.getInstance().put(1, "Mac detected without JVM flag. Restarting on first thread...");
                try {
                    List<String> command = new ArrayList<>();
                    command.add("java");
                    command.add("-XstartOnFirstThread");
                    command.add("-DmacRelaunched=true"); 
                    command.add("-jar");
                    command.add(jarFileName);
                    for (String arg : args) {
                        command.add(arg);
                    }

                    ProcessBuilder builder = new ProcessBuilder(command);
                    builder.redirectErrorStream(true);
                    Process process = builder.start();
                    
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Log.getInstance().put(1, line);
                        }
                    }
                    System.exit(0);
                } catch (Exception ex) {
                    Log.getInstance().put(0, ex);
                }
            }
        }

    }
}
