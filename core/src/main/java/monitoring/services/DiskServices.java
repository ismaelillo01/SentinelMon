package monitoring.services;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // mapear letra de particion a modelo de disco
        Map<String, String> letraAModelo = new HashMap<>();
        List<HWDiskStore> discosHW = si.getHardware().getDiskStores();
        for (HWDiskStore disco : discosHW) {
            String modeloDisco = disco.getModel();
            if (modeloDisco == null || modeloDisco.isBlank()) {
                modeloDisco = "Desconocido";
            }
            for (HWPartition particion : disco.getPartitions()) {
                String mountPoint = particion.getMountPoint();
                if (mountPoint != null && !mountPoint.isBlank()) {
                    letraAModelo.put(mountPoint, modeloDisco);
                }
            }
        }

        List<OSFileStore> particiones = os.getFileSystem().getFileStores();//lista particiones
        List<DiscoInfo> infoDiscos = new ArrayList<>();

        for (OSFileStore particion : particiones) {
            String letra = particion.getMount(); // letra particion
            String modelo = letraAModelo.getOrDefault(letra, "Desconocido");
            long totalBytes = particion.getTotalSpace();
            long libresBytes = particion.getUsableSpace();
            long usadosBytes = totalBytes - libresBytes;

            double totalGB = (double) totalBytes / (1024 * 1024 * 1024);
            double libreGB = (double) libresBytes / (1024 * 1024 * 1024);
            double usadoGB = (double) usadosBytes / (1024 * 1024 * 1024);

            infoDiscos.add(new DiscoInfo(letra, modelo, totalGB, libreGB, usadoGB));
        }

        return infoDiscos;
    }






}
