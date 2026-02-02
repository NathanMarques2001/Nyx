package com.editor_texto.nyx.compiler.semantico;

import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.erros.GerenciadorErros;
import com.editor_texto.nyx.compiler.modelo.Token;

import java.util.ArrayList;

/**
 * Realiza a análise semântica do código.
 * Esta fase verifica a coerência e o significado do programa, como a checagem
 * de tipos,
 * a declaração de variáveis e a validade das expressões.
 */
public class AnalisadorSemantico {

    private final TabelaSimbolos tabelaSimbolos;
    // Lista para manter o controle de todos os identificadores
    // (variáveis/constantes) declarados.
    private final ArrayList<Token> tokensDeclarados = new ArrayList<>();
    // Lista para guardar os nomes das constantes.
    private final ArrayList<String> nomesConstantes = new ArrayList<>();
    private Token tokenAtual;
    private int indiceTokenAtual = 0;
    // Mantém o tipo esperado durante a análise de uma declaração ou expressão.
    private String tipoAtual;

    public AnalisadorSemantico(TabelaSimbolos tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.tokenAtual = tabelaSimbolos.tokenAtual(indiceTokenAtual);
    }

    // Ponto de entrada principal para a análise semântica.
    public void analisar() throws ExcecaoCompilador {
        // 1. Processa todas as declarações primeiro para popular a lista de
        // 'tokensDeclarados'.
        verificarDeclaracoes();
        // 2. Com base nas declarações, atualiza o tipo de todos os tokens na tabela de
        // símbolos.
        atualizarTiposSimbolos();
        // 3. Percorre o código novamente para verificar a semântica das atribuições e
        // expressões.
        verificarAtribuicoes();
    }

    private void proximoToken() {
        if (indiceTokenAtual < tabelaSimbolos.getTamanho() - 1) {
            tokenAtual = tabelaSimbolos.tokenAtual(++indiceTokenAtual);
        }
    }

    private void tokenAnterior() {
        tokenAtual = tabelaSimbolos.tokenAtual(--indiceTokenAtual);
    }

    // Verifica se o token atual (um identificador) já foi declarado.
    private boolean isDeclarado() {
        for (Token t : tokensDeclarados) {
            if (t.getNome().equalsIgnoreCase(tokenAtual.getNome())) {
                return true;
            }
        }
        return false;
    }

    // Valida se o tipo do token atual é compatível com o tipo do alvo da
    // atribuição.
    private void esperarAtribuicao(Token alvo) throws ExcecaoCompilador {
        if (!tokenAtual.getTipo().equalsIgnoreCase(alvo.getTipo())) {
            GerenciadorErros.erroSemanticoAtribuicao(tokenAtual, alvo);
        }
    }

    /*
     * Percorre a seção de declarações, registrando cada variável e constante
     * declarada.
     * Também realiza a primeira verificação de tipo para declarações com
     * inicialização.
     */
    private void verificarDeclaracoes() throws ExcecaoCompilador {
        if (tokenAtual.isTipoPrimitivo() || tokenAtual.getNome().equalsIgnoreCase("final")) {
            // Verifica se a declaração atual é de uma constante.
            boolean isConstant = tokenAtual.getNome().equalsIgnoreCase("final");

            if (isConstant) {
                tipoAtual = "final";
            } else {
                tipoAtual = tokenAtual.getNome();
            }
            proximoToken(); // Avança para o ID

            Token declarado = tokenAtual;

            // ADICIONADO: Se for uma constante, registre seu nome.
            if (isConstant) {
                nomesConstantes.add(declarado.getNome());
            }

            declarado.setTipo(tipoAtual);
            tokensDeclarados.add(declarado);
            proximoToken(); // Avança para '=' ou ';'

            // Se for uma declaração com inicialização
            if (tokenAtual.getNome().equals("=")) {
                proximoToken(); // Avança para o valor

                if (tipoAtual.equalsIgnoreCase("final")) {
                    tipoAtual = tokenAtual.getTipo();
                    declarado.setTipo(tipoAtual); // Define o tipo de dado real (int, string, etc.)
                }

                esperarAtribuicao(declarado);
                proximoToken();
            }

            proximoToken();
            verificarDeclaracoes();
        }
    }

