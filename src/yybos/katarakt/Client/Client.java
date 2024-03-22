package yybos.katarakt.Client;

import yybos.katarakt.Objects.Message.User;

import java.net.Socket;

public class Client extends User {
    public final Socket socket;
    public final String ip;

    public final Utils thisClient;

    public Client (Socket clientSocket) {
        this.socket = clientSocket;

        this.ip = socket.getInetAddress().toString().replace("/", "");
        this.thisClient = new Utils(socket);
    }
}
