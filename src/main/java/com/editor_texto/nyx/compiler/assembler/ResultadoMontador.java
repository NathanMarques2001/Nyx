package com.editor_texto.nyx.compiler.assembler;

import java.util.List;

/**
 * Estrutura para armazenar o resultado da execução do Assembler externo.
 */
public class ResultadoMontador {
    private final boolean sucesso;
    private final List<String> stdout;
    private final List<String> stderr;
    private final int codigoRetorno;

    public ResultadoMontador(boolean sucesso, List<String> stdout, List<String> stderr, int codigoRetorno) {
        this.sucesso = sucesso;
        this.stdout = stdout;
        this.stderr = stderr;
        this.codigoRetorno = codigoRetorno;
    }

    public boolean obterSucesso() {
        return sucesso;
    }

    public List<String> obterSaida() {
        return stdout;
    }

    public List<String> obterErro() {
        return stderr;
    }

    public int obterCodigoRetorno() {
        return codigoRetorno;
    }
}
