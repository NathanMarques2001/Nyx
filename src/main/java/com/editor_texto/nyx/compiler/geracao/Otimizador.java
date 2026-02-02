package com.editor_texto.nyx.compiler.geracao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementa um otimizador Peephole simples para o código Assembly gerado.
 * Este otimizador examina uma pequena "janela" (peephole) de instruções
 * e substitui sequências conhecidas por outras mais eficientes.
 */
public class Otimizador {

    // Otimiza um arquivo .asm de entrada e salva o resultado em um novo arquivo.
    public static void otimizarArquivo(String caminhoEntrada) throws IOException {
        File arquivoEntrada = new File(caminhoEntrada);
        if (!arquivoEntrada.exists()) {
            throw new FileNotFoundException("Arquivo de entrada não encontrado: " + arquivoEntrada.getAbsolutePath());
        }

        // Gera o nome do arquivo de saída (ex: "programa.asm" ->
        // "programa_otimizado.asm").
        String nomeBase = arquivoEntrada.getName().replaceFirst("[.][^.]+$", "");
        File arquivoSaida = new File(arquivoEntrada.getParent(), nomeBase + "_otimizado.asm");

        List<String> linhasOriginais = Files.readAllLines(arquivoEntrada.toPath());
        List<String> linhasOtimizadas = otimizar(linhasOriginais);
        Files.write(arquivoSaida.toPath(), linhasOtimizadas);

        System.out.println("Arquivo otimizado gerado: " + arquivoSaida.getAbsolutePath());
    }

    /**
     * Aplica as regras de otimização Peephole a uma lista de instruções Assembly.
     * A lógica foi tornada mais robusta para evitar exceções com formatos de
     * instrução inesperados.
     */
    private static List<String> otimizar(List<String> instrucoes) {
        List<String> otimizadas = new ArrayList<>();
        int i = 0;

        while (i < instrucoes.size()) {
            String linhaAtual = instrucoes.get(i);
            String atual = linhaAtual.trim();

            // Pula linhas vazias
            if (atual.isEmpty()) {
                otimizadas.add(linhaAtual);
                i++;
                continue;
            }

            // Otimização: Remoção de operações de identidade.
            // Ex: ADD EAX, 0 ou MUL EBX, 1 são inúteis.
            if (atual.toUpperCase().startsWith("ADD ") && atual.endsWith(", 0") ||
                    atual.toUpperCase().startsWith("SUB ") && atual.endsWith(", 0") ||
                    atual.toUpperCase().startsWith("IMUL ") && atual.endsWith(", 1")) {
                i++; // Simplesmente pula a instrução.
                continue;
            }

            // Otimização: Redução de força.
            // Multiplicar por 2 é mais lento que um deslocamento de bits para a esquerda
            // (SHL).
            // Ex: IMUL EAX, 2 -> SHL EAX, 1
            if (atual.toUpperCase().matches("IMUL \\w+, 2")) {
                String[] partes = atual.split("\\s+");
                if (partes.length > 1) {
                    String reg = partes[1].split(",")[0];
                    // Mantém a identação original
                    String indentacao = linhaAtual.substring(0, linhaAtual.indexOf(atual.charAt(0)));
                    otimizadas.add(indentacao + "shl " + reg + ", 1");
                    i++;
                    continue;
                }
            }

            // Otimização: Remoção de saltos redundantes.
            // Um salto para a linha imediatamente seguinte é desnecessário.
            // Ex: JMP _label1
            // _label1:
            if (atual.toUpperCase().startsWith("JMP ") && i + 1 < instrucoes.size()) {
                String proxima = instrucoes.get(i + 1).trim();
                String[] partes = atual.split("\\s+");
                if (partes.length > 1) {
                    String labelAlvo = partes[1];
                    if (proxima.equalsIgnoreCase(labelAlvo + ":")) {
                        i++; // Pula a instrução JMP.
                        continue;
                    }
                }
            }

            // Otimização: Remoção de movimentações redundantes (troca inútil).
            // Ex: MOV EAX, EBX
            // MOV EBX, EAX
            if (atual.toUpperCase().startsWith("MOV ") && i + 1 < instrucoes.size()) {
                String proxima = instrucoes.get(i + 1).trim();
                if (proxima.toUpperCase().startsWith("MOV ")) {
                    // Split robusto que lida com múltiplos espaços
                    String[] partes1 = atual.split("\\s+", 2);
                    String[] partes2 = proxima.split("\\s+", 2);

                    // Garante que a instrução tem o formato "MOV operands"
                    if (partes1.length == 2 && partes2.length == 2) {
                        String[] ops1 = partes1[1].split(",");
                        String[] ops2 = partes2[1].split(",");

                        // Garante que há dois operandos
                        if (ops1.length == 2 && ops2.length == 2) {
                            String r1Atual = ops1[0].trim();
                            String r2Atual = ops1[1].trim();
                            String r1Prox = ops2[0].trim();
                            String r2Prox = ops2[1].trim();

                            // Verifica a troca: MOV R1, R2 -> MOV R2, R1
                            if (r1Atual.equalsIgnoreCase(r2Prox) && r2Atual.equalsIgnoreCase(r1Prox)) {
                                otimizadas.add(linhaAtual); // Mantém a primeira instrução
                                i += 2; // Pula as duas instruções originais.
                                continue;
                            }
                        }
                    }
                }
            }

            // Se nenhuma otimização for aplicada, mantém a instrução original.
            otimizadas.add(linhaAtual);
            i++;
        }

        return otimizadas;
    }
}
