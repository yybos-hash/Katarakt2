package yybos.katarakt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {
    public static final String version = "1.0.0";

    public static final String server = "192.168.0.111";
    public static final int messagePort = 4080;
    public static final int mediaPort = messagePort + 1;
    public static final int logPort = messagePort + 2;

    public static byte[] buffer = new byte[4096];
    public static Charset encoding = StandardCharsets.UTF_8;
}
