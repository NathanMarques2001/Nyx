package com.editor_texto.nyx.compiler.geracao;

import com.editor_texto.nyx.compiler.modelo.Token;
import com.editor_texto.nyx.compiler.semantico.TabelaSimbolos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Responsável por gerar o código Assembly (MASM) a partir da tabela de
 * símbolos.
 * Esta é a fase final do compilador, traduzindo as estruturas da linguagem
 * fonte
 * para instruções de máquina de baixo nível.
 */
public class GeradorAssembly {

    private final TabelaSimbolos tabelaSimbolos;
    private Token tokenAtual;
    private int indiceTokenAtual = 0;

    // Caminho e nome do arquivo de saída.
    private final String diretorioSaida;
    private String nomeArquivo = "";

    // StringBuilders para montar as diferentes seções do arquivo Assembly.
    private final StringBuilder secaoCabecalho = new StringBuilder(); // Cabeçalho e includes.
    private final StringBuilder secaoDados = new StringBuilder(); // Seção .data para variáveis e constantes.
    private final StringBuilder secaoCodigo = new StringBuilder(); // Seção .code para o código executável.

    // Contadores para gerar rótulos (labels) únicos.
    private int contadorString = 1;
    private int contadorLoop = 1;
    private int contadorIf = 1;

    // Flag para controlar a declaração de formatos de `scanf` e evitar duplicação.
    private boolean formatoDSDeclarado = false;

    // Construtor que inicializa o gerador com a tabela de símbolos e o nome do
    // arquivo de saída.
    public GeradorAssembly(TabelaSimbolos tabelaSimbolos, String nomeArquivo, String diretorioSaida) {
        this.tabelaSimbolos = tabelaSimbolos;
        this.nomeArquivo = nomeArquivo + ".asm";
        this.diretorioSaida = diretorioSaida;
        if (this.tabelaSimbolos.getTamanho() > 0) {
            this.tokenAtual = this.tabelaSimbolos.tokenAtual(this.indiceTokenAtual);
        } else {
            this.tokenAtual = null;
        }
    }

    // Avança para o próximo token na tabela de símbolos.
    private void proximoToken() {
        this.indiceTokenAtual++;
        if (this.indiceTokenAtual < this.tabelaSimbolos.getTamanho()) {
            this.tokenAtual = this.tabelaSimbolos.tokenAtual(this.indiceTokenAtual);
        } else {
            this.tokenAtual = null; // Fim da tabela de símbolos.
        }
    }

    // Método auxiliar para verificar se uma string é um operador relacional.
    private boolean isOperadorRelacional(String op) {
        if (op == null)
            return false;
        return op.equals("==") || op.equals("<>") || op.equals("<") || op.equals(">") || op.equals("<=")
                || op.equals(">=");
    }

    // Cria o diretório de saída para os arquivos .asm, se ele não existir.
    private void criarDiretorioSaida() {
        File outDir = new File(this.diretorioSaida);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
    }

    // Escreve o conteúdo final do código Assembly em um arquivo .asm.
    private void escreverCodigoAssembly(String codigoAssembly) {
        File arqAsm = new File(this.diretorioSaida, this.nomeArquivo);
        try (FileWriter escritor = new FileWriter(arqAsm)) {
            escritor.write(codigoAssembly);
            // Removendo print de sucesso para manter output limpo conforme solicitado
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo '" + this.nomeArquivo + "': " + e.getMessage());
        }
    }

    // Ponto de entrada público para iniciar o processo de conversão para Assembly.
    public void gerar() {
        this.criarDiretorioSaida();
        this.gerarCodigoAssembly();
    }

    // Orquestra a geração das seções do código Assembly e escreve o resultado no
    // arquivo.
    private void gerarCodigoAssembly() {
        StringBuilder codigoAssembly = new StringBuilder();

        // Gera cada seção separadamente.
        this.gerarCabecalho();
        this.gerarSecaoDados();
        this.gerarSecaoCodigo();

        // Concatena todas as seções para formar o arquivo final.
        codigoAssembly.append(this.secaoCabecalho);
        codigoAssembly.append(this.secaoDados);
        codigoAssembly.append(this.secaoCodigo);

        this.escreverCodigoAssembly(codigoAssembly.toString());
    }

