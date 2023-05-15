import java.util.concurrent.*;

public class CommandExecutor {
    // ...
    // Ваш код
    // ...

    private static final ConcurrentMap<String, Command> commands = new ConcurrentHashMap<>(){
        {
            // Ваши команды
        }
    };

    private final ExecutorService sendResponseExecutor = Executors.newFixedThreadPool(10);
    private final ForkJoinPool readAndHandleRequestPool = new ForkJoinPool();

    public void run() throws IOException {
        // ...
        // Ваш код
        // ...

        while(true){
            readAndHandleRequestPool.submit(() -> {
                try {
                    DatagramPacket cmdPacket = new DatagramPacket(new byte[3000],3000);
                    datagramSocket.receive(cmdPacket);

                    InetAddress inetAddress = cmdPacket.getAddress();
                    int port = cmdPacket.getPort();

                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cmdPacket.getData()));

                    Object obj;
                    InfoPacket inf = null;
                    while (!((obj = ois.readObject()) instanceof EofIndicatorClass)) {
                        inf = (InfoPacket) obj;
                    }
                    assert inf != null;

                    // Обработка запроса
                    ForkJoinPool.commonPool().submit(() -> {
                        // Ваш код обработки запроса

                        // Отправка ответа
                        sendResponseExecutor.submit(() -> {
                            try {
                                // Ваш код отправки ответа
                            } catch (Exception e) {
                                // Обработка исключений
                            }
                        });
                    });
                } catch (Exception e) {
                    // Обработка исключений
                }
            });
        }
    }
}
