package server.computation;

import server.exception.*;
import java.util.*;


// The DataComputation class, which is responsible for managing variable ranges, parsing and evaluating expressions,
// and performing computations based on those expressions.
public class DataComputation {

    // A map to hold variable names and their corresponding ranges
    private static HashMap<String, List<Double>> variableRanges;
    // A list to hold nodes resulting from parsed expressions
    private static List<Node> nodes;
    // A Parser object used for parsing expressions
    private Parser parser;

    public DataComputation() {
        variableRanges = new HashMap<>();
        nodes = new ArrayList<>();
        parser = new Parser("");
    }

    // This method handles adding a variable range to the variableRanges map
    public void addVariableRange(String variableRange) throws InvalidVariableRangeException {
        // Splitting the input string into individual parts based on the ':' character.
        String[] variableRangeParts = variableRange.split(":");

        // If the length of variableRangeParts is not 4, it means the format of variable range is incorrect.
        if (variableRangeParts.length != 4) {
            throw new InvalidVariableRangeException("ERR; (InvalidVariableRangeException) Invalid variable range format");
        }

        // The first part of the split string is considered as the variable name.
        String variableName = variableRangeParts[0];

        // A regex pattern to validate the variable name. The variable name should start with a lowercase letter and can contain lowercase letters or numbers.
        String regex = "^[a-z][a-z0-9]*$";
        // If the variable name does not match the regex pattern, throw an IllegalArgumentException.
        if (!variableName.matches(regex)) {
            throw new IllegalArgumentException("ERR; (IllegalArgumentException) Invalid variable name: " + variableName);
        }

        // Parse the remaining parts of the string as the start, increment, and end values of the range.
        double start = Double.parseDouble(variableRangeParts[1]);
        double increment = Double.parseDouble(variableRangeParts[2]);
        double end = Double.parseDouble(variableRangeParts[3]);

        // Create a format string for the increment using the number of significant digits after the decimal point.
        String format = "%." + getSignificantDigits(increment) + "f";
        List<Double> ranges = new ArrayList<>();

        // Create a loop to generate the range values from start to end, incrementing by the increment value.
        for (double i = start; i <= end; i += increment) {
            // Format the value to the desired number of significant digits and replace any comma with a dot.
            String formattedValue = String.format(format, i);
            formattedValue = formattedValue.replace(",", ".");

            // Parse the formatted string as a double value. If the value is negative zero, set it to positive zero.
            double truncated = Double.parseDouble(formattedValue);
            if (truncated == -0.0) {
                truncated = 0.0;
            }

            // Adjust the loop counter to match the truncated value and add it to the ranges list.
            i=truncated;
            ranges.add(truncated);
        }
        // Put the variable name and corresponding ranges into the variableRanges map.
        variableRanges.put(variableName, ranges);
    }

    // This method is responsible for finding the number of significant digits after the decimal point in a given number.
    public static int getSignificantDigits (double number){
        String numberString = Double.toString(number);

        // Find the index of the decimal point.
        int indexPoint = numberString.indexOf(".");
        int digitsAfterComma = 0;

        // If there is a decimal point, calculate the number of digits after it.
        if (indexPoint >= 0) {
            digitsAfterComma = numberString.length() - indexPoint - 1;
        }
        return digitsAfterComma;
    }

    // This method parses multiple mathematical expressions separated by ';' and returns a list of Node representations of the expressions.
    public List<Node> parseExpressions(String expressions) throws ExpressionParsingException {
        List<Node> nodes = new ArrayList<>();
        // Splits the input string into individual expressions.
        String[] exprs = expressions.split(";");
        for (String expr : exprs) {
            parser = new Parser(expr);
            nodes.add(parser.parse());
        }
        // Returns the list of parsed Node objects.
        return nodes;
    }

    // This method evaluates a given Node and returns its numerical value. It distinguishes between different types of nodes: Operator, Constant, and Variable.
    public double evaluateNode(Node node, Map<String, Double> values) throws ComputationException {
        if (node instanceof Operator) {
            Operator operator = (Operator) node;
            List<Double> evaluatedChildren = new ArrayList<>();
            for (Node child : operator.getChildren()) {
                evaluatedChildren.add(evaluateNode(child, values));
            }
            double[] args = evaluatedChildren.stream().mapToDouble(Double::doubleValue).toArray();

            // If the operator is a division and the second operand is zero, throws an exception
            if (operator.getType() == Operator.Type.DIVISION && args[1] == 0) {
                if (args[0] == 0) {
                    // If both operands are zero, throws a ZeroOverZeroException
                    throw new ZeroOverZeroException("Undefined result at node '" + node + "'");
                } else {
                    // If only the second operand is zero, throws a DivisionByZeroException
                    throw new DivisionByZeroException("Division by zero at node '" + node + "'");
                }
            }
            return operator.getType().getFunction().apply(args);
        } else if (node instanceof Constant) {
            Constant constant = (Constant) node;
            return constant.getValue();
        } else if (node instanceof Variable) {
            Variable variable = (Variable) node;
            String varName = variable.getName();
            if (values.containsKey(varName)) {
                return values.get(varName);
            } else {
                throw new ComputationException("ERR; (ComputationException) Unvalued variable: " + varName);
            }
        } else {
            throw new ComputationException("ERR; (ComputationException) Unknown node type: " + node.getClass());
        }
    }