    /**
     * Gera o cabeçalho padrão para um executável MASM de 32 bits para Windows,
     * incluindo as bibliotecas necessárias para operações de I/O.
     */
    private void gerarCabecalho() {
        this.secaoCabecalho.append(".686\n")
                .append(".model flat, stdcall\n")
                .append("option casemap :none\n\n")
                .append("include \\masm32\\include\\windows.inc\n")
                .append("include \\masm32\\include\\kernel32.inc\n")
                .append("include \\masm32\\include\\masm32.inc\n")
                .append("include \\masm32\\include\\msvcrt.inc\n")
                .append("includelib \\masm32\\lib\\kernel32.lib\n")
                .append("includelib \\masm32\\lib\\masm32.lib\n")
                .append("includelib \\masm32\\lib\\msvcrt.lib\n")
                .append("include \\masm32\\macros\\macros.asm\n\n");
    }

    /**
     * Gera a seção .data, percorrendo a tabela de símbolos para encontrar
     * declarações de variáveis e constantes e alocando espaço para elas.
     */
    private void gerarSecaoDados() {
        this.secaoDados.append(".data\n");
        int indiceOriginal = this.indiceTokenAtual; // Salva a posição atual.
        this.indiceTokenAtual = 0; // Reseta para o início da tabela.
        if (this.tabelaSimbolos.getTamanho() > 0) {
            this.tokenAtual = this.tabelaSimbolos.tokenAtual(this.indiceTokenAtual);
            // Itera apenas sobre a parte de declarações do código.
            while (this.tokenAtual != null && isEscopoDeclaracao()) {
                identificarDeclaracao();
            }
        }
        // Restaura a posição original para a geração da seção de código.
        this.indiceTokenAtual = indiceOriginal;
        if (this.indiceTokenAtual < this.tabelaSimbolos.getTamanho()) {
            this.tokenAtual = this.tabelaSimbolos.tokenAtual(this.indiceTokenAtual);
        } else {
            this.tokenAtual = null;
        }
    }

    // Itera sobre as declarações de variáveis e constantes e as traduz para
    // diretivas MASM.
    private void identificarDeclaracao() {
        // Trata declarações de constantes (final).
        if (this.tokenAtual.getNome().equalsIgnoreCase("final")) {
            proximoToken(); // Consome 'final'.
            String nomeConst = this.tokenAtual.getNome();
            proximoToken(); // Consome o nome da constante.
            proximoToken(); // Consome '='.
            String valorConst = this.tokenAtual.getNome();

            // Constantes string são declaradas como 'db' e seu endereço é atribuído com
            // 'equ'.
            if (valorConst.startsWith("\"")) {
                String stringReal = valorConst.substring(1, valorConst.length() - 1);
                String labelStr = "const_str_" + nomeConst;
                this.secaoDados.append(String.format("    %-15s db \"%s\", 0\n", labelStr, stringReal));
                this.secaoDados.append(String.format("    %-15s equ addr %s\n", nomeConst, labelStr));
            } else { // Constantes numéricas são diretamente traduzidas com 'equ'.
                this.secaoDados
                        .append(String.format("    %-15s equ %s\n", nomeConst, formatarValor(valorConst, "int")));
            }
            proximoToken(); // Consome o valor.
            proximoToken(); // Consome ';'.
        } else {
            String tipo = this.tokenAtual.getNome();
            String tipoDadosMASM = tipoPrimitivoMASM(tipo);
            proximoToken(); // Consome o tipo.

            // Loop para tratar múltiplas declarações na mesma linha (ex: int a, b;).
            while (this.tokenAtual != null && !this.tokenAtual.getNome().equals(";")) {
                String nomeDado = this.tokenAtual.getNome();
                proximoToken(); // Consome o nome da variável.
                String valorDado = "0"; // Valor padrão para variáveis não inicializadas.

                // Strings são alocadas com um buffer de 256 bytes.
                if (tipo.equalsIgnoreCase("string")) {
                    this.secaoDados.append(String.format("    %-15s db 256 dup(0)\n", nomeDado));
                } else {
                    // Verifica se há uma inicialização de valor.
                    if (this.tokenAtual.getNome().equals("=")) {
                        proximoToken(); // Consome '='.
                        valorDado = formatarValor(this.tokenAtual.getNome(), tipo);
                        proximoToken(); // Consome o valor.
                    }
                    this.secaoDados.append(String.format("    %-15s %-5s %s\n", nomeDado, tipoDadosMASM, valorDado));
                }

                if (this.tokenAtual != null && this.tokenAtual.getNome().equals(",")) {
                    proximoToken(); // Consome ','.
                }
            }
            if (this.tokenAtual != null && this.tokenAtual.getNome().equals(";")) {
                proximoToken(); // Consome ';'.
            }
        }
    }

