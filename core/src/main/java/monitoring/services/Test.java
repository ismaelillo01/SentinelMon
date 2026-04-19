package monitoring.services;

import io.github.pandalxb.jlibrehardwaremonitor.config.ComputerConfig;
import io.github.pandalxb.jlibrehardwaremonitor.manager.LibreHardwareManager;
import io.github.pandalxb.jlibrehardwaremonitor.model.Computer;
import io.github.pandalxb.jlibrehardwaremonitor.model.Hardware;
import io.github.pandalxb.jlibrehardwaremonitor.model.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Test {
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

        CpuServices cpu = new CpuServices();
        Ramservices ram = new Ramservices();
        GpuServices gpu = new GpuServices();
        DiskServices disco = new DiskServices();

        System.out.println(cpu.getInfo());
        System.out.println(ram.getInfo());
        System.out.println(gpu.getInfo());
        System.out.println(disco.getSpace());

        Computer computer = LibreHardwareManager.createInstance(ComputerConfig.getInstance().enableAll()).getComputer();
        List<SensorSnapshot> sensoresTemperatura = new ArrayList<>();
        collectTemperatureSensors(computer, sensoresTemperatura);
        Double temperaturaCpu = findCpuTemperature(sensoresTemperatura);

        if (temperaturaCpu == null) {
            System.out.println("Temperatura CPU: no encontrada");
            dumpTemperatureSensors(sensoresTemperatura);
        } else {
            System.out.printf(Locale.ROOT, "Temperatura CPU: %.1f C%n", temperaturaCpu);
        }
    }

    private static void collectTemperatureSensors(Computer computer, List<SensorSnapshot> sensoresTemperatura) {
        if (computer == null || computer.getHardware() == null) {
            return;
        }

        for (Hardware hardware : computer.getHardware()) {
            collectTemperatureSensors(hardware, sensoresTemperatura);
        }
    }

    private static void collectTemperatureSensors(Hardware hardware, List<SensorSnapshot> sensoresTemperatura) {
        if (hardware == null) {
            return;
        }

        if (hardware.getSensors() != null) {
            for (Sensor sensor : hardware.getSensors()) {
                if (!"Temperature".equalsIgnoreCase(sensor.getSensorType())) {
                    continue;
                }
                sensoresTemperatura.add(new SensorSnapshot(
                        hardware.getHardwareType(),
                        hardware.getName(),
                        sensor.getName(),
                        sensor.getValue()
                ));
            }
        }

        if (hardware.getSubHardware() == null) {
            return;
        }

        for (Hardware subHardware : hardware.getSubHardware()) {
            collectTemperatureSensors(subHardware, sensoresTemperatura);
        }
    }

    private static Double findCpuTemperature(List<SensorSnapshot> sensoresTemperatura) {
        if (sensoresTemperatura.isEmpty()) {
            return null;
        }

        Double primeraTemperaturaCpu = null;
        for (SensorSnapshot sensor : sensoresTemperatura) {
            if (sensor.value() <= 0) {
                continue;
            }

            String hardwareType = safeLower(sensor.hardwareType());
            String hardwareName = safeLower(sensor.hardwareName());
            String sensorName = safeLower(sensor.sensorName());
            boolean contextoCpu = hardwareType.contains("cpu")
                    || hardwareName.contains("cpu")
                    || sensorName.contains("cpu");

            boolean sensorPreferido = sensorName.contains("package")
                    || sensorName.contains("tdie")
                    || sensorName.contains("tctl");

            if (sensorPreferido && (contextoCpu || hardwareType.contains("mainboard")
                    || hardwareType.contains("motherboard"))) {
                return sensor.value();
            }

            if (contextoCpu && primeraTemperaturaCpu == null
                    && !sensorName.contains("max")
                    && !sensorName.contains("average")) {
                primeraTemperaturaCpu = sensor.value();
            }
        }

        return primeraTemperaturaCpu;
    }

    private static void dumpTemperatureSensors(List<SensorSnapshot> sensoresTemperatura) {
        if (sensoresTemperatura.isEmpty()) {
            System.out.println("No se detecto ningun sensor de temperatura.");
            return;
        }

        System.out.println("Sensores de temperatura detectados:");
        for (SensorSnapshot sensor : sensoresTemperatura) {
            System.out.printf(
                    Locale.ROOT,
                    "- hardwareType=%s, hardwareName=%s, sensorName=%s, value=%.1f%n",
                    sensor.hardwareType(),
                    sensor.hardwareName(),
                    sensor.sensorName(),
                    sensor.value()
            );
        }
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record SensorSnapshot(String hardwareType, String hardwareName, String sensorName, double value) {
    }
}
