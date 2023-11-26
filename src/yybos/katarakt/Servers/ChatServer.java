package yybos.katarakt.Servers;

import yybos.katarakt.Client.Client;
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
    private Client client;

    public ChatServer (Client client) {
        this.client = client;
    }

    public void run () {
        Thread server = new Thread(() -> client(this.client));
        server.start();
    }

    private void client(Client client) {
        DBConnection dbConnection = new DBConnection();

        // test version of the client (side note: now the version will come with the username and password as well. "1.9.0;username;password")
        try {
            String credentials = client.thisClient.readString();

            String version = credentials.split(";")[0];
            String email = credentials.split(";")[1].trim(); // trim because when client's email and password are null it send only a " ". Also trim spaces from the email and password
            String password = credentials.split(";")[2].trim();

            // bla bla bla
            if (!version.equals(Constants.version)) {
                client.thisClient.close();
                return;
            }


            // get all the chats and send them
            for (Chat chat : dbConnection.getChats(client))
                client.thisClient.sendObject(chat);
            //

            ConsoleLog.info("Done sending chats to client " + client.ip + ". Closing");
        }
        catch (Exception e) {
            client.thisClient.close();

            ConsoleLog.exception("Exception in client: " + client.ip);
            ConsoleLog.error(e.getMessage());
        }
    }
}
