package com.editor_texto.nyx.ui;

import javafx.scene.control.TextArea;

/**
 * Componente responsável por exibir o console de saída/logs do editor.
 */
public class PainelConsole {
    private final TextArea console;

    public PainelConsole() {
        console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        configurarEstiloConsole();
    }

    public TextArea obterConsole() {
        return console;
    }

    private void configurarEstiloConsole() {
        console.setStyle("-fx-control-inner-background: #000000;");
        console.setStyle("-fx-text-fill: #ffffff;");
        console.setStyle("-fx-font-family: 'Consolas';");
        console.setStyle("-fx-font-size: 12px;");
    }
}
