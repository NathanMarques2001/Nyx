package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.geracao.GeradorAssembly;
import com.editor_texto.nyx.compiler.geracao.Otimizador;
import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.TipoErro;
import com.editor_texto.nyx.sistema.ServicoLog;
import java.io.File;
import java.io.IOException;

public class PassoGeracaoCodigo implements PassoPipeline {

    private final String nomeArquivoSaida;

    public PassoGeracaoCodigo(String nomeArquivoSaida) {
        this.nomeArquivoSaida = nomeArquivoSaida;
    }

    public PassoGeracaoCodigo() {
        this.nomeArquivoSaida = "output";
    }

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        try {
            File dirSaida = contexto.getDiretorioSaida().toFile();
            // Pequeno hack: PassoInicializacao já ajustou o dirSaida para 'out', mas o
            // contexto guarda o base.
            // Idealmente o Contexto deveria atualizar o path. Vamos assumir que o
            // GeradorAssembly receba o absoluto correto.
            if (!dirSaida.getName().equalsIgnoreCase("out")) {
                dirSaida = new File(dirSaida, "out");
            }

            GeradorAssembly gerador = new GeradorAssembly(contexto.getTabelaSimbolos(), this.nomeArquivoSaida,
                    dirSaida.getAbsolutePath());
            gerador.gerar();

            File arqGerado = new File(dirSaida, "output.asm");

            // Otimização
            try {
                Otimizador.otimizarArquivo(arqGerado.getAbsolutePath());
                File arqOtimizado = new File(dirSaida, "output_otimizado.asm");
                if (arqOtimizado.exists()) {
                    contexto.setArquivoAssemblyGerado(arqOtimizado.toPath());
                    ServicoLog.info("Código otimizado gerado em: " + arqOtimizado.getName());
                } else {
                    contexto.setArquivoAssemblyGerado(arqGerado.toPath());
                    ServicoLog.info("Código gerado em: " + arqGerado.getName());
                }
            } catch (IOException e) {
                contexto.adicionarAviso("Falha na otimização: " + e.getMessage());
                contexto.setArquivoAssemblyGerado(arqGerado.toPath());
                ServicoLog.aviso("Otimização falhou, usando arquivo não otimizado.");
            }

            return true;
        } catch (Exception e) {
            contexto.adicionarErro(
                    new ErroCompilacao(TipoErro.OUTRO, "Erro na geração de código: " + e.getMessage(), 0, 0));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getNome() {
        return "Geração de Código";
    }
}
