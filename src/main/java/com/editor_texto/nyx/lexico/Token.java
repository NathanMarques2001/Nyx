package com.editor_texto.nyx.lexico;

/**
 * Representa um token identificado no código fonte.
 * Contém informações sobre o tipo, o lexema (texto), e a posição no texto original.
 */
public class Token {
    private final TipoToken tipo;
    private final String lexema;
    private final int inicio;
    private final int tamanho;

    /**
     * Construtor do Token.
     *
     * @param tipo    O tipo do token.
     * @param lexema  O texto correspondente ao token.
     * @param inicio  A posição inicial (offset) no texto.
     * @param tamanho O comprimento do token.
     */
    public Token(TipoToken tipo, String lexema, int inicio, int tamanho) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.inicio = inicio;
        this.tamanho = tamanho;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getInicio() {
        return inicio;
    }

    public int getTamanho() {
        return tamanho;
    }

    @Override
    public String toString() {
        return String.format("Token[%s, '%s', %d, %d]", tipo, lexema, inicio, tamanho);
    }
}
