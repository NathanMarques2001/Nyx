package com.editor_texto.nyx.compiler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadoCompilacao {
    private final boolean sucesso;
    private final List<ErroCompilacao> erros;
    private final List<String> avisos;
    private final Path arquivoAssemblyGerado;

    public ResultadoCompilacao(boolean sucesso, List<ErroCompilacao> erros, List<String> avisos,
            Path arquivoAssemblyGerado) {
        this.sucesso = sucesso;
        this.erros = erros != null ? new ArrayList<>(erros) : Collections.emptyList();
        this.avisos = avisos != null ? new ArrayList<>(avisos) : Collections.emptyList();
        this.arquivoAssemblyGerado = arquivoAssemblyGerado;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public List<ErroCompilacao> getErros() {
        return Collections.unmodifiableList(erros);
    }

    public List<String> getAvisos() {
        return Collections.unmodifiableList(avisos);
    }

    public Path getArquivoAssemblyGerado() {
        return arquivoAssemblyGerado;
    }
}
