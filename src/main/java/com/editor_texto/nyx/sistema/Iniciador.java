package com.editor_texto.nyx.sistema;

/**
 * Classe de entrada para o JAR executável.
 * Necessária para contornar problemas de módulos do JavaFX em alguns ambientes.
 */
public class Iniciador {
    public static void main(String[] args) {
        AplicacaoPrincipal.main(args);
    }
}
