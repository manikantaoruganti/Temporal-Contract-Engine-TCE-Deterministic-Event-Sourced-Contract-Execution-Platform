package com.tce.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class RuleParser {

    public interface Expression {
        boolean evaluate(Map<String, Object> context);
    }

    public static class AndExpression implements Expression {
        private final Expression left;
        private final Expression right;

        public AndExpression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(Map<String, Object> context) {
            return left.evaluate(context) && right.evaluate(context);
        }

        @Override
        public String toString() {
            return "(" + left + " AND " + right + ")";
        }
    }

    public static class OrExpression implements Expression {
        private final Expression left;
        private final Expression right;

        public OrExpression(Expression left, Expression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(Map<String, Object> context) {
            return left.evaluate(context) || right.evaluate(context);
        }

        @Override
        public String toString() {
            return "(" + left + " OR " + right + ")";
        }
    }

    public static class ComparisonExpression implements Expression {
        private final String variable;
        private final String operator;
        private final Object value; // Can be Double, String, Boolean

        public ComparisonExpression(String variable, String operator, Object value) {
            this.variable = variable.trim();
            this.operator = operator.trim();
            this.value = value;
        }

        @Override
        public boolean evaluate(Map<String, Object> context) {
            if (!context.containsKey(variable)) {
                log.warn("Variable '{}' not found in evaluation context.", variable);
                return false;
            }

            Object varVal = context.get(variable);
            if (varVal == null) {
                return value == null && "==".equals(operator);
            }

            // Numeric comparison
            if (varVal instanceof Number || value instanceof Number) {
                double leftNum = ((Number) varVal).doubleValue();
                double rightNum = getNumericValue(value);

                return switch (operator) {
                    case ">" -> leftNum > rightNum;
                    case "<" -> leftNum < rightNum;
                    case ">=" -> leftNum >= rightNum;
                    case "<=" -> leftNum <= rightNum;
                    case "==" -> leftNum == rightNum;
                    case "!=" -> leftNum != rightNum;
                    default -> throw new IllegalArgumentException("Unsupported numeric operator: " + operator);
                };
            }

            // String or Boolean comparison
            String leftStr = varVal.toString();
            String rightStr = value.toString();

            return switch (operator) {
                case "==" -> leftStr.equals(rightStr);
                case "!=" -> !leftStr.equals(rightStr);
                default -> throw new IllegalArgumentException("Operator '" + operator + "' is only supported for numbers.");
            };
        }

        private double getNumericValue(Object obj) {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }
            try {
                return Double.parseDouble(obj.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert value to number: " + obj);
            }
        }

        @Override
        public String toString() {
            return variable + " " + operator + " " + (value instanceof String ? "'" + value + "'" : value);
        }
    }

    // Token helper
    private enum TokenType {
        AND, OR, COMP_OP, LPAREN, RPAREN, IDENTIFIER, NUMBER, STRING, BOOLEAN, EOF
    }

    private static class Token {
        final TokenType type;
        final String value;

        Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return type + "[" + value + "]";
        }
    }

    public Expression parse(String expressionStr) {
        if (expressionStr == null || expressionStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty");
        }
        List<Token> tokens = tokenize(expressionStr);
        TokenStream stream = new TokenStream(tokens);
        Expression expr = parseExpression(stream);
        if (stream.peek().type != TokenType.EOF) {
            throw new IllegalArgumentException("Unexpected tokens after valid expression: " + stream.peek().value);
        }
        return expr;
    }

    private List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = input.length();

        while (i < n) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "("));
                i++;
                continue;
            }
            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")"));
                i++;
                continue;
            }

            // Operators (>=, <=, ==, !=, >, <)
            if (i + 1 < n) {
                String twoChars = input.substring(i, i + 2);
                if (List.of(">=", "<=", "==", "!=").contains(twoChars)) {
                    tokens.add(new Token(TokenType.COMP_OP, twoChars));
                    i += 2;
                    continue;
                }
            }
            if (c == '>' || c == '<') {
                tokens.add(new Token(TokenType.COMP_OP, String.valueOf(c)));
                i++;
                continue;
            }

            // String Literal
            if (c == '\'') {
                int start = ++i;
                while (i < n && input.charAt(i) != '\'') {
                    i++;
                }
                if (i >= n) {
                    throw new IllegalArgumentException("Unterminated string literal in expression");
                }
                tokens.add(new Token(TokenType.STRING, input.substring(start, i)));
                i++; // skip closing quote
                continue;
            }

            // Numbers (decimals/integers)
            if (Character.isDigit(c) || c == '-') {
                int start = i;
                if (c == '-') i++;
                boolean hasDecimal = false;
                while (i < n && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    if (input.charAt(i) == '.') {
                        if (hasDecimal) throw new IllegalArgumentException("Malformed number in expression");
                        hasDecimal = true;
                    }
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, i)));
                continue;
            }

            // Words (Identifiers, AND, OR, Booleans)
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    i++;
                }
                String word = input.substring(start, i);
                String upper = word.toUpperCase();

                if ("AND".equals(upper)) {
                    tokens.add(new Token(TokenType.AND, word));
                } else if ("OR".equals(upper)) {
                    tokens.add(new Token(TokenType.OR, word));
                } else if ("TRUE".equals(upper) || "FALSE".equals(upper)) {
                    tokens.add(new Token(TokenType.BOOLEAN, word));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
                continue;
            }

            throw new IllegalArgumentException("Unexpected character in expression: " + c);
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    // Grammar:
    // Expression -> OrTerm
    // OrTerm     -> AndTerm ( OR AndTerm )*
    // AndTerm    -> Comparison ( AND Comparison )*
    // Comparison -> IDENTIFIER COMP_OP Value
    //             | LPAREN Expression RPAREN
    // Value      -> NUMBER | STRING | BOOLEAN

    private Expression parseExpression(TokenStream stream) {
        return parseOrTerm(stream);
    }

    private Expression parseOrTerm(TokenStream stream) {
        Expression left = parseAndTerm(stream);
        while (stream.peek().type == TokenType.OR) {
            stream.consume(); // consume OR
            Expression right = parseAndTerm(stream);
            left = new OrExpression(left, right);
        }
        return left;
    }

    private Expression parseAndTerm(TokenStream stream) {
        Expression left = parseComparison(stream);
        while (stream.peek().type == TokenType.AND) {
            stream.consume(); // consume AND
            Expression right = parseComparison(stream);
            left = new AndExpression(left, right);
        }
        return left;
    }

    private Expression parseComparison(TokenStream stream) {
        Token next = stream.peek();
        if (next.type == TokenType.LPAREN) {
            stream.consume(); // consume (
            Expression expr = parseExpression(stream);
            stream.consume(TokenType.RPAREN, "Expected closing parenthesis");
            return expr;
        }

        Token varToken = stream.consume(TokenType.IDENTIFIER, "Expected variable name in comparison");
        Token opToken = stream.consume(TokenType.COMP_OP, "Expected comparison operator (>, <, >=, <=, ==, !=)");

        Token valToken = stream.peek();
        Object value;
        if (valToken.type == TokenType.NUMBER) {
            stream.consume();
            value = Double.parseDouble(valToken.value);
        } else if (valToken.type == TokenType.STRING) {
            stream.consume();
            value = valToken.value;
        } else if (valToken.type == TokenType.BOOLEAN) {
            stream.consume();
            value = Boolean.parseBoolean(valToken.value);
        } else {
            throw new IllegalArgumentException("Expected numeric, string, or boolean value after operator, found: " + valToken.value);
        }

        return new ComparisonExpression(varToken.value, opToken.value, value);
    }

    private static class TokenStream {
        private final List<Token> tokens;
        private int pointer = 0;

        TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        Token peek() {
            return tokens.get(pointer);
        }

        Token consume() {
            return tokens.get(pointer++);
        }

        Token consume(TokenType expectedType, String errorMsg) {
            Token t = peek();
            if (t.type != expectedType) {
                throw new IllegalArgumentException(errorMsg + ". Found: " + t.value);
            }
            return consume();
        }
    }
}
