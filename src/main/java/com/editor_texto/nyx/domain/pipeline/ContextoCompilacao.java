package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.ResultadoCompilacao;
import com.editor_texto.nyx.compiler.assembler.ResultadoMontador;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Armazena o estado e os dados compartilhados durante a execução do pipeline.
 */
public class ContextoCompilacao {

    // Entrada
    private final String codigoFonte;
    private final Path diretorioSaida;

    // Estado Compartilhado
    private final TabelaSimbolos tabelaSimbolos;
    private final List<ErroCompilacao> erros;
    private final List<String> avisos;
    private Path arquivoAssemblyGerado;

    // Resultados Finais (Legacy support / wrappers)
    private ResultadoCompilacao resultadoCompilacao;
    private ResultadoMontador resultadoMontador;

    public ContextoCompilacao(String codigoFonte, Path diretorioSaida) {
        this.codigoFonte = codigoFonte;
        this.diretorioSaida = diretorioSaida;
        this.tabelaSimbolos = new TabelaSimbolos();
        this.erros = new ArrayList<>();
        this.avisos = new ArrayList<>();
    }

    public String getCodigoFonte() {
        return codigoFonte;
    }

    public Path getDiretorioSaida() {
        return diretorioSaida;
    }

    public TabelaSimbolos getTabelaSimbolos() {
        return tabelaSimbolos;
    }

    public List<ErroCompilacao> getErros() {
        return erros;
    }

    public void adicionarErro(ErroCompilacao erro) {
        erros.add(erro);
    }

    public void adicionarAviso(String aviso) {
        avisos.add(aviso);
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public Path getArquivoAssemblyGerado() {
        return arquivoAssemblyGerado;
    }

    public void setArquivoAssemblyGerado(Path arquivoAssemblyGerado) {
        this.arquivoAssemblyGerado = arquivoAssemblyGerado;
    }

    // Deprecated / Bridge methods
    public ResultadoCompilacao getResultadoCompilacao() {
        // Reconstrói resultado on-demand se necessário, ou usa o setado
        if (resultadoCompilacao == null) {
            boolean sucesso = erros.isEmpty();
            return new ResultadoCompilacao(sucesso, erros, avisos, arquivoAssemblyGerado);
        }
        return resultadoCompilacao;
    }

    public void setResultadoCompilacao(ResultadoCompilacao resultadoCompilacao) {
        this.resultadoCompilacao = resultadoCompilacao;
    }

    public ResultadoMontador getResultadoMontador() {
        return resultadoMontador;
    }

    public void setResultadoMontador(ResultadoMontador resultadoMontador) {
        this.resultadoMontador = resultadoMontador;
    }
}
