package com.editor_texto.nyx.compiler.erros;

import com.editor_texto.nyx.compiler.modelo.Token;

/**
 * Classe utilitária para centralizar a criação e o lançamento de erros de
 * compilação.
 * Garante que todas as mensagens de erro sigam um formato padrão e rigoroso.
 */
public class GerenciadorErros {

    // ========== ERROS LÉXICOS ==========

    public static void erroLexicoSimboloInvalido(char simbolo, int linha, int coluna) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Símbolo inválido: '" + simbolo + "' na linha " + linha + ", coluna " + coluna);
    }

    public static void erroLexicoIdentificadorMuitoLongo(String lexema, int linha, int coluna)
            throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Léxico: identificador '" + lexema
                + "' excede o limite de 255 caracteres na linha " + linha + ", coluna " + coluna);
    }

    public static void erroLexicoStringMuitoLonga(int linha, int coluna) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Léxico: literal string excede o limite de 255 caracteres na linha " + linha
                + ", coluna " + coluna);
    }

    public static void erroLexicoQuebraDeLinha(int linha, int coluna) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(
                "Erro Léxico: literal string não pode conter quebra de linha com '\\n' ou '\\r' na linha " + linha
                        + ", coluna " + coluna);
    }

    public static void erroLexicoByteHexInvalido(String lexema, int linha, int coluna) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Léxico: byte hexadecimal inválido! O formato deve ser 0hXX (X vai de 0 a F) '"
                + lexema + "' na linha " + linha + ", coluna " + coluna);
    }

    public static void erroLexicoIntForaDoIntervalo(String lexema, int linha, int coluna) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Léxico: valor inteiro '" + lexema
                + "' fora do intervalo permitido (-32768 a 32767), na linha " + linha + ", coluna " + coluna);
    }

    // ========== ERROS SINTÁTICOS ==========

    public static void erroSintatico(String esperado, Token encontrado) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Sintático: esperado '" + esperado + "', mas encontrado '" +
                encontrado.getNome() + "' na linha " + encontrado.getLinha() + ", coluna " + encontrado.getColuna());
    }

    public static void erroSintaticoAtribuicao(Token encontrado) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(
                "Erro Sintático: esperado uma atribuição de valor vindo ou não de uma variável, mas encontrado '" +
                        encontrado.getNome() + "' na linha " + encontrado.getLinha() + ", coluna "
                        + encontrado.getColuna());
    }

    public static void erroSintaticoAtribuicaoExpressaoLogica(Token encontrado) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(
                "Erro Sintático: atribuição inválida! Não é possível atribuir valor a uma variável vindo de uma expressão lógica '"
                        +
                        encontrado.getNome() + "' na linha " + encontrado.getLinha() + ", coluna "
                        + encontrado.getColuna());
    }

    // ========== ERROS SEMÂNTICOS ==========

    public static void erroSemanticoAtribuicao(Token tokenErrado, Token tokenDeclarado) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(construirMensagemSemanticaAtribuicao(tokenErrado, tokenDeclarado));
    }

    public static void erroSemanticoNaoDeclarado(Token token) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(construirMensagemSemanticaNaoDeclarado(token));
    }

    public static void erroSemanticoExpressaoInvalida(String tipoEsperado, String tipoAtual, Token token)
            throws ExcecaoCompilador {
        throw new ExcecaoCompilador(construirMensagemSemanticaExpressaoInvalida(tipoEsperado, tipoAtual, token));
    }

    public static void erroSemanticoTokenInvalido(Token token) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Semântico: expressão inválida com token '" + token.getNome() +
                "' na linha " + token.getLinha() + ", coluna " + token.getColuna());
    }

    public static void erroSemanticoEsperadoOperandoApos(String operador, Token token) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Semântico: esperado operando após operador '" + operador +
                "' na linha " + token.getLinha() + ", coluna " + token.getColuna());
    }

    public static void erroSemanticoExpressaoInvalidaAposControle(Token token) throws ExcecaoCompilador {
        throw new ExcecaoCompilador("Erro Semântico: expressão inválida após if/while, token '" + token.getNome() +
                "' na linha " + token.getLinha() + ", coluna " + token.getColuna());
    }

    public static void erroSemanticoAtribuicaoConstante(Token token) throws ExcecaoCompilador {
        throw new ExcecaoCompilador(
                "Erro Semântico: tentativa de atribuir um novo valor à constante '" + token.getNome() +
                        "' na linha " + token.getLinha() + ", coluna " + token.getColuna());
    }

    // ========== MÉTODOS AUXILIARES PARA CONSTRUIR MENSAGENS ==========

    private static String construirMensagemSemanticaAtribuicao(Token tokenErrado, Token tokenDeclarado) {
        return "Erro Semântico: atribuição incorreta! Foi atribuído um tipo '" + tokenErrado.getTipo() +
                "' à variável '" + tokenDeclarado.getNome() +
                "', mas deveria ter sido atribuído um tipo '" + tokenDeclarado.getTipo() +
                "', na linha " + tokenErrado.getLinha() + ", coluna " + tokenErrado.getColuna();
    }

    private static String construirMensagemSemanticaNaoDeclarado(Token token) {
        return "Erro Semântico: variável '" + token.getNome() + "' não foi declarada! " +
                "Na linha " + token.getLinha() + ", coluna " + token.getColuna();
    }

    private static String construirMensagemSemanticaExpressaoInvalida(String tipoEsperado, String tipoInvalido,
            Token token) {
        return "Erro Semântico: operação inválida! Era esperado um tipo '" + tipoEsperado +
                "', mas foi utilizado um tipo '" + tipoInvalido +
                "', na linha " + token.getLinha() + ", coluna " + token.getColuna();
    }
}