
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import aima.core.probability.bayes.impl.BayesNet;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public abstract class BifBNReader {


    private List<BifVariableParser> bifVariableParsers;
    private List<BifProbabilityParser> bifProbabilityParsers;

    private Map<String, RandomVariable> variableHashMap = new HashMap<>();

    private BayesianNetwork bayesianNetwork;

    public BifBNReader(String file) throws Exception {
        bifVariableParsers = new ArrayList<>();
        bifProbabilityParsers = new ArrayList<>();
        Logger.log("Building bayesian network from file <" + file + "> ...");
        bayesianNetwork = buildNetwork(file);
        System.out.println("Bayesian network built.");
    }

    private BayesianNetwork buildNetwork(String file) throws Exception {


        Logger.log("Parsing file <" + file + "> ...");
        varAndProbDef(file);
        Logger.log("End of parsing.");


        Logger.log("Sorting probability...");
        sortProbParsers();
        Logger.log("End of sorting.");

        Map<String, Node> nodeHashMap = new HashMap<>();
        Set<Node> roots = new HashSet<>();

        String actualVar;
        Set<String> varsName;

        Set<Node> parents;

        Node actualNode;


        fixProbabilities();

        for (BifProbabilityParser p : bifProbabilityParsers){
            actualVar = p.getCondVar();
            varsName = new HashSet<>(p.getParsedVariables());

            if(!variableHashMap.containsKey(actualVar))
                throw new Exception("Variable ["+ actualVar +"] not exists");
            if(varsName.size() > 0){
                parents = new HashSet<>();

                for (String varName : varsName){
                    if(!variableHashMap.containsKey(varName))
                        throw new Exception("Variable ["+ varName +"] not exists");
                    if(!nodeHashMap.containsKey(varName))
                        throw new Exception("Probability for ["+ varName +"] not defined");
                    parents.add(nodeHashMap.get(varName));
                }

                actualNode = nodeCreation(variableHashMap.get(actualVar),p.getProbabilities(),
                        parents.toArray(new Node[parents.size()]));
            }else{
                actualNode = nodeCreation(variableHashMap.get(actualVar),p.getProbabilities());
                roots.add(actualNode);
            }
            nodeHashMap.put(actualVar,actualNode);

        }

        return new BayesNet(roots.toArray(new Node[0]));
    }



    private Pair<String[],String[]> varAndProbDef(String filename) {

        BifProbabilityParser bifProbabilityParser;
        long line = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {

            String sCurrentLine,definition;

            while ((sCurrentLine = br.readLine()) != null) {
                line++;
                definition = "";
                String PROB = "probability";
                String VAR = "variable";
                if(sCurrentLine.startsWith(PROB) || sCurrentLine.startsWith(VAR)){
                    do{
                        definition += sCurrentLine+"\n";
                        sCurrentLine = br.readLine();
                        line++;
                    }while(!sCurrentLine.startsWith("}"));
                    definition += sCurrentLine;
                    if(definition.startsWith(PROB)) {
                        try {
                            boolean foundFirst = false;

                            bifProbabilityParser = new BifProbabilityParser(definition, variableHashMap);
                            for (int i = 0; i <  bifProbabilityParsers.size() && !foundFirst; i++) {
                                if(bifProbabilityParsers.get(i).getParsedVariables().contains(
                                        bifProbabilityParser.getCondVar())){
                                    foundFirst = true;
                                    if(i>0){
                                        bifProbabilityParsers.add(i-1, bifProbabilityParser);
                                    }else
                                        bifProbabilityParsers.add(0, bifProbabilityParser);
                                }
                            }
                            if(!foundFirst)
                                bifProbabilityParsers.add(bifProbabilityParser);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.err("line:" + line);
                        }
                    }
                    else {

                        try {
                            BifVariableParser v = new BifVariableParser(definition);
                            bifVariableParsers.add(v);
                            variableHashMap.put(v.getParsedVariable().getName(), v.getParsedVariable());

                        } catch (Exception e) {
                            e.printStackTrace();
                            Logger.err("line:" + line);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Node nodeCreation(RandomVariable var, double[] probs){
        return nodeCreation(var, probs,(Node[]) null);
    }

    private void sortProbParsers() throws Exception {
        Set<String> missing = missingVariables(bifProbabilityParsers);
        List<BifProbabilityParser> parsers;
        Set<String> added = new HashSet<>();
        if(missing.size()>0)
            throw new Exception("Missing probability for "+ missing);
        parsers = new ArrayList<>();
        BifProbabilityParser actual;
        while(bifProbabilityParsers.size() >0){
            actual = bifProbabilityParsers.remove(0);
            if(added.containsAll(actual.getParsedVariables())) {
                parsers.add(actual);
                added.add(actual.getCondVar());
            }else
                bifProbabilityParsers.add(actual);
        }
        bifProbabilityParsers =  parsers;
    }

    private void fixProbabilities() throws Exception {
        RandomVariable var;
        double sum;
        double error =Math.pow(10,-6);
        for (BifProbabilityParser p : bifProbabilityParsers){
            sum = 0;
            var = variableHashMap.get(p.getCondVar());

            for (int i=0;i<=p.getProbabilities().length;i++){
                if(var == null)
                    throw new Exception("var is null for " + p);

                if(var.getDomain() == null)
                    throw new Exception("var.getDomain() is null");

                if(i>0 && i%var.getDomain().size()==0){
                    if(Math.abs(1-sum) > 0){
                        if(Math.abs(1-sum) > error)
                            throw new Exception("Row "+ ((i/var.getDomain().size()) + 1) +" of CPT does not sum to 1.0 with error equals to "+error + " (sum to "+sum+") " + p);
                        else{
                            p.getProbabilities()[i-1]+=(1-sum);
                        }
                    }
                    sum = 0;
                }
                if(i<p.getProbabilities().length) {
                    if ((sum + p.getProbabilities()[i] - p.getProbabilities()[i] != sum)) {
                        sum = sum * 1000;
                        sum += (p.getProbabilities()[i] * 1000);
                        sum = sum / 1000;
                    } else
                        sum += p.getProbabilities()[i];

                }
            }
        }
    }

    private Set<String> missingVariables(List<BifProbabilityParser> set){
        Set<String> allVar = new HashSet<>();
        Set<String> missingVariable = new  HashSet<>();
        for (BifProbabilityParser parser : set)
            allVar.add(parser.getCondVar());
        for(BifProbabilityParser parser : set)
            for (String s : parser.getParsedVariables())
                if(!allVar.contains(s))
                    missingVariable.add(s);
        return missingVariable;

    }

    protected abstract Node nodeCreation(RandomVariable var, double[] probs, Node... parents);

    // PUBLIC METHODS

    public BayesianNetwork getBayesianNetwork() {
        return bayesianNetwork;
    }
}
