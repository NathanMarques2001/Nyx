package com.editor_texto.nyx.compiler.semantico;

import com.editor_texto.nyx.compiler.modelo.Token;

import java.util.Set;
import java.util.ArrayList;

/**
 * Gerencia todos os tokens (símbolos) do código fonte.
 * Funciona como um repositório central que armazena os tokens na ordem em que
 * aparecem
 * e fornece métodos para acessá-los e validá-los.
 */
public class TabelaSimbolos {

    // Conjunto de todas as palavras reservadas da linguagem LC
    private static final Set<String> palavrasReservadas = Set.of(
            "final", "int", "byte", "string", "while", "if", "else",
            "and", "or", "not", "==", "=", "(", ")",
            "<", ">", "<>", ">=", "<=", ",", "+",
            "-", "*", "/", ";", "begin", "end", "readln",
            "write", "writeln", "true", "false", "boolean");

    private final ArrayList<Token> tabela;

    public TabelaSimbolos() {
        this.tabela = new ArrayList<>();
    }

    public Token tokenAtual(int index) {
        return this.tabela.get(index);
    }

    public void adicionarToken(Token token) {
        tabela.add(token);
    }

    public boolean isPalavraReservada(String word) {
        return palavrasReservadas.contains(word);
    }

    // Busca na tabela de símbolos o tipo de um determinado identificador.
    public String getTipoSimbolo(String nomeSimbolo) {
        for (int i = this.tabela.size() - 1; i >= 0; i--) {
            Token token = this.tabela.get(i);
            if (token.getNome().equals(nomeSimbolo)) {
                if (token.getTipo() != null) {
                    return token.getTipo();
                }
            }
        }
        return null; // Retorna nulo se o símbolo não for encontrado.
    }

    public int getTamanho() {
        return this.tabela.size();
    }
}
