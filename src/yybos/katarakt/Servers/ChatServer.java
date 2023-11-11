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

public class ChatServer {
    private final String server = Constants.server;
    private final int port = Constants.messagePort;

    private final List<Socket> messageClients = new ArrayList<>();

    private ServerSocket socket;

    public void run () {
        Thread server = new Thread(this::waitMessageConnections);
        server.start();
    }

    private void waitMessageConnections() {
        try {
            InetAddress InetHostname = InetAddress.getByName(server);

            ConsoleLog.info("Binding Chat Server to address " + server);
            ConsoleLog.info("Using ports [" + Constants.chatPort + ']');

            // bind main server socket
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress(InetHostname, port));

            if (!socket.isBound())
                return;

            // server client acceptance
            String clientAddress;
            List<Socket> _clients;
            Socket client;

            try {
                while (true) {
                    client = socket.accept();

                    // check to see if the ip is already connected. if it is, then disconnect and remove it from clients
                    clientAddress = client.getInetAddress().getHostAddress();
                    _clients = new ArrayList<>(messageClients);
                    for (Socket _client : _clients) {
                        if (_client.getInetAddress().getHostAddress().equals(clientAddress)) {
                            if (!client.isClosed()) {
                                _client.shutdownInput();
                                _client.shutdownOutput();
                                _client.close();
                            }

                            ConsoleLog.info(_client.getInetAddress().toString() + " has been kicked due to already existing connection");
                            messageClients.remove(_client);
                        }
                    }
                    messageClients.add(client);

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
            String username = credentials.split(";")[1].trim(); // trim because when client's username and password are null it send only a " ". Also trim spaces from the username and password
            String password = credentials.split(";")[2].trim();

            // bla bla bla
            if (!version.equals(Constants.version)) {
                thisClient.close();
                ConsoleLog.warning("Version of client " + client.getInetAddress() + " (" + version + ')' +" differs from the server (" + Constants.version + ')');
                return;
            }

            User dbUser = dbConnection.getUser(username);

            // if username doesnt exist
            if (dbUser.getName() == null) {
                thisClient.close();
                return;
            }

            // if password doesnt match
            if (!dbUser.getPass().equals(password)) {
                thisClient.close();
                return;
            }

            // get all the chats and send them
            for (Chat chat : dbConnection.getChats(dbUser))
                thisClient.sendMessage(Message.toMessage(Message.Type.Chat, chat.getNm() + ';' + chat.getId() + ';' + chat.getDate(), 0, "", 0));
            //

//            // get the log messages and send them
//            for (Message message : dbConnection.getLog(1))
//                thisClient.sendMessage(message);
//            //
        }
        catch (Exception e) {
            thisClient.close();

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

        boolean receiving;

        try {
            while (true) {
                // receive message
                do {
                    receiving = true;

                    // rawMessage will be the parsed message, and the bucket will be the next message. Break the loop and parse :Sex_Penis:
                    if (!bucket.isEmpty()) {
                        rawMessage = new StringBuilder(bucket.substring(0, bucket.indexOf('\0')));
                        bucket = bucket.substring(bucket.indexOf('\0') + 1);

                        break;
                    }

                    packet = thisClient.in.read(Constants.buffer);
                    if (packet <= 0) // if the packet bytes count is less or equal to 0 then the client has disconnected, which means that the thread should be terminated
                        return;

                    temp = new String(Constants.buffer, 0, packet, Constants.encoding);

                    // checks for the \0 in the temp
                    for (int i = 0; i < temp.length(); i++) {
                        if (temp.charAt(i) == '\0') {
                            receiving = false;

                            // bucket will store the beginning of the other message ( ...}/0{... )
                            bucket = temp.substring(i + 1);
                            rawMessage = new StringBuilder(temp.substring(0, i));

                            break;
                        }
                    }
                } while (receiving);

                if (rawMessage.length() == 0)
                    continue;

                // remove null character
                rawMessage = new StringBuilder(rawMessage.toString().replace("\0", ""));

                // parse raw message
                message = Message.fromString(rawMessage.toString());

                // deal with message
                if (message.getType() == Message.Type.Message) {
                    ConsoleLog.info("Message");
                }
                else if (message.getType() == Message.Type.Command) {
                    ConsoleLog.info("Command");
                }

                dbConnection.pushMessage(message);
            }
        }
        catch (Exception e) {
            thisClient.close();

            ConsoleLog.exception("Client " + client.getInetAddress().toString() + " disconnected");
            ConsoleLog.error(e.getMessage());
        }
    }
}