    /**
     * Percorre o corpo do programa (após as declarações) para validar o uso de
     * variáveis.
     * Verifica atribuições e expressões em estruturas de controle.
     */
    private void verificarAtribuicoes() throws ExcecaoCompilador {
        proximoToken();

        // Se encontrar um identificador, pode ser o início de uma atribuição.
        if (tokenAtual.getClassificacao().equalsIgnoreCase("id")) {
            if (!isDeclarado()) {
                GerenciadorErros.erroSemanticoNaoDeclarado(tokenAtual);
            }
            verificarAtribuicao();
        }

        // Se for uma estrutura de controle, a expressão seguinte deve ser booleana.
        if (tokenAtual.getNome().equalsIgnoreCase("while") || tokenAtual.getNome().equalsIgnoreCase("if")) {
            tipoAtual = "boolean"; // O tipo esperado para a expressão é 'boolean'.
            verificarExpressaoBooleana();
        }

        // Continua a verificação até o final da tabela de símbolos.
        if (indiceTokenAtual < tabelaSimbolos.getTamanho() - 1) {
            verificarAtribuicoes();
        }
    }

    // Valida uma única instrução de atribuição.
    private void verificarAtribuicao() throws ExcecaoCompilador {
        // Loop para verificar se o token atual é uma constante.
        for (String constName : nomesConstantes) {
            if (constName.equalsIgnoreCase(tokenAtual.getNome())) {
                GerenciadorErros.erroSemanticoAtribuicaoConstante(tokenAtual);
            }
        }

        for (Token declarado : tokensDeclarados) {
            // Encontra a declaração correspondente ao ID atual.
            if (declarado.getNome().equalsIgnoreCase(tokenAtual.getNome())) {
                tipoAtual = declarado.getTipo(); // Define o tipo esperado para a expressão.
                proximoToken(); // Avança para o '='

                if (tokenAtual.getNome().equals("=")) {
                    proximoToken(); // Avança para o início da expressão.

                    // Se for uma atribuição simples (ex: x = 10;), a validação é mais direta.
                    if (isValorSimples()) {
                        esperarAtribuicao(declarado);
                        proximoToken();
                    } else {
                        // Se for uma expressão complexa (ex: x = 5 * y;), valida a expressão inteira.
                        validarExpressao();
                    }
                }
            }
        }
    }

    // Verifica se uma atribuição é de um valor simples (sem operadores).
    private boolean isValorSimples() {
        proximoToken();
        boolean isEnd = tokenAtual.getNome().equals(";");
        tokenAnterior();
        return isEnd;
    }

    // Valida uma expressão complexa, garantindo que o tipo resultante seja o
    // esperado.
    private void validarExpressao() throws ExcecaoCompilador {
        String tipoResultado = avaliarExpressao(); // Calcula o tipo resultante da expressão.

        // Compara o tipo resultante com o tipo da variável que recebe a atribuição.
        if (!tipoResultado.equalsIgnoreCase(tipoAtual)) {
            GerenciadorErros.erroSemanticoExpressaoInvalida(tipoAtual, tipoResultado, tokenAtual);
        }
    }

    /**
     * Avalia uma expressão e retorna seu tipo resultante.
     * Lida com operadores aritméticos.
     */
    private String avaliarExpressao() throws ExcecaoCompilador {
        String tipoEsquerda;

        if (tokenAtual.getNome().equals("(")) { // Expressão entre parênteses
            proximoToken();
            tipoEsquerda = avaliarExpressao();
            proximoToken(); // consome ')'
        } else if (tokenAtual.isConstOuId()) { // Valor ou variável
            tipoEsquerda = tokenAtual.getTipo();
            proximoToken();
        } else {
            GerenciadorErros.erroSemanticoTokenInvalido(tokenAtual);
            return "unknown";
        }

        // Loop para lidar com operadores
        while (!isFimExpressao()) {
            boolean isArith = tokenAtual.isOperadorAritmetico();
            boolean isLogic = tokenAtual.isOperadorLogico();

            proximoToken();
            String tipoDireita = avaliarExpressao(); // Avalia o lado direito recursivamente.

            if (isArith) {
                // Para operações aritméticas, ambos os operandos devem ser 'int'.
                if (!tipoEsquerda.equals("int") || !tipoDireita.equals("int")) {
                    GerenciadorErros.erroSemanticoExpressaoInvalida("int", tipoDireita, tokenAtual);
                }
                tipoEsquerda = "int"; // O resultado de uma operação aritmética é 'int'.
            } else if (isLogic) {
                // Para operações lógicas, ambos devem ser 'int' ou 'boolean' (dependendo do
                // operador).
                if (!tipoEsquerda.equals("int") || !tipoDireita.equals("int")) {
                    GerenciadorErros.erroSemanticoExpressaoInvalida("boolean", tipoDireita, tokenAtual);
                }
                tipoEsquerda = "boolean"; // O resultado é 'boolean'.
                break;
            }
        }

        return tipoEsquerda;
    }

