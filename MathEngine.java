public class MathEngine {

    public static String evaluate(String expression) 
    {
        try
        {
            // Instantiate a parse object and send the user request to it
            RecursiveDescentParser parser = new RecursiveDescentParser(expression.trim());
            double result = parser.parseExpression();
            if (parser.hasMore())
            {
                return "ERROR:Invalid expression";
            }

            if (result == Math.floor(result) && !Double.isInfinite(result))
            {
                return String.valueOf((long) result);
            }
            return String.valueOf(result);
        }
        catch (ArithmeticException e)
        {
            return "ERROR:" + e.getMessage();
        }
        catch (Exception e)
        {
            return "ERROR:Invalid expression";
        }
    }
}
