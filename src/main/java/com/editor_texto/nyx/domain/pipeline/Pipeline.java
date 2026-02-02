package com.editor_texto.nyx.domain.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador que executa uma sequência de passos (etapas).
 */
public class Pipeline {

    private final List<PassoPipeline> passos = new ArrayList<>();
    private OuvintePipeline ouvinte;

    public void adicionarPasso(PassoPipeline passo) {
        passos.add(passo);
    }

    public void setOuvinte(OuvintePipeline ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void executar(ContextoCompilacao contexto) {
        // Notifica início
        if (ouvinte != null) {
            ouvinte.aoIniciar(passos.size());
        }

        try {
            for (PassoPipeline passo : passos) {
                boolean sucesso = passo.executar(contexto);

                // Notifica fim do passo
                if (ouvinte != null) {
                    ouvinte.aoFinalizarPasso(passo, sucesso);
                }

                if (!sucesso) {
                    // Se falhar e não for tratado internamente para continuar, paramos.
                    // (Alguns passos podem falhar mas permitir continuação? Por enquanto assumimos
                    // que falha para tudo)
                    // Mas wait, se compilação falhar com erros léxicos, não montamos.
                    // Quem define erro fatal é o 'sucesso' do passo.
                    return; // Interrompe pipeline
                }
            }

            // Se chegou aqui, pipeline completo
            if (ouvinte != null) {
                ouvinte.aoFinalizar(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (ouvinte != null) {
                ouvinte.aoFalhar(e.toString());
                ouvinte.aoFinalizar(false);
            }
        }
    }
}
