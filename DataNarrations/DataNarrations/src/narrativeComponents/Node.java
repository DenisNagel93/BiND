package narrativeComponents;

import java.util.HashSet;

public class Node {

    //Node label
    String label;
    //Set of outgoing edges
    HashSet<Edge> outgoing;
    //Concepts associated with the node label
    HashSet<HashSet<String>> concepts;

    //Constructor (no adjacent edges)
    public Node(String label) {
        this.label = label;
        outgoing = new HashSet<>();
        this.concepts = new HashSet<>();
    }

    //Adds an adjacent outgoing edge
    public void addEdge(Edge e) {
        this.outgoing.add(e);
    }

    //Adds a new associated concept
    public void addConcepts(HashSet<String> terms) {
        this.concepts.add(terms);
    }

    //Equals Method
    public boolean equals(Node compare) {
        return compare.getLabel().equals(this.getLabel());
    }

    //----------Getter/Setter----------

    public String getLabel() {
        return this.label;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return this.concepts;
    }

    //--------------------
}
