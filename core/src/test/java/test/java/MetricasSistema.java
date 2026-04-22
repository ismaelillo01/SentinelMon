package test.java;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.Sensors;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class MetricasSistema {

    public static void main(String[] args) throws InterruptedException {
        SystemInfo infoSistema = new SystemInfo();
        HardwareAbstractionLayer hardwareSistema = infoSistema.getHardware();

        System.out.println("--- memoria ---");
        GlobalMemory memoriaSistema = hardwareSistema.getMemory();
        long memoriaTotal = memoriaSistema.getTotal();
        long memoriaLibre = memoriaSistema.getAvailable();
        long memoriaUsada = memoriaTotal - memoriaLibre;

        System.out.printf("total: %.2f gb%n", bytesAGigas(memoriaTotal));
        System.out.printf("usada: %.2f gb%n", bytesAGigas(memoriaUsada));
        System.out.printf("libre: %.2f gb%n", bytesAGigas(memoriaLibre));

        System.out.println("\n--- cpu ---");
        CentralProcessor cpuSistema = hardwareSistema.getProcessor();
        System.out.println(cpuSistema.getProcessorIdentifier().getName());
       // long[] ticksAnteriores = cpuSistema.getSystemCpuLoadTicks();
     //   System.out.println("midiendo carga de cpu");
     //  Thread.sleep(1000);
     //   double cargaCpu = cpuSistema.getSystemCpuLoadBetweenTicks(ticksAnteriores) * 100;
      //  System.out.printf("carga actual: %.1f%%%n", cargaCpu);

        System.out.println("\n--- temperaturas ---");
        Sensors sensoresSistema = hardwareSistema.getSensors();
        double temperaturaCpu = sensoresSistema.getCpuTemperature();

        if (temperaturaCpu > 0) {
            System.out.printf("temperatura cpu: %.1f c%n", temperaturaCpu);
        } else {
            System.out.println("temperatura cpu: no disponible");
        }

        System.out.println("\n--- gpu ---");
        List<GraphicsCard> graficasSistema = hardwareSistema.getGraphicsCards();
        for (GraphicsCard graficaSistema : graficasSistema) {
            System.out.printf(
                    "grafica: %s | modelo: %s | tamano: %.2f gb%n",
                    graficaSistema.getName(),
                    graficaSistema.getVendor(),
                    bytesAGigas(graficaSistema.getVRam())
            );
        }

        System.out.println("\n--- discos ---");
        List<HWDiskStore> discosSistema = hardwareSistema.getDiskStores();
        for (HWDiskStore discoSistema : discosSistema) {
            System.out.printf(
                    "disco: %s | modelo: %s | tamano: %.2f gb%n",
                    discoSistema.getName(),
                    discoSistema.getModel(),
                    bytesAGigas(discoSistema.getSize())
            );
        }
        System.out.println("So");
        OperatingSystem os = infoSistema.getOperatingSystem();
        System.out.println(os.toString());

        System.out.println("\n--- interfaces de red ---");
        List<NetworkIF> redesSistema = hardwareSistema.getNetworkIFs();
        for (NetworkIF redSistema : redesSistema) {
            redSistema.updateAttributes();

            if (redSistema.getBytesRecv() > 0 || redSistema.getBytesSent() > 0) {
                System.out.printf(
                        "red: %s (%s) | recibido: %.2f mb | enviado: %.2f mb%n",
                        redSistema.getName(),
                        redSistema.getDisplayName(),
                        bytesAMegas(redSistema.getBytesRecv()),
                        bytesAMegas(redSistema.getBytesSent())
                );
            }
        }
    }

    private static double bytesAGigas(long bytesTotales) {
        return (double) bytesTotales / (1024 * 1024 * 1024);
    }

    private static double bytesAMegas(long bytesTotales) {
        return (double) bytesTotales / (1024 * 1024);
    }
}
