package yybos.katarakt.Servers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.*;
import yybos.katarakt.Objects.Media.Directory;
import yybos.katarakt.Objects.Media.DirectoryObject;
import yybos.katarakt.Objects.Media.MediaFile;
import yybos.katarakt.Objects.Message.Command;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MediaServer {
    private final Client client;

    public MediaServer (Client client) {
        this.client = client;
    }

    public void run () {
        Thread server = new Thread(() -> this.handleClient(this.client));
        server.start();
    }

    /*
    *   NOTE: MediaServer will not send previews, it will only send the files data
    */
    private void handleClient (Client client) {
        DBConnection dbConnection = new DBConnection();

        String bucket = "";
        StringBuilder rawMessage;

        try {
            Thread.sleep(5000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

//        MediaFile m = MediaFile.toMediaFile("C:\\Users\\plusm\\Downloads\\H.mp3", DOWNLOAD);
//        m.getUser().setUsername("Server");
//        client.thisClient.sendFile(m);

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

                    int packet = client.thisClient.in.read(Constants.buffer);
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
                PacketObject.Type packetType = this.getPacketType(rawMessage.toString());

                // deal with file
                if (packetType == PacketObject.Type.File) {
                    MediaFile media = MediaFile.fromString(rawMessage.toString());

                    if (media.getFileType() == MediaFile.MediaFileType.UPLOAD) {
                        File file = media.toFile();
                        if (!file.exists())
                            file.createNewFile();

                        client.thisClient.sendObject(media);

                        FileOutputStream outputStream = new FileOutputStream(file);

                        long progress = 0;
                        int packet;

                        System.out.println("filesize " + media.getSize());
                        do {
                            packet = client.thisClient.in.read(Constants.buffer);
                            progress += packet; // increase progress with each packet size

                            outputStream.write(packet);
                            System.out.println("progress at " + progress / media.getSize() * 100 + '%');
                        } while (progress < media.getSize());

                        outputStream.close();
                    }
                    else {
                        try {
                            File file = media.toFile();
                            if (!file.exists())
                                continue;

                            media.setSize(file.length());
                            client.thisClient.sendObject(media);

                            FileInputStream inputStream = new FileInputStream(file);

                            do {
                                client.thisClient.out.write(inputStream.read(Constants.buffer));
                                client.thisClient.out.flush();

                            } while (inputStream.available() != 0);

                            System.out.println("done sending");
                            inputStream.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if (packetType == PacketObject.Type.Command) {
                    Command command = Command.fromString(rawMessage.toString());

                    switch (command.getCommand()) {
                        case "getSubs": {
                            if (command.getArgs().size() == 0)
                                continue;

                            String path = command.getArgs().get("path").getAsString();

                            try {
                                Directory dir = new Directory();
                                dir.path = path;

                                // List immediate files and subdirectories
                                Files.list(Paths.get(path))
                                    .forEach(filePath -> {
                                        DirectoryObject obj = new DirectoryObject();
                                        obj.path = path;

                                        if (Files.isDirectory(filePath))
                                            obj.isFolder = true;
                                        else if (Files.isRegularFile(filePath))
                                            obj.isFolder = false;

                                        dir.objects.add(obj);
                                    });

                                client.thisClient.sendObject(dir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
                        }
                        case "getChats": {

                            break;
                        }

                        default: {
                            this.client.thisClient.sendObject(Command.output("Command '" + command.getCommand() + "' not found"));

                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            client.thisClient.close();

            ConsoleLog.info("Client " + client.ip + " disconnected");
            ConsoleLog.exception(e.getMessage());
        }
    }

    private PacketObject.Type getPacketType (String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return PacketObject.Type.getEnumByValue(jsonObject.get("type").getAsInt());
    }
}
