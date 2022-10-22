package matches;

public class NarrativeBinding {

    RelationScore score;
    InstanceMatch imA,imB;
    String supports;

    public NarrativeBinding(InstanceMatch imA, InstanceMatch imB, RelationScore score, String supports) {
        this.imA = imA;
        this.imB = imB;
        this.score = score;
        this.supports = supports;
    }

    public InstanceMatch getImA() {
        return imA;
    }

    public InstanceMatch getImB() {
        return imB;
    }

    public RelationScore getScore() {
        return score;
    }

    public String getSupport() {
        return supports;
    }
}
