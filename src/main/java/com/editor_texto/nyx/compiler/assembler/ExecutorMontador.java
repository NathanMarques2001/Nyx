package com.editor_texto.nyx.compiler.assembler;

import java.nio.file.Path;

/**
 * Interface para execução de assemblers.
 * Abstrai a ferramenta específica de montagem (JWASM, NASM, MASM, etc).
 */
public interface ExecutorMontador {

    /**
     * Monta o código Assembly.
     * 
     * @param arquivoAsm Caminho do arquivo .asm fonte
     * @param pastaSaida Diretório para os artefatos de saída
     * @return Resultado da operação contendo sucesso/falha e logs
     */
    ResultadoMontador montar(Path arquivoAsm, Path pastaSaida);
}
