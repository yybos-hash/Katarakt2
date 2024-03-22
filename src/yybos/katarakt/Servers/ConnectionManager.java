package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.Message.Command;
import yybos.katarakt.Objects.Login;
import yybos.katarakt.Objects.Message.Message;
import yybos.katarakt.Objects.Message.User;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private final List<Client> messageClients = new ArrayList<>();

    public void listen () {
        try {
            InetAddress InetHostname = InetAddress.getByName(Constants.server);

            ConsoleLog.info("Binding Connection Manager to address " + Constants.server + ". Using port [" + Constants.managerPort + ']');

            // bind main server socket
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(InetHostname, Constants.managerPort));

            if (!server.isBound())
                return;

            // server client acceptance
            Client client;
            DBConnection db = new DBConnection();

            try {
                while (true) {
                    // in case it sends an RST package and resets the connection with the peer
                    try {
                        client = new Client(server.accept());

                        // receive credentials packetObject
                        String credentials;
                        try {
                            int packet;
                            StringBuilder rawMessage = new StringBuilder();

                            // while there is no null character in temp, keep receiving
                            do {
                                packet = client.thisClient.in.read(Constants.buffer);
                                if (packet <= 0) // if the packet bytes count is less or equal to 0 then the client has disconnected, which means that the thread should be terminated
                                    throw new IOException("Client connection was abruptly interrupted");

                                String temp = new String(Constants.buffer, 0, packet, Constants.encoding);

                                if (!temp.contains("\0"))
                                    rawMessage.append(temp);
                                else {
                                    rawMessage.append(temp, 0, temp.indexOf("\0") + 1);
                                    break;
                                }
                            } while (true);
                            credentials = rawMessage.toString().replace("\0", "");
                        }
                        catch (Exception e) {
                            ConsoleLog.exception(e.getMessage());
                            return;
                        }

                        // authentication
                        Login login = Login.fromString(credentials);

                        String version = login.getVersion();
                        String email = login.getEmail().trim(); // trim because when client's email and password are null it sends only a " ". Also trim spaces from the email and password
                        String password = login.getPassword().trim();
                        int serverId = login.getServer();

                        if (!version.equals(Constants.version)) {
                            Message message = Message.toMessage("Wrong version, stupid", "Server");
                            client.thisClient.sendObject(message);

                            continue;
                        }

                        User dbUser = db.getUser(email);

                        if (dbUser.getEmail() == null) {
                            dbUser.setUsername(client.ip);

                            // user doesnt exist (yet). Register him
                            db.registerUser(email, "", password);

                            client.thisClient.sendObject(Message.toMessage("No account? No problem. I (The Server) created a brand new account for you", "Server"));
                            client.thisClient.sendObject(Message.toMessage("Oh, by the way! A main chat was created for you as well. Of course, you can rename the chat later (Maybe, idk if I will create that function)", "The server.. Again"));
                        }
                        else if (!password.equals(dbUser.getPassword())) {
                            client.thisClient.sendObject(Command.errorToast("Wrong password, lil nigga"));
                            client.thisClient.close();

                            continue;
                        }

                        // if the user doesnt have an username the server will keep asking for it everytime he logs in
                        if (dbUser.getUsername().isBlank() || dbUser.getUsername() == null) {
                            Command command = Command.askForUsername();
                            client.thisClient.sendObject(command);
                        }

                        // get the dbUser again (because of the id)
                        dbUser = db.getUser(email);

                        client.setId(dbUser.getId());
                        client.setUsername(dbUser.getUsername());
                        client.setEmail(email);
                        client.setPassword(password);

                        // send the credentials to the client
                        client.thisClient.sendObject(dbUser);

                        if (serverId == Constants.messagePort) {
                            MessageServer messageServer = new MessageServer(client);
                            messageServer.run();
                        }
                        else if (serverId == Constants.mediaPort) {
                            MediaServer mediaServer = new MediaServer(client);
                            mediaServer.run();
                        }
                    }
                    catch (Exception e) {
                        ConsoleLog.exception("Exception in Connection Manager");
                        e.printStackTrace();
                        ConsoleLog.info("Happens. Continuing");
                        continue;
                    }

                    this.messageClients.add(client);
                }
            }
            catch (Exception e) {
                ConsoleLog.error(e.getMessage());
                ConsoleLog.info("Exiting");
            }
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Exiting");
        }
    }
}
