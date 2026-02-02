package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.assembler.ExecutorMontador;
import com.editor_texto.nyx.compiler.assembler.FabricaMontador;
import com.editor_texto.nyx.compiler.assembler.ResultadoMontador;

/**
 * Passo 2: Montagem (Assembler) (.asm -> .obj / .exe)
 */
public class PassoMontagem implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        // Só executa se o passo anterior (compilação) gerou arquivo ASM.
        // A lógica do pipeline já para se passo anterior falhou, mas checamos por
        // segurança.
        if (contexto.getResultadoCompilacao() == null || !contexto.getResultadoCompilacao().isSucesso()) {
            return false;
        }

        ExecutorMontador montador = FabricaMontador.criarMontador();
        ResultadoMontador resultado = montador.montar(
                contexto.getResultadoCompilacao().getArquivoAssemblyGerado(),
                contexto.getDiretorioSaida());

        contexto.setResultadoMontador(resultado);
        return resultado.obterSucesso();
    }

    @Override
    public String getNome() {
        return "Montagem (ASM -> EXE)";
    }
}
