package narrativeComponents;

public class NarrativeRelation extends Edge {

    public NarrativeRelation(String label, Event nodeA, Event nodeB) {
        super(label, nodeA, nodeB);
    }

    public Event getNodeA() {
        return (Event) this.nodeA;
    }

    public Event getNodeB() {
        return (Event) this.nodeB;
    }
}
