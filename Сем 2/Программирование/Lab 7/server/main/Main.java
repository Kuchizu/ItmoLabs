package main;

import managers.CommandExecutor;
import managers.DBManager;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.time.LocalTime;

/***

 * AYO MAIN
 * Author Kuchizu
 * Var: 3131704
 * <p>
 * Passed 17.05.2023 90 / 100

 ***/

public class Main {
    public static void main(String[] args) throws IOException, SQLException {

        System.out.printf("[%s] Starting server...\n", LocalTime.now());
        System.out.printf("[%s] Syncing DB...\n", LocalTime.now());

        if (args.length > 1) {
            DBManager.setHost(args[1]);
            System.out.printf("[%s] Connecting to %s ...\n", LocalTime.now(), args[1]);
        } else {
            System.out.printf("[%s] Connecting to %s ...\n", LocalTime.now(), "jdbc:postgresql://127.0.0.1:5432/studs");
        }

        DBManager.loadData();

        System.out.printf("[%s] DB synced, loaded %s objects, starting handling...\n", LocalTime.now(), DBManager.getData().size());

        CommandExecutor executor;
        if (args.length >= 1) {
            executor = new CommandExecutor(args);
            System.out.printf("[%s] Server started handling [%s][%s].\n", LocalTime.now(), InetAddress.getLocalHost(), args[0]);
        } else {
            executor = new CommandExecutor();
            System.out.printf("[%s] Server started handling [%s][%s].\n", LocalTime.now(), InetAddress.getLocalHost(), 1234);
        }

        executor.run();
    }
}
