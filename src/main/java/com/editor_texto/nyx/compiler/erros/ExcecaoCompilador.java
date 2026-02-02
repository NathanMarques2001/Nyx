package com.editor_texto.nyx.compiler.erros;

/**
 * Exceção customizada para representar qualquer erro encontrado durante o
 * processo de compilação.
 * Ao lançar essa exceção, o compilador pode parar a execução imediatamente.
 */
public class ExcecaoCompilador extends Exception {

    public ExcecaoCompilador(String message) {
        super(message);
    }

}
