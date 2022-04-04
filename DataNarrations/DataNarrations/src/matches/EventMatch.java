package matches;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.util.HashMap;
import java.util.HashSet;

public class EventMatch {

    Event e;
    Attribute a;
    DataSet d;
    double score;
    double refinedScore;
    boolean refinedSuccessful;

    HashMap<EventProperty,PropertyMatch> pm;
    HashMap<FactualRelation,FactMatch> fm;

    public EventMatch(Event e, Attribute a, double score) {
        this.refinedSuccessful = true;
        this.e = e;
        this.a = a;
        this.d = a.getDs();
        this.score = score;
        this.refinedScore = refinedScore;
        this.pm = new HashMap<>();
        this.fm = new HashMap<>();
    }

    public HashSet<Attribute> getAssociatedAttributes() {
        HashSet<Attribute> associated = new HashSet<>();
        associated.add(this.a);
        for (EventProperty ep : pm.keySet()) {
            if (ep.getNodeA() == e) {
                if (pm.get(ep).getScore() >= 0.5) {
                    associated.add(pm.get(ep).getA());
                }
                for (FactualRelation fr : fm.keySet()) {
                    if (fr.getNodeA() == ep.getNodeB()) {
                        if (fm.get(fr).getScore() > 0.5) {
                            associated.add(fm.get(fr).getA());
                        }
                    }
                }
            }
        }
        return associated;
    }

    public boolean isRefinedSuccessful() {
        return refinedSuccessful;
    }

    public Event getE() {
        return e;
    }

    public Attribute getA() {
        return a;
    }

    public DataSet getD() {
        return d;
    }

    public double getScore() {
        return score;
    }

    public double getRefinedScore() {
        return refinedScore;
    }

    public HashMap<EventProperty,PropertyMatch> getPm() {
        return pm;
    }

    public HashMap<FactualRelation,FactMatch> getFm() {
        return fm;
    }

    public void setPm(HashMap<EventProperty,PropertyMatch> pm) {
        this.pm = pm;
    }

    public void setFm(HashMap<FactualRelation,FactMatch> fm) {
        this.fm = fm;
    }

    public void setRefinedSuccessful(boolean refinedSuccessful) {
        this.refinedSuccessful = refinedSuccessful;
    }

    public void setRefinedScore(double refinedScore) {
        this.refinedScore = refinedScore;
    }
}
