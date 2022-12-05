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
    boolean isTitleMatch;
    boolean perfectTitleMatch;

    public PropertyMatch(EventProperty ep, Attribute a, double score) {
        this.ep = ep;
        this.e = ep.getNodeA();
        this.a = a;
        this.score = score;
        this.isTitleMatch = false;
        this.perfectTitleMatch = false;
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

    public boolean isTitleMatch() {
        return isTitleMatch;
    }

    public void setTitleMatch(boolean titleMatch) {
        isTitleMatch = titleMatch;
    }

    public void setPerfectTitleMatch(boolean perfectTitleMatch) {
        this.perfectTitleMatch = perfectTitleMatch;
    }
}
