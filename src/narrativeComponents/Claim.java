package narrativeComponents;

public class Claim extends Edge {

    boolean isUnary;
    boolean isBinary;

    public Claim(String label, Event nodeA, Node nodeB) {
        super(label, nodeA, nodeB);
        this.isUnary = false;
        this.isBinary = false;
    }

    public Event getNodeA() {
        return (Event) this.nodeA;
    }

    public Event getEventB() {
        return (Event)this.nodeB;
    }

    public Entity getNodeB() {
        if (isUnary) {
            return null;
        }
        return (Entity)this.nodeB;
    }

    public boolean isBinary() {
        return isBinary;
    }

    public void setBinary(boolean binary) {
        isBinary = binary;
    }

    public void setUnary(boolean unary) {
        isUnary = unary;
    }
}
