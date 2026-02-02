package com.editor_texto.nyx.sistema;

import com.editor_texto.nyx.editor.LayoutPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principal da aplicação JavaFX.
 * Configura o palco (Stage) e a cena inicial.
 */
public class AplicacaoPrincipal extends Application {

    @Override
    public void start(Stage palco) {
        palco.setTitle("Nyx Editor");

        // Cria a cena e define o layout
        LayoutPrincipal layoutPrincipal = new LayoutPrincipal();
        Scene cena = new Scene(layoutPrincipal.obterPainelPrincipal(), 800, 600); // Tamanho padrão sugerido
        palco.setScene(cena);

        // Aplica o tema após a cena estar configurada
        layoutPrincipal.aplicarTemaAtual();

        // Lidar com requisição de fechamento da janela
        palco.setOnCloseRequest(e -> {
            if (!layoutPrincipal.obterPainelEditor().confirmarFechamento()) {
                e.consume(); // Cancela o fechamento se o usuário cancelar
            }
        });

        // Exibe a janela
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
