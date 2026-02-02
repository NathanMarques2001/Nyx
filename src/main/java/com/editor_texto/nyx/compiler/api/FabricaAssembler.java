package com.editor_texto.nyx.compiler.api;

/**
 * Fábrica para instanciar a estratégia de assembler correta.
 * Oculta a lógica de escolha da implementação (por OS, config, etc) do
 * chamador.
 */
public class FabricaAssembler {

    public static ExecutorAssembler criarAssembler() {
        // No futuro, aqui pode haver um switch/if para detectar OS ou ler config
        // e retornar AssemblerLinux, AssemblerMac, etc.
        // Por enquanto, nosso requisito é apenas Windows com JWASM.
        // A validação de OS já está encapsulada dentro do AssemblerWindowsJWASM,
        // mas também poderíamos retornar null ou um "AssemblerNaoSuportado" aqui.
        // Por simplicidade e robustez, retornamos o padrão que faz a validação ao
        // rodar.
        return new AssemblerWindowsJWASM();
    }
}
