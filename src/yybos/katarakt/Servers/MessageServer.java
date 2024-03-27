package yybos.katarakt.Servers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.*;
import yybos.katarakt.Objects.Message.Chat;
import yybos.katarakt.Objects.Message.Command;
import yybos.katarakt.Objects.Message.Message;
import yybos.katarakt.Objects.Message.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MessageServer {
    private final Client client;

    public MessageServer (Client client) {
        this.client = client;
    }

    public void run () {
        Thread server = new Thread(() -> handleClient(this.client));
        server.start();
    }

    private void handleClient(Client client) {
        DBConnection dbConnection = new DBConnection();

        ConsoleLog.info("Connection accepted: " + client.ip);

        int packet;

        String bucket = "";
        StringBuilder rawMessage;

        try {
            while (true) {
                rawMessage = new StringBuilder();

                // receive packet
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

                // get packet type
                PacketObject.Type packetType = this.getPacketType(rawMessage.toString());

                // deal with message
                if (packetType == PacketObject.Type.Message) {
                    Message message = Message.fromString(rawMessage.toString());
                    message.setUserId(client.getId());

                    ConsoleLog.info(client.getUsername() + ": " + message.getMessage());

                    // send the message with the generated id from the database
                    client.thisClient.sendObject(dbConnection.pushMessage(message));
                }
                else if (packetType == PacketObject.Type.Command) {
                    Command command = Command.fromString(rawMessage.toString());

                    switch (command.getCommand()) {
                        case "getChatHistory": {
                            if (command.getArgs().size() == 0)
                                continue;

                            int chatId = command.getArgs().get("chatId").getAsInt();
                            List<Message> log = dbConnection.getLog(chatId);

                            if (log == null) {
                                client.thisClient.sendObject(Command.errorToast("This chat doesn't exist"));
                                break;
                            }

                            for (Message chatMessage : log)
                                client.thisClient.sendObject(chatMessage);

                            break;
                        }
                        case "getChats": {
                            for (Chat chat : dbConnection.getChats(client.getId()))
                                client.thisClient.sendObject(chat);

                            break;
                        }
                        case "setUsername": {
                            if (command.getArgs().size() == 0)
                                continue;

                            String newUsername = command.getArgs().get("username").getAsString();

                            dbConnection.updateUsername(client.getId(), newUsername);
                            client.setUsername(newUsername);

                            User newUser = new User();
                            newUser.setId(client.getId());
                            newUser.setUsername(client.getUsername());
                            newUser.setEmail(client.getEmail());
                            newUser.setPassword(client.getPassword());
                            newUser.setDate(client.getDate());

                            this.client.thisClient.sendObject(newUser);

                            break;
                        }
                        case "createChat": {
                            if (command.getArgs().size() == 0)
                                continue;

                            String chat = command.getArgs().get("chat").getAsString();

                            Chat newChat = dbConnection.createChat(chat, client.getId());

                            if (newChat == null) {
                                client.thisClient.sendObject(Command.errorToast("Failed to create chat :("));
                                client.thisClient.sendObject(Command.output("Failed to create chat"));
                                break;
                            }

                            client.thisClient.sendObject(newChat);
                            client.thisClient.sendObject(Command.output("Chat created: " + newChat));

                            break;
                        }
                        case "deleteChat": {
                            if (command.getArgs().size() == 0)
                                continue;

                            int chat = command.getArgs().get("chatId").getAsInt();
                            dbConnection.deleteChat(chat);

                            break;
                        }

                        // user commands

                        case "cmd": {
                            if (command.getArgs().size() == 0)
                                continue;

                            // Command to execute (e.g., "dir" to list files in the current directory)
                            String prompt = command.getArgs().get("prompt").getAsString();
                            String[] commandArg = prompt.split(" ");

                            try {
                                Process cmd = Runtime.getRuntime().exec(concatenateArrays(new String[]{"cmd.exe", "/c"}, commandArg));
                                BufferedReader buf = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

                                // Wait for the command to complete
                                cmd.waitFor();

                                StringBuilder temp = new StringBuilder();
                                String line;
                                while (true) {
                                    line = buf.readLine();
                                    if (line == null)
                                        break;

                                    temp.append(line).append('\n');
                                }

                                this.client.thisClient.sendObject(Command.output(temp.toString()));
                            } catch (IOException e) {
                                this.client.thisClient.sendObject(Command.output("Internal server error"));
                                e.printStackTrace();
                            }

                            break;
                        }

                        // otaku uwu

                        case "getAnimeList": {
                            for (Anime anime : dbConnection.getAnimeList())
                                client.thisClient.sendObject(anime);

                            break;
                        }
                        case "getTowatch": {
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

    // Concatenate two arrays
    private static String[] concatenateArrays(String[] array1, String[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;

        // Create a new array with combined length
        String[] resultArray = new String[length1 + length2];

        // Copy elements from the first array
        System.arraycopy(array1, 0, resultArray, 0, length1);

        // Copy elements from the second array
        System.arraycopy(array2, 0, resultArray, length1, length2);

        return resultArray;
    }
    private PacketObject.Type getPacketType (String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        return PacketObject.Type.getEnumByValue(jsonObject.get("type").getAsInt());
    }
}
