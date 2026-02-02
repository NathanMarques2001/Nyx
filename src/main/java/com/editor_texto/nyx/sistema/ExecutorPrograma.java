package com.editor_texto.nyx.sistema;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Responsável por executar o programa (.exe) gerado.
 * Captura stdout e stderr e envia para o ServicoLog.
 */
public class ExecutorPrograma {

    public static void executar(String caminhoExecutavel) {
        File arquivoExe = new File(caminhoExecutavel);
        if (!arquivoExe.exists()) {
            ServicoLog.erro("Executável não encontrado: " + caminhoExecutavel);
            return;
        }

        ServicoLog.info("--- Iniciando Execução do Programa ---");
        ServicoLog.info("Comando: " + caminhoExecutavel);

        Thread threadExecucao = new Thread(() -> {
            try {
                // Executa em uma janela de console separada ou captura a saída?
                // Requisito: Integrar logs no console. Então captura a saída.
                // Mas programas interativos (cin >>) vão travar se não houver input.
                // Por enquanto assumimos programas não-interativos ou que apenas imprimem.
                // Para interatividade completa precisaríamos de um terminal emulator embutido.
                // Vamos tentar rodar capturando a saída.

                ProcessBuilder pb = new ProcessBuilder(caminhoExecutavel);
                pb.directory(arquivoExe.getParentFile());

                Process processo = pb.start();

                BufferedReader readerOut = new BufferedReader(new InputStreamReader(processo.getInputStream()));
                BufferedReader readerErr = new BufferedReader(new InputStreamReader(processo.getErrorStream()));

                String line;
                while ((line = readerOut.readLine()) != null) {
                    ServicoLog.info("[PROG] " + line);
                }
                while ((line = readerErr.readLine()) != null) {
                    ServicoLog.erro("[PROG] " + line);
                }

                int exitCode = processo.waitFor();
                ServicoLog.info("--- Programa finalizado com código: " + exitCode + " ---");

            } catch (Exception e) {
                e.printStackTrace();
                ServicoLog.erro("Erro na execução do programa: " + e.getMessage());
            }
        });

        threadExecucao.setDaemon(true);
        threadExecucao.start();
    }
}
