package com.editor_texto.nyx.sistema;

import com.editor_texto.nyx.ui.LayoutPrincipal;
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
        // Configura o tratador de erros global para a Thread JavaFX
        Thread.currentThread().setUncaughtExceptionHandler(new TratadorErrosGlobal());
        // Configura para outras threads que possam ser criadas
        Thread.setDefaultUncaughtExceptionHandler(new TratadorErrosGlobal());

        palco.setTitle("Nyx Editor");
        try {
            java.io.InputStream iconStream = getClass().getResourceAsStream("/icons/icon.png");
            if (iconStream != null) {
                palco.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception ex) {
            System.err.println("Não foi possível carregar o ícone: " + ex.getMessage());
        }

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
            } else {
                layoutPrincipal.salvarPreferencias();
            }
        });

        // Exibe a janela
        palco.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
