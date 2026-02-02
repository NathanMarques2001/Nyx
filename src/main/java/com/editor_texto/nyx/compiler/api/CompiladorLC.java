package com.editor_texto.nyx.compiler.api;

import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.geracao.GeradorAssembly;
import com.editor_texto.nyx.compiler.geracao.Otimizador;
import com.editor_texto.nyx.compiler.lexico.AnalisadorLexico;
import com.editor_texto.nyx.compiler.semantico.AnalisadorSemantico;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;
import com.editor_texto.nyx.compiler.sintatico.AnalisadorSintatico;

import java.io.File;
import java.io.IOException;

/**
 * Fachada (Facade) que encapsula todo o processo de compilação da linguagem LC.
 * Fornece uma interface simplificada para integração com o editor de texto Nyx.
 */
public class CompiladorLC {

    /**
     * Compila o código fonte fornecido e gera o código Assembly otimizado.
     *
     * @param codigoFonte    O código fonte completo do programa LC.
     * @param diretorioSaida O diretório onde os arquivos de saída serão salvos.
     * @param nomeArquivo    O nome base do arquivo de saída (sem extensão).
     * @return Uma mensagem de sucesso com o caminho do arquivo gerado.
     * @throws ExcecaoCompilador Se ocorrer algum erro durante as fases de análise.
     * @throws IOException       Se ocorrer erro de I/O durante a geração de
     *                           arquivos.
     */
    public String compilar(String codigoFonte, String diretorioSaida, String nomeArquivo)
            throws ExcecaoCompilador, IOException {
        // 0. Preparação
        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorLexico lexico = new AnalisadorLexico(tabelaSimbolos);

        File dirSaida = new File(diretorioSaida);
        if (!dirSaida.exists()) {
            dirSaida.mkdirs();
        }

        // 1. Análise Léxica
        String[] linhas = codigoFonte.split("\\r?\\n");
        for (int i = 0; i < linhas.length; i++) {
            lexico.analisar(linhas[i], i + 1);
        }

        // 2. Análise Sintática
        AnalisadorSintatico sintatico = new AnalisadorSintatico(tabelaSimbolos);
        sintatico.analisarPrograma();

        // 3. Análise Semântica
        AnalisadorSemantico semantico = new AnalisadorSemantico(tabelaSimbolos);
        semantico.analisar();

        // 4. Geração de Código
        GeradorAssembly gerador = new GeradorAssembly(tabelaSimbolos, nomeArquivo, diretorioSaida);
        gerador.gerar();

        String caminhoAssembly = new File(diretorioSaida, nomeArquivo + ".asm").getAbsolutePath();

        // 5. Otimização
        try {
            Otimizador.otimizarArquivo(caminhoAssembly);
            // Se otimizou, o arquivo final é o otimizado
            caminhoAssembly = new File(diretorioSaida, nomeArquivo + "_otimizado.asm").getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao otimizar o código Assembly: " + e.getMessage());
            // Não interrompe o fluxo, pois o código base foi gerado.
        }

        return "Compilação concluída com sucesso! Arquivo gerado em: " + caminhoAssembly;
    }
}
