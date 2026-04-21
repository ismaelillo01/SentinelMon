package monitoring.services;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;

import java.util.List;

public class Ramservices {
    private final GlobalMemory ram;

    public Ramservices() {
        HardwareAbstractionLayer hrdw = new SystemInfo().getHardware();
        this.ram = hrdw.getMemory();
    }

    //devuelve porcentaje uso
    public double getUsagePercentage() {
        long memoriaTotal = ram.getTotal();
        if (memoriaTotal <= 0) {
            return 0;
        }

        long memoriaUsada = memoriaTotal - ram.getAvailable();
        double porcentajeUso = ((double) memoriaUsada / memoriaTotal) * 100;
        return redondear(porcentajeUso);
    }

    //devuelve el total en GB
    public double getTotalMemoryGB() {
        return redondear(bytesAGigas(ram.getTotal()));
    }

    //devuelve modelo de la ram
    public String getModelo() {
        List<PhysicalMemory> memorias = ram.getPhysicalMemory();
        if (memorias == null || memorias.isEmpty()) {
            return "No disponible";
        }
        String fabricante = memorias.get(0).getManufacturer();
        String tipo = memorias.get(0).getMemoryType();
        if (fabricante == null || fabricante.isBlank()) {
            fabricante = "Desconocido";
        }
        if (tipo == null || tipo.isBlank()) {
            tipo = "";
        }
        return fabricante + " " + tipo;
    }

    //devuelve toda la info
    public RamInfo getInfo() {
        return new RamInfo(getModelo(), getUsagePercentage(), getTotalMemoryGB());
    }

    private double bytesAGigas(long bytesTotales) {
        return (double) bytesTotales / (1024 * 1024 * 1024);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
