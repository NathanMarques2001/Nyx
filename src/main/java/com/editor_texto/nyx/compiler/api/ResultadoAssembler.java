package com.editor_texto.nyx.compiler.api;

import java.util.List;

/**
 * Estrutura para armazenar o resultado da execução do Assembler externo.
 */
public class ResultadoAssembler {
    private final boolean sucesso;
    private final List<String> stdout;
    private final List<String> stderr;
    private final int codigoRetorno;

    public ResultadoAssembler(boolean sucesso, List<String> stdout, List<String> stderr, int codigoRetorno) {
        this.sucesso = sucesso;
        this.stdout = stdout;
        this.stderr = stderr;
        this.codigoRetorno = codigoRetorno;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public List<String> getStdout() {
        return stdout;
    }

    public List<String> getStderr() {
        return stderr;
    }

    public int getCodigoRetorno() {
        return codigoRetorno;
    }
}
