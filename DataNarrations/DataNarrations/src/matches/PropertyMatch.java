package matches;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;

public class PropertyMatch {

    Event e;
    EventProperty ep;
    Attribute a;
    double score;

    public PropertyMatch(EventProperty ep, Attribute a, double score) {
        this.ep = ep;
        this.e = ep.getNodeA();
        this.a = a;
        this.score = score;
    }

    public DataSet getDS() {
        return a.getDs();
    }

    public Event getE() {
        return e;
    }

    public EventProperty getEP() {
        return ep;
    }

    public Attribute getA() {
        return a;
    }

    public double getScore() {
        return score;
    }
}
