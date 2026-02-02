package com.editor_texto.nyx.domain.pipeline;

/**
 * Interface para ouvir eventos do ciclo de vida do pipeline.
 */
public interface OuvintePipeline {
    // Ciclo geral
    default void aoIniciar(int totalPassos) {
    }

    default void aoFinalizar(boolean sucesso) {
    }

    // Ciclo por passo
    default void aoIniciarPasso(PassoPipeline passo) {
    }

    void aoFinalizarPasso(PassoPipeline passo, boolean sucesso);

    // Falhas
    void aoFalhar(String mensagemErro);
}
