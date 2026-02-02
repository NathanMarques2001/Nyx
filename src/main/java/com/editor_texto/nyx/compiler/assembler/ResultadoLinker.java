package com.editor_texto.nyx.compiler.assembler;

import java.util.List;

public class ResultadoLinker {
    private final boolean sucesso;
    private final List<String> saida;
    private final List<String> erros;
    private final int codigoSaida;

    public ResultadoLinker(boolean sucesso, List<String> saida, List<String> erros, int codigoSaida) {
        this.sucesso = sucesso;
        this.saida = saida;
        this.erros = erros;
        this.codigoSaida = codigoSaida;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public List<String> getSaida() {
        return saida;
    }

    public List<String> getErros() {
        return erros;
    }

    public int getCodigoSaida() {
        return codigoSaida;
    }
}
