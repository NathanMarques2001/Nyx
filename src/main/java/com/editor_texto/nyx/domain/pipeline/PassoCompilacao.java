package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.CompiladorLC;
import com.editor_texto.nyx.compiler.ResultadoCompilacao;

/**
 * Passo 1: Compilação do Código Fonte (.lc -> .asm)
 */
public class PassoCompilacao implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        CompiladorLC compilador = new CompiladorLC();
        ResultadoCompilacao resultado = compilador.compilar(
                contexto.getCodigoFonte(),
                contexto.getDiretorioSaida());

        contexto.setResultadoCompilacao(resultado);
        return resultado.isSucesso();
    }

    @Override
    public String getNome() {
        return "Compilação (LC -> ASM)";
    }
}
