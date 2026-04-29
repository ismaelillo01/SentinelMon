package monitoring.ui.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import monitoring.services.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    // servicios
    private CpuServices miCpu;
    private Ramservices miRam;
    private GpuServices miGpu;
    private DiskServices misDiscos;
    private ExecutorService executor;
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        miCpu = new CpuServices();
        miRam = new Ramservices();
        miGpu = new GpuServices();
        misDiscos = new DiskServices();

        ThreadFactory fHilo = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread hilo = new Thread(r, "sentinel-monitor");
                hilo.setDaemon(true);
                return hilo;
            }
        };
        executor = Executors.newSingleThreadExecutor(fHilo);

        cargarDatosEstaticos();

        lanzarLectura();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> lanzarLectura()));
        timeline.setCycleCount(timeline.INDEFINITE);
        timeline.play();
    }

    private void cargarDatosEstaticos(){
        // sistema operativo
        OsServices osServices = new OsServices();
        OsInfo osInfo = osServices.getInfo();
        lblSystemInfo.setText("Equipo: " + osInfo.getNombreEquipo() + "   Sistema: " + osInfo.getNombre()
                + "   Version app: 1.0.0(beta)   Estado: ");

        // cpu update
        CpuInfo datosCpu = miCpu.getInfo();
        lblCpuName.setText(datosCpu.getNombre());
        lblCpuCores.setText(datosCpu.getNucleos() + " Nucleos");
        lblCpuSpeed.setText(datosCpu.getVelocidad());

        // ram update
        RamInfo datosRam = miRam.getInfo();
        lblRamModel.setText(datosRam.getModelo());
        lblRamTotal.setText(datosRam.getTotalMemoryGB() + " GB");

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
    }

    private void lanzarLectura(){
        Task<DatosSnapshot> tarea = new Task<>(){
            @Override
            protected DatosSnapshot call(){
                double cpuUso = miCpu.getUsagePercentage();
                String cpuTemp = miCpu.getTemperatura();
                String cpuFan = miCpu.getCpuFan();
                double ramUso = miRam.getUsagePercentage();
                List<DiscoInfo> discos = misDiscos.getSpace();
                return new DatosSnapshot(cpuUso,cpuTemp,cpuFan,ramUso,discos);
            }
        };
        tarea.setOnSucceeded(event -> {
            DatosSnapshot datos = tarea.getValue();
            actualizarUi(datos);
        });
        tarea.setOnFailed(event -> {
            Throwable ex = tarea.getException();
            System.err.println("Error leyendo metricas: " + ex.getMessage());
        });
        executor.submit(tarea);
    }
    private void actualizarUi(DatosSnapshot snap){
        //cpu
        lblCpuLoad.setText(Math.round(snap.cpuUso)+ "%");
        setArcProgress(arcCpu, snap.cpuUso);
        lblCpuTemp.setText(snap.cpuTemp + " Cº");
        lblCpuFan.setText(snap.cpuFan);
        try{
            pbCpuTemp.setProgress(Double.parseDouble(snap.cpuTemp) / 100.0);
        }catch (Exception e){
            pbCpuTemp.setProgress(0);
        }
        //ram
        double ramUso = snap.ramUso;
        lblRamLoad.setText(Math.round(ramUso)+ "%");
        setArcProgress(arcRam, ramUso);
        double libre = 100 - ramUso;
        lblRamFree.setText(Math.round(libre) + "%");
        pbRamFree.setProgress(libre / 100.0);
        lblRamUsedVal.setText(Math.round(ramUso) + "%");
        pbRamUsed.setProgress(ramUso / 100.0);

        //disco
        actualizarDiscos(snap.discos);

    }






    private void actualizarDiscos(List<DiscoInfo> discos){
        // discos update
        vboxDisks.getChildren().clear();
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
    public void shutdown() {
        if (timeline != null) timeline.stop();
        if (executor != null) executor.shutdownNow();
    }

    private record DatosSnapshot(
            double cpuUso,
            String cpuTemp,
            String cpuFan,
            double ramUso,
            List<DiscoInfo> discos
    ) {}
}
