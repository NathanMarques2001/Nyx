package com.editor_texto.nyx.compiler;

public class ErroCompilacao {
    private final TipoErro tipo;
    private final String mensagem;
    private final int linha;
    private final int coluna;

    public ErroCompilacao(TipoErro tipo, String mensagem, int linha, int coluna) {
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.linha = linha;
        this.coluna = coluna;
    }

    public TipoErro getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    @Override
    public String toString() {
        return String.format("[%s] Linha %d, Coluna %d: %s", tipo, linha, coluna, mensagem);
    }
}
