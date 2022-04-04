package narrativeComponents;

import java.util.HashSet;

public class Edge {

    //Edge label
    String label;
    //Origin- and Target-Node
    Node nodeA, nodeB;
    //Concepts associated with the relation label
    HashSet<HashSet<String>> concepts;

    //Constructor
    public Edge(String label, Node nodeA, Node nodeB) {
        this.label = label;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.concepts = new HashSet<>();
    }

    //Adds a new associated concept
    public void addConcepts(HashSet<String> terms) {
        this.concepts.add(terms);
    }

    //----------Getter/Setter----------

    public String getLabel() {
        return label;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return concepts;
    }

    //--------------------

}
