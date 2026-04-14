package monitoring.services;

public class DiscoInfo {
    private String letra;
    private double totalGB;
    private double libreGB;
    private double usadoGB;

    public DiscoInfo(String letra, double totalGB, double libreGB, double usadoGB) {
        this.letra = letra;
        this.totalGB = totalGB;
        this.libreGB = libreGB;
        this.usadoGB = usadoGB;
    }

    public String getLetra() {
        return letra;
    }
    public double getTotalGB() {
        return totalGB;
    }
    public double getLibreGB() {
        return libreGB;
    }
    public double getUsadoGB() {
        return usadoGB;
    }

    @Override
    public String toString() {
        return "DiscoInfo{" +
                "letra='" + letra + '\'' +
                ", totalGB=" + totalGB +
                ", libreGB=" + libreGB +
                ", usadoGB=" + usadoGB +
                '}';
    }
}
