package com.editor_texto.nyx.lexer;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int startOffset;
    private final int length;

    public Token(TokenType type, String lexeme, int startOffset, int length) {
        this.type = type;
        this.lexeme = lexeme;
        this.startOffset = startOffset;
        this.length = length;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("Token[%s, '%s', %d, %d]", type, lexeme, startOffset, length);
    }
}
