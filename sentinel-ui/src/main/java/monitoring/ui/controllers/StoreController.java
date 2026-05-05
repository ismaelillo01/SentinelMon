package monitoring.ui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import monitoring.plugins.SentinelPluginManager;
import monitoring.ui.ThemeManager;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class StoreController implements Initializable {

    @FXML
    private FlowPane appsContainer;

    private final SentinelPluginManager pluginManager = SentinelPluginManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //refresh
    }

    //limpia el pane y regenera las tarjetas leyendo plugins
    private void refrescarTarjetas(){
        appsContainer.getChildren().clear();
        //escanea plugins instalados y lo guardamos
        List<SentinelPluginManager.PluginMetadatos> instalados= pluginManager.escanearPluginsInstalados();

        //si no hay no hay
        if(instalados.isEmpty()){
            Label vacio = new Label("No hay modulos instalados. \nInstala el JAR");
            vacio.setStyle("-fx-text-fill: #8b92b3; -fx-font-size: 14px; -fx-padding: 40;");
            vacio.setWrapText(true);
            appsContainer.getChildren().add(vacio);
            //pa que no siga
            return;
        }
        for(SentinelPluginManager.PluginMetadatos meta : instalados){
            boolean activo = pluginManager.estaInstalado(meta.id());
            appsContainer.getChildren().add(crearTarjeta(meta,activo));
        }
    }


    private VBox crearTarjeta(SentinelPluginManager.PluginMetadatos meta,boolean activo) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setPrefWidth(220);

        //nombre plugin
        Label lblNombre = new Label(meta.nombreTienda());
        lblNombre.getStyleClass().add("card-title");

        //descripcion plugin
        String descTexto = meta.descripcion();
        if (descTexto.isEmpty() || descTexto == null){
            descTexto = "Sin descripcion";
        }
        Label lblDesc = new Label(descTexto);
        lblDesc.getStyleClass().add("metric-label");
        lblDesc.setWrapText(true);

        //version y proovedor
        String infoTexto = "v" + (meta.version() != null ? meta.version() : "?");
        if (meta.proveedor() != null && !meta.proveedor().isBlank()) {
            infoTexto += "  ·  " + meta.proveedor();
        }
        Label lblInfo = new Label(infoTexto);
        lblInfo.setStyle("-fx-text-fill: #4a5585; -fx-font-size: 11px;");

        //estado
        Label lblEstado = new Label(activo ? "Activo" : "Inactivo");
        lblEstado.getStyleClass().add(activo ? "status-label" : "metric-label");//cambio el estilo depende del estado
        Region espcaio = new Region();
        VBox.setVgrow(espcaio, Priority.ALWAYS);
        //botones

        Button btnDesinstalar = new Button("Desinstalar");
        btnDesinstalar.getStyleClass().add("btn-outline");
        btnDesinstalar.setOnAction(event -> desinstalarPlugin(meta.id()));

        HBox botones = new HBox(10);
        botones.getChildren().add(btnDesinstalar);

        card.getChildren().addAll(lblNombre, lblDesc, lblInfo, lblEstado, espcaio, botones);
        return card;
    }

    @FXML
    public void handleInstalarJar(ActionEvent event){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar plugin .JAR");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Plugin JAR","*.jar")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File archivo = chooser.showOpenDialog(stage);
        if (archivo == null){
            return; //cancelar
        }
        Path jarPath= archivo.toPath();
        boolean ok = pluginManager.instalar(jarPath);
        if (ok){
            mostrarMensaje("El plugin se ha instalado","#1dc2bb");
        }else{
            mostrarMensaje("Error al instalar el plugin, comprueba que el JAR sea valido", "#e05555");
        }
        refrescarTarjetas();
    }

    private void desinstalarPlugin(String pluginId){
        boolean ok = pluginManager.desinstalar(pluginId);
        if (ok){
            mostrarMensaje("Plugin '"+ pluginId + "+' desinstalado", "#1dc2bb");
        } else{
            mostrarMensaje("✗ No se pudo desinstalar '" + pluginId + "'.", "#e05555");
        }
        refrescarTarjetas();
    }



     //muestra un mensaje temporal en el FlowPane que dura 3seg
    private void mostrarMensaje(String texto, String color) {
        Label msg = new Label(texto);
        msg.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-padding: 10 0;");

        //añadimos arriba del todo
        appsContainer.getChildren().add(0, msg);

        //dejamos preparad que se elimine
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> appsContainer.getChildren().remove(msg));
        }).start();
    }

    @FXML
    public void handleMenuClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            ThemeManager.applyTheme(root);
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error al volver al menú: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
