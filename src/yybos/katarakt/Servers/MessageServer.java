package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
                            for (Message chatMessage : dbConnection.getLog(command.getA()))
                                client.thisClient.sendObject(chatMessage);

                            break;
                        }
                        case "getChats": {
                            for (Chat chat : dbConnection.getChats(client.getId()))
                                client.thisClient.sendObject(chat);

                            break;
                        }
                        case "cmd": {
                            // Command to execute (e.g., "dir" to list files in the current directory)
                            String[] commandArg = command.getCommand().split(" ");

                            try {
                                // Execute the command
                                Process process = new ProcessBuilder(commandArg).start();

                                // Read the output of the command
                                InputStream inputStream = process.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                                StringBuilder output = new StringBuilder();
                                String line = reader.readLine();
                                while (line != null) {
                                    output.append(line);

                                    line = reader.readLine();
                                }

                                // Wait for the command to complete
                                process.waitFor();

                                this.client.thisClient.sendObject(Message.toMessage(output.toString(), "Server"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            break;
                        }
                        case "setUsername": {
                            if (command.getF().isBlank())
                                break;

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
                            if (command.getF().isBlank())
                                break;

                            Chat newChat = dbConnection.createChat(command.getF(), client.getId());

                            if (newChat == null) {
                                client.thisClient.sendObject(Command.errorToast("Failed to create chat :("));
                                break;
                            }

                            client.thisClient.sendObject(newChat);

                            break;
                        }
                        case "deleteChat": {
                            dbConnection.deleteChat(command.getA());

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
}
