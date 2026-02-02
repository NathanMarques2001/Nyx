package com.editor_texto.nyx.compiler.assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação do assembler JWASM para Windows.
 * Verifica explicitamente se o SO é Windows antes de executar.
 */
public class MontadorWindowsJWASM implements ExecutorMontador {

    private final Path caminhoExecutavel;

    public MontadorWindowsJWASM() {
        this(Paths.get("tools", "jwasm.exe").toAbsolutePath());
    }

    public MontadorWindowsJWASM(Path caminhoExecutavel) {
        this.caminhoExecutavel = caminhoExecutavel;
    }

    @Override
    public ResultadoMontador montar(Path arquivoAsm, Path pastaSaida) {
        // Validação de SO
        if (!isWindows()) {
            return new ResultadoMontador(false, Collections.emptyList(),
                    List.of("ERRO: O pipeline de compilação atual suporta apenas Windows."), -1);
        }

        // Validação do Executável
        if (!validarExecutavel()) {
            return new ResultadoMontador(false, Collections.emptyList(),
                    List.of("ERRO: Executável do JWASM não encontrado ou inválido em: " + caminhoExecutavel), -1);
        }

        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        int exitCode = -1;

        try {
            List<String> comando = montarComando(arquivoAsm, pastaSaida);

            ProcessBuilder pb = new ProcessBuilder(comando);
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

        return new ResultadoMontador(exitCode == 0, stdout, stderr, exitCode);
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
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
