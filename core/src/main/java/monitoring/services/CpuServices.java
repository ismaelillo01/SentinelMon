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
        return "%.1f C".formatted(tempCpu);
    }

    //devuelve ventiladores
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
        return new CpuInfo(getName(), getUsagePercentage(), getTemperatura(), getCpuFan());
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
