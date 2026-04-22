package monitoring.services;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;

import java.util.Locale;

public class CpuServices {
    private static final String NO_DISPONIBLE = "No disponible";
    private final CentralProcessor cpu;
    private final Sensors sens;

    public CpuServices() {
        HardwareAbstractionLayer hrdw = new SystemInfo().getHardware();
        this.cpu = hrdw.getProcessor();
        this.sens = hrdw.getSensors();
    }

    //devuelve nombre cps
    public String getName() {
        String nombreCpu = cpu.getProcessorIdentifier().getName();
        if (nombreCpu==null||nombreCpu.isBlank()){
            nombreCpu=NO_DISPONIBLE;
        }
        return nombreCpu;
    }

    //devuelve numero de nucleos fisicos
    public int getNucleos() {
        return cpu.getPhysicalProcessorCount();
    }

    //devuelve velocidad maxima en GHz
    public String getVelocidad() {
        long freqHz = cpu.getMaxFreq();
        if (freqHz <= 0) {
            return NO_DISPONIBLE;
        }
        double freqGHz = freqHz / 1_000_000_000.0;
        return "%.2f GHz".formatted(freqGHz);
    }

    //devuelve porcentaje uso
    public double getUsagePercentage() {
        return redondear(cpu.getSystemCpuLoad(1000) * 100);
    }

    //devuelve temperatura con formato
    public String getTemperatura() {
        double tempCpu = sens.getCpuTemperature();
        if (tempCpu <= 0) {
            return NO_DISPONIBLE;
        }
        //me toca decirle que estamos en estados unidos para que no me ponga una coma en el decimal, y evito parsearlo luego en le controller
        return String.format(Locale.US, "%.1f", tempCpu);
    }

    //devuelve ventiladores
    /*
    recordar cuando funcione, crear clase propia ventiladores, y enviar a las infos
    */
    public String getCpuFan() {
        int[] velocidadesVentilador = sens.getFanSpeeds();
        for (int velocidad : velocidadesVentilador) {
            if (velocidad > 0) {
                return velocidad + " RPM";
            }
        }
        return NO_DISPONIBLE;
    }

    //info completa de cpu
    public CpuInfo getInfo() {
        return new CpuInfo(getName(), getNucleos(), getVelocidad(), getUsagePercentage(), getTemperatura(), getCpuFan());
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
