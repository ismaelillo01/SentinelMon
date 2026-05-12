package monitoring.plugins;

import monitoring.bd.PluginDAO;
import monitoring.logs.SentinelLogs;
import org.api.SentinelExtension;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class SentinelPluginManager {
    // carpeta donde estan los jars
    private static final Path PLUGINGS_PATH = Paths.get("plugins");
    private static SentinelPluginManager instancia;
    private static PluginManager pf4j;

    // instanciamos una vez
    private SentinelPluginManager() {
        crearDirSiNoExiste();
        ;
        // detecta automaticamente jar e la carpeta
        this.pf4j = new DefaultPluginManager(PLUGINGS_PATH);
    }

    // singeltone
    public static synchronized SentinelPluginManager getInstance() {
        // si no tiene instancia la crea y la devuelve
        if (instancia == null) {
            instancia = new SentinelPluginManager();
        }
        // si ya tiene instancia se salta el if y la devuleve
        return instancia;
    }

    // carga y arranca todos los JARs en plugings/.
    // llamar al iniciar la applicacion
    public void cargarTodos() {
        pf4j.loadPlugins();
        pf4j.startPlugins();

        // cargamos los plugins guardados en la BD que pf4j no haya detectado
        List<PluginDAO.PluginBD> pluginsBD = PluginDAO.listarTodos();
        for (PluginDAO.PluginBD pluginBD : pluginsBD) {
            // si ya esta cargado no hacemos nada
            if (pf4j.getPlugin(pluginBD.id()) != null) {
                continue;
            }
            // si no esta cargado, lo cargamos manualmente con la ruta guardada en BD
            Path jarPath = Paths.get(pluginBD.jarPath());
            if (Files.exists(jarPath)) {
                String pluginId = pf4j.loadPlugin(jarPath);
                if (pluginId != null) {
                    pf4j.startPlugin(pluginId);
                    SentinelLogs.info("Plugin cargado desde BD: " + pluginId);
                }
            } else {
                SentinelLogs.advertencia("JAR no encontrado para plugin '" + pluginBD.id() + "': " + jarPath);
            }
        }
        SentinelLogs.info("Plugings cargados: " + pf4j.getPlugins().size());
    }

    // parar y descargar de memoria
    // llamar al parar la apliucacion
    public void detenerTodos() {
        pf4j.stopPlugins();
        pf4j.unloadPlugins();
    }

    // devuelve todas las extensiones que implementan SentenelExtension
    public List<SentinelExtension> getExtensionesActivas() {
        return pf4j.getExtensions(SentinelExtension.class);
    }

    // devuelve metadatos de todos los plugings cargados
    public List<PluginWrapper> getPlugingsCargados() {
        return pf4j.getPlugins();
    }

    // instalar
    public boolean instalar(Path origen) {
        try {
            PluginMetadatos meta = leerMetadatos(origen);
            if (meta == null || meta.id() == null || meta.id().isBlank()) {
                SentinelLogs.error("El JAR no tiene Plugin-Id en el Manifest: " + origen);
                return false;
            }

            // Crear subcarpeta plugins/<plugin-id>/
            Path subcarpeta = PLUGINGS_PATH.resolve(meta.id());
            Files.createDirectories(subcarpeta);

            Path destino = subcarpeta.resolve(origen.getFileName());
            Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
            SentinelLogs.info("Jar copiado a " + destino);

            String pluginId = pf4j.loadPlugin(destino); // carga directamente el JAR
            if (pluginId == null) {
                SentinelLogs.error("No se ha podido cargar el pluging de " + subcarpeta);
                return false;
            }

            PluginState estado = pf4j.startPlugin(pluginId);
            SentinelLogs.info("Pluging '" + pluginId + "' estado " + estado);

            // si se ha iniciado correctamente, lo guardamos en la BD
            if (PluginState.STARTED.equals(estado)) {
                String desc = meta.descripcion() != null ? meta.descripcion() : "";
                String prov = meta.proveedor() != null ? meta.proveedor() : "";
                String ver = meta.version() != null ? meta.version() : "";
                PluginDAO.guardar(meta.id(), ver, desc, prov, destino.toString());
            }

            return PluginState.STARTED.equals(estado);

        } catch (IOException e) {
            SentinelLogs.error("Error instalando pluging " + e.getMessage());
            return false;
        }
    }

    // desinstalar
    public boolean desinstalar(String pluginId) {
        PluginWrapper metadatos = pf4j.getPlugin(pluginId);
        if (metadatos == null) {
            SentinelLogs.error("(desinstalando) pluging no encontradp " + pluginId);
            return false;
        }

        Path pluginDir = metadatos.getPluginPath();
        pf4j.stopPlugin(pluginId);// parar
        pf4j.unloadPlugin(pluginId);// descargar de memorai
        try {
            // Borramos la subcarpeta y todo su contenido
            Path carpetaPlugin = pluginDir.getParent();
            if (carpetaPlugin == null) {
                carpetaPlugin = pluginDir;
            }
            if (Files.exists(carpetaPlugin)) {
                borrarCarpetaRecursivo(carpetaPlugin.toFile());
            }

            // eliminamos el plugin de la BD
            PluginDAO.eliminar(pluginId);

            SentinelLogs.info("Plugin eliminado: " + pluginId);
            return true;
        } catch (Exception e) {
            SentinelLogs.error("(desinstalando) Error borrando plugin: " + e.getMessage());
            return false;
        }
    }

    // borra una carpeta y todo lo que haya dentro de forma recursiva
    private void borrarCarpetaRecursivo(File carpeta) {
        if (carpeta == null || !carpeta.exists()) {
            return;
        }
        File[] contenido = carpeta.listFiles();
        if (contenido != null) {
            for (File archivo : contenido) {
                if (archivo.isDirectory()) {
                    borrarCarpetaRecursivo(archivo);
                } else {
                    archivo.delete();
                }
            }
        }
        // al final borramos la carpeta ya vacia
        carpeta.delete();
    }

    public boolean estaInstalado(String pluginId) {
        return pf4j.getPlugin(pluginId) != null;
    }

    // leemos los metadatos del JAR individual, no de pf4j
    // leemos el manifest del jar sin cargarlo como plugIN
    // mostramos los plugings sus descripciones y tal sin y podemos elegir si lo
    // instalamos
    public PluginMetadatos leerMetadatos(Path jarPath) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return null;
            }
            Attributes attrb = manifest.getMainAttributes();
            String id = attrb.getValue("Plugin-Id");
            String version = attrb.getValue("Plugin-Version");
            String descripcion = attrb.getValue("Plugin-Description");
            String proveedor = attrb.getValue("Plugin-Provider");
            if (id == null || id.isBlank()) {
                return null;
            }
            return new PluginMetadatos(id, version, descripcion, proveedor, jarPath);

        } catch (IOException e) {
            SentinelLogs.error("error leyendo metadatos " + e.getMessage());
            return null;
        }
    }

    // leemos metadatos de todos los jar
    // recorre las subcarpetas de plugins/ buscando JARs
    public List<PluginMetadatos> escanearPluginsInstalados() {
        List<PluginMetadatos> result = new ArrayList<>();
        try {
            if (!Files.exists(PLUGINGS_PATH)) {
                return result;
            }
            // estructura plugins/<plugin-id>/<plugin.jar>
            // itero subcarpetas y cogemos el primer JAR de cada una
            File[] subcarpetas = PLUGINGS_PATH.toFile().listFiles();
            if (subcarpetas == null) {
                return result;
            }
            for (File subDir : subcarpetas) {
                if (!subDir.isDirectory()) {
                    continue;
                }
                File[] archivos = subDir.listFiles();
                if (archivos == null) {
                    continue;
                }
                // buscamos el primer .jar en la subcarpeta
                for (File archivo : archivos) {
                    if (archivo.getName().endsWith(".jar")) {
                        PluginMetadatos meta = leerMetadatos(archivo.toPath());
                        if (meta != null) {
                            result.add(meta);
                        }
                        break; // solo cogemos el primer jar
                    }
                }
            }
        } catch (Exception e) {
            SentinelLogs.error("Error escaneando metadatos de todos los plugins: " + e.getMessage());
        }
        return result;
    }

    // devuelve los plugins guardados en la BD
    // sirve para que al abrir la tienda aparezcan los instalados
    public List<PluginDAO.PluginBD> getPluginsGuardados() {
        return PluginDAO.listarTodos();
    }

    private void crearDirSiNoExiste() {
        try {
            if (!Files.exists(PLUGINGS_PATH)) {
                Files.createDirectories(PLUGINGS_PATH);
                SentinelLogs.info("Directorio creado en " + PLUGINGS_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            SentinelLogs.error("Error creando carpeta plugins " + e.getMessage());
        }
    }

    // clase contenedora de informacion de metadatos !!!!!SIN CARGAR EL JAR!!!!
    public record PluginMetadatos(
            String id,
            String version,
            String descripcion,
            String proveedor,
            Path jarPath) {
        public String nombreTienda() {
            // pasa de "driver-update" a Driver Update
            return normalizar(id.replace("-", " "));
        }

        private String normalizar(String texto) {
            if (texto.isBlank() || texto == null) {
                return texto;
            }
            StringBuilder sb = new StringBuilder();
            for (String temp : texto.split(" ")) {
                if (!temp.isEmpty()) {
                    sb.append(Character.toUpperCase(temp.charAt(0)))
                            .append(temp.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return sb.toString().trim();
        }
    }

}
