package narrativeComponents;

public class FactualRelation extends Edge {

    public FactualRelation(String label, Entity nodeA, Entity nodeB) {
        super(label, nodeA, nodeB);
    }

    public Entity getNodeA() {
        return (Entity) this.nodeA;
    }

    public Entity getNodeB() {
        return (Entity) this.nodeB;
    }
}
