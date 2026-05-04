package monitoring.ui;

import javafx.scene.Parent;

public class ThemeManager {
    public static String currentTheme = "Oscuro";
    public static String currentFont = "Predeterminada";

    public static void applyTheme(Parent root) {
        root.getStylesheets().clear();
        if ("Claro".equals(currentTheme)) {
            root.getStylesheets().add(ThemeManager.class.getResource("/css/style-light.css").toExternalForm());
        } else {
            root.getStylesheets().add(ThemeManager.class.getResource("/css/style.css").toExternalForm());
        }

        if ("Arial".equals(currentFont)) {
            root.setStyle("-fx-font-family: 'Arial';");
        } else if ("OpenDyslexic".equals(currentFont)) {
            /*
             * se usa Comic Sans si OpenDyslexic no esta instalada que e leido por ahi
             * que es buena para la dislexia
             */
            root.setStyle("-fx-font-family: 'OpenDyslexic', 'Comic Sans MS', sans-serif;");
        } else if ("Verdana".equals(currentFont)) {
            root.setStyle("-fx-font-family: 'Verdana';");
        } else {
            root.setStyle(""); // vuelve a Predeterminada
        }
    }
}
