package monitoring.services;

public class CpuInfo {
    private String nombre;
    private int nucleos;
    private String velocidad;
    private double usoPorcentaje;
    private String temperatura;
    private String cpuFan;

    public CpuInfo(String nombre, int nucleos, String velocidad, double usoPorcentaje, String temperatura, String cpuFan) {
        this.nombre = nombre;
        this.nucleos = nucleos;
        this.velocidad = velocidad;
        this.usoPorcentaje = usoPorcentaje;
        this.temperatura = temperatura;
        this.cpuFan = cpuFan;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNucleos() {
        return nucleos;
    }

    public String getVelocidad() {
        return velocidad;
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
                ", nucleos=" + nucleos +
                ", velocidad='" + velocidad + '\'' +
                ", usoPorcentaje=" + usoPorcentaje +
                ", temperatura='" + temperatura + '\'' +
                ", cpuFan='" + cpuFan + '\'' +
                '}';
    }
}
