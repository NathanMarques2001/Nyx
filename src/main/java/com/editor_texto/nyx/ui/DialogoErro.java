package com.editor_texto.nyx.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utilitário para exibir diálogos de erro padronizados com suporte a detalhes
 * (Stack Trace).
 */
public class DialogoErro {

    public static void mostrarErro(String titulo, String cabecalho, String mensagem) {
        mostrarErro(titulo, cabecalho, mensagem, null);
    }

    public static void mostrarErro(String titulo, String cabecalho, String mensagem, Throwable excecao) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(mensagem);

        if (excecao != null) {
            // Criar conteúdo expansível para a exceção
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            excecao.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("O rastreamento da pilha de exceção é:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alerta.getDialogPane().setExpandableContent(expContent);
        }

        alerta.showAndWait();
    }
}
