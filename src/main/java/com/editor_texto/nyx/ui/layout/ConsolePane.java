package com.editor_texto.nyx.ui.layout;

import javafx.scene.control.TextArea;

public class ConsolePane {
    TextArea console;

    public ConsolePane() {
        console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        setConsoleStyle();
    }

    public TextArea getConsole() {
        return console;
    }

    private void setConsoleStyle() {
        console.setStyle("-fx-control-inner-background: #000000;");
        console.setStyle("-fx-text-fill: #ffffff;");
        console.setStyle("-fx-font-family: 'Consolas';");
        console.setStyle("-fx-font-size: 12px;");
    }
}
