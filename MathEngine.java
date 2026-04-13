public class MathEngine {

    /**
     * Evaluates a simple math expression string.
     * Supports: +  -  *  /  with optional spaces.
     * Returns the result as a String, or an ERROR: message.
     *
     * Examples:
     *   evaluate("5 + 3")    -> "8.0"
     *   evaluate("10 / 0")   -> "ERROR:Division by zero"
     *   evaluate("abc")      -> "ERROR:Invalid expression"
     */
    public static String evaluate(String expression) {
        try {
            expression = expression.trim();

            // Split on operator — keep spaces flexible
            // Regex: split on space-operator-space OR just operator
            String[] tokens = expression.split("\\s*(\\+|\\-|\\*|\\/)\\s*", 2);

            if (tokens.length != 2) {
                return "ERROR:Invalid expression";
            }

            double a = Double.parseDouble(tokens[0].trim());
            double b = Double.parseDouble(tokens[1].trim());

            // Find which operator was used
            char op = findOperator(expression);

            switch (op) {
                case '+': return String.valueOf(a + b);
                case '-': return String.valueOf(a - b);
                case '*': return String.valueOf(a * b);
                case '/':
                    if (b == 0) return "ERROR:Division by zero";
                    return String.valueOf(a / b);
                default:
                    return "ERROR:Unknown operator";
            }
        } catch (NumberFormatException e) {
            return "ERROR:Invalid number in expression";
        } catch (Exception e) {
            return "ERROR:" + e.getMessage();
        }
    }

    private static char findOperator(String expr) {
        // Walk the string looking for an operator character
        // Skip leading negative sign if any
        for (int i = 1; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                return c;
            }
        }
        return '?';
    }

    // Quick self-test — run with: java MathEngine
    public static void main(String[] args) {
        String[] tests = {
            "5 + 3", "10 - 4", "7 * 8", "15 / 3",
            "10 / 0", "abc + 5", "hello"
        };
        for (String t : tests) {
            System.out.println("  " + t + " => " + evaluate(t));
        }
    }
}
