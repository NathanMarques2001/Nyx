package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.semantico.AnalisadorSemantico;
import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.TipoErro;
import com.editor_texto.nyx.sistema.ServicoLog;

public class PassoSemantico implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        try {
            AnalisadorSemantico semantico = new AnalisadorSemantico(contexto.getTabelaSimbolos());
            semantico.analisar();
            ServicoLog.info("Análise semântica concluída sem erros.");
            return true;
        } catch (ExcecaoCompilador e) {
            if (e.getErro() != null) {
                contexto.adicionarErro(e.getErro());
            } else {
                contexto.adicionarErro(new ErroCompilacao(TipoErro.SEMANTICO, e.getMessage(), 0, 0));
            }
            return false;
        }
    }

    @Override
    public String getNome() {
        return "Análise Semântica";
    }
}
