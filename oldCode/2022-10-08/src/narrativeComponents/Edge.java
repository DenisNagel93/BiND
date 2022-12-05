package narrativeComponents;

//An edge of a narrative
public class Edge {

    //Edge label
    String label;
    //Origin- and Target-Node
    Node nodeA, nodeB;

    //Constructor
    public Edge(String label, Node nodeA, Node nodeB) {
        this.label = label;
        this.nodeA = nodeA;
        this.nodeB = nodeB;
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

    //--------------------

}
