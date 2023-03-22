//package managers;
//
//import commandManagers.commands.History;
//import exceptions.CommandInterruptedException;
//
//import java.io.InputStream;
//import java.util.NoSuchElementException;
//import java.util.Scanner;
//
///**
// * A class for executing commands from an input stream.
// */
//public class CommandExecutor {
//    /**
//     * Start executing commands from InputStream.
//     *
//     * @param input commands stream (File, System.in, e.t.c.)
//     * @param mode  variant of command behavior (see CommandMode enum)
//     */
//    public void startExecuting(InputStream input) {
//        Scanner cmdScanner = new Scanner(input);
////        CommandManager commandManager = new CommandManager(mode, cmdScanner);
////        History.initializeCommandsHistoryQueue();
//
//        while (cmdScanner.hasNext()) {
//            String line = cmdScanner.nextLine().trim();
//            if (line.isEmpty()) continue;
//            try {
//                commandManager.executeCommand(line.split(" "));
//                System.out.println();
//            } catch (CommandInterruptedException | NoSuchElementException ex) {
//                if (mode.equals(CommandMode.CLI_UserMode))
//                    System.err.println("Выполнение команды было прервано. Вы можете продолжать работу. Программа возвращена в безопасное состояние.");
//                else System.err.println("Команда была пропущена... Обработчик продолжает работу");
//            }
//        }
//    }
//}