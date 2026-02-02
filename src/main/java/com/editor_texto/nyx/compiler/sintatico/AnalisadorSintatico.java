package com.editor_texto.nyx.compiler.sintatico;

import com.editor_texto.nyx.compiler.erros.ExcecaoCompilador;
import com.editor_texto.nyx.compiler.erros.GerenciadorErros;
import com.editor_texto.nyx.compiler.modelo.Token;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;

/**
 * Realiza a análise sintática (parsing) da sequência de tokens.
 * Verifica se a estrutura do programa está em conformidade com a gramática da
 * linguagem LC.
 * Utiliza a abordagem de "Recursive Descent Parsing".
 */
public class AnalisadorSintatico {

    private final TabelaSimbolos tabelaSimbolos;
    private Token tokenAtual;
    private int indiceTokenAtual = 0;

    public AnalisadorSintatico(TabelaSimbolos tabelaSimbolos) {
        this.tabelaSimbolos = tabelaSimbolos;
        // Pega o primeiro token para iniciar a análise.
        this.tokenAtual = tabelaSimbolos.tokenAtual(indiceTokenAtual);
    }

    // Avança para o próximo token na tabela de símbolos.
    private void proximoToken() {
        if (indiceTokenAtual < tabelaSimbolos.getTamanho() - 1) {
            tokenAtual = tabelaSimbolos.tokenAtual(++indiceTokenAtual);
        }
    }

    /**
     * Valida se a classificação do token atual é a esperada.
     * Lança um erro sintático se não for.
     */
    private void esperarClassificacao(String esperado) throws ExcecaoCompilador {
        if (!tokenAtual.getClassificacao().equalsIgnoreCase(esperado) &&
                !tokenAtual.getNome().equalsIgnoreCase(esperado)) {
            GerenciadorErros.erroSintatico(esperado, tokenAtual);
        }
    }

    /**
     * Valida se o nome (lexema) do token atual é o esperado.
     * Lança um erro sintático se não for.
     */
    private void esperarNome(String esperado) throws ExcecaoCompilador {
        if (!tokenAtual.getNome().equalsIgnoreCase(esperado)) {
            GerenciadorErros.erroSintatico(esperado, tokenAtual);
        }
    }

    /**
     * Ponto de entrada do parser. Inicia a análise da estrutura geral do programa.
     * Gramática: Programa -> Declarações Bloco
     */
    public void analisarPrograma() throws ExcecaoCompilador {
        analisarDeclaracoes();
        analisarBloco();
    }

    /**
     * Analisa a seção de declarações de variáveis e constantes.
     * Gramática: Declarações -> (Declaração_Var | Declaração_Const) Declarações | ε
     */
    private void analisarDeclaracoes() throws ExcecaoCompilador {
        // Verifica se o token atual pode iniciar uma declaração.
        if (tokenAtual.isTipoPrimitivo() || tokenAtual.getNome().equalsIgnoreCase("final")) {
            // Consome o tipo (int, byte, final, etc.)
            proximoToken();
            esperarClassificacao("id"); // Espera um identificador.

            proximoToken();
            // Verifica se há uma inicialização opcional.
            if (tokenAtual.getNome().equalsIgnoreCase("=")) {
                proximoToken();
                if (!tokenAtual.isConstOuId()) { // O valor deve ser uma constante ou outro id.
                    GerenciadorErros.erroSintaticoAtribuicao(tokenAtual);
                }
                proximoToken();
            }

            esperarNome(";"); // Toda declaração termina com ';'.
            proximoToken();

            // Chamada recursiva para analisar múltiplas declarações.
            analisarDeclaracoes();
        }
    }

    /**
     * Analisa um bloco de comandos.
     * Gramática: Bloco -> 'begin' Comandos 'end'
     */
    private void analisarBloco() throws ExcecaoCompilador {
        esperarNome("begin");
        proximoToken();
        analisarComandos();
        esperarNome("end");
        // Avança o token após o 'end' se não for o final do arquivo.
        if (indiceTokenAtual < tabelaSimbolos.getTamanho() - 1) {
            proximoToken();
        }
    }

    /**
     * Analisa uma sequência de comandos dentro de um bloco.
     * Gramática: Comandos -> Comando Comandos | ε
     */
    private void analisarComandos() throws ExcecaoCompilador {
        // A condição de parada é encontrar o 'end' do bloco.
        if (!tokenAtual.getNome().equalsIgnoreCase("end")) {
            analisarComando();
            analisarComandos(); // Recursão para analisar o próximo comando.
        }
    }

    /**
     * Analisa um único comando, delegando para o método específico.
     * Gramática: Comando -> Comando_Atrib | Comando_IO | Comando_Cond | Comando_Rep
     * | Bloco
     */
    private void analisarComando() throws ExcecaoCompilador {
        String nome = tokenAtual.getNome().toLowerCase();

        switch (nome) {
            case "write":
            case "writeln":
                analisarWrite();
                break;
            case "readln":
                analisarReadln();
                break;
            case "while":
                analisarWhile();
                break;
            case "if":
                analisarIf();
                break;
            case "else":
                analisarElse();
                break;
            case "begin":
                analisarBloco(); // Um bloco pode conter outros blocos.
                break;
            default:
                // Se não for uma palavra-chave de comando, deve ser uma atribuição (que começa
                // com um id).
                if (tokenAtual.getClassificacao().equalsIgnoreCase("id")) {
                    analisarAtribuicao();
                } else {
                    GerenciadorErros.erroSintatico("um comando válido", tokenAtual);
                }
        }
    }

    // Analisa os comandos de escrita (write/writeln).
    private void analisarWrite() throws ExcecaoCompilador {
        proximoToken(); // Consome 'write' ou 'writeln'
        analisarConcatenacaoString(); // Analisa a lista de expressões a serem impressas.
        esperarNome(";");
        proximoToken();
    }

    // Analisa a lista de expressões para os comandos de escrita.
    private void analisarConcatenacaoString() throws ExcecaoCompilador {
        esperarNome(","); // A lista de expressões é separada por vírgula.
        proximoToken();

        if (!tokenAtual.isConstOuId()) {
            GerenciadorErros.erroSintaticoAtribuicao(tokenAtual);
        }
        proximoToken();
        // Verifica se há mais expressões na lista.
        analisarConcatenacaoStringCauda();
    }

    // Analisa a "cauda" (continuação) de uma lista de expressões de escrita.
    private void analisarConcatenacaoStringCauda() throws ExcecaoCompilador {
        if (tokenAtual.getNome().equalsIgnoreCase(",")) {
            analisarConcatenacaoString(); // Se encontrar outra vírgula, analisa a próxima expressão.
        }
    }

    // Analisa o comando de leitura (readln).
    private void analisarReadln() throws ExcecaoCompilador {
        proximoToken(); // Consome 'readln'
        esperarNome(",");
        proximoToken();
        esperarClassificacao("ID"); // Espera um identificador de variável.
        proximoToken();
        esperarNome(";");
        proximoToken();
    }

    /**
     * Analisa um comando de atribuição.
     * Gramática: Atribuição -> id '=' Expressão ';'
     */
    private void analisarAtribuicao() throws ExcecaoCompilador {
        proximoToken(); // Consome o 'id'
        esperarNome("=");
        proximoToken();
        analisarExpressao(false); // Analisa a expressão à direita. 'false' impede expressões lógicas aqui.
        esperarNome(";");
        proximoToken();
    }

    // Ponto de entrada para análise de qualquer tipo de expressão.
    private void analisarExpressao(boolean permitirLogica) throws ExcecaoCompilador {
        // Delega para o método de expressão lógica, que tem maior precedência.
        analisarExpressaoLogica(permitirLogica);
    }

    /**
     * Analisa expressões lógicas (com 'not', 'and', 'or').
     * A estrutura segue a ordem de precedência.
     */
    private void analisarExpressaoLogica(boolean permitirLogica) throws ExcecaoCompilador {
        // 'not' tem alta precedência.
        if (tokenAtual.getNome().equalsIgnoreCase("not")) {
            proximoToken();
            analisarExpressaoLogica(permitirLogica);
            return;
        }

        analisarExpressaoAritmetica(permitirLogica);

        if (tokenAtual.isOperadorLogico()) {
            if (!permitirLogica) {
                // Não se pode ter 'and' ou 'or' em uma atribuição normal.
                GerenciadorErros.erroSintaticoAtribuicaoExpressaoLogica(this.tokenAtual);
            } else {
                proximoToken();
                analisarExpressaoAritmetica(permitirLogica);
            }
        }
    }

    // Os métodos a seguir (analisarExpressaoAritmetica, analisarTermo,
    // analisarFator) implementam
    // a análise de expressões aritméticas com a precedência correta:
    // 1. Fator (números, variáveis, expressões entre parênteses)
    // 2. Termo (multiplicação e divisão)
    // 3. Expressão Aritmética (adição e subtração)

    private void analisarExpressaoAritmetica(boolean permitirLogica) throws ExcecaoCompilador {
        analisarTermo(permitirLogica);
        analisarExpressaoAritmeticaCauda(permitirLogica);
    }

    private void analisarExpressaoAritmeticaCauda(boolean permitirLogica) throws ExcecaoCompilador {
        if (tokenAtual.getNome().equalsIgnoreCase("+") ||
                tokenAtual.getNome().equalsIgnoreCase("-")) {
            proximoToken();
            analisarTermo(permitirLogica);
            analisarExpressaoAritmeticaCauda(permitirLogica); // Recursão para lidar com múltiplos operadores
        }
    }

    private void analisarTermo(boolean permitirLogica) throws ExcecaoCompilador {
        analisarFator(permitirLogica);
        analisarTermoCauda(permitirLogica);
    }

    private void analisarTermoCauda(boolean permitirLogica) throws ExcecaoCompilador {
        if (tokenAtual.getNome().equalsIgnoreCase("*") ||
                tokenAtual.getNome().equalsIgnoreCase("/")) {
            proximoToken();
            analisarFator(permitirLogica);
            analisarTermoCauda(permitirLogica); // Recursão
        }
    }

    // Analisa o nível mais fundamental de uma expressão: um valor, uma variável ou
    // outra expressão entre parênteses.
    private void analisarFator(boolean permitirLogica) throws ExcecaoCompilador {
        if (tokenAtual.isConstOuId() || tokenAtual.getTipo().equalsIgnoreCase("boolean")) {
            proximoToken();
        } else if (tokenAtual.getNome().equalsIgnoreCase("(")) { // Trata expressões entre parênteses.
            proximoToken();
            analisarExpressao(permitirLogica); // Analisa a expressão interna.
            esperarNome(")");
            proximoToken();
        } else {
            GerenciadorErros.erroSintatico("CONST, ID ou EXPRESSÃO entre parênteses", tokenAtual);
        }
    }

    // Analisa um comando 'if'.
    private void analisarIf() throws ExcecaoCompilador {
        proximoToken(); // consome 'if'
        analisarExpressao(true); // A condição do 'if' deve ser uma expressão lógica.
        analisarBloco(); // O corpo do 'if' é um bloco.
    }

    // Analisa um comando 'while'.
    private void analisarWhile() throws ExcecaoCompilador {
        proximoToken(); // consome 'while'
        analisarExpressao(true); // A condição do 'while' deve ser uma expressão lógica.
        analisarBloco(); // O corpo do 'while' é um bloco.
    }

    // Analisa a cláusula 'else'.
    private void analisarElse() throws ExcecaoCompilador {
        proximoToken(); // consome 'else'
        analisarBloco();
    }
}