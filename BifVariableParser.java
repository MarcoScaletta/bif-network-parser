package networkbuilding;

import aima.core.probability.RandomVariable;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.util.RandVar;

import java.util.Arrays;

public class BifVariableParser {

    private static final String HEADER_NAME_REGEX = "(variable) (\\w+) \\{";
    private static final String TYPE_VAL = "([^;,]+)";
    private static final String TYPE_VALUE_REGEX =
            "(\\s*)(type)(\\s*)(\\w+)(\\s*)(\\[)(\\s*)(\\d+)(\\s*)](\\s*)" +
                    "\\{ ((\\w+)|("+TYPE_VAL+", )+"+TYPE_VAL+") };";
    private static final String CLOSE = "}";

    private RandomVariable parsedVariable;

    public BifVariableParser(String toParse) throws Exception {
        String varName;
        Object [] values;
        Domain domain = null;

        if(!toParse.matches(HEADER_NAME_REGEX+"\n"+TYPE_VALUE_REGEX+"\n"+CLOSE))
            throw new Exception("wrong variable definition " + toParse);

        String[] parsed = toParse.replaceAll(
                "(variable)|(\\s+type\\s+)|(,)|(;)|(\n)|\\{|}|\\[|]", "")
                .replaceAll("(\\s+\\s)", " ").trim().split(" ");

        varName = parsed[0];

        if (parsed[1].equals("discrete")) {
            values = Arrays.copyOfRange(parsed,3,parsed.length);
            if(values.length != Integer.parseInt(parsed[2]))
                throw new Exception("error defining domain size for variable "+"["+varName+"]");
            domain = new ArbitraryTokenDomain(values);
        }else if(parsed[1].equals("boolean"))
            domain = new BooleanDomain();

        if(domain == null)
            throw new Exception("domain not defined for variable "+"["+varName+"]");

        this.parsedVariable = new RandVar(varName.toUpperCase(),domain);
    }

    public RandomVariable getParsedVariable() {
        return parsedVariable;
    }
}
