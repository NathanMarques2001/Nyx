package com.editor_texto.nyx.ui.layout;

import com.editor_texto.nyx.lexer.Lexer;
import com.editor_texto.nyx.lexer.Token;
import com.editor_texto.nyx.lexer.TokenType;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LCSyntax {

    private static final Lexer lexer = new Lexer();

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        List<Token> tokens = lexer.scan(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastPos = 0;

        for (Token token : tokens) {
            // Fill gap between reset of last token and start of this token (whitespace
            // usually, if skipped)
            // The lexer handles whitespace, but if it didn't, we'd need this.
            // Our Lexer emits WHITESPACE tokens, so gaps should be non-existent unless
            // logic fails.
            // But let's be safe.
            if (token.getStartOffset() > lastPos) {
                spansBuilder.add(Collections.emptyList(), token.getStartOffset() - lastPos);
            }

            String styleClass = getStyleClass(token.getType());
            spansBuilder.add(Collections.singleton(styleClass), token.getLength());
            lastPos = token.getStartOffset() + token.getLength();
        }

        // Fill remaining text
        if (lastPos < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastPos);
        }

        return spansBuilder.create();
    }

    private static String getStyleClass(TokenType type) {
        switch (type) {
            case KEYWORD:
                return "token-keyword";
            case TYPE:
                return "token-type";
            case STRING:
                return "token-string";
            case COMMENT:
                return "token-comment";
            case NUMBER:
                return "token-number";
            case HEX:
                return "token-hex";
            case BOOLEAN_LITERAL:
                return "token-boolean";
            case OPERATOR:
                return "token-operator";
            case DELIMITER:
                return "token-delimiter";
            case ERROR:
                return "token-error";
            default:
                return "token-default";
        }
    }
}
