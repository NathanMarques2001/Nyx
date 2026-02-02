package com.editor_texto.nyx.compiler.assembler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.editor_texto.nyx.sistema.ServicoLog;

/**
 * Implementação de Linker para Windows.
 * Tenta usar 'link.exe' (MSVC/Polink) localizado na pasta 'tools'.
 */
public class LinkerWindows implements ExecutorLinker {

    private final Path caminhoLinker;

    public LinkerWindows() {
        // Assume que existe um link.exe ou golink.exe na pasta tools
        // Por padrão vamos tentar 'link.exe'
        this(Paths.get("tools", "link.exe").toAbsolutePath());
    }

    public LinkerWindows(Path caminhoLinker) {
        this.caminhoLinker = caminhoLinker;
    }

    @Override
    public ResultadoLinker ligar(Path arquivoObj, Path pastaSaida) {
        if (!Files.exists(caminhoLinker)) {
            String msg = "Linker não encontrado em: " + caminhoLinker.toString();
            ServicoLog.erro(msg);
            return new ResultadoLinker(false, List.of(), List.of(msg), -1);
        }

        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        int exitCode = -1;

        try {
            // Monta comando: link.exe /SUBSYSTEM:CONSOLE /ENTRY:start /OUT:output.exe
            // input.obj kernel32.lib user32.lib
            // Nota: Flags variam dependendo do linker (MSVC link.exe vs Polink vs Golink).
            // Vamos assumir uma sintaxe estilo Polink/MSVC comum para MASM.
            // /SUBSYSTEM:CONSOLE é essencial.

            String nomeExecutavel = arquivoObj.getFileName().toString().replace(".obj", ".exe");
            Path saidaExe = pastaSaida.resolve(nomeExecutavel);

            List<String> cmd = new ArrayList<>();
            cmd.add(caminhoLinker.toString());
            cmd.add("/SUBSYSTEM:CONSOLE");
            cmd.add("/ENTRY:start"); // O entry point do MASM geralmente é definido no .asm, mas as vezes precisa
                                     // avisar o linker
            cmd.add("/OUT:" + saidaExe.toString());
            cmd.add(arquivoObj.toString());
            // Bibliotecas padrão do Windows essenciais para chamadas de sistema (se usar
            // alguma)
            // cmd.add("kernel32.lib"); // Opcional se o assembly não chamar nada externo,
            // mas geralmente chama ExitProcess ou WriteConsole

            ServicoLog.info("Executando Linker: " + String.join(" ", cmd));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(pastaSaida.toFile());

            Process processo = pb.start();

            BufferedReader readerOut = new BufferedReader(new InputStreamReader(processo.getInputStream()));
            BufferedReader readerErr = new BufferedReader(new InputStreamReader(processo.getErrorStream()));

            String line;
            while ((line = readerOut.readLine()) != null)
                stdout.add(line);
            while ((line = readerErr.readLine()) != null)
                stderr.add(line);

            exitCode = processo.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            stderr.add("Exceção ao executar linker: " + e.getMessage());
            ServicoLog.erro("Exceção Linker: " + e.getMessage());
        }

        return new ResultadoLinker(exitCode == 0, stdout, stderr, exitCode);
    }
}
