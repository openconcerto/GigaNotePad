package test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AsciiTableGenerator {

    public static void main(String[] args) throws IOException {
        StringBuilder b = new StringBuilder();
        b.append("| Dec. | Character |");
        b.append("\r\n");
        for (int i = 0; i < 127; i++) {
            if (i == 10 || i == 13) {
                continue;
            }

            String str = String.valueOf(i);
            if (str.length() == 1) {
                str = "  " + str;
            }
            if (str.length() == 2) {
                str = " " + str;
            }
            b.append("|  " + str + " |         " + (char) i + " |");
            b.append("\r\n");
        }

        try (BufferedOutputStream f = new BufferedOutputStream(new FileOutputStream(new File("ascii.txt")), 32 * 1000 * 1000)) {
            f.write(b.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

}
