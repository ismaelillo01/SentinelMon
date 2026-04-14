package monitoring.services;

public class GpuInfo {
    private String nombre;
    private double totalVramGB;

    public GpuInfo(String nombre, double totalVramGB) {
        this.nombre = nombre;
        this.totalVramGB = totalVramGB;
    }

    public String getNombre() {
        return nombre;
    }

    public double getTotalVramGB() {
        return totalVramGB;
    }

    @Override
    public String toString() {
        return "GpuInfo{" +
                "nombre='" + nombre + '\'' +
                ", totalVramGB=" + totalVramGB +
                '}';
    }
}