    // Gera a seção .code, onde a lógica do programa é traduzida em instruções.
    private void gerarSecaoCodigo() {
        this.secaoCodigo.append(".code\n").append("start:\n");
        this.iniciarGeracao();
        // Finaliza o programa chamando a função ExitProcess.
        this.secaoCodigo.append("\n    invoke ExitProcess, 0\n").append("end start\n");
    }

    // Inicia a geração de código a partir do bloco principal 'begin'.
    private void iniciarGeracao() {
        // Avança todos os tokens da fase de declaração até encontrar 'begin'.
        while (this.tokenAtual != null && !this.tokenAtual.getNome().equalsIgnoreCase("begin")) {
            proximoToken();
        }
        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("begin")) {
            proximoToken(); // Consome 'begin'.
        }

        // Processa todos os comandos dentro do bloco principal.
        while (this.tokenAtual != null && !this.tokenAtual.getNome().equalsIgnoreCase("end")) {
            identificarComandos();
        }
        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("end")) {
            proximoToken(); // Consome 'end'.
        }
    }

    /**
     * Identifica o comando atual e delega para o método de geração apropriado.
     * Atua como um dispatcher para os diferentes comandos da linguagem.
     */
    private void identificarComandos() {
        if (this.tokenAtual == null)
            return;

        switch (this.tokenAtual.getNome().toLowerCase()) {
            case "write":
            case "writeln":
                identificarWrite();
                break;
            case "readln":
                identificarRead();
                break;
            case "while":
                identificarWhile();
                break;
            case "if":
                identificarIf();
                break;
            case ";":
                proximoToken(); // Ignora comandos nulos (ponto e vírgula extra).
                break;
            default:
                // Se não for uma palavra-chave, assume que é uma atribuição (que começa com um
                // ID).
                if (this.tokenAtual.getClassificacao().equalsIgnoreCase("ID")) {
                    identificarAtribuicao();
                } else {
                    // Ignora tokens inesperados que não sejam 'end'.
                    if (!this.tokenAtual.getNome().equalsIgnoreCase("end")) {
                        proximoToken();
                    }
                }
        }
    }

    // Gera código Assembly para os comandos 'write' e 'writeln' usando crt_printf.
    private void identificarWrite() {
        boolean quebraLinha = this.tokenAtual.getNome().equalsIgnoreCase("writeln");
        proximoToken(); // Consome 'write' ou 'writeln'.
        proximoToken(); // Consome ','.

        StringBuilder stringFormato = new StringBuilder(); // String de formato para printf (ex: "%d %s").
        ArrayList<String> args = new ArrayList<>(); // Argumentos para printf.

        // Constrói a string de formato e a lista de argumentos.
        while (this.tokenAtual != null && !this.tokenAtual.getNome().equals(";")) {
            if (this.tokenAtual.getClassificacao().equalsIgnoreCase("ID")) {
                String nomeVar = this.tokenAtual.getNome();
                String tipoVar = this.tabelaSimbolos.getTipoSimbolo(nomeVar);
                if (tipoVar == null)
                    tipoVar = "int"; // Fallback para tipo desconhecido.

                if (tipoVar.equalsIgnoreCase("string")) {
                    stringFormato.append("%s");
                    args.add("addr " + nomeVar); // Para strings, passamos o endereço.
                } else { // Trata literais de string no meio do write.
                    stringFormato.append("%d");
                    args.add(nomeVar); // Para outros tipos, passamos o valor.
                }
            } else {
                String literal = this.tokenAtual.getNome().replace("\"", "").replace("'", "");
                stringFormato.append(literal);
            }
            proximoToken();
            if (this.tokenAtual != null && this.tokenAtual.getNome().equals(",")) {
                proximoToken(); // Consome a vírgula entre os argumentos.
            }
        }

        if (this.tokenAtual != null && this.tokenAtual.getNome().equals(";")) {
            proximoToken(); // Consome o ';' final.
        }

        // Declara a string de formato na seção .data.
        String labelDados = "str" + this.contadorString++;
        String finalLinha = quebraLinha ? ", 13, 10, 0" : ", 0"; // Adiciona quebra de linha para writeln.
        this.secaoDados
                .append(String.format("    %-15s db \"%s\"%s\n", labelDados, stringFormato.toString(), finalLinha));

        // Gera a chamada para a função printf.
        this.secaoCodigo.append("    invoke crt_printf, addr ").append(labelDados);
        for (String arg : args) {
            this.secaoCodigo.append(", ").append(arg);
        }
        this.secaoCodigo.append("\n");
    }

    // Gera código Assembly para o comando 'readln' usando crt_scanf ou crt_gets.
    private void identificarRead() {
        proximoToken(); // Consome 'readln'.
        proximoToken(); // Consome ','.
        String nomeVariavel = this.tokenAtual.getNome();
        String tipoVar = this.tabelaSimbolos.getTipoSimbolo(nomeVariavel);

        // Usa crt_scanf para tipos numéricos e booleanos.
        if (tipoVar != null && (tipoVar.equalsIgnoreCase("int") || tipoVar.equalsIgnoreCase("byte")
                || tipoVar.equalsIgnoreCase("boolean"))) {
            // Declara a string de formato "%d" uma única vez.
            if (!this.formatoDSDeclarado) {
                this.secaoDados.append(String.format("    %-15s db \"%%d\", 0\n", "format_d"));
                this.formatoDSDeclarado = true;
            }
            this.secaoCodigo.append("    invoke crt_scanf, addr format_d, addr ").append(nomeVariavel).append("\n");
        } else { // Usa crt_gets para ler strings.
            this.secaoCodigo.append("    invoke crt_gets, addr ").append(nomeVariavel).append("\n");
        }

        proximoToken(); // Consome o nome da variável.
        if (this.tokenAtual != null && this.tokenAtual.getNome().equals(";")) {
            proximoToken(); // Consome ';'.
        }
    }

    // Gera a estrutura de um loop 'while' em Assembly, com labels e saltos.
    private void identificarWhile() {
        int contadorLoopLocal = this.contadorLoop++;
        String labelLoop = "_loop" + contadorLoopLocal;
        String labelFimLoop = "_fimLoop" + contadorLoopLocal;

        this.secaoCodigo.append("\n").append(labelLoop).append(":\n"); // Label de início do loop.
        proximoToken(); // Consome 'while'.

        // Gera o código para a condição. O salto para o fim do loop ocorrerá se a
        // condição for falsa.
        gerarExpressaoCondicional(labelFimLoop, true);

        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("begin")) {
            proximoToken(); // Consome 'begin'.
        }

        // Gera o código para o corpo do loop.
        while (this.tokenAtual != null && !this.tokenAtual.getNome().equalsIgnoreCase("end")) {
            identificarComandos();
        }

        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("end")) {
            proximoToken(); // Consome 'end'.
        }

        this.secaoCodigo.append("\n    jmp ").append(labelLoop).append("\n"); // Salta de volta para o início do loop.
        this.secaoCodigo.append(labelFimLoop).append(":\n"); // Label de saída do loop.
    }

    // Gera a estrutura de um condicional 'if-else' em Assembly.
    private void identificarIf() {
        int contadorIfLocal = this.contadorIf++;
        String labelElse = "_else" + contadorIfLocal;
        String labelFimIf = "_fimIf" + contadorIfLocal;

        proximoToken(); // Consome 'if'.
        // Gera a condição. Se for falsa, salta para o bloco 'else' (ou para o fim do
        // 'if').
        gerarExpressaoCondicional(labelElse, true);

        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("begin")) {
            proximoToken();
        }

        // Processa o corpo do IF
        while (this.tokenAtual != null && !this.tokenAtual.getNome().equalsIgnoreCase("end")
                && !this.tokenAtual.getNome().equalsIgnoreCase("else")) {
            identificarComandos();
        }

        // Verifica se temos um bloco else
        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("else")) {
            // Se o bloco IF foi executado, salta sobre o bloco ELSE.
            this.secaoCodigo.append("    jmp ").append(labelFimIf).append("\n");
            this.secaoCodigo.append(labelElse).append(":\n");
            proximoToken(); // Consome 'else'.
            if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("begin")) {
                proximoToken(); // Consome 'begin' do else.
            }
            // Processa o corpo do ELSE.
            while (this.tokenAtual != null && !this.tokenAtual.getNome().equalsIgnoreCase("end")) {
                identificarComandos();
            }
            if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("end")) {
                proximoToken(); // Consome o 'end' do ELSE
            }
            this.secaoCodigo.append(labelFimIf).append(":\n");
        } else {
            // Sem bloco else, o labelElse é o fim do IF
            this.secaoCodigo.append(labelElse).append(":\n");
        }

        // Consome o 'end' que fecha a estrutura IF (ou IF-ELSE aninhado).
        if (this.tokenAtual != null && this.tokenAtual.getNome().equalsIgnoreCase("end")) {
            proximoToken();
        }
    }

    // Gera código para uma expressão condicional, resultando em um salto.
    private void gerarExpressaoCondicional(String labelAlvo, boolean saltarSeFalso) {
        String primeiroOperando = this.tokenAtual.getNome();
        String tipo = tabelaSimbolos.getTipoSimbolo(primeiroOperando);
        proximoToken(); // Consome o primeiro operando.

        // Verifica se é uma comparação explícita (ex: n >= 10).
        if (isOperadorRelacional(this.tokenAtual.getNome())) {
            String operador = this.tokenAtual.getNome();
            proximoToken();
            String segundoOperando = this.tokenAtual.getNome();
            proximoToken();

            // Usa 'eax' (32 bits) para inteiros, 'al' (8 bits) para bytes/booleanos.
            String reg = "eax";
            if (tipo != null && (tipo.equalsIgnoreCase("boolean") || tipo.equalsIgnoreCase("byte"))) {
                reg = "al";
            }

            // Carrega os operandos, compara e salta.
            this.secaoCodigo.append("    mov ").append(reg).append(", ").append(formatarValor(primeiroOperando, tipo))
                    .append("\n");
            this.secaoCodigo.append("    cmp ").append(reg).append(", ").append(formatarValor(segundoOperando, tipo))
                    .append("\n");

            String instrucaoSalto = obterInstrucaoSalto(operador, saltarSeFalso);
            this.secaoCodigo.append("    ").append(instrucaoSalto).append(" ").append(labelAlvo).append("\n");

        } else { // Trata comparações booleanas implícitas (ex: while naoTerminou).
            String reg = "al"; // Booleans são sempre bytes.

            this.secaoCodigo.append("    mov ").append(reg).append(", ").append(primeiroOperando).append("\n");
            this.secaoCodigo.append("    cmp ").append(reg).append(", 1\n"); // Compara com 'true' (1).

            String instrucaoSalto = saltarSeFalso ? "jne" : "je"; // jne: salta se não for verdadeiro.
            this.secaoCodigo.append("    ").append(instrucaoSalto).append(" ").append(labelAlvo).append("\n");
        }
    }

    // Mapeia um operador relacional para a instrução de salto condicional
    // correspondente em Assembly.
    private String obterInstrucaoSalto(String operador, boolean saltarSeFalso) {
        return switch (operador) {
            case "==" -> saltarSeFalso ? "jne" : "je";
            case "<>" -> saltarSeFalso ? "je" : "jne";
            case "<" -> saltarSeFalso ? "jge" : "jl";
            case ">" -> saltarSeFalso ? "jle" : "jg";
            case "<=" -> saltarSeFalso ? "jg" : "jle";
            case ">=" -> saltarSeFalso ? "jl" : "jge";
            default -> ""; // Caso inválido
        };
    }

    // Gera código para um comando de atribuição.
    private void identificarAtribuicao() {
        String nomeVariavel = this.tokenAtual.getNome();
        String tipoVar = this.tabelaSimbolos.getTipoSimbolo(nomeVariavel);
        if (tipoVar == null)
            tipoVar = "int"; // Fallback.

        proximoToken(); // Consome o nome da variável.
        proximoToken(); // Consome '='.

        // Atribuição de string usa a função crt_strcpy.
        if (tipoVar.equalsIgnoreCase("string")) {
            String literalString = this.tokenAtual.getNome();
            String valorStringReal;

            // Validação para remover aspas de forma segura.
            if (literalString.length() >= 2 && literalString.startsWith("\"") && literalString.endsWith("\"")) {
                valorStringReal = literalString.substring(1, literalString.length() - 1);
            } else {
                // Emite um aviso se o valor não for uma string entre aspas.
                System.err.println("[Aviso de Geração de Código] Atribuição para string '" + nomeVariavel
                        + "' com valor malformado: " + literalString);
                valorStringReal = ""; // Usa uma string vazia para evitar crash.
            }

            // Declara a string na seção .data e invoca a cópia.
            String labelStringDados = "str_assign_" + this.contadorString++;
            this.secaoDados.append(String.format("    %-15s db \"%s\", 0\n", labelStringDados, valorStringReal));
            this.secaoCodigo.append("    invoke crt_strcpy, addr ").append(nomeVariavel).append(", addr ")
                    .append(labelStringDados).append("\n");
            proximoToken();
        } else { // Para tipos numéricos/booleanos, avalia a expressão.
            ArrayList<Token> tokensExpressao = new ArrayList<>();
            while (this.tokenAtual != null && !this.tokenAtual.getNome().equals(";")) {
                tokensExpressao.add(this.tokenAtual);
                proximoToken();
            }
            avaliarExpressao(tokensExpressao);

            // O resultado da expressão está no topo da pilha do processador.
            this.secaoCodigo.append("    pop eax\n");
            // Move o resultado para a variável correta (8 bits para boolean/byte, 32 bits
            // para int).
            if (tipoVar.equalsIgnoreCase("boolean") || tipoVar.equalsIgnoreCase("byte")) {
                this.secaoCodigo.append("    mov ").append(nomeVariavel).append(", al\n");
            } else {
                this.secaoCodigo.append("    mov ").append(nomeVariavel).append(", eax\n");
            }
        }

        if (this.tokenAtual != null && this.tokenAtual.getNome().equals(";")) {
            proximoToken(); // Consome ';' final.
        }
    }

    /**
     * Avalia uma expressão aritmética infixa usando o algoritmo Shunting-yard
     * para gerar código Assembly em ordem pós-fixa (usando a pilha do processador).
     */
    private void avaliarExpressao(ArrayList<Token> tokens) {
        Stack<String> ops = new Stack<>(); // Pilha para operadores.

        for (Token token : tokens) {
            String nome = token.getNome();

            // Se o token for um operando (ID ou constante), empurra seu valor na pilha do
            // processador.
            if (token.getClassificacao().equalsIgnoreCase("ID") || token.getClassificacao().equalsIgnoreCase("CONST")
                    || nome.equalsIgnoreCase("true") || nome.equalsIgnoreCase("false")) {
                String valorParaEmpilhar = formatarValor(nome, token.getTipo());
                this.secaoCodigo.append("    push ").append(valorParaEmpilhar).append("\n");
            } else if (nome.equals("(")) { // Empilha parênteses de abertura.
                ops.push(nome);
            } else if (nome.equals(")")) { // Ao encontrar ')', desempilha operadores até encontrar '('.
                while (!ops.empty() && !ops.peek().equals("(")) {
                    gerarOp(ops.pop());
                }
                if (!ops.empty())
                    ops.pop(); // Descarta o '('.
            } else if (isOperador(nome)) { // Se for um operador aritmético...
                // Desempilha operadores com maior ou igual precedência antes de empilhar o
                // atual.
                while (!ops.empty() && terPrecedencia(ops.peek(), nome)) {
                    gerarOp(ops.pop());
                }
                ops.push(nome);
            }
        }
        // Desempilha e aplica os operadores restantes.
        while (!ops.empty()) {
            gerarOp(ops.pop());
        }
    }

    // Gera a instrução Assembly para um operador aritmético (+, -, *, /).
    private void gerarOp(String op) {
        // Retira os dois operandos do topo da pilha para os registradores.
        this.secaoCodigo.append("    pop ebx\n"); // Segundo operando.
        this.secaoCodigo.append("    pop eax\n"); // Primeiro operando.
        switch (op) {
            case "+" -> this.secaoCodigo.append("    add eax, ebx\n");
            case "-" -> this.secaoCodigo.append("    sub eax, ebx\n");
            case "*" -> this.secaoCodigo.append("    imul eax, ebx\n");
            case "/" -> {
                // Prepara para a divisão de 32 bits.
                this.secaoCodigo.append("    cdq\n"); // Estende o sinal de eax para edx.
                this.secaoCodigo.append("    idiv ebx\n"); // Quociente em eax, resto em edx.
            }
        }
        this.secaoCodigo.append("    push eax\n"); // Empurra o resultado de volta para a pilha.
    }

    // Verifica se o token atual está no escopo de declaração.
    private boolean isEscopoDeclaracao() {
        return this.tokenAtual != null
                && (isTipoPrimitivo() || this.tokenAtual.getNome().equalsIgnoreCase("final"));
    }

    // Formata um valor da linguagem fonte para o formato correto em Assembly.
    private String formatarValor(String valor, String tipo) {
        if (valor == null)
            return "0";
        if (tipo != null && tipo.equalsIgnoreCase("boolean")) {
            // Verificação para Fh (true) e 0h (false).
            if (valor.equalsIgnoreCase("true") || valor.equalsIgnoreCase("1") || valor.equalsIgnoreCase("Fh"))
                return "1";
            if (valor.equalsIgnoreCase("false") || valor.equalsIgnoreCase("0") || valor.equalsIgnoreCase("0h"))
                return "0";
        }
        // Converte o formato 0hXX para XXh para bytes.
        if (valor.toLowerCase().startsWith("0h") && tipo != null && tipo.equalsIgnoreCase("byte")) {
            return valor.substring(2) + "h";
        }
        return valor;
    }

    // Mapeia um tipo primitivo da linguagem para a diretiva de dados correspondente
    // do MASM.
    private String tipoPrimitivoMASM(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "int" -> "dd"; // Define Double Word (32 bits)
            case "boolean", "byte" -> "db"; // Define Byte (8 bits)
            default -> "";
        };
    }

    // Verifica se o token atual é um tipo primitivo da linguagem.
    private boolean isTipoPrimitivo() {
        if (this.tokenAtual == null)
            return false;
        String nome = this.tokenAtual.getNome().toLowerCase();
        return nome.equals("int") || nome.equals("string") || nome.equals("boolean") || nome.equals("byte");
    }

    // Verifica se uma string é um operador aritmético.
    private boolean isOperador(String op) {
        return op.matches("[+\\-*/]");
    }

    // Verifica a precedência entre dois operadores aritméticos.
    private boolean terPrecedencia(String op1, String op2) {
        if (op1.equals("(") || op1.equals(")"))
            return false;
        if ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-")))
            return true;
        if ((op1.equals("*") || op1.equals("/")) && (op2.equals("*") || op2.equals("/")))
            return true;
        return (op1.equals("+") || op1.equals("-")) && (op2.equals("+") || op2.equals("-"));
    }
}
