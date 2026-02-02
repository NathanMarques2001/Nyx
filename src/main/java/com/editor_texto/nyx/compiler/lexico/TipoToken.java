package com.editor_texto.nyx.compiler.lexico;

/**
 * Enumeração que define os tipos de tokens suportados pela linguagem LC.
 */
public enum TipoToken {
    PALAVRA_CHAVE,
    TIPO,
    IDENTIFICADOR,
    NUMERO,
    HEXADECIMAL,
    STRING,
    BOOLEANO,
    OPERADOR,
    DELIMITADOR,
    COMENTARIO,
    ERRO,
    ESPACO_EM_BRANCO
}
