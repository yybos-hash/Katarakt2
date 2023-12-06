package yybos.katarakt.Client;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Objects.Message;
import yybos.katarakt.Objects.User;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
