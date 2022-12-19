package edu.dkv.internal.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Utils {

    public static String getFullStackTrace(Exception e){
        OutputStream os = new OutputStream() {

            private StringBuilder sb = new StringBuilder();

            @Override
            public void write(int i) throws IOException {
                this.sb.append((char)i);
            }

            public String toString(){
                return this.sb.toString();
            }
        };

        PrintWriter pw = new PrintWriter(os);
        e.printStackTrace(pw);
        pw.close();

        return os.toString();
    }


}