    // Verifica se o token atual marca o fim de uma expressão.
    private boolean isFimExpressao() {
        String nome = tokenAtual.getNome();
        return nome.equals(";") || nome.equals(")") || nome.equals(",")
                || (!tokenAtual.isOperadorAritmetico() && !tokenAtual.isOperadorLogico());
    }

    // Valida a expressão de um 'if' ou 'while', garantindo que resulte em
    // 'boolean'.
    private void verificarExpressaoBooleana() throws ExcecaoCompilador {
        proximoToken(); // Avança para o início da expressão.
        String tipoResultado = analisarExpressaoAte("begin");
        if (!tipoResultado.equals("boolean")) {
            GerenciadorErros.erroSemanticoExpressaoInvalida("boolean", tipoResultado, tokenAtual);
        }
    }

    /**
     * Analisa uma expressão até encontrar um token de parada (como 'begin').
     * Usado para expressões de if/while.
     */
    private String analisarExpressaoAte(String tokenParada) throws ExcecaoCompilador {
        String tipoExpr = null;

        if (tokenAtual.isConstOuId()) {
            if (tokenAtual.getClassificacao().equalsIgnoreCase("id") && !isDeclarado()) {
                GerenciadorErros.erroSemanticoNaoDeclarado(tokenAtual);
            }
            tipoExpr = tokenAtual.getTipo();
            proximoToken();
        } else {
            GerenciadorErros.erroSemanticoExpressaoInvalidaAposControle(tokenAtual);
        }

        while (!tokenAtual.getNome().equalsIgnoreCase(tokenParada)) {
            String op = tokenAtual.getNome();
            boolean isLogic = tokenAtual.isOperadorLogico();
            boolean isArith = tokenAtual.isOperadorAritmetico();

            proximoToken();

            if (!tokenAtual.isConstOuId()) {
                GerenciadorErros.erroSemanticoEsperadoOperandoApos(op, tokenAtual);
            }

            String tipoDireita = tokenAtual.getTipo();

            if (isLogic) {
                // Operadores lógicos (and, or) podem operar em booleanos.
                // Operadores relacionais (==, <, >) operam em inteiros e resultam em booleano.
                if (!(tipoExpr.equals("int") && tipoDireita.equals("int")) &&
                        !(tipoExpr.equals("boolean") && tipoDireita.equals("boolean"))) {
                    GerenciadorErros.erroSemanticoExpressaoInvalida(tipoExpr, tipoDireita, tokenAtual);
                }
                tipoExpr = "boolean"; // O resultado final é sempre booleano.
            } else if (isArith) {
                if (!tipoExpr.equals("int") || !tipoDireita.equals("int")) {
                    GerenciadorErros.erroSemanticoExpressaoInvalida("int", tipoDireita, tokenAtual);
                }
                tipoExpr = "int"; // O resultado intermediário é inteiro.
            }

            proximoToken();
        }

        return tipoExpr;
    }

    /**
     * Após as declarações serem processadas, esta função percorre a tabela de
     * símbolos inteira
     * e atualiza o tipo de cada token de identificador com o tipo que foi
     * determinado
     * na análise de declaração.
     */
    private void atualizarTiposSimbolos() {
        // Para cada variável/constante que foi declarada
        for (Token declarado : tokensDeclarados) {
            // percorre toda a tabela de símbolos.
            for (int i = 0; i < tabelaSimbolos.getTamanho(); i++) {
                Token simbolo = tabelaSimbolos.tokenAtual(i);
                // Se encontrar um uso dessa variável
                if (declarado.getNome().equalsIgnoreCase(simbolo.getNome())) {
                    // atualize seu tipo.
                    simbolo.setTipo(declarado.getTipo());
                }
            }
        }
    }
}