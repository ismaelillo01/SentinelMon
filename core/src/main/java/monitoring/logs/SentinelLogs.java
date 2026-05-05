package monitoring.logs;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SentinelLogs {
    //instanciamos la clase estatica nativa
    private static final Logger LOGGER = Logger.getLogger(SentinelLogs.class.getName());

    //constructor privado, para no instanciar por si acaso excepcion
    private SentinelLogs(){
        throw new UnsupportedOperationException("No se puede instanciar SentinelLogs");
    }

    public static void info(String msg){
        LOGGER.info(msg);
    }

    public static void advertencia(String msg){
        LOGGER.warning(msg);
    }

    public static void error(String msg){
        LOGGER.severe(msg);
    }

    public static void errorConTraza(String mensaje, Throwable excepcion) {
        LOGGER.log(Level.SEVERE, mensaje, excepcion);
    }



}

