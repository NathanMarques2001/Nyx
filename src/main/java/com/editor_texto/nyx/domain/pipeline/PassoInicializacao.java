package com.editor_texto.nyx.domain.pipeline;

import java.io.File;

public class PassoInicializacao implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        // Garante que o diretório de saída existe
        File dirSaida = contexto.getDiretorioSaida().toFile();

        if (!dirSaida.getName().equalsIgnoreCase("out")) {
            dirSaida = new File(dirSaida, "out");
        }

        if (dirSaida.exists()) {
            File[] arquivos = dirSaida.listFiles((dir, name) -> name.endsWith(".asm"));
            if (arquivos != null) {
                for (File f : arquivos) {
                    f.delete();
                }
            }
        } else {
            dirSaida.mkdirs();
        }

        return true; // Inicialização sempre deve passar, a menos que falhe ao criar diretório
                     // (exceção tratada no pipeline)
    }

    @Override
    public String getNome() {
        return "Inicialização";
    }
}