    // This method is responsible for computing an expression given the computation type and merge type.
    public String computeExpression(String expression, String computationType, String mergeType) {
        // Tries to parse the provided expression into nodes.
        try {
            nodes = parseExpressions(expression);
        } catch (ExpressionParsingException e) {
            return "ERR; (ExpressionParsingException) " + e.getMessage();
        }
        List<List<Double>> mergedVariables;

        // Tries to merge variables depending on the merge type.
        try {
            // If there is only one variable, simply stores it in the list of merged variables.
            if (variableRanges.size() == 1) {
                List<Double> singleVarList = variableRanges.values().iterator().next();
                mergedVariables = new ArrayList<>();
                for (Double value : singleVarList) {
                    mergedVariables.add(Collections.singletonList(value));
                }
            } else {
                // Depending on the merge type, it merges variables either element-wise or in a Cartesian product.
                if (mergeType.equals("LIST")) {
                    mergedVariables = elementWiseMerge();
                } else {
                    mergedVariables = cartesianProduct();
                }
            }
        } catch (IllegalArgumentException e) {
            // If merging fails due to an illegal argument, it returns the error message.
            return e.getMessage();
        }

        // If the computation type is 'COUNT', it simply returns the size of the merged variables.
        if (computationType.equals("COUNT")) {
            return String.valueOf(mergedVariables.size());
        }

        List<Double> results = new ArrayList<>();
        double sum = 0.0;
        double result=0.0;

        // Iterates over each list of values in the merged variables.
        for (List<Double> values : mergedVariables) {
            Map<String, Double> valuesMap = new HashMap<>();
            int i = 0;
            for (String varName : variableRanges.keySet()) {
                valuesMap.put(varName, values.get(i));
                i++;
            }
            boolean isFirstExpression = true;
            try {

                // Evaluates each node and stores the result.
                for (Node node : nodes) {
                    try {
                        result = evaluateNode(node, valuesMap);
                    } catch (ZeroOverZeroException e) {
                        throw new ZeroOverZeroException("ERR;"+ " (ZeroOverZeroException) "+ e.getMessage());
                    } catch (DivisionByZeroException e) {
                        throw new DivisionByZeroException("ERR;"+ " (DivisionByZeroException) "+  e.getMessage());
                    }
                    results.add(result);

                    // If the computation type is 'AVG', it calculates the sum of the results only on the first expression.
                    if (isFirstExpression && computationType.equals("AVG")) {
                        sum += result;
                        isFirstExpression = false;
                    }
                }
            } catch (ComputationException e) {
                return e.getMessage();

            }
        }
        // Depending on the computation type, it performs different operations on the results.
        switch (computationType) {
            case "MIN":
                return String.valueOf(Collections.min(results));
            case "MAX":
                return String.valueOf(Collections.max(results));
            case "AVG":
                return String.valueOf(sum / mergedVariables.size());
            default:
                throw new IllegalArgumentException("ERR; (IllegalArgumentException) Invalid computation type: " + computationType);
        }
    }


    // This method computes the Cartesian product of the variable ranges and returns a list of lists representing all possible combinations of variable values.
    public List<List<Double>> cartesianProduct() {
        List<List<Double>> variables = new ArrayList<>(variableRanges.values());
        return cartesianProduct(variables, 0);
    }

    // This is a helper method for the above method that performs the actual recursive computation of the Cartesian product.
    private List<List<Double>> cartesianProduct(List<List<Double>> variables, int index) {
        List<List<Double>> result = new ArrayList<>();
        if (index == variables.size()) {
            result.add(new ArrayList<>());
        } else {
            for (Double value : variables.get(index)) {
                for (List<Double> product : cartesianProduct(variables, index + 1)) {
                    product.add(0, value);
                    result.add(product);
                }
            }
        }
        return result;
    }

    // This method performs an element-wise merge of the variable ranges, i.e., it groups together the i-th values of all variables into a list.
    // It throws an IllegalArgumentException if the variable ranges do not all have the same size.
    public List<List<Double>> elementWiseMerge() {
        List<List<Double>> mergedVariables = new ArrayList<>();
        int size = -1;
        for (List<Double> variableRange : variableRanges.values()) {
            if (size == -1) {
                size = variableRange.size();
            } else if (variableRange.size() != size) {
                throw new IllegalArgumentException("ERR; (IllegalArgumentException) All variables must have the same number of values for element-wise merge.");
            }
        }

        for (int index = 0; index < size; index++) {
            List<Double> currentMerge = new ArrayList<>();
            for (List<Double> variableRange : variableRanges.values()) {
                currentMerge.add(variableRange.get(index));
            }
            mergedVariables.add(currentMerge);
        }
        return mergedVariables;
    }


}

