package monitoring.services;

public class DiscoInfo {
    private String letra;
    private String modelo;
    private double totalGB;
    private double libreGB;
    private double usadoGB;

    public DiscoInfo(String letra, String modelo, double totalGB, double libreGB, double usadoGB) {
        this.letra = letra;
        this.modelo = modelo;
        this.totalGB = totalGB;
        this.libreGB = libreGB;
        this.usadoGB = usadoGB;
    }

    public String getLetra() {
        return letra;
    }
    public String getModelo() {
        return modelo;
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
                ", modelo='" + modelo + '\'' +
                ", totalGB=" + totalGB +
                ", libreGB=" + libreGB +
                ", usadoGB=" + usadoGB +
                '}';
    }
}
