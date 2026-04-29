package monitoring.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import monitoring.ui.controllers.DashboardController;


public class MainApp extends Application {
    private DashboardController controller;
    @Override
    public void start(Stage escenario) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
        Parent root = loader.load();
        this.controller = loader.getController();

        Scene scene = new Scene(root, 1000, 700);
        escenario.setOnCloseRequest(e -> controller.shutdown());
        escenario.setTitle("SentinelMon");
        escenario.setScene(scene);
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
