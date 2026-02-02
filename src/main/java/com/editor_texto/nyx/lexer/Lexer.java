package com.editor_texto.nyx.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

    private static final Set<String> KEYWORDS = Set.of(
            "if", "else", "while", "begin", "end", "final", "write", "writeln", "readln"
    );

    private static final Set<String> TYPES = Set.of(
            "int", "byte", "string", "boolean"
    );

    // Regex Patterns adapted for tokenization
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern COMMENT = Pattern.compile("/\\*(.|[\\r\\n])*?\\*/|\\{[^}]*\\}");
    private static final Pattern STRING = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    // Hex: 0h followed by hex digits. Error check done by logic if needed, but for highlighting we grab the chunk.
    private static final Pattern HEX = Pattern.compile("0h[a-zA-Z0-9]*");
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final Pattern BOOLEAN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_]\\w*");
    private static final Pattern OPERATOR = Pattern.compile("==|<>|<=|>=|<|>|[+\\-*/=]|and|or|not", Pattern.CASE_INSENSITIVE);
    private static final Pattern DELIMITER = Pattern.compile("[,;()]");

    public List<Token> scan(String text) {
        List<Token> tokens = new ArrayList<>();
        int offset = 0;
        String remainingResult = text;

        while (!remainingResult.isEmpty()) {
            Matcher matcher;
            boolean matched = false;
            int length = 0;

            // 1. Whitespace
            matcher = WHITESPACE.matcher(remainingResult);
            if (matcher.lookingAt()) {
                length = matcher.end();
                // We can choose to emit WHITESPACE or ignore. 
                // Creating token for consistency, Highlighter can decide to ignore style-wise.
                tokens.add(new Token(TokenType.WHITESPACE, matcher.group(), offset, length));
                matched = true;
            }

            // 2. Comments (Priority)
            if (!matched) {
                matcher = COMMENT.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.COMMENT, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 3. Strings
            if (!matched) {
                // Check simple quote start to optimize regex check
                if (remainingResult.startsWith("\"")) {
                    matcher = STRING.matcher(remainingResult);
                    if (matcher.lookingAt()) {
                        length = matcher.end();
                        tokens.add(new Token(TokenType.STRING, matcher.group(), offset, length));
                        matched = true;
                    } else {
                        // Unclosed string or invalid string start? 
                        // Treat as error until newline or just consume one char?
                        // Let's attempt to match until end of line to be safe or just 1 char
                        // Actually, if it starts with " but regex doesn't match, it's likely unclosed string.
                        // We will report as ERROR until end of line or next quote.
                        // For simplicity in editor: capture as ERROR token.
                        int nextQuote = remainingResult.indexOf('\"', 1);
                        int nextLine = remainingResult.indexOf('\n');
                        int end = (nextQuote != -1) ? nextQuote + 1 : (nextLine != -1 ? nextLine : remainingResult.length());
                        length = end;
                        tokens.add(new Token(TokenType.ERROR, remainingResult.substring(0, length), offset, length));
                        matched = true;
                    }
                }
            }

            // 4. Hex
            if (!matched) {
                matcher = HEX.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.HEX, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 5. Boolean
            if (!matched) {
                matcher = BOOLEAN.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.BOOLEAN_LITERAL, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 6. Identifier / Keyword / Type 
            // Check this BEFORE Number if identifiers can start with digits (usually no), 
            // but checked AFTER Hex because 0h... starts with digit.
            if (!matched) {
                matcher = IDENTIFIER.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    String word = matcher.group();
                    TokenType type = TokenType.IDENTIFIER;
                    
                    if (KEYWORDS.contains(word)) type = TokenType.KEYWORD;
                    else if (TYPES.contains(word)) type = TokenType.TYPE;
                    // Check logic operators 'and', 'or', 'not' which are technically identifiers in regex
                    // but operators in semantics.
                    // The regex for IDENTIFIER matches them.
                    else if (word.equalsIgnoreCase("and") || word.equalsIgnoreCase("or") || word.equalsIgnoreCase("not")) {
                        type = TokenType.OPERATOR;
                    }

                    tokens.add(new Token(type, word, offset, length));
                    matched = true;
                }
            }

            // 7. Numbers
            if (!matched) {
                matcher = NUMBER.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.NUMBER, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 8. Operators (Non-textual like +, -, ==)
            if (!matched) {
                matcher = OPERATOR.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.OPERATOR, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 9. Delimiters
            if (!matched) {
                matcher = DELIMITER.matcher(remainingResult);
                if (matcher.lookingAt()) {
                    length = matcher.end();
                    tokens.add(new Token(TokenType.DELIMITER, matcher.group(), offset, length));
                    matched = true;
                }
            }

            // 10. Unknown / Error
            if (!matched) {
                length = 1;
                tokens.add(new Token(TokenType.ERROR, remainingResult.substring(0, 1), offset, length));
                matched = true;
            }

            // Advance
            offset += length;
            remainingResult = remainingResult.substring(length);
        }

        return tokens;
    }
}
