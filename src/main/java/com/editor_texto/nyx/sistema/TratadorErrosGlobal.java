package com.editor_texto.nyx.sistema;

import com.editor_texto.nyx.ui.DialogoErro;
import javafx.application.Platform;

/**
 * Captura exceções não tratadas em qualquer thread e exibe um diálogo de erro
 * amigável.
 * Evita que a aplicação morra silenciosamente ou apenas imprima no console.
 */
public class TratadorErrosGlobal implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Log no console para debug (sempre útil)
        System.err.println("CRASH DETECTADO na thread " + t.getName());
        e.printStackTrace();

        // Se o erro foi na thread JavaFX, já estamos no contexto certo.
        // Se foi em outra thread, precisamos usar Platform.runLater.
        if (Platform.isFxApplicationThread()) {
            mostrarErro(e);
        } else {
            Platform.runLater(() -> mostrarErro(e));
        }
    }

    private void mostrarErro(Throwable e) {
        DialogoErro.mostrarErro(
                "Erro Fatal",
                "Ocorreu um erro inesperado na aplicação.",
                "Um erro não tratado causou uma falha. Detalhes técnicos estão disponíveis abaixo.\n" +
                        "Erro: " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                e);
    }
}
