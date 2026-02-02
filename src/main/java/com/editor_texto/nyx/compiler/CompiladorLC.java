package com.editor_texto.nyx.compiler;

import com.editor_texto.nyx.compiler.geracao.Otimizador;
import com.editor_texto.nyx.domain.pipeline.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Fachada (Facade) que encapsula todo o processo de compilação da linguagem LC.
 * Fornece uma interface simplificada para integração com o editor de texto Nyx
 * ou uso via CLI.
 */
public class CompiladorLC {

    /**
     * Compila o código fonte fornecido e gera o código Assembly.
     *
     * @param codigoFonte O código fonte completo do programa LC.
     * @param pastaSaida  O caminho para a pasta onde os arquivos de saída devem ser
     *                    gerados.
     * @return Objeto ResultadoCompilacao contendo status, erros e caminho do
     *         arquivo gerado.
     */
    public ResultadoCompilacao compilar(String codigoFonte, java.nio.file.Path pastaSaida) {
        // 0. Preparação de Diretórios
        File dirSaida = pastaSaida.toFile();
        if (!dirSaida.getName().equalsIgnoreCase("out")) {
            dirSaida = new File(dirSaida, "out");
        }
        if (!dirSaida.exists()) {
            dirSaida.mkdirs();
        }

        // Contexto do Pipeline
        ContextoCompilacao contexto = new ContextoCompilacao(codigoFonte, dirSaida.toPath());

        try {
            // 1. Pipeline de Compilação
            // Instancia os passos (pipeline linear)
            PassoPipeline[] passos = {
                    new PassoLexico(),
                    new PassoSintatico(),
                    new PassoSemantico(),
                    new PassoGeracaoCodigo()
            };

            for (PassoPipeline passo : passos) {
                boolean sucesso = passo.executar(contexto);
                if (!sucesso) {
                    // Se falhar algum passo, retorna o resultado parcial (com erros)
                    return construirResultado(contexto, false);
                }
            }

            // 2. Otimização (Pós-processamento)
            Path assemblyGerado = contexto.getArquivoAssemblyGerado();
            if (assemblyGerado != null) {
                try {
                    String caminhoAssembly = assemblyGerado.toAbsolutePath().toString();
                    Otimizador.otimizarArquivo(caminhoAssembly);
                    // Otimizador gera um *_otimizado.asm
                    // Podemos atualizar o path se desejado, mas o original ainda é válido.
                } catch (IOException e) {
                    contexto.adicionarAviso("Falha na otimização: " + e.getMessage());
                }
            }

            return construirResultado(contexto, true);

        } catch (Exception e) {
            contexto.adicionarErro(
                    new ErroCompilacao(TipoErro.OUTRO, "Erro fatal no compilador: " + e.getMessage(), 0, 0));
            e.printStackTrace();
            return construirResultado(contexto, false);
        }
    }

    private ResultadoCompilacao construirResultado(ContextoCompilacao contexto, boolean sucesso) {
        return new ResultadoCompilacao(
                sucesso && contexto.getErros().isEmpty(),
                contexto.getErros(),
                contexto.getAvisos(),
                contexto.getArquivoAssemblyGerado());
    }
}
