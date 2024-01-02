package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.*;

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
                if (packetObject.getType() == PacketObject.Type.Message) {
                    Message message = Message.fromString(rawMessage.toString());

                    ConsoleLog.info(client.getUsername() + ": " + message.getMessage());
                    dbConnection.pushMessage(message);
                }
                else if (packetObject.getType() == PacketObject.Type.Command) {
                    Command command = Command.fromString(rawMessage.toString());

                    switch (command.getCommand()) {
                        case "getChatHistory": {
                            if (command.getA() == 0)
                                continue;

                            List<Message> log = dbConnection.getLog(command.getA());

                            if (log == null) {
                                client.thisClient.sendObject(Command.errorToast("This chat doesn't exist anymore"));
                                break;
                            }

                            for (Message chatMessage : log)
                                client.thisClient.sendObject(chatMessage);

                            break;
                        }
                        case "getChats": {
                            for (Chat chat : dbConnection.getChats(client.getId())) {
                                client.thisClient.sendObject(chat);
                            }

                            break;
                        }
                        case "setUsername": {
                            if (command.getF() == null || command.getF().isBlank())
                                continue;

                            dbConnection.updateUsername(client.getId(), command.getF());
                            client.setUsername(command.getF());

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
                            if (command.getF() == null || command.getF().isBlank())
                                continue;

                            Chat newChat = dbConnection.createChat(command.getF(), client.getId());

                            if (newChat == null) {
                                client.thisClient.sendObject(Command.errorToast("Failed to create chat :("));
                                client.thisClient.sendObject(Command.toCommand(Constants.outputCommand, "Failed to create chat"));
                                break;
                            }

                            client.thisClient.sendObject(newChat);
                            client.thisClient.sendObject(Command.toCommand(Constants.outputCommand, "Chat created: " + newChat));

                            break;
                        }
                        case "deleteChat": {
                            if (command.getA() == 0)
                                continue;

                            dbConnection.deleteChat(command.getA());

                            break;
                        }

                        // user commands

                        case "cmd": {
                            if (command.getF() == null || command.getF().isBlank())
                                continue;

                            // Command to execute (e.g., "dir" to list files in the current directory)
                            String[] commandArg = command.getF().trim().split(" ");

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

                                this.client.thisClient.sendObject(Command.toCommand(Constants.outputCommand, temp.toString()));
                            } catch (IOException e) {
                                this.client.thisClient.sendObject(Command.toCommand(Constants.outputCommand, "Internal server error"));
                                e.printStackTrace();
                            }

                            break;
                        }

                        default: {
                            this.client.thisClient.sendObject(Command.toCommand(Constants.outputCommand, "Command '" + command.getCommand() + "' not found"));

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
}
