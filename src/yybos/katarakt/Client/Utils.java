package yybos.katarakt.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;

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

        //
            SimpleDateFormat customDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(customDateFormat.format(src)));
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson messageParser = gsonBuilder.create();

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
            in.close();
            out.close();

            client.close();
            client.shutdownOutput();
            client.shutdownInput();
        }
        catch (Exception e) {
            ConsoleLog.warning(e.getMessage());
        }
    }
}
