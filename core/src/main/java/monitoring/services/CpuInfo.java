package monitoring.services;

public class CpuInfo {
    private String nombre;
    private double usoPorcentaje;
    private String temperatura;
    private String cpuFan;

    public CpuInfo(String nombre, double usoPorcentaje, String temperatura, String cpuFan) {
        this.nombre = nombre;
        this.usoPorcentaje = usoPorcentaje;
        this.temperatura = temperatura;
        this.cpuFan = cpuFan;
    }

    public String getNombre() {
        return nombre;
    }

    public double getUsoPorcentaje() {
        return usoPorcentaje;
    }

    public String getTemperatura() {
        return temperatura;
    }

    public String getCpuFan() {
        return cpuFan;
    }

    @Override
    public String toString() {
        return "CpuInfo{" +
                "nombre='" + nombre + '\'' +
                ", usoPorcentaje=" + usoPorcentaje +
                ", temperatura='" + temperatura + '\'' +
                ", cpuFan='" + cpuFan + '\'' +
                '}';
    }
}
