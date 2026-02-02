package com.editor_texto.nyx.compiler.erros;

import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.TipoErro;

/**
 * Exceção customizada para representar qualquer erro encontrado durante o
 * processo de compilação.
 * Carrega informações estruturadas sobre o erro.
 */
public class ExcecaoCompilador extends Exception {

    private final ErroCompilacao erro;

    public ExcecaoCompilador(String message, TipoErro tipo, int linha, int coluna) {
        super(message);
        this.erro = new ErroCompilacao(tipo, message, linha, coluna);
    }

    public ExcecaoCompilador(String message) {
        super(message);
        this.erro = new ErroCompilacao(TipoErro.OUTRO, message, 0, 0);
    }

    public ErroCompilacao getErro() {
        return erro;
    }
}
