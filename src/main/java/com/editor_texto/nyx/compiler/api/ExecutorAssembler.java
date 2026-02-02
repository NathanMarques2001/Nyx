package com.editor_texto.nyx.compiler.api;

import java.nio.file.Path;

/**
 * Interface para execução de assemblers.
 * Abstrai a ferramenta específica de montagem (JWASM, NASM, MASM, etc).
 */
public interface ExecutorAssembler {

    /**
     * Monta o código Assembly.
     * 
     * @param arquivoAsm Caminho do arquivo .asm fonte
     * @param pastaSaida Diretório para os artefatos de saída
     * @return Resultado da operação contendo sucesso/falha e logs
     */
    ResultadoAssembler montar(Path arquivoAsm, Path pastaSaida);
}
