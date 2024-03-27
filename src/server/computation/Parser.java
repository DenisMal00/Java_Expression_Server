package server.computation;
import server.exception.*;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// The Parser class is responsible for parsing expressions from a string
public class Parser {

    //BNF
    //  <e> ::= <n> | <v> | (<e> <o> <e>)

    private final String string;

    // Current position in the string
    private int cursor = 0;

    public Parser(String string) {
        // Remove spaces from the input string
        this.string = string.replace(" ", "");
    }

    // Enum representing the possible types of tokens in an expression
    public enum TokenType {
        CONSTANT("[0-9]+(\\.[0-9]+)?"),
        VARIABLE("[a-z][a-z0-9]*"),
        OPERATOR("[+\\-\\*/\\^]"),
        OPEN_BRACKET("\\("),
        CLOSED_BRACKET("\\)");
        private final String regex;

        TokenType(String regex) {
            this.regex = regex;
        }

        // Function to get the next token of this type in a string, starting from a given position
        public Token next(String s, int i) {
            Matcher matcher = Pattern.compile(regex).matcher(s);
            if (!matcher.find(i)) {
                return null;
            }
            return new Token(matcher.start(), matcher.end());
        }

        public String getRegex() {
            return regex;
        }
    }

    // Class representing a token, with a start and end position in the string
    private static class Token {
        private final int start;
        private final int end;

        public Token(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    // Function to parse an expression from the string
    public Node parse() throws ExpressionParsingException {
        Node expression = parseExpression();

        // Check if whole expression was not enclosed in brackets
        if (cursor < string.length()) {
            throw new ExpressionParsingException(String.format(
                    "There's an error in the expression: '%s'",
                    string
            ));
        }
        return expression;
    }

    // Helper function to parse an expression, throwing an exception if it encounters invalid syntax
    private Node parseExpression() throws ExpressionParsingException {
        // Check if the expression is unexpectedly empty
        if (cursor >= string.length()) {
            throw new ExpressionParsingException("Unexpected end of expression.");
        }

        Token token;
        token = TokenType.CONSTANT.next(string, cursor);
        if (token != null && token.start == cursor) {
            // If a constant was successfully parsed, return a new Constant node
            cursor = token.end;
            return new Constant(Double.parseDouble(string.substring(token.start, token.end)));
        }

        // Continue with other token types if a constant was not found
        token = TokenType.VARIABLE.next(string, cursor);
        if (token != null && token.start == cursor) {
            cursor = token.end;
            return new Variable(string.substring(token.start, token.end));
        }

        // Continue with other token types if a variable was not found
        token = TokenType.OPEN_BRACKET.next(string, cursor);
        if (token != null && token.start == cursor) {
            cursor = token.end;

            // Try to parse a nested expression, an operator, and another nested expression
            Node child1 = parseExpression();

            if (cursor >= string.length()) {
                throw new ExpressionParsingException("Unexpected end of expression.");
            }

            Token operatorToken = TokenType.OPERATOR.next(string, cursor);
            if (operatorToken != null && operatorToken.start == cursor) {
                cursor = operatorToken.end;

                String operatorString = string.substring(operatorToken.start, operatorToken.end);
                Operator.Type operatorType = null;
                for (Operator.Type type : Operator.Type.values()) {
                    if (operatorString.equals(Character.toString(type.getSymbol()))) {
                        operatorType = type;
                        break;
                    }
                }
                if (operatorType == null) {
                    throw new ExpressionParsingException(String.format(
                            "Unknown operator at %d: '%s'",
                            operatorToken.start,
                            operatorString
                    ));
                }

                Node child2 = parseExpression();

                // Check if the operation is correctly enclosed in brackets
                if (cursor >= string.length()) {
                    throw new ExpressionParsingException("Unexpected end of expression.");
                }
                Token closedBracketToken = TokenType.CLOSED_BRACKET.next(string, cursor);
                if (closedBracketToken != null && closedBracketToken.start == cursor) {
                    cursor = closedBracketToken.end;
                } else {
                    throw new ExpressionParsingException(String.format(
                            "Operator not enclosed in brackets in expression: '%s'",
                            string
                    ));
                }
                return new Operator(operatorType, Arrays.asList(child1, child2));
            } else {
                char ch = string.charAt(cursor);
                if (Character.isAlphabetic(ch)) {
                    throw new ExpressionParsingException(String.format(
                            "Unvalued variable or unknown operation at %d: '%s'",
                            cursor,
                            ch
                    ));
                } else {
                    throw new ExpressionParsingException(String.format(
                            "Unexpected char at %d instead of operator: '%s'",
                            cursor,
                            ch
                    ));
                }
            }
        }

        // Check for unvalued variable or unknown operation
        if (cursor < string.length() && Character.isAlphabetic(string.charAt(cursor))) {
            throw new ExpressionParsingException(String.format(
                    "Unvalued variable or unknown operation at %d: '%s'",
                    cursor,
                    string.substring(cursor)
            ));
        }

        throw new ExpressionParsingException(String.format(
                "Unexpected char at %d: '%s'",
                cursor,
                string.charAt(cursor)
        ));
    }


}
