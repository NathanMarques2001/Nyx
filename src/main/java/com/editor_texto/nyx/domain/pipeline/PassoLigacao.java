package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.assembler.ExecutorLinker;
import com.editor_texto.nyx.compiler.assembler.FabricaMontador;
import com.editor_texto.nyx.compiler.assembler.ResultadoLinker;
import com.editor_texto.nyx.sistema.ServicoLog;
import java.nio.file.Path;

public class PassoLigacao implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        // Verifica se a montagem foi bem sucedida
        if (contexto.getResultadoMontador() == null || !contexto.getResultadoMontador().obterSucesso()) {
            return false;
        }

        // Assume que o objeto foi gerado com o mesmo nome base do arquivo ASM
        // Ex: output.asm -> output.obj
        // O MontadorWindowsJWASM coloca o OBJ na pasta de saída.
        Path asmGerado = contexto.getResultadoCompilacao().getArquivoAssemblyGerado();
        String nomeBase = asmGerado.getFileName().toString();
        if (nomeBase.contains(".")) {
            nomeBase = nomeBase.substring(0, nomeBase.lastIndexOf('.'));
        }
        Path arquivoObj = contexto.getDiretorioSaida().resolve(nomeBase + ".obj");

        ServicoLog.info("Iniciando Ligação (Linking) de: " + arquivoObj.toString());

        ExecutorLinker linker = FabricaMontador.criarLinker();
        ResultadoLinker resultado = linker.ligar(arquivoObj, contexto.getDiretorioSaida());

        // Loga saídas
        for (String s : resultado.getSaida())
            ServicoLog.info("[LINKER] " + s);
        for (String e : resultado.getErros())
            ServicoLog.erro("[LINKER] " + e);

        if (resultado.isSucesso()) {
            // Atualiza contexto com o executável (poderíamos salvar o path do exe no
            // contexto se tivéssemos campo)
            // Por enquanto, apenas logamos
            ServicoLog.sucesso("Linkagem concluída. Executável gerado.");
        } else {
            ServicoLog.erro("Falha na linkagem.");
        }

        return resultado.isSucesso();
    }

    @Override
    public String getNome() {
        return "Ligação (Linker)";
    }
}
