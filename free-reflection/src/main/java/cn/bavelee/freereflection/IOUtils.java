package cn.bavelee.freereflection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class IOUtils {
    public static boolean writeStreamToFile(InputStream is, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[2048];
            int cnt = 0;
            while ((cnt = is.read(buf)) > 0) {
                fos.write(buf, 0, cnt);
            }
            fos.flush();
            fos.close();
            is.close();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
