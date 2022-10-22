package narrativeComponents;

import java.util.HashSet;

public class Node {

    //Node label
    String label;
    //Set of outgoing edges
    HashSet<Edge> outgoing;

    //Constructor (no adjacent edges)
    public Node(String label) {
        this.label = label;
        outgoing = new HashSet<>();
    }

    //Adds an adjacent outgoing edge
    public void addEdge(Edge e) {
        this.outgoing.add(e);
    }

    //Equals Method
    public boolean equals(Node compare) {
        return compare.getLabel().equals(this.getLabel());
    }

    //----------Getter/Setter----------

    public String getLabel() {
        return this.label;
    }

    //--------------------
}
