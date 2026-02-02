package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.sintatico.AnalisadorSintatico;
import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.TipoErro;
import com.editor_texto.nyx.sistema.ServicoLog;

public class PassoSintatico implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        try {
            AnalisadorSintatico sintatico = new AnalisadorSintatico(contexto.getTabelaSimbolos());
            sintatico.analisarPrograma();
            ServicoLog.info("Análise sintática concluída com sucesso.");
            return true;
        } catch (ExcecaoCompilador e) {
            if (e.getErro() != null) {
                contexto.adicionarErro(e.getErro());
            } else {
                contexto.adicionarErro(new ErroCompilacao(TipoErro.SINTATICO, e.getMessage(), 0, 0));
            }
            return false;
        }
    }

    @Override
    public String getNome() {
        return "Análise Sintática";
    }
}
