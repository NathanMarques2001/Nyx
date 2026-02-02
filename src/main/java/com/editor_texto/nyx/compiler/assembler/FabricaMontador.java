package com.editor_texto.nyx.compiler.assembler;

/**
 * Fábrica para instanciar a estratégia de assembler correta.
 * Oculta a lógica de escolha da implementação (por OS, config, etc) do
 * chamador.
 */
public class FabricaMontador {

    public static ExecutorMontador criarMontador() {
        // Por enquanto, apenas Windows/JWASM suportado
        return new MontadorWindowsJWASM();
    }

    public static ExecutorLinker criarLinker() {
        return new LinkerWindows();
    }
}
