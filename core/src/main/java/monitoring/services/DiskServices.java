package monitoring.services;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiskServices {
    private final SystemInfo si;
    private final OperatingSystem os;

    public DiskServices() {
        this.si = new SystemInfo();
        this.os = si.getOperatingSystem();
    }

    //devuelve el nombre de todos los discos
    public List<String> getName(){
        List<HWDiskStore> discos= si.getHardware().getDiskStores();
        List<String> nombres = new ArrayList<>();

        for (HWDiskStore disco : discos) {
            nombres.add(disco.getModel());
        }
        return nombres;// devuelve Lista string de no,bres
    }

    public List<DiscoInfo> getSpace() {
        List<OSFileStore> particiones = os.getFileSystem().getFileStores();//lista particiones
        List<DiscoInfo> infoDiscos = new ArrayList<>();

        for (OSFileStore particion : particiones) {
            String nombre = particion.getMount(); // letra particion
            long totalBytes = particion.getTotalSpace();
            long libresBytes = particion.getUsableSpace();
            long usadosBytes = totalBytes - libresBytes;

            double totalGB = (double) totalBytes / (1024 * 1024 * 1024);
            double libreGB = (double) libresBytes / (1024 * 1024 * 1024);
            double usadoGB = (double) usadosBytes / (1024 * 1024 * 1024);

            infoDiscos.add(new DiscoInfo(nombre, totalGB, libreGB, usadoGB));
        }

        return infoDiscos;
    }






}
