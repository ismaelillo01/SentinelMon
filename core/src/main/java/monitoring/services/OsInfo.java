package monitoring.services;

public class OsInfo {
    private String nombre;
    private String version;
    private String arquitectura;
    private String nombreEquipo;
    
    public OsInfo(String nombre, String version, String arquitectura, String nombreEquipo) {
        this.nombre = nombre;
        this.version = version;
        this.arquitectura = arquitectura;
        this.nombreEquipo = nombreEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getVersion() {
        return version;
    }

    public String getArquitectura() {
        return arquitectura;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    @Override
    public String toString() {
        return "OsInfo{" +
                "nombre='" + nombre + '\'' +
                ", version='" + version + '\'' +
                ", arquitectura='" + arquitectura + '\'' +
                ", nombreEquipo='" + nombreEquipo + '\'' +
                '}';
    }
}