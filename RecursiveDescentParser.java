public class RecursiveDescentParser
{
    private final String input;
    private int pos;
    
    public RecursiveDescentParser(String input)
    {
        this.input = input.replaceAll(" ", "");
        this.pos = 0;
    }

    public boolean hasMore()
    {
        return pos < input.length();
    }

    // Default top down parsing logic for all methods
    public double parseExpression()
    {
        double left = parseTerm();
        while (true)
        {
            if (pos < input.length() && input.charAt(pos) == '+')
            {
                pos++;
                left += parseTerm();
            }
            else if (pos < input.length() && input.charAt(pos) == '-')
            {
                pos++; left -= parseTerm();
            }
            else
            {
                 break;
            }
        }
        return left;
    }

    private double parseTerm() 
    {
        double left = parseFactor();
        while (true) 
        {
            if (pos < input.length() && input.charAt(pos) == '*') 
            {
                pos++;
                left *= parseFactor();
            } 
            else if (pos < input.length() && input.charAt(pos) == '/')
            {
                pos++;
                double divisor = parseFactor();
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                left /= divisor;
            } 
            else 
            {
                break;
            }
        }
        return left;
    }

    private double parseFactor()
    {
        if (pos >= input.length())
        {
            throw new RuntimeException("Unexpected end of expression");
        }

        if (input.charAt(pos) == '(')
        {
            pos++; 
            double result = parseExpression();
            if (pos >= input.length() || input.charAt(pos) != ')')
            {
                throw new RuntimeException("Missing closing parenthesis");
            }
            pos++; 
            return result;
        }

        // Unary minus 
        if (input.charAt(pos) == '-')
        {
            pos++;
            return -parseFactor();
        }

        // Unary plus 
        if (input.charAt(pos) == '+')
        {
            pos++;
            return parseFactor();
        }

        return parseNumber();
    }

    private double parseNumber()
    {
        int start = pos;
        if (pos < input.length() && input.charAt(pos) == '-') pos++;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) 
        {
            pos++;
        }
        if (start == pos) throw new RuntimeException("Expected number");
        try
        {
            return Double.parseDouble(input.substring(start, pos));
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException("Invalid number");
        }
    }

}
