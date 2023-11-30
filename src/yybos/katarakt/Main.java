package yybos.katarakt;

import yybos.katarakt.Servers.ConnectionManager;

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
