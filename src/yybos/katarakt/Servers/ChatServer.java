package yybos.katarakt.Servers;

import yybos.katarakt.Client.Utils;
import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Database.DBConnection;
import yybos.katarakt.Objects.Chat;
import yybos.katarakt.Objects.Message;
import yybos.katarakt.Objects.User;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final String server = Constants.server;
    private final int port = Constants.chatPort;

    private ServerSocket socket;

    public void run () {
        Thread server = new Thread(this::waitConnections);
        server.start();
    }

    private void waitConnections() {
        try {
            InetAddress InetHostname = InetAddress.getByName(this.server);

            // bind main server socket
            this.socket = new ServerSocket();
            this.socket.bind(new InetSocketAddress(InetHostname, this.port));

            if (!this.socket.isBound())
                return;

            // server client acceptance
            try {
                Socket client;
                while (true) {
                    client = this.socket.accept();

                    Socket finalClient = client;
                    Thread clientThread = new Thread(() -> client(finalClient));
                    clientThread.start();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
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

        // test version of the client (side note: now the version will come with the username and password as well. "1.9.0;username;password")
        try {
            String credentials = new String(Constants.buffer, 0, thisClient.in.read(Constants.buffer), Constants.encoding);

            String version = credentials.split(";")[0];
            String email = credentials.split(";")[1].trim(); // trim because when client's email and password are null it send only a " ". Also trim spaces from the email and password
            String password = credentials.split(";")[2].trim();

            // bla bla bla
            if (!version.equals(Constants.version)) {
                thisClient.close();
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

            // get all the chats and send them
            for (Chat chat : dbConnection.getChats(dbUser))
                thisClient.sendObject(chat);
            //

//            // get the log messages and send them
//            for (Message message : dbConnection.getLog(1))
//                thisClient.sendMessage(message);
//            //

            ConsoleLog.info("Done sending chats to client " + client.getInetAddress() + ". Closing");
        }
        catch (Exception e) {
            thisClient.close();

            ConsoleLog.exception("Exception in client: " + client.getInetAddress());
            ConsoleLog.error(e.getMessage());
        }
    }
}
