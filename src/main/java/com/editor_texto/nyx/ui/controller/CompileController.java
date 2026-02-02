package com.editor_texto.nyx.ui.controller;

import com.editor_texto.nyx.compiler.api.CompiladorLC;
import com.editor_texto.nyx.ui.layout.ConsolePane;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompileController {

    private final EditorPane editorPane;
    private final ConsolePane consolePane;

    public CompileController(EditorPane editorPane, ConsolePane consolePane) {
        this.editorPane = editorPane;
        this.consolePane = consolePane;
    }

    public void onCompile() {
        System.out.println("DEBUG: Botão Compilar acionado.");

        // Limpa o console antes de nova compilação? Opcional.
        // consolePane.getConsole().clear();
        if (consolePane != null && consolePane.getConsole() != null) {
            consolePane.getConsole().appendText("\n--------------------------------------------------\n");
            consolePane.getConsole().appendText("Iniciando processo de compilação...\n");
        } else {
            System.err.println("ERRO CRÍTICO: ConsolePane não inicializado corretamente.");
        }

        String sourceCode = editorPane.getCurrentCode();
        File currentFile = editorPane.getCurrentFile();

        System.out
                .println("DEBUG: Código fonte obtido. Tamanho: " + (sourceCode != null ? sourceCode.length() : "null"));
        System.out.println("DEBUG: Arquivo atual: " + (currentFile != null ? currentFile.getAbsolutePath() : "null"));

        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            if (consolePane != null && consolePane.getConsole() != null) {
                consolePane.getConsole().appendText("[ERRO] Nenhum código fonte para compilar.\n");
            }
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText("Nenhum código para compilar");
            alert.setContentText("O editor está vazio ou o código não foi carregado.");
            alert.showAndWait();
            return;
        }

        // Determina nome do arquivo
        String fileName = "output";
        if (currentFile != null) {
            String name = currentFile.getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                fileName = name.substring(0, lastDot);
            } else {
                fileName = name;
            }
        }

        // Determina diretório de saída: raiz/out
        String userDir = System.getProperty("user.dir");
        Path outputDir = Paths.get(userDir, "out");

        System.out.println("DEBUG: Diretório de saída alvo: " + outputDir.toAbsolutePath());

        if (consolePane != null && consolePane.getConsole() != null) {
            consolePane.getConsole().appendText("Arquivo alvo: " + fileName + "\n");
            consolePane.getConsole().appendText("Diretório de saída: " + outputDir.toAbsolutePath() + "\n");
        }

        // Executa em thread separada para não travar a UI? O compilador é rápido, mas
        // boa prática.
        // Como o compilador original chamava callbacks na UI ou imprimia no console, e
        // agora a facade retorna string,
        // podemos rodar na JavaFX thread se for rápido, ou background task.
        // Vamos rodar síncrono por enquanto, pois a atualização de UI é simples e a
        // compilação é rápida.

        try {
            CompiladorLC compilador = new CompiladorLC();
            System.out.println("DEBUG: Chamando compilador...");
            String message = compilador.compilar(sourceCode, outputDir.toAbsolutePath().toString(), fileName);
            System.out.println("DEBUG: Compilação retornou: " + message);

            if (consolePane != null && consolePane.getConsole() != null) {
                consolePane.getConsole().appendText("[SUCESSO] " + message + "\n");
            }

            // Feedback visual extra para garantir que o usuário veja algo
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText("Compilação Concluída");
            alert.setContentText(message);
            alert.show(); // show não bloqueante para não travar fluxo se tiver mais coisas

        } catch (Exception e) {
            System.err.println("DEBUG: Exceção durante compilação:");
            e.printStackTrace();

            if (consolePane != null && consolePane.getConsole() != null) {
                consolePane.getConsole().appendText("[FALHA] Erro de compilação:\n");
                consolePane.getConsole().appendText(e.getMessage() + "\n");
            }

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erro de Compilação");
            alert.setHeaderText("Falha ao compilar");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        if (consolePane != null && consolePane.getConsole() != null) {
            consolePane.getConsole().appendText("--------------------------------------------------\n");
        }
    }
}
