package org.api;
import org.pf4j.ExtensionPoint;
import javafx.scene.Node;
/*
* Notas
******* interfaz usada para sobrescribir en los plugins, todos los plugins deben implementar esta clase;
* */

public interface SentinelExtension extends ExtensionPoint{
    String getName();
    String getDescription();
    void start();
    void stop();
    Node getUiComponent();
}