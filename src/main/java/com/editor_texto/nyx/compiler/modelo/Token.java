package com.editor_texto.nyx.compiler.modelo;

/**
 * Representa um token, a unidade fundamental de código fonte para o compilador.
 * Cada token possui um nome (o lexema), uma classificação, um tipo de dado,
 * e sua localização (linha e coluna) no arquivo fonte original.
 */
public class Token {

    private final String nome;
    private String tipo;
    private final String classificacao;
    private final int linha;
    private final int coluna;

    public Token(String nome, String classificacao, String tipo, int linha, int coluna) {
        this.nome = nome;
        this.classificacao = classificacao;
        this.tipo = tipo;
        this.linha = linha;
        this.coluna = coluna;
    }

    public String getNome() {
        return this.nome;
    }

    public String getTipo() {
        return this.tipo;
    }

    public String getClassificacao() {
        return this.classificacao;
    }

    public int getLinha() {
        return this.linha;
    }

    public int getColuna() {
        return this.coluna;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // Métodos utilitários movidos de TokenUtils

    // Métodos utilitários integrados

    public boolean isOperadorLogico() {
        String op = this.nome;
        return op.equals("==") || op.equals("<") || op.equals("<=") ||
                op.equals(">") || op.equals(">=") || op.equals("<>") ||
                op.equalsIgnoreCase("and") || op.equalsIgnoreCase("or") ||
                op.equalsIgnoreCase("not");
    }

    public boolean isOperadorAritmetico() {
        String op = this.nome;
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }

    public boolean isTipoPrimitivo() {
        String nomeLower = this.nome.toLowerCase();
        return nomeLower.equals("int") || nomeLower.equals("string") ||
                nomeLower.equals("boolean") || nomeLower.equals("byte");
    }

    public boolean isConstOuId() {
        return this.classificacao.equalsIgnoreCase("const") || this.classificacao.equalsIgnoreCase("id");
    }

    @Override
    public String toString() {
        return String.format("[Nome: %s, Classe: %s, Tipo: %s, Linha: %d, Coluna: %d]", this.nome, this.classificacao,
                this.tipo, this.linha, this.coluna);
    }
}