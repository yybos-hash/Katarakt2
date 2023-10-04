package yybos.katarakt.Client;

import com.google.gson.Gson;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Utils {
    public Socket client;
    public OutputStream out;
    public InputStream in;


    public Utils (Socket client) {
        try {
            this.client = client;
            this.out = client.getOutputStream();
            this.in = client.getInputStream();
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Exiting");
        }
    }

    public void sendMessage (Message message) {
        if (message == null)
            return;

        Gson messageParser = new Gson();
        String text = messageParser.toJson(message) + '\0';

        try {
            send(text);
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
    public void sendRawMessage(String message) {
        if (message == null)
            return;

        try {
            send(message);
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
    private void send (String message) throws IOException {
        if (message == null)
            return;

        if (message.isBlank())
            return;

        out.write(message.getBytes(Constants.encoding));
        out.flush();
    }

    public void close () {
        try {
            client.close();
            client.shutdownOutput();
            client.shutdownInput();

            in.close();
            out.close();
        }
        catch (Exception e) {
            ConsoleLog.warning(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
}
