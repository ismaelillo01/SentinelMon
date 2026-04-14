package monitoring.services;

import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.ArrayList;
import java.util.List;

public class GpuServices {
    private static final String NO_DISPONIBLE = "No disponible";
    private final List<GraphicsCard> gpu;

    public GpuServices() {
        HardwareAbstractionLayer hrdw = new SystemInfo().getHardware();
        this.gpu = hrdw.getGraphicsCards();
    }

    //devuelve lista de nombres
    public List<String> getName() {
        List<String> nombres = new ArrayList<>();
        for (GraphicsCard grafica : gpu) {
            String nombreGrafica = grafica.getName();
            if (nombreGrafica == null || nombreGrafica.isBlank()) {
                nombres.add(NO_DISPONIBLE);
            } else {
                nombres.add(nombreGrafica);
            }
        }
        return nombres;
    }


    //devuelve Vram totoal
    public List<Double> getTotalVram() {
        List<Double> totalVram = new ArrayList<Double>();
        for (GraphicsCard graficaSistema : gpu) {
            totalVram.add(redondear(bytesAGigas(graficaSistema.getVRam())));
        }
        return totalVram;
    }


    //informacion completa de graficas
    public List<GpuInfo> getInfo() {
        List<GpuInfo> infoGraficas = new ArrayList();
        for (GraphicsCard graficaSistema : gpu) {
            String nombreGrafica = graficaSistema.getName();
            if (nombreGrafica == null || nombreGrafica.isBlank()) {
                nombreGrafica = "No disponible";
            }
            infoGraficas.add(new GpuInfo(nombreGrafica, redondear(bytesAGigas(graficaSistema.getVRam()))));
        }
        return infoGraficas;
    }

    private double bytesAGigas(long bytesTotales) {
        return (double) bytesTotales / (1024 * 1024 * 1024);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
