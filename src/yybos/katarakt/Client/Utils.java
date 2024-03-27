package yybos.katarakt.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Objects.Media.MediaFile;

import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.sql.Timestamp;
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

    public String serializeObject (Object obj) {
        if (obj == null)
            return "";

        //
        SimpleDateFormat customDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(customDateFormat.format(src)));
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson objParser = gsonBuilder.create();

        return objParser.toJson(obj);
    }
    public void sendObject (Object obj) {
        if (obj == null)
            return;

        //
        SimpleDateFormat customDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Timestamp.class, (JsonSerializer<Timestamp>) (src, typeOfSrc, context) -> context.serialize(customDateFormat.format(src)));
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson objParser = gsonBuilder.create();

        String text = objParser.toJson(obj) + '\0';

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

    // although the MessageServer has access to this method, it should NOT be used by it.
    public void sendFile (MediaFile file) {
        if (file == null)
            return;

        this.sendObject(file);

        // so... to separate the bytes from the json I will receive a byte from the client and then send the actual data
        try {
            this.in.read(new byte[1]);

            // now to the actual data

            File f = file.toFile();
            FileInputStream fileInputStream = new FileInputStream(f);

            int bytesRead;

            // Read and write the file in chunks
            while ((bytesRead = fileInputStream.read(Constants.buffer)) != -1) {
                this.out.write(Constants.buffer, 0, bytesRead);
            }

            this.out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
