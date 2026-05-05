package monitoring.logs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SentinelLogs {
    private static final Path LOG_DIR = Paths.get("log");
    private static final Path LOG_FILE = LOG_DIR.resolve("sentinel.log");
    private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    // instanciamos la clase estatica nativa
    private static final Logger LOGGER = Logger.getLogger(SentinelLogs.class.getName());

    static {
        configurarLogger();
    }

    // constructor privado, para no instanciar por si acaso excepcion
    private SentinelLogs() {
        throw new UnsupportedOperationException("No se puede instanciar SentinelLogs");
    }

    private static void configurarLogger() {
        try {
            Files.createDirectories(LOG_DIR);

            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(Level.ALL);

            FileHandler fileHandler = new FileHandler(LOG_FILE.toString(), true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setEncoding("UTF-8");
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String fecha = FORMATTER_FECHA.format(Instant.ofEpochMilli(record.getMillis()));
                    String throwable = "";
                    if (record.getThrown() != null) {
                        StringWriter sw = new StringWriter();
                        try (PrintWriter pw = new PrintWriter(sw)) {
                            record.getThrown().printStackTrace(pw);
                        }
                        throwable = System.lineSeparator() + sw;
                    }
                    return String.format("[%s] [%s] %s%s%n",
                            fecha,
                            record.getLevel().getName(),
                            formatMessage(record),
                            throwable);
                }
            });

            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            throw new IllegalStateException("No se ha podido inicializar el logger en " + LOG_FILE.toAbsolutePath(), e);
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void advertencia(String msg) {
        LOGGER.warning(msg);
    }

    public static void error(String msg) {
        LOGGER.severe(msg);
    }

    public static void errorConTraza(String mensaje, Throwable excepcion) {
        LOGGER.log(Level.SEVERE, mensaje, excepcion);
    }



}

