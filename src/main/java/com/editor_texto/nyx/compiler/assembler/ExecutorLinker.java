package com.editor_texto.nyx.compiler.assembler;

import java.nio.file.Path;

/**
 * Interface para o processo de Linkagem (Ligação).
 * Transforma arquivos objeto (.obj) em executáveis (.exe).
 */
public interface ExecutorLinker {

    /**
     * Realiza a ligação do arquivo objeto para gerar o executável.
     * 
     * @param arquivoObj Caminho do arquivo .obj de entrada
     * @param pastaSaida Diretório onde o executável será gerado
     * @return Resultado do processo de ligação
     */
    ResultadoLinker ligar(Path arquivoObj, Path pastaSaida);
}
