#  Bayesian Network Parser for .bif file 
As the title suggests, this project contains a useful JAVA parser for building [Bayesian Network](https://github.com/aimacode/aima-java/blob/AIMA3e/aima-core/src/main/java/aima/core/probability/bayes/BayesianNetwork.java) from a .bif file.
## ```BifBNReader``` class
The class ```BifBNReader``` is **abstract**, so you have to **implement the node creation method**.


### Example
The follow is an example of use:
```java
BifBNReader bnReader = new BifBNReader("<file-name>.bif") {
    @Override
    protected Node nodeCreation(RandomVariable var, double[] probs, Node... parents) {
        return new FullCPTNode(var, probs, parents);
    }
};

BayesianNetwork bn = bnReader.getBayesianNetwork();
```
### New updates
Now probabilities are sorted, and BayesianNetwork objects can be built correctly.

:warning: **I'm sorry but javadocs are actually missing, I'll add them as soon as a I can.**
