package monitoring.plugins;

import monitoring.logs.SentinelLogs;
import org.api.SentinelExtension;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

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
    //carpeta donde estan los jars
    private static final Path PLUGINGS_PATH = Paths.get("plugins");
    private static SentinelPluginManager instancia;
    private static PluginManager pf4j;


    //instanciamos una vez
    private SentinelPluginManager(){
        crearDirSiNoExiste();;
        //detecta automaticamente jar e la carpeta
        this.pf4j = new DefaultPluginManager(PLUGINGS_PATH);
    }

    //singeltone
    public static synchronized SentinelPluginManager getInstance(){
        //si no tiene instancia la crea y la devuelve
        if (instancia == null){
            instancia = new SentinelPluginManager();
        }
        //si ya tiene instancia se salta el if y la devuleve
        return instancia;
    }


    //carga y arranca todos los JARs en plugings/.
    //llamar al iniciar la applicacion
    public void cargarTodos(){
        pf4j.loadPlugins();
        pf4j.startPlugins();
        SentinelLogs.info("Plugings cargados" + pf4j.getPlugins().size());
    }

    //parar y descargar de memoria
    //llamar al parar la apliucacion
    public void detenerTodos(){
        pf4j.stopPlugins();
        pf4j.unloadPlugins();
    }

    //devuelve todas las extensiones que implementan SentenelExtension
    public List<SentinelExtension> getExtensionesActivas(){
        return pf4j.getExtensions(SentinelExtension.class);
    }

    //devuelve metadatos de todos los plugings cargados
    public List<PluginWrapper> getPlugingsCargados(){
        return pf4j.getPlugins();
    }

    //instalar
    public boolean instalar(Path origen){
        try {
            Path destino = PLUGINGS_PATH.resolve(origen.getFileName());//une pluging/+nombre
            Files.copy(origen,destino, StandardCopyOption.REPLACE_EXISTING);//copia archivo a destino, si existe sobrescribe, asi actualizamos facil
            SentinelLogs.info("Jar copiado a "+destino);
            String pluginId = pf4j.loadPlugin(destino);//carga plugin devuelve id
            if (pluginId==null){
                SentinelLogs.error("No se a podido cargar el pluging de "+destino);
                //Files.deleteIfExists(destino) !!!!!!!!!!Comprobar esto
                return false;
            }

            PluginState estado = pf4j.startPlugin(pluginId);// iniciamos y recogemos estado
            SentinelLogs.info("Pluging '"+pluginId+"' estado "+estado);
            return PluginState.STARTED.equals(estado); //si inicia devuelve true, si no false


        } catch (IOException e) {
            SentinelLogs.error("Error instalando pluging "+e.getMessage());
            return false;
        }
    }

    //desinstalar
    public boolean desinstalar(String pluginId){
        PluginWrapper metadatos = pf4j.getPlugin(pluginId);
        if(metadatos==null){
            SentinelLogs.error("(desinstalando) pluging no encontradp "+pluginId);
            return false;
        }

        Path path = metadatos.getPluginPath();
        pf4j.stopPlugin(pluginId);//parar
        pf4j.unloadPlugin(pluginId);//descargar de memorai
        try{
            boolean borrado = Files.deleteIfExists(path);
            SentinelLogs.info("Plugin Eliminado " + pluginId + "="+ borrado);
            return borrado;
        }catch (IOException e){
            SentinelLogs.error("(desinstalando) Error borrando plugin "+e.getMessage());
            return false;
        }
    }


    public boolean estaInstalado(String pluginId){
        return pf4j.getPlugin(pluginId) != null;
    }

    //leemos los metadatos del JAR individual, no de pf4j
    //leemos el manifest del jar sin cargarlo como plugIN
    //mostramos los plugings sus descripciones y tal sin y podemos elegir si lo instalamos
    public PluginMetadatos leerMetadatos(Path jarPath){
        try(JarFile jar = new JarFile(jarPath.toFile())){
            Manifest manifest = jar.getManifest();
            if (manifest == null){
                return null;
            }
            Attributes attrb = manifest.getMainAttributes();
            String id = attrb.getValue("Plugin-Id");
            String version = attrb.getValue("Plugin-Version");
            String descripcion = attrb.getValue("Plugin-Description");
            String proveedor= attrb.getValue("Plugin-Provider");
            if(id==null || id.isBlank()){
                return null;
            }
            return new PluginMetadatos(id,version,descripcion,proveedor,jarPath);

        }catch(IOException e){
            SentinelLogs.error("error leyendo metadatos "+e.getMessage());
            return null;
        }
    }

    //leemos metadatos de todos los jar
    public List<PluginMetadatos> escanearPluginsInstalados(){
        List<PluginMetadatos> result = new ArrayList<>();
        try{
            if(!Files.exists(PLUGINGS_PATH)){
                return result;
            }
            Files.list(PLUGINGS_PATH).filter(path -> path.toString().endsWith(".jar"))
                    .forEach(jar -> {
                        PluginMetadatos meta = leerMetadatos(jar);
                        if (meta!=null){
                            result.add(meta);
                        }
                    });
        }catch (IOException e){
            SentinelLogs.error("Error escaneando metadatos de todos los plugins "+e.getMessage());
        }
        return  result;
    }

    private void crearDirSiNoExiste(){
        try{
            if (!Files.exists(PLUGINGS_PATH)){
                Files.createDirectories(PLUGINGS_PATH);
                SentinelLogs.info("Directorio creado en "+PLUGINGS_PATH.toAbsolutePath());
            }
        }catch (IOException e){
            SentinelLogs.error("Error creando carpeta plugins "+e.getMessage());
        }
    }



    //clase contenedora de informacion de metadatos !!!!!SIN CARGAR EL JAR!!!!
    public record PluginMetadatos(
            String id,
            String version,
            String descripcion,
            String proveedor,
            Path jarPath
    ){
        public String nombreTienda(){
            //pasa de "driver-update" a Driver Update
            return normalizar(id.replace("-"," "));
        }

        private String normalizar(String texto){
            if (texto.isBlank() || texto==null){
                return texto;
            }
            StringBuilder sb = new StringBuilder();
            for (String temp : texto.split(" ")){
                if (!temp.isEmpty()){
                    sb.append(Character.toUpperCase(temp.charAt(0)))
                            .append(temp.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return sb.toString().trim();
        }
    }




}
