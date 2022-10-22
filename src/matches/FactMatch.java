package matches;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import narrativeComponents.Event;
import narrativeComponents.FactualRelation;

public class FactMatch {

    Event e;
    FactualRelation fr;
    Attribute a;
    double score;

    public FactMatch(FactualRelation fr, Event e, Attribute a, double score) {
        this.e = e;
        this.fr = fr;
        this.a = a;
        this.score = score;
    }

    public DataSet getDS() {
        return a.getDs();
    }

    public Event getE() {
        return e;
    }

    public FactualRelation getFR() {
        return fr;
    }

    public Attribute getA() {
        return a;
    }

    public double getScore() {
        return score;
    }
}
