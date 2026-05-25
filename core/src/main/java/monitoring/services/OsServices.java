package monitoring.services;

import java.net.InetAddress;

public class OsServices {

    public String getOsName(){
        return System.getProperty("os.name");
    }

    public String getOsVersion(){
        return System.getProperty("os.version");
    }

    public String getOsArchitecture(){
        return System.getProperty("os.arch");
    }

    public String getOsNameAndVersion(){
        return getOsName() + " " + getOsVersion();
    }
    
    public String getComputerName(){
        // primero intenta la variable de Windows
        String nombre = System.getenv("COMPUTERNAME");
        if (nombre == null || nombre.isBlank()) {
            // fallback portable para Linux/Mac
            try {
                nombre = InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                nombre = "Desconocido";
            }
        }
        return nombre;
    }

    public OsInfo getInfo(){
        return new OsInfo(getOsName(), getOsVersion(), getOsArchitecture(), getComputerName());
    }
}