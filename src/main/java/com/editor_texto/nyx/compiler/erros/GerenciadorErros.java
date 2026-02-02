package com.editor_texto.nyx.compiler.erros;

import com.editor_texto.nyx.compiler.TipoErro;
import com.editor_texto.nyx.compiler.modelo.Token;

/**
 * Classe utilitária para centralizar a criação e o lançamento de erros de
 * compilação.
 * Garante que todas as mensagens de erro sigam um formato padrão e rigoroso.
 */
public class GerenciadorErros {

        // ========== ERROS LÉXICOS ==========

        public static void erroLexicoSimboloInvalido(char simbolo, int linha, int coluna) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Símbolo inválido: '" + simbolo + "'", TipoErro.LEXICO, linha, coluna);
        }

        public static void erroLexicoIdentificadorMuitoLongo(String lexema, int linha, int coluna)
                        throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Identificador '" + lexema + "' excede o limite de 255 caracteres",
                                TipoErro.LEXICO,
                                linha, coluna);
        }

        public static void erroLexicoStringMuitoLonga(int linha, int coluna) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Literal string excede o limite de 255 caracteres", TipoErro.LEXICO, linha,
                                coluna);
        }

        public static void erroLexicoQuebraDeLinha(int linha, int coluna) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Literal string não pode conter quebra de linha com '\\n' ou '\\r'",
                                TipoErro.LEXICO,
                                linha, coluna);
        }

        public static void erroLexicoByteHexInvalido(String lexema, int linha, int coluna) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Byte hexadecimal inválido! O formato deve ser 0hXX (X vai de 0 a F): '" + lexema + "'",
                                TipoErro.LEXICO, linha, coluna);
        }

        public static void erroLexicoIntForaDoIntervalo(String lexema, int linha, int coluna) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Valor inteiro '" + lexema + "' fora do intervalo permitido (-32768 a 32767)",
                                TipoErro.LEXICO, linha, coluna);
        }

        // ========== ERROS SINTÁTICOS ==========

        public static void erroSintatico(String esperado, Token encontrado) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Esperado '" + esperado + "', mas encontrado '" + encontrado.getNome() + "'",
                                TipoErro.SINTATICO, encontrado.getLinha(), encontrado.getColuna());
        }

        public static void erroSintaticoAtribuicao(Token encontrado) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Esperado uma atribuição de valor vindo ou não de uma variável, mas encontrado '"
                                                + encontrado.getNome()
                                                + "'",
                                TipoErro.SINTATICO, encontrado.getLinha(), encontrado.getColuna());
        }

        public static void erroSintaticoAtribuicaoExpressaoLogica(Token encontrado) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Atribuição inválida! Não é possível atribuir valor a uma variável vindo de uma expressão lógica '"
                                                +
                                                encontrado.getNome() + "'",
                                TipoErro.SINTATICO, encontrado.getLinha(), encontrado.getColuna());
        }

        // ========== ERROS SEMÂNTICOS ==========

        public static void erroSemanticoAtribuicao(Token tokenErrado, Token tokenDeclarado) throws ExcecaoCompilador {
                throw new ExcecaoCompilador(construirMensagemSemanticaAtribuicao(tokenErrado, tokenDeclarado),
                                TipoErro.SEMANTICO,
                                tokenErrado.getLinha(), tokenErrado.getColuna());
        }

        public static void erroSemanticoNaoDeclarado(Token token) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Variável '" + token.getNome() + "' não foi declarada!", TipoErro.SEMANTICO,
                                token.getLinha(), token.getColuna());
        }

        public static void erroSemanticoExpressaoInvalida(String tipoEsperado, String tipoAtual, Token token)
                        throws ExcecaoCompilador {
                throw new ExcecaoCompilador(
                                "Operação inválida! Era esperado um tipo '" + tipoEsperado
                                                + "', mas foi utilizado um tipo '"
                                                + tipoAtual + "'",
                                TipoErro.SEMANTICO, token.getLinha(), token.getColuna());
        }

        public static void erroSemanticoTokenInvalido(Token token) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Expressão inválida com token '" + token.getNome() + "'",
                                TipoErro.SEMANTICO,
                                token.getLinha(), token.getColuna());
        }

        public static void erroSemanticoEsperadoOperandoApos(String operador, Token token) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Esperado operando após operador '" + operador + "'", TipoErro.SEMANTICO,
                                token.getLinha(), token.getColuna());
        }

        public static void erroSemanticoExpressaoInvalidaAposControle(Token token) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Expressão inválida após if/while, token '" + token.getNome() + "'",
                                TipoErro.SEMANTICO,
                                token.getLinha(), token.getColuna());
        }

        public static void erroSemanticoAtribuicaoConstante(Token token) throws ExcecaoCompilador {
                throw new ExcecaoCompilador("Tentativa de atribuir um novo valor à constante '" + token.getNome() + "'",
                                TipoErro.SEMANTICO, token.getLinha(), token.getColuna());
        }

        // ========== MÉTODOS AUXILIARES PARA CONSTRUIR MENSAGENS (apenas o texto base)
        // ==========

        private static String construirMensagemSemanticaAtribuicao(Token tokenErrado, Token tokenDeclarado) {
                return "Atribuição incorreta! Foi atribuído um tipo '" + tokenErrado.getTipo() +
                                "' à variável '" + tokenDeclarado.getNome() +
                                "', mas deveria ter sido atribuído um tipo '" + tokenDeclarado.getTipo() + "'";
        }
}