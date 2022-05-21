package cn.bavelee.freereflection;

import java.io.PrintStream;

public class Log {
    private static boolean enabled = true;
    private static final String TAG = "[FreeReflection] ";

    public static void setEnabled(boolean enabled) {
        Log.enabled = enabled;
    }

    public static void info(String... texts) {
        print(System.out, texts);
    }

    public static void error(String... texts) {
        print(System.err, texts);
    }

    private static void print(PrintStream ps, String... texts) {
        if (!enabled) {
            return;
        }
        ps.print(TAG);
        ps.print(" ");
        for (String text : texts) {
            ps.print(text);
        }
        ps.print("\n");
    }
}
