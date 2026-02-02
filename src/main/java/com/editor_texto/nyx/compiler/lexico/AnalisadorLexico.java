package com.editor_texto.nyx.compiler.lexico;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analisador Léxico responsável por converter o código fonte em uma lista de
 * tokens.
 * Esta implementação é resiliente a erros e focada em syntax highlighting,
 * não interrompendo a análise em caso de caracteres inválidos.
 */
public class AnalisadorLexico {

    private static final Set<String> PALAVRAS_CHAVE = Set.of(
            "if", "else", "while", "begin", "end", "final", "write", "writeln", "readln");

    private static final Set<String> TIPOS = Set.of(
            "int", "byte", "string", "boolean");

    // Padrões Regex para tokenização
    private static final Pattern ESPACO_EM_BRANCO = Pattern.compile("\\s+");
    // Comentários de bloco e linha (adaptado para { } e /* */)
    private static final Pattern COMENTARIO = Pattern.compile("/\\*(.|[\\r\\n])*?\\*/|\\{[^}]*\\}");
    private static final Pattern STRING = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    private static final Pattern HEXADECIMAL = Pattern.compile("0h[a-zA-Z0-9]*");
    private static final Pattern NUMERO = Pattern.compile("\\d+");
    private static final Pattern BOOLEANO = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    private static final Pattern IDENTIFICADOR = Pattern.compile("[a-zA-Z_]\\w*");
    private static final Pattern OPERADOR = Pattern.compile("==|<>|<=|>=|<|>|[+\\-*/=]|and|or|not",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DELIMITADOR = Pattern.compile("[,;()]");

    /**
     * Realiza a análise léxica (scanner) do texto fornecido.
     *
     * @param texto O código fonte a ser analisado.
     * @return Uma lista de tokens identificados.
     */
    public List<Token> analisar(String texto) {
        List<Token> tokens = new ArrayList<>();
        int deslocamento = 0;
        String textoRestante = texto;

        while (!textoRestante.isEmpty()) {
            Matcher match;
            boolean encontrou = false;
            int tamanho = 0;

            // 1. Espaços em Branco
            match = ESPACO_EM_BRANCO.matcher(textoRestante);
            if (match.lookingAt()) {
                tamanho = match.end();
                tokens.add(new Token(TipoToken.ESPACO_EM_BRANCO, match.group(), deslocamento, tamanho));
                encontrou = true;
            }

            // 2. Comentários (Prioridade Alta)
            if (!encontrou) {
                match = COMENTARIO.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.COMENTARIO, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 3. Strings
            if (!encontrou) {
                if (textoRestante.startsWith("\"")) {
                    match = STRING.matcher(textoRestante);
                    if (match.lookingAt()) {
                        tamanho = match.end();
                        tokens.add(new Token(TipoToken.STRING, match.group(), deslocamento, tamanho));
                        encontrou = true;
                    } else {
                        // String não fechada ou erro. Tratar como ERRO até o final da linha ou próxima
                        // aspa.
                        int proximaAspa = textoRestante.indexOf('\"', 1);
                        int proximaLinha = textoRestante.indexOf('\n');
                        int fim = (proximaAspa != -1) ? proximaAspa + 1
                                : (proximaLinha != -1 ? proximaLinha : textoRestante.length());
                        tamanho = fim;
                        tokens.add(
                                new Token(TipoToken.ERRO, textoRestante.substring(0, tamanho), deslocamento, tamanho));
                        encontrou = true;
                    }
                }
            }

            // 4. Hexadecimal
            if (!encontrou) {
                match = HEXADECIMAL.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.HEXADECIMAL, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 5. Booleano
            if (!encontrou) {
                match = BOOLEANO.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.BOOLEANO, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 6. Identificador / Palavra-Chave / Tipo
            if (!encontrou) {
                match = IDENTIFICADOR.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    String palavra = match.group();
                    TipoToken tipo = TipoToken.IDENTIFICADOR;

                    if (PALAVRAS_CHAVE.contains(palavra))
                        tipo = TipoToken.PALAVRA_CHAVE;
                    else if (TIPOS.contains(palavra))
                        tipo = TipoToken.TIPO;
                    else if (palavra.equalsIgnoreCase("and") || palavra.equalsIgnoreCase("or")
                            || palavra.equalsIgnoreCase("not")) {
                        tipo = TipoToken.OPERADOR;
                    }

                    tokens.add(new Token(tipo, palavra, deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 7. Números
            if (!encontrou) {
                match = NUMERO.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.NUMERO, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 8. Operadores
            if (!encontrou) {
                match = OPERADOR.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.OPERADOR, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 9. Delimitadores
            if (!encontrou) {
                match = DELIMITADOR.matcher(textoRestante);
                if (match.lookingAt()) {
                    tamanho = match.end();
                    tokens.add(new Token(TipoToken.DELIMITADOR, match.group(), deslocamento, tamanho));
                    encontrou = true;
                }
            }

            // 10. Desconhecido / Erro
            if (!encontrou) {
                tamanho = 1;
                tokens.add(new Token(TipoToken.ERRO, textoRestante.substring(0, 1), deslocamento, tamanho));
                encontrou = true;
            }

            // Avançar
            deslocamento += tamanho;
            textoRestante = textoRestante.substring(tamanho);
        }

        return tokens;
    }
}
