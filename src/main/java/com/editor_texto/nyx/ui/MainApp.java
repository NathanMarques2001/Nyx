package com.editor_texto.nyx.ui;

import com.editor_texto.nyx.ui.layout.MainLayout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Nyx");

        // Cria a cena e define o layout
        // Cria a cena e define o layout
        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout.getBorderPane());
        stage.setScene(scene);

        // Apply theme after scene is set
        mainLayout.applyCurrentTheme();

        // Handle window close request
        stage.setOnCloseRequest(e -> {
            if (!mainLayout.getEditorPane().confirmClose()) {
                e.consume(); // Cancel close if user clicked Cancel
            }
        });

        // Exibe a janela
        stage.show();
    }

    public static void main(String[] args) {
        // Inicializa o JavaFX
        launch(args);
    }
}
