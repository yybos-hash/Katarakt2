package yybos.katarakt.Servers;

import yybos.katarakt.Client.Utils;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.Message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MessageServer {
    private final String server = Constants.server;
    private final int port = Constants.messagePort;

    private final List<Socket> clients = new ArrayList<>();

    private ServerSocket socket;

    public void run () {
        waitConnections();
    }

    private void waitConnections () {
        try {
            InetAddress InetHostname = InetAddress.getByName(server);

            ConsoleLog.info("Binding to address " + server);
            ConsoleLog.info("Using ports [" + Constants.messagePort + " - " + (Constants.messagePort + 3) + ']');

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

                    // copy client to effectively final temporary variable
                    Socket finalClient = client;
                    new Thread(() -> client(finalClient)).start();

                    // check to see if the ip is already connected. if it is, then disconnect and remove it from clients
                    clientAddress = client.getInetAddress().getHostAddress();
                    _clients = new ArrayList<>(clients);
                    for (Socket _client : _clients) {
                        if (_client.getInetAddress().getHostAddress().equals(clientAddress)) {
                            if (!client.isClosed()) {
                                _client.shutdownInput();
                                _client.shutdownOutput();
                                _client.close();
                            }
                            clients.remove(_client);
                        }
                    }
                    clients.add(client);
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

    private void client (Socket client) {
        Utils thisClient = new Utils(client);
        DBConnection dbConnection = new DBConnection();

        ConsoleLog.info("Connection accepted: " + client.getInetAddress());

        // test version of the client
        try {
            String version = new String(Constants.buffer, 0, thisClient.in.read(Constants.buffer), Constants.encoding);

            // did NOT pass the inspection
            if (!version.equals(Constants.version)) {
                thisClient.close();
                ConsoleLog.warning("Version of client " + client.getInetAddress() + " (" + version + ')' +" differs from the server (" + Constants.version + ')');
                return;
            }
        }
        catch (Exception e) {
            ConsoleLog.exception("Exception in client: " + client.getInetAddress());
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
            return;
        }

        // get the log messages and send them
        List<Message> messages = dbConnection.getLog(1);
        for (Message message : messages)
            thisClient.sendMessage(message);

        thisClient.sendMessage(Message.toMessage(Message.Type.Message, "cu", 1, 2));

        //
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

                    // rawMessage will be the parsed message, and the bucket will be the next message. Break the loop and parse :Sex_penis:
                    if (!bucket.isEmpty()) {
                        rawMessage = new StringBuilder(bucket.substring(0, bucket.indexOf('\0')));
                        bucket = bucket.substring(bucket.indexOf('\0') + 1);

                        break;
                    }

                    packet = thisClient.in.read(Constants.buffer);
                    if (packet <= 0)
                        continue;

                    temp = new String(Constants.buffer, 0, packet, Constants.encoding);

                    System.out.println("b: " + temp);

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

            e.printStackTrace();
            ConsoleLog.exception("Exception in client: " + client.getInetAddress().toString());
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
}
