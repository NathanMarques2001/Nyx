package com.editor_texto.nyx.ui.sintaxe;

import com.editor_texto.nyx.compiler.lexico.AnalisadorLexico;
import com.editor_texto.nyx.compiler.lexico.TipoToken;
import com.editor_texto.nyx.compiler.lexico.Token;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Responsável por definir as regras de realce de sintaxe para a linguagem LC.
 * Utiliza o AnalisadorLexico para tokenizar o texto e atribui classes CSS
 * correspondentes a cada tipo de token.
 */
public class SintaxeLC {

    private static final AnalisadorLexico analisador = new AnalisadorLexico();

    /**
     * Calcula os spans de estilo (regiões de realce) para um determinado texto.
     *
     * @param texto O código fonte a ser realçado.
     * @return Um objeto StyleSpans contendo as informações de estilização para o
     *         RichTextFX.
     */
    public static StyleSpans<Collection<String>> calcularRealce(String texto) {
        if (texto == null || texto.isEmpty()) {
            return org.fxmisc.richtext.model.StyleSpans.singleton(Collections.emptyList(), 0);
        }
        List<Token> tokens = analisador.analisar(texto);
        StyleSpansBuilder<Collection<String>> construtorSpans = new StyleSpansBuilder<>();
        int ultimaPosicao = 0;

        for (Token token : tokens) {
            // Preenche o intervalo entre o último token e o atual (geralmente espaços não
            // tokenizados,
            // embora nosso lexer atual capture espaços, é bom manter para robustez)
            if (token.getInicio() > ultimaPosicao) {
                construtorSpans.add(Collections.emptyList(), token.getInicio() - ultimaPosicao);
            }

            String classeEstilo = obterClasseDeEstilo(token.getTipo());
            construtorSpans.add(Collections.singleton(classeEstilo), token.getTamanho());
            ultimaPosicao = token.getInicio() + token.getTamanho();
        }

        // Preenche o restante do texto após o último token
        if (ultimaPosicao < texto.length()) {
            construtorSpans.add(Collections.emptyList(), texto.length() - ultimaPosicao);
        }

        return construtorSpans.create();
    }

    /**
     * Mapeia um TipoToken para sua classe CSS correspondente.
     *
     * @param tipo O tipo do token.
     * @return O nome da classe CSS.
     */
    private static String obterClasseDeEstilo(TipoToken tipo) {
        switch (tipo) {
            case PALAVRA_CHAVE:
                return "token-keyword";
            case TIPO:
                return "token-type";
            case STRING:
                return "token-string";
            case COMENTARIO:
                return "token-comment";
            case NUMERO:
                return "token-number";
            case HEXADECIMAL:
                return "token-hex";
            case BOOLEANO:
                return "token-boolean";
            case OPERADOR:
                return "token-operator";
            case DELIMITADOR:
                return "token-delimiter";
            case ERRO:
                return "token-error";
            default:
                return "token-default";
        }
    }
}
