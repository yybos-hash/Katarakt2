package yybos.katarakt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {
    public static final String version = "1.0.0";

    public static final String server = "0.0.0.0";
    public static final int managerPort = 5135;
    public static final int messagePort = managerPort + 1;
    public static final int mediaPort = messagePort + 2;

    public static final String outputCommand = "output";

    public static byte[] buffer = new byte[2048];
    public static Charset encoding = StandardCharsets.UTF_8;
}
