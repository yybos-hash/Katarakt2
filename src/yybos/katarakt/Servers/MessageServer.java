package yybos.katarakt.Servers;

import yybos.katarakt.Client.Utils;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.Chat;
import yybos.katarakt.Objects.Message;
import yybos.katarakt.Objects.User;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MessageServer {
    private final String server = Constants.server;
    private final int port = Constants.messagePort;

    private final List<Socket> messageClients = new ArrayList<>();

    private ServerSocket socket;

    public void run () {
        Thread server = new Thread(this::waitConnections);
        server.start();
    }

    private void waitConnections() {
        try {
            InetAddress InetHostname = InetAddress.getByName(this.server);

            ConsoleLog.info("Binding Chat Server to address " + this.server + ". Using port [" + this.port + ']');

            // bind main server socket
            this.socket = new ServerSocket();
            this.socket.bind(new InetSocketAddress(InetHostname, this.port));

            if (!this.socket.isBound())
                return;

            // server client acceptance
            String clientAddress;
            List<Socket> _clients;
            Socket client;

            try {
                while (true) {
                    client = this.socket.accept();

                    // check to see if the ip is already connected. if it is, then disconnect and remove it from clients
                    clientAddress = client.getInetAddress().getHostAddress();
                    _clients = new ArrayList<>(this.messageClients);
                    for (Socket _client : _clients) {
                        if (_client.getInetAddress().getHostAddress().equals(clientAddress)) {
                            if (!client.isClosed()) {
                                _client.shutdownInput();
                                _client.shutdownOutput();
                                _client.close();
                            }

                            ConsoleLog.info(_client.getInetAddress().toString() + " has been kicked due to already existing connection");
                            this.messageClients.remove(_client);
                        }
                    }
                    this.messageClients.add(client);

                    Socket finalClient = client;
                    Thread clientThread = new Thread(() -> client(finalClient));
                    clientThread.start();
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

    private void client(Socket client) {
        Utils thisClient = new Utils(client);
        DBConnection dbConnection = new DBConnection();

        ConsoleLog.info("Connection accepted: " + client.getInetAddress());

        // test version of the client (side note: now the version will come with the username and password as well. "1.9.0;username;password")
        try {
            String credentials = new String(Constants.buffer, 0, thisClient.in.read(Constants.buffer), Constants.encoding);

            String version = credentials.split(";")[0];
            String email = credentials.split(";")[1].trim(); // trim because when client's email and password are null it send only a " ". Also trim spaces from the email and password
            String password = credentials.split(";")[2].trim();

            // bla bla bla
            if (!version.equals(Constants.version)) {
                thisClient.close();
                ConsoleLog.warning("Version of client " + client.getInetAddress() + " (" + version + ')' +" differs from the server (" + Constants.version + ')');
                return;
            }

            User dbUser = dbConnection.getUser(email);

            // if email doesnt exist
            if (dbUser.getName() == null) {
                thisClient.close();
                return;
            }

            // if password doesnt match
            if (!dbUser.getPass().equals(password)) {
                thisClient.close();
                return;
            }

            // send user information
            String serializedUser = thisClient.serializeObject(dbUser);
            thisClient.sendRawMessage(serializedUser + "\0".repeat(110 - serializedUser.length()));
        }
        catch (Exception e) {
            thisClient.close();

            this.messageClients.remove(client);

            ConsoleLog.exception("Exception in client: " + client.getInetAddress());
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
            return;
        }

        int packet;
        Message message;

        String temp;
        String bucket = "";
        StringBuilder rawMessage = new StringBuilder();

        try {
            while (true) {
                rawMessage = new StringBuilder();

                // receive message
                do {
                    // rawMessage will be the parsed message and the bucket will be the next message. Break the loop and parse :Sex_penis:
                    if (!bucket.isEmpty()) {
                        if (bucket.contains("\0")) {
                            rawMessage = new StringBuilder(bucket.substring(0, bucket.indexOf('\0') + 1));
                            bucket = bucket.substring(bucket.indexOf('\0') + 1);

                            break;
                        }
                        else
                            rawMessage.append(bucket);
                    }

                    packet = thisClient.in.read(Constants.buffer);
                    if (packet <= 0) // if the packet bytes count is less or equal to 0 then the client has disconnected, which means that the thread should be terminated
                        return;

                    temp = new String(Constants.buffer, 0, packet, Constants.encoding);


                    // checks for the \0 in the temp
                    if (temp.contains("\0")) {
                        int i = temp.indexOf('\0');

                        rawMessage.append(temp.substring(0, i + 1));
                        bucket = temp.substring(i + 1);

                        break;
                    }

                    // tem que ter
                    rawMessage.append(temp);
                } while (true);

                // remove null character
                rawMessage = new StringBuilder(rawMessage.toString().replace("\0", ""));

                // parse raw message
                message = Message.fromString(rawMessage.toString());

                // deal with message
                if (message.getType() == Message.Type.Message) {
                    ConsoleLog.info(client.getInetAddress().toString() + ": " + message.getMessage());
                    dbConnection.pushMessage(message);
                }
                else if (message.getType() == Message.Type.Command) {
                    switch (message.getMessage()) {
                        case "getChatHistory": {
                            for (Message chatMessage : dbConnection.getLog(message.getChat()))
                                thisClient.sendObject(chatMessage);

                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            thisClient.close();

            this.messageClients.remove(client);

            ConsoleLog.exception("Client " + client.getInetAddress().toString() + " disconnected");
            ConsoleLog.error(e.getMessage());
        }
    }
}
