package yybos.katarakt.Client;

import yybos.katarakt.ConsoleLog;
import yybos.katarakt.Constants;
import yybos.katarakt.Objects.Message;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {
    private final Socket socket = new Socket();

    public void connect (String ipv4, int port) {
        if (this.socket.isConnected())
            return;

        SocketAddress address = new InetSocketAddress(ipv4, port);

        try {
            this.socket.connect(address, 4000);

            final Utils thisClient = new Utils(socket);
            thisClient.sendRawMessage(Constants.version);

            int packet;
            Message message;

            String temp;
            String bucket = "";
            StringBuilder rawMessage;

            boolean receiving;

            try {
                while (true) {
                    // receive message
                    do {
                        receiving = true;

                        rawMessage = new StringBuilder();
                        rawMessage.append(bucket);
                        // adds the bucket if there is anything in it

                        // gotta find a way to seek through the entire rawMessage, and then break the do while when finding a null character
                        //

                        packet = thisClient.in.read(Constants.buffer);

                        temp = new String(Constants.buffer, 0, packet, Constants.encoding);
                        rawMessage.append(temp);

                        // checks for the \0 in the temp
                        for (int i = 0; i < temp.length(); i++) {
                            if (temp.charAt(i) == '\0') {
                                receiving = false;

                                // bucket will store the beginning of the other message ( ...}/0{... )
                                if (temp.length() < i + 1)
                                    bucket = "";
                                else
                                    bucket = temp.substring(i + 1);

                                ConsoleLog.info("bucket: " + bucket);

                                break;
                            }
                        }
                    } while (receiving);

                    ConsoleLog.info("b4: " + rawMessage);

                    // parse raw message
                    message = Message.fromString(rawMessage.toString().replace("\0", ""));

                    // deal with message
                    if (message.getType() == Message.Type.Message) {
                        ConsoleLog.info(message.getMessage());
                    }
                    else if (message.getType() == Message.Type.Command) {
                        ConsoleLog.info("Command");
                    }
                }
            }
            catch (Exception e) {
                thisClient.close();

                ConsoleLog.exception("Exception in client: " + this.socket.getInetAddress().toString());
                ConsoleLog.error(e.getMessage());
                ConsoleLog.info("Returning");
            }
        }
        catch (Exception e) {
            ConsoleLog.error(e.getMessage());
            ConsoleLog.info("Returning");
        }
    }
}
