package matches;

import narrativeComponents.NarrativeRelation;

import java.util.HashMap;

public class NarrativeBinding {

    InstanceMatch im;
    HashMap<NarrativeRelation,RelationScore> scores;

    RelationScore score;
    InstanceMatch imA,imB;

    public NarrativeBinding(InstanceMatch im, HashMap<NarrativeRelation,RelationScore> scores) {
        this.im = im;
        this.scores = scores;
    }

    public NarrativeBinding(InstanceMatch imA, InstanceMatch imB, RelationScore score) {
        this.imA = imA;
        this.imB = imB;
        this.score = score;
    }

    public InstanceMatch getIm() {
        return im;
    }

    public InstanceMatch getImA() {
        return imA;
    }

    public InstanceMatch getImB() {
        return imB;
    }

    public HashMap<NarrativeRelation, RelationScore> getScores() {
        return scores;
    }

    public RelationScore getScore() {
        return score;
    }
}
