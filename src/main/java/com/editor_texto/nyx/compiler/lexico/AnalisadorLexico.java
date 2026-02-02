package com.editor_texto.nyx.compiler.lexico;

import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.erros.GerenciadorErros;
import com.editor_texto.nyx.compiler.modelo.Token;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Realiza a análise léxica do código fonte.
 * Responsável por ler o código caractere por caractere (através de uma linha
 * inteira),
 * identificar padrões (lexemas) e convertê-los em tokens.
 */
public class AnalisadorLexico {

    private final TabelaSimbolos tabelaSimbolos;

    // Expressões Regulares para identificar os padrões da linguagem

    private final Pattern numeros = Pattern.compile("\\d+"); // Encontra sequências de dígitos.
    private final Pattern hexadecimais = Pattern.compile("0h[a-zA-Z0-9]*"); // Encontra hexadecimais no formato 0hHH.
    private final Pattern identificadores = Pattern.compile("[a-zA-Z_]\\w*"); // Encontra identificadores (começa com
                                                                              // letra ou _, seguido por letras, dígitos
                                                                              // ou _).
    private final Pattern booleano = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE); // Encontra "true" ou
                                                                                              // "false".
    private final Pattern operadores = Pattern.compile("==|<>|<=|>=|<|>|[+\\-*/=]"); // Encontra operadores relacionais
                                                                                     // e aritméticos.
    private final Pattern delimitadores = Pattern.compile("[,;()]"); // Encontra delimitadores.
    private final Pattern comentarios = Pattern.compile("/\\*(.|\\R)*?\\*/|\\{[^\\}]*\\}"); // Encontra comentários de
                                                                                            // bloco (/*...*/ ou {...}).
    private final Pattern strings = Pattern.compile("\"([^\"\\\\\\r\\n]|\\\\.)*\""); // Encontra literais string entre
                                                                                     // aspas.
    private final Pattern espacosEmBranco = Pattern.compile("\\s+"); // Encontra espaços em branco.

    public AnalisadorLexico(TabelaSimbolos tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
    }

    // Analisa uma única linha de código, transformando-a em tokens.
    public void analisar(String codigo, int numeroLinha) throws ExcecaoCompilador {
        int numeroColuna = 1;
        codigo = codigo.stripLeading();

        while (!codigo.isEmpty()) {
            Matcher matcher = null;
            boolean encontrou = false;
            int tamanhoConsumido = 0;

            // Ignorar espaços e comentários
            matcher = ignorarLexema(codigo);
            if (matcher != null && matcher.lookingAt()) {
                tamanhoConsumido = matcher.end();
                encontrou = true;
            } else if (codigo.startsWith("\"")) {
                Matcher matcherString = strings.matcher(codigo);
                if (matcherString.lookingAt()) {
                    String lexema = matcherString.group();

                    if (lexema.length() > 255) {
                        GerenciadorErros.erroLexicoStringMuitoLonga(numeroLinha, numeroColuna);
                    }

                    tabelaSimbolos.adicionarToken(new Token(lexema, "const", "string", numeroLinha, numeroColuna));
                    tamanhoConsumido = matcherString.end();
                    encontrou = true;
                } else {
                    GerenciadorErros.erroLexicoQuebraDeLinha(numeroLinha, numeroColuna);
                }
            } else if ((matcher = booleano.matcher(codigo)).lookingAt()) {
                tabelaSimbolos.adicionarToken(
                        new Token(resolverBoolean(matcher.group()), "const", "boolean", numeroLinha, numeroColuna));
                tamanhoConsumido = matcher.end();
                encontrou = true;
            } else if ((matcher = hexadecimais.matcher(codigo)).lookingAt()) {
                String lexemaHex = matcher.group();
                String valorHex = lexemaHex.substring(2);
                if (!valorHex.matches("[a-fA-F0-9]{1,2}")) {
                    GerenciadorErros.erroLexicoByteHexInvalido(lexemaHex, numeroLinha, numeroColuna);
                }
                tabelaSimbolos.adicionarToken(new Token(lexemaHex, "const", "byte", numeroLinha, numeroColuna));
                tamanhoConsumido = matcher.end();
                encontrou = true;
            } else if ((matcher = numeros.matcher(codigo)).lookingAt()) {
                String lexemaInt = matcher.group();
                if (lexemaInt.length() > 6) { // Verificação rápida de overflow antes de parse
                    GerenciadorErros.erroLexicoIntForaDoIntervalo(lexemaInt, numeroLinha, numeroColuna);
                }
                try {
                    int valor = Integer.parseInt(lexemaInt);
                    if (valor < -32768 || valor > 32767) {
                        GerenciadorErros.erroLexicoIntForaDoIntervalo(lexemaInt, numeroLinha, numeroColuna);
                    }
                } catch (NumberFormatException e) {
                    GerenciadorErros.erroLexicoIntForaDoIntervalo(lexemaInt, numeroLinha, numeroColuna);
                }
                tabelaSimbolos.adicionarToken(new Token(lexemaInt, "const", "int", numeroLinha, numeroColuna));
                tamanhoConsumido = matcher.end();
                encontrou = true;
            } else if ((matcher = matchReservadaOuID(codigo)) != null && matcher.lookingAt()) {
                String lexema = matcher.group();
                String lexemaLower = lexema.toLowerCase();

                if (lexemaLower.length() > 255) {
                    GerenciadorErros.erroLexicoIdentificadorMuitoLongo(lexema, numeroLinha, numeroColuna);
                }

                String tipo = tabelaSimbolos.isPalavraReservada(lexemaLower) ? "reserved_word" : "id";
                tabelaSimbolos.adicionarToken(new Token(lexema, tipo, "null", numeroLinha, numeroColuna));
                tamanhoConsumido = matcher.end();
                encontrou = true;
            } else {
                GerenciadorErros.erroLexicoSimboloInvalido(codigo.charAt(0), numeroLinha, numeroColuna);
            }

            if (encontrou) {
                numeroColuna += tamanhoConsumido;
                codigo = codigo.substring(tamanhoConsumido).stripLeading();
            }
        }
    }

    // Tenta encontrar correspondência com padrões que devem ser ignorados (espaços
    // e comentários).
    private Matcher ignorarLexema(String codigo) {
        Matcher matcher = espacosEmBranco.matcher(codigo);
        if (matcher.lookingAt()) {
            return matcher;
        }
        matcher = comentarios.matcher(codigo);
        if (matcher.lookingAt()) {
            return matcher;
        }
        return null;
    }

    // Tenta encontrar correspondência com identificadores, operadores ou
    // delimitadores.
    private Matcher matchReservadaOuID(String codigo) {
        Matcher matcher = identificadores.matcher(codigo);
        if (matcher.lookingAt())
            return matcher;

        matcher = operadores.matcher(codigo);
        if (matcher.lookingAt())
            return matcher;

        matcher = delimitadores.matcher(codigo);
        if (matcher.lookingAt())
            return matcher;

        return null;
    }

    // Converte os literais "true" e "false" para seus equivalentes hexadecimais,
    // conforme a especificação.
    private String resolverBoolean(String codigo) {
        return codigo.equalsIgnoreCase("true") ? "Fh" : "0h";
    }
}