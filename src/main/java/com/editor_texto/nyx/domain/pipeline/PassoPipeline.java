package com.editor_texto.nyx.domain.pipeline;

/**
 * Representa uma etapa única do pipeline de execução.
 */
public interface PassoPipeline {

    /**
     * Executa a lógica desta etapa.
     * 
     * @param contexto O contexto compartilhado.
     * @return true se a etapa foi bem sucedida e o pipeline deve continuar, false
     *         caso contrário.
     * @throws Exception Se ocorrer um erro fatal.
     */
    boolean executar(ContextoCompilacao contexto) throws Exception;

    /**
     * @return O nome legível desta etapa (ex: "Compilação").
     */
    String getNome();
}
