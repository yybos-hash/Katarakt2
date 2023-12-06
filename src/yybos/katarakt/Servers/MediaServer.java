package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
import yybos.katarakt.Client.Utils;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.*;

import java.io.IOException;
import java.util.Base64;

public class MediaServer {
    private final Client client;

    public MediaServer (Client client) {
        this.client = client;
    }

    public void run () {
        Thread server = new Thread(() -> this.handleClient(this.client));
        server.start();
    }

    private void handleClient (Client client) {
        DBConnection dbConnection = new DBConnection();

        int packet;
        PacketObject packetObject;

        String bucket = "";
        StringBuilder rawMessage;

        try {
            while (true) {
                rawMessage = new StringBuilder();

                // receive packetObject
                do {
                    // rawMessage will be the parsed packetObject and the bucket will be the next packetObject. Break the loop and parse :Sex_penis:
                    if (!bucket.isEmpty()) {
                        if (bucket.contains("\0")) {
                            rawMessage = new StringBuilder(bucket.substring(0, bucket.indexOf('\0') + 1));
                            bucket = bucket.substring(bucket.indexOf('\0') + 1);

                            break;
                        }
                        else
                            rawMessage.append(bucket);
                    }

                    packet = client.thisClient.in.read(Constants.buffer);
                    if (packet <= 0) // if the packet bytes count is less or equal to 0 then the client has disconnected, which means that the thread should be terminated
                        throw new IOException("Client connection was abruptly interrupted");

                    String temp = new String(Constants.buffer, 0, packet, Constants.encoding);

                    // checks for the \0 in the temp
                    int i = temp.indexOf('\0');
                    if (i != -1) {
                        rawMessage.append(temp, 0, i + 1);
                        bucket = temp.substring(i + 1);

                        break;
                    }

                    // tem que ter
                    rawMessage.append(temp);
                } while (true);
                rawMessage = new StringBuilder(rawMessage.toString().replace("\0", ""));

                // parse raw packetObject
                packetObject = PacketObject.fromString(rawMessage.toString());

                // deal with message
                if (packetObject.getType() == PacketObject.Type.FileRequest) {
                    MediaFile media = MediaFile.fromString(rawMessage.toString());


                }
            }
        }
        catch (Exception e) {
            client.thisClient.close();

            ConsoleLog.info("Client " + client.ip + " disconnected");
            ConsoleLog.exception(e.getMessage());
        }
    }
}
