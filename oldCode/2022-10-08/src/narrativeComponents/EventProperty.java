package narrativeComponents;

public class EventProperty extends Edge {

    public EventProperty(String label, Event nodeA, Entity nodeB) {
        super(label, nodeA, nodeB);
    }

    @Override
    public Event getNodeA() {
        return (Event) this.nodeA;
    }

    @Override
    public Entity getNodeB() {
        return (Entity) this.nodeB;
    }
}
