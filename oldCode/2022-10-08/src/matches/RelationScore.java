package matches;

import datasetComponents.ColumnPair;
import narrativeComponents.NarrativeRelation;

public class RelationScore {

    NarrativeRelation nr;
    ColumnPair cp;
    String metric;
    double score;

    public RelationScore(NarrativeRelation nr,ColumnPair cp,double score) {
        this.nr = nr;
        this.cp = cp;
        this.metric = "";
        this.score = score;
    }

    public double getScore() {
        return score;
    }

}
