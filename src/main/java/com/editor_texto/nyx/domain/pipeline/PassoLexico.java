package com.editor_texto.nyx.domain.pipeline;

import com.editor_texto.nyx.compiler.lexico.AnalisadorLexico;
import com.editor_texto.nyx.compiler.lexico.TipoToken;
import com.editor_texto.nyx.compiler.ErroCompilacao;
import com.editor_texto.nyx.compiler.TipoErro;
import com.editor_texto.nyx.sistema.ServicoLog;

/**
 * Passo 1 do Pipeline: Análise Léxica.
 * Responsável por tokenizar o código fonte e popular a Tabela de Símbolos.
 * Utiliza um Adapter para converter tokens do scanner moderno para o modelo
 * legado esperado pelo parser.
 */
public class PassoLexico implements PassoPipeline {

    @Override
    public boolean executar(ContextoCompilacao contexto) throws Exception {
        try {
            AnalisadorLexico lexico = new AnalisadorLexico();
            String[] linhas = contexto.getCodigoFonte().split("\\r?\\n");

            boolean sucesso = true;
            contexto.getTabelaSimbolos().limpar();

            for (int i = 0; i < linhas.length; i++) {
                var tokens = lexico.analisar(linhas[i]);
                for (var t : tokens) {
                    if (t.getTipo() == TipoToken.ERRO) {
                        contexto.adicionarErro(new ErroCompilacao(
                                TipoErro.LEXICO,
                                "Caractere inválido: " + t.getLexema(),
                                i + 1,
                                t.getInicio() + 1));
                        sucesso = false;
                    } else if (t.getTipo() == TipoToken.ESPACO_EM_BRANCO || t.getTipo() == TipoToken.COMENTARIO) {
                        // Ignora espaços e comentários para a tabela de símbolos
                        continue;
                    } else {
                        // ADAPTER: Converte lexico.Token -> modelo.Token
                        String lexema = t.getLexema();
                        String classificacao = mapTipoToken(t.getTipo());
                        String tipo = null;

                        // Ajustes de compatibilidade para o Parser legado
                        if (t.getTipo() == TipoToken.NUMERO ||
                                t.getTipo() == TipoToken.STRING ||
                                t.getTipo() == TipoToken.HEXADECIMAL ||
                                t.getTipo() == TipoToken.BOOLEANO) {
                            classificacao = "const";
                            // Inferência básica
                            if (t.getTipo() == TipoToken.NUMERO)
                                tipo = "int";
                            if (t.getTipo() == TipoToken.STRING)
                                tipo = "string";
                            if (t.getTipo() == TipoToken.BOOLEANO)
                                tipo = "boolean";
                            if (t.getTipo() == TipoToken.HEXADECIMAL)
                                tipo = "byte";
                        }

                        // Tratamento especial para palavras-chave (parser espera 'palavra_reservada' ou
                        // o próprio nome?)
                        // AnalisadorSintatico verifica: token.getNome().equalsIgnoreCase("if")
                        // E token.getNome().equalsIgnoreCase("int") e token.isTipoPrimitivo()
                        if (t.getTipo() == TipoToken.PALAVRA_CHAVE || t.getTipo() == TipoToken.TIPO) {
                            classificacao = "palavra_reservada";
                        }

                        com.editor_texto.nyx.compiler.modelo.Token tokenModelo = new com.editor_texto.nyx.compiler.modelo.Token(
                                lexema,
                                classificacao,
                                tipo,
                                i + 1,
                                t.getInicio() + 1);

                        contexto.getTabelaSimbolos().adicionarToken(tokenModelo);
                    }
                }
            }
            if (sucesso) {
                ServicoLog.info("Análise léxica concluída com sucesso. Tabela de Símbolos populada.");
            }
            return sucesso;
        } catch (Exception e) {
            ServicoLog.erro("Exceção na análise léxica: " + e.getMessage());
            contexto.adicionarErro(new ErroCompilacao(TipoErro.OUTRO, "Erro interno léxico: " + e.getMessage(), 0, 0));
            e.printStackTrace();
            return false;
        }
    }

    private String mapTipoToken(TipoToken tipo) {
        switch (tipo) {
            case IDENTIFICADOR:
                return "id";
            case NUMERO:
                return "const";
            case STRING:
                return "const";
            case HEXADECIMAL:
                return "const";
            case BOOLEANO:
                return "const";
            case PALAVRA_CHAVE:
                return "palavra_reservada";
            case TIPO:
                return "tipo";
            case OPERADOR:
                return "operador";
            case DELIMITADOR:
                return "delimitador";
            default:
                return "outro";
        }
    }

    @Override
    public String getNome() {
        return "Análise Léxica";
    }
}
