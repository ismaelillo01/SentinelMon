package monitoring.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import monitoring.services.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label lblSystemInfo;

    // CPU
    @FXML
    private Arc arcCpu;
    @FXML
    private Label lblCpuName;
    @FXML
    private Label lblCpuLoad;
    @FXML
    private Label lblCpuCores;
    @FXML
    private Label lblCpuSpeed;
    @FXML
    private Label lblCpuTemp;
    @FXML
    private Label lblCpuFan;
    @FXML
    private ProgressBar pbCpuTemp;

    // GPU
    @FXML
    private Arc arcGpu;
    @FXML
    private Label lblGpuName;
    @FXML
    private Label lblGpuLoad;
    @FXML
    private Label lblGpuVram;
    @FXML
    private Label lblGpuTemp;
    @FXML
    private Label lblGpuFan;
    @FXML
    private ProgressBar pbGpuTemp;

    // RAM
    @FXML
    private Arc arcRam;
    @FXML
    private Label lblRamModel;
    @FXML
    private Label lblRamLoad;
    @FXML
    private Label lblRamTotal;
    @FXML
    private Label lblRamFree;
    @FXML
    private ProgressBar pbRamFree;
    @FXML
    private Label lblRamUsedVal;
    @FXML
    private ProgressBar pbRamUsed;

    // discos
    @FXML
    private VBox vboxDisks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // servicios
        CpuServices miCpu = new CpuServices();
        Ramservices miRam = new Ramservices();
        GpuServices miGpu = new GpuServices();
        DiskServices misDiscos = new DiskServices();

        // sistema operativo
        OsServices osServices = new OsServices();
        OsInfo osInfo = osServices.getInfo();
        lblSystemInfo.setText("Equipo: " + osInfo.getNombreEquipo() + "   Sistema: " + osInfo.getNombre()
                + "   Version app: 1.0.0(beta)   Estado: ");

        // cpu update
        CpuInfo datosCpu = miCpu.getInfo();
        lblCpuName.setText(datosCpu.getNombre());
        double usoDeCpu = datosCpu.getUsoPorcentaje();
        lblCpuLoad.setText(Math.round(usoDeCpu) + "%");
        setArcProgress(arcCpu, usoDeCpu);
        lblCpuCores.setText(datosCpu.getNucleos() + " Nucleos");
        lblCpuSpeed.setText(datosCpu.getVelocidad()+ " GHz");
        lblCpuTemp.setText(datosCpu.getTemperatura()+ " Cº");
        lblCpuFan.setText(datosCpu.getCpuFan());
        // barra de prgreso maximo 100º de temperatura
        try {
            System.out.println(datosCpu.getTemperatura());
            pbCpuTemp.setProgress(Double.parseDouble(datosCpu.getTemperatura()) / 100.0);
        } catch (Exception e) {
            pbCpuTemp.setProgress(0);
        }

        // ram update
        RamInfo datosRam = miRam.getInfo();
        lblRamModel.setText(datosRam.getModelo());
        double usoDeRam = datosRam.getUsoPorcentaje();
        lblRamLoad.setText(Math.round(usoDeRam) + "%");
        setArcProgress(arcRam, usoDeRam);
        lblRamTotal.setText(datosRam.getTotalMemoryGB() + " GB");

        double libre = 100 - usoDeRam;
        lblRamFree.setText(Math.round(libre) + "%");
        pbRamFree.setProgress(libre / 100.0);

        lblRamUsedVal.setText(Math.round(usoDeRam) + "%");
        pbRamUsed.setProgress(usoDeRam / 100.0);

        // gpu update
        List<GpuInfo> listaGpus = miGpu.getInfo();
        if (listaGpus != null && !listaGpus.isEmpty()) {
            GpuInfo gpuPrincipal = listaGpus.get(0);
            lblGpuName.setText(gpuPrincipal.getNombre());
            lblGpuVram.setText(gpuPrincipal.getTotalVramGB() + " GB");
            //oshi no puede leer esto
            lblGpuLoad.setText("Por dev");
            setArcProgress(arcGpu, 0);
            lblGpuTemp.setText("Por dev");
            lblGpuFan.setText("Por dev");
            pbGpuTemp.setProgress(0.0);
        } else {
            lblGpuName.setText("No disponible");
            lblGpuVram.setText("No disponible");
            lblGpuLoad.setText("0%");
            setArcProgress(arcGpu, 0);
            lblGpuTemp.setText("No disponible");
            lblGpuFan.setText("No disponible");
            pbGpuTemp.setProgress(0.0);
        }

        // discos update
        List<DiscoInfo> listadoDiscos = misDiscos.getSpace();
        for (DiscoInfo discoActual : listadoDiscos) {
            if (discoActual.getLetra() != null && !discoActual.getLetra().isEmpty()) {
                double porcentajeUsado = discoActual.getUsadoGB() / discoActual.getTotalGB();

                HBox cabecera = new HBox();
                Label lblLetra = new Label(discoActual.getLetra());
                lblLetra.getStyleClass().add("metric-value-small");
                Label lblModelo = new Label("  " + discoActual.getModelo());
                lblModelo.getStyleClass().add("metric-label");
                Region espacio = new Region();
                HBox.setHgrow(espacio, Priority.ALWAYS);
                double totalRedondeado = Math.round(discoActual.getTotalGB() * 10.0) / 10.0;
                Label lblTotal = new Label(totalRedondeado + " GB");
                lblTotal.getStyleClass().addAll("metric-label", "pad-right-sm");
                Label lblPorcentaje = new Label(Math.round(porcentajeUsado * 100) + "%");
                lblPorcentaje.getStyleClass().add("metric-value-small");
                cabecera.getChildren().addAll(lblLetra, lblModelo, espacio, lblTotal, lblPorcentaje);

                ProgressBar barraDisco = new ProgressBar(porcentajeUsado);
                barraDisco.setPrefWidth(2000);
                barraDisco.getStyleClass().add("progress-bar-cyan");

                VBox cajita = new VBox(5, cabecera, barraDisco);
                cajita.getStyleClass().add("disk-item");
                vboxDisks.getChildren().add(cajita);
            }
        }
    }

    private void setArcProgress(Arc arc, double percentage) {
        double length = (percentage / 100.0) * -360.0;
        arc.setLength(length);
    }
}
