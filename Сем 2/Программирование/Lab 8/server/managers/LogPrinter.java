package managers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogPrinter extends PrintStream {
    private static final Logger LOGGER = Logger.getLogger(CommandExecutor.class.getName());

    public LogPrinter(OutputStream out) throws IOException {
        super(out);

        File dir = new File("Logs");
        if (!dir.exists()) {
            dir.mkdir();
        }

        FileHandler fh = new FileHandler(String.format("Logs/[%s] Serverlogs.log", new Date()));
        fh.setFormatter(new SimpleFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(fh);
        LOGGER.setLevel(Level.INFO);
    }

    @Override
    public void print(String s) {
        super.print(s);
        LOGGER.info(s);
    }

    @Override
    public void println(String s) {
        super.println(s);
        LOGGER.info(s);
    }

}