package yybos.katarakt;

import yybos.katarakt.Client.Client;
import yybos.katarakt.Objects.Chat;
import yybos.katarakt.Servers.ChatServer;
import yybos.katarakt.Servers.ConnectionManager;
import yybos.katarakt.Servers.MessageServer;

public class Main {
    /*
    *   Ports used 4080 - 4083
    */

    public static void main(String[] args) throws InterruptedException {
        ConsoleLog.info("Katarakt 2\n");
        System.out.print("           Initiating");

        // pretty pretty
        Thread.sleep(300);
        for (int i = 0; i < 3; i++) {
            System.out.print('.');
            Thread.sleep(300);
        }

        System.out.println("");

        ConnectionManager manager = new ConnectionManager();
        manager.listen();
    }
}
