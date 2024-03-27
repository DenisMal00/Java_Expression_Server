package server.request;

public class ComputationRequest {
    private String valuesKind;
    private String computationKind;
    private String variable;
    private String expression;

    public ComputationRequest(String valuesKind, String computationKind, String variable, String expression) {
        this.valuesKind = valuesKind;
        this.computationKind = computationKind;
        this.variable = variable;
        this.expression = expression;
    }

    public String getValuesKind() {
        return valuesKind;
    }

    public void setValuesKind(String valuesKind) {
        this.valuesKind = valuesKind;
    }

    public String getComputationKind() {
        return computationKind;
    }

    public void setComputationKind(String computationKind) {
        this.computationKind = computationKind;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}

