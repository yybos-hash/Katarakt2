package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.Message;
import yybos.katarakt.Objects.PacketObject;
import yybos.katarakt.Objects.User;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private final List<Client> messageClients = new ArrayList<>();

    public void listen () {
        try {
            InetAddress InetHostname = InetAddress.getByName(Constants.server);

            ConsoleLog.info("Binding Connection Manager to address " + Constants.server + ". Using port [" + Constants.messagePort + ']');

            // bind main server socket
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(InetHostname, Constants.messagePort));

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

                        // authentication
                        // test version of the client (side note: now the version will come with the username and password as well. "1.9.0;username;password")
                        String credentials = client.thisClient.readString();

                        String version = credentials.split(";")[0];
                        String email = credentials.split(";")[1].trim(); // trim because when client's email and password are null it send only a " ". Also trim spaces from the email and password
                        String password = credentials.split(";")[2].trim();

                        if (!version.equals(Constants.version)) {
                            Message message = Message.toMessage("Wrong version, stupid", 0, "Server", 0);
                            client.thisClient.sendObject(message);

                            continue;
                        }

                        User dbUser = db.getUser(email);

                        client.setEmail(email);
                        client.setPassword(password);

                        if (dbUser.getEmail() == null) {
                            Message message = Message.toMessage("Email does not exist", 0, "Server", 0);
                            client.thisClient.sendObject(message);

                            continue;
                        }
                        else if (!client.getPassword().equals(dbUser.getPassword())) {
                            Message message = Message.toMessage("Apologies, nigga. But the PASSWORD DOES NOT SEEM TO BE CORRECT", 0, "Server", 0);
                            client.thisClient.sendObject(message);

                            continue;
                        }

                        client.setId(dbUser.getId());
                        client.setUsername(dbUser.getUsername());

                        // send the credentials to the client
                        client.thisClient.sendObject(User.toUser(client.getId(), client.getUsername(), client.getEmail(), client.getPassword()));

                        MessageServer messageServer = new MessageServer(client);
                        messageServer.run();
                    }
                    catch (Exception e) {
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
