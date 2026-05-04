package monitoring.ui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import monitoring.ui.ThemeManager;

import java.net.URL;
import java.util.ResourceBundle;

public class StoreController implements Initializable {

    @FXML
    private FlowPane appsContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // la interfaz está lista para que en el futuro se lean y carguen los JARs aquí.
    }

    private VBox createAppCard(String name, String description, boolean isInstalled) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setPrefHeight(200);

        Label lblName = new Label(name);
        lblName.getStyleClass().add("card-title");

        Label lblDesc = new Label(description);
        lblDesc.getStyleClass().add("metric-label");
        lblDesc.setWrapText(true);

        Label lblStatus = new Label(isInstalled ? "Instalado" : "No instalado");
        lblStatus.getStyleClass().add(isInstalled ? "status-label" : "metric-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        Button btnAction = new Button(isInstalled ? "Desinstalar" : "Instalar");
        // usar btn-outline si no esta instalado, menu-btn si esta instalado para
        // diferenciar visualmente
        btnAction.getStyleClass().add("btn-outline");
        btnAction.setOnAction(e -> {
            System.out.println((isInstalled ? "Desinstalando: " : "Instalando: ") + name);
        });
        buttonBox.getChildren().add(btnAction);

        card.getChildren().addAll(lblName, lblDesc, lblStatus, spacer, buttonBox);
        return card;
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
