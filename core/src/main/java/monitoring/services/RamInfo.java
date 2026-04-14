package monitoring.services;

public class RamInfo {
    private double usoPorcentaje;
    private double totalMemoryGB;

    public RamInfo(double usoPorcentaje, double totalMemoryGB) {
        this.usoPorcentaje = usoPorcentaje;
        this.totalMemoryGB = totalMemoryGB;
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
                "usoPorcentaje=" + usoPorcentaje +
                ", totalMemoryGB=" + totalMemoryGB +
                '}';
    }
}
