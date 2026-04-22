package monitoring.services;

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
        return System.getenv("COMPUTERNAME");
    }

    public OsInfo getInfo(){
        return new OsInfo(getOsName(), getOsVersion(), getOsArchitecture(), getComputerName());
    }
}