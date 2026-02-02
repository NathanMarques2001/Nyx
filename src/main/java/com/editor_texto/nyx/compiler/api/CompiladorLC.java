package com.editor_texto.nyx.compiler.api;

import com.editor_texto.nyx.compiler.api.ErroCompilacao;
import com.editor_texto.nyx.compiler.api.ResultadoCompilacao;
import com.editor_texto.nyx.compiler.api.TipoErro;
import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.geracao.GeradorAssembly;
import com.editor_texto.nyx.compiler.geracao.Otimizador;
import com.editor_texto.nyx.compiler.lexico.AnalisadorLexico;
import com.editor_texto.nyx.compiler.semantico.AnalisadorSemantico;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;
import com.editor_texto.nyx.compiler.sintatico.AnalisadorSintatico;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Fachada (Facade) que encapsula todo o processo de compilação da linguagem LC.
 * Fornece uma interface simplificada para integração com o editor de texto Nyx.
 */
public class CompiladorLC {

    /**
     * Compila o código fonte fornecido e gera o código Assembly otimizado.
     *
     * @param codigoFonte O código fonte completo do programa LC.
     * @param pastaSaida  O caminho (referência) para a pasta onde os arquivos de
     *                    saída devem ser gerados.
     *                    Este método cuidará de criar a pasta 'out' dentro do
     *                    diretório, se necessário
     *                    (embora a especificação peça para receber o Path 'out'
     *                    diretamente).
     * @return Objeto ResultadoCompilacao contendo status, erros e caminho do
     *         arquivo gerado.
     */
    public ResultadoCompilacao compilar(String codigoFonte, java.nio.file.Path pastaSaida) {
        List<ErroCompilacao> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        Path arquivoAssembly = null;

        try {
            // 0. Preparação de Diretórios
            File dirSaida = pastaSaida.toFile();

            // Garante que a pasta de saída é 'out'. Se o usuário passou a raiz do projeto,
            // ajusta.
            // Para simplicidade, assumiremos que 'pastaSaida' JÁ É o diretório 'out' ou
            // onde se deseja salvar.
            // Mas a especificação diz [8. Padronizar a pasta de saída como out/].
            // Então vamos garantir que escrevemos em 'out' dentro do path fornecido ou se o
            // path já é 'out'.

            // Ajuste: A UI passará o caminho onde quer salvar. Vamos garantir que vai para
            // uma pasta 'out'
            // ou se o path fornecido já termina em 'out'.
            if (!dirSaida.getName().equalsIgnoreCase("out")) {
                dirSaida = new File(dirSaida, "out");
            }

            if (dirSaida.exists()) {
                // Limpeza básica: deletar arquivos .asm antigos
                File[] arquivos = dirSaida.listFiles((dir, name) -> name.endsWith(".asm"));
                if (arquivos != null) {
                    for (File f : arquivos) {
                        f.delete();
                    }
                }
            } else {
                dirSaida.mkdirs();
            }

            // Nome padrão do arquivo, já que não recebemos o nome do arquivo original na
            // nova assinatura
            // Mas podemos inferir ou usar um padrão. Vamos usar "output" se não for
            // possível deduzir.
            String nomeArquivo = "output";

            // 1. Preparação
            TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
            AnalisadorLexico lexico = new AnalisadorLexico(tabelaSimbolos);

            // 2. Análise Léxica
            String[] linhas = codigoFonte.split("\\r?\\n");
            for (int i = 0; i < linhas.length; i++) {
                lexico.analisar(linhas[i], i + 1);
            }

            // 3. Análise Sintática
            AnalisadorSintatico sintatico = new AnalisadorSintatico(tabelaSimbolos);
            sintatico.analisarPrograma();

            // 4. Análise Semântica
            AnalisadorSemantico semantico = new AnalisadorSemantico(tabelaSimbolos);
            semantico.analisar();

            // 5. Geração de Código
            GeradorAssembly gerador = new GeradorAssembly(tabelaSimbolos, nomeArquivo, dirSaida.getAbsolutePath());
            gerador.gerar();

            // Caminho do arquivo gerado
            File arqGerado = new File(dirSaida, nomeArquivo + ".asm");
            String caminhoAssembly = arqGerado.getAbsolutePath();

            // 6. Otimização
            try {
                Otimizador.otimizarArquivo(caminhoAssembly);
                // Se otimizou, o arquivo final é o otimizado
                File arqOtimizado = new File(dirSaida, nomeArquivo + "_otimizado.asm");
                if (arqOtimizado.exists()) {
                    arquivoAssembly = arqOtimizado.toPath();
                } else {
                    arquivoAssembly = arqGerado.toPath();
                }
            } catch (IOException e) {
                avisos.add("Falha na otimização: " + e.getMessage());
                arquivoAssembly = arqGerado.toPath();
            }

            return new ResultadoCompilacao(true, erros, avisos, arquivoAssembly);

        } catch (ExcecaoCompilador e) {
            if (e.getErro() != null) {
                erros.add(e.getErro());
            } else {
                erros.add(new ErroCompilacao(TipoErro.OUTRO, e.getMessage(), 0, 0));
            }
            return new ResultadoCompilacao(false, erros, avisos, null);
        } catch (Exception e) {
            erros.add(new ErroCompilacao(TipoErro.OUTRO, "Erro interno: " + e.getMessage(), 0, 0));
            e.printStackTrace(); // Log interno apenas
            return new ResultadoCompilacao(false, erros, avisos, null);
        }
    }
}
