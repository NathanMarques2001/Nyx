package com.editor_texto.nyx.compiler.api;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe responsável por gerenciar a chamada ao assembler externo (JWASM).
 */
public class GerenciadorJWASM {

    private final Path caminhoExecutavel;

    /**
     * Construtor padrão que espera encontrar o jwasm na pasta tools.
     */
    public GerenciadorJWASM() {
        this(Paths.get("tools", "jwasm.exe").toAbsolutePath());
    }

    /**
     * Construtor com caminho explícito para o executável.
     * 
     * @param caminhoExecutavel Caminho para o jwasm.exe
     */
    public GerenciadorJWASM(Path caminhoExecutavel) {
        this.caminhoExecutavel = caminhoExecutavel;
    }

    /**
     * Executa o assembler para o arquivo ASM fornecido.
     * 
     * @param arquivoAsm Caminho completo para o arquivo .asm fonte
     * @param pastaSaida Caminho para a pasta onde os arquivos gerados (.obj, .exe)
     *                   devem ficar
     * @return ResultadoAssembler contendo logs e status
     */
    public ResultadoAssembler montar(Path arquivoAsm, Path pastaSaida) {
        if (!validarExecutavel()) {
            return new ResultadoAssembler(false, Collections.emptyList(),
                    List.of("ERRO: Executável do JWASM não encontrado ou inválido em: " + caminhoExecutavel), -1);
        }

        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        int exitCode = -1;

        try {
            List<String> comando = montarComando(arquivoAsm, pastaSaida);

            ProcessBuilder pb = new ProcessBuilder(comando);

            // Define diretório de trabalho como a pasta de saída para evitar sujeira,
            // embora paths absolutos no comando resolvam.
            pb.directory(pastaSaida.toFile());

            Process processo = pb.start();

            // Captura streams
            BufferedReader readerOut = new BufferedReader(new InputStreamReader(processo.getInputStream()));
            BufferedReader readerErr = new BufferedReader(new InputStreamReader(processo.getErrorStream()));

            String line;
            while ((line = readerOut.readLine()) != null) {
                stdout.add(line);
            }
            while ((line = readerErr.readLine()) != null) {
                stderr.add(line);
            }

            exitCode = processo.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            stderr.add("Erro ao executar JWASM: " + e.getMessage());
        }

        return new ResultadoAssembler(exitCode == 0, stdout, stderr, exitCode);
    }

    private boolean validarExecutavel() {
        return Files.exists(caminhoExecutavel) && Files.isExecutable(caminhoExecutavel);
    }

    private List<String> montarComando(Path arquivoAsm, Path pastaSaida) {
        String nomeBase = arquivoAsm.getFileName().toString();
        if (nomeBase.contains(".")) {
            nomeBase = nomeBase.substring(0, nomeBase.lastIndexOf('.'));
        }

        Path arquivoObj = pastaSaida.resolve(nomeBase + ".obj");

        List<String> cmd = new ArrayList<>();
        cmd.add(caminhoExecutavel.toString());
        cmd.add("-coff");
        cmd.add("-Fo" + arquivoObj.toString());
        cmd.add(arquivoAsm.toString());

        return cmd;
    }
}
