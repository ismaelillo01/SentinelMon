package monitoring.services;

public class RamInfo {
    private String modelo;
    private double usoPorcentaje;
    private double totalMemoryGB;

    public RamInfo(String modelo, double usoPorcentaje, double totalMemoryGB) {
        this.modelo = modelo;
        this.usoPorcentaje = usoPorcentaje;
        this.totalMemoryGB = totalMemoryGB;
    }

    public String getModelo() {
        return modelo;
    }

    public double getUsoPorcentaje() {
        return usoPorcentaje;
    }

    public double getTotalMemoryGB() {
        return totalMemoryGB;
    }

    @Override
    public String toString() {
        return "RamInfo{" +
                "modelo='" + modelo + '\'' +
                ", usoPorcentaje=" + usoPorcentaje +
                ", totalMemoryGB=" + totalMemoryGB +
                '}';
    }
}
