package com.editor_texto.nyx.interface_usuario.layout;

import com.editor_texto.nyx.lexico.AnalisadorLexico;
import com.editor_texto.nyx.lexico.Token;
import com.editor_texto.nyx.lexico.TipoToken;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Responsável por aplicar o realce de sintaxe (highlighting) no código LC.
 * Utiliza o AnalisadorLexico para tokenizar o texto e mapear para classes CSS.
 */
public class SintaxeLC {

    private static final AnalisadorLexico analisadorLexico = new AnalisadorLexico();

    /**
     * Calcula os spans de estilo para o texto fornecido.
     *
     * @param texto O código fonte a ser analisado.
     * @return Os spans de estilo compatíveis com o CodeArea.
     */
    public static StyleSpans<Collection<String>> calcularRealce(String texto) {
        List<Token> tokens = analisadorLexico.analisar(texto);
        StyleSpansBuilder<Collection<String>> construtorSpans = new StyleSpansBuilder<>();
        int ultimaPosicao = 0;

        for (Token token : tokens) {
            // Preencher lacuna entre o fim do último token e o início deste (espaços não tokenizados caso existam)
            // Embora nosso lexer capture espaços, essa verificação garante robustez.
            if (token.getInicio() > ultimaPosicao) {
                construtorSpans.add(Collections.emptyList(), token.getInicio() - ultimaPosicao);
            }

            String classeEstilo = obterClasseDeEstilo(token.getTipo());
            construtorSpans.add(Collections.singleton(classeEstilo), token.getTamanho());
            ultimaPosicao = token.getInicio() + token.getTamanho();
        }

        // Preencher o restante do texto se houver
        if (ultimaPosicao < texto.length()) {
            construtorSpans.add(Collections.emptyList(), texto.length() - ultimaPosicao);
        }

        return construtorSpans.create();
    }

    /**
     * Mapeia o TipoToken para a classe CSS correspondente.
     *
     * @param tipo O tipo do token.
     * @return O nome da classe CSS.
     */
    private static String obterClasseDeEstilo(TipoToken tipo) {
        switch (tipo) {
            case PALAVRA_CHAVE: return "token-keyword";
            case TIPO: return "token-type";
            case STRING: return "token-string";
            case COMENTARIO: return "token-comment";
            case NUMERO: return "token-number";
            case HEXADECIMAL: return "token-hex";
            case BOOLEANO: return "token-boolean";
            case OPERADOR: return "token-operator";
            case DELIMITADOR: return "token-delimiter";
            case ERRO: return "token-error";
            default: return "token-default";
        }
    }
}
