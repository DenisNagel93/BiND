package matches;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;
import narrativeComponents.Narrative;

import java.util.HashMap;
import java.util.HashSet;

public class SchemaMatch {

    Narrative n;
    HashMap<Event,EventMatch> eventMatches;
    HashSet<DataSet> bindingCandidates;
    double score;
    boolean isSuccessful;

    public SchemaMatch(Narrative n,HashMap<Event,EventMatch> eventMatches) {
        this.isSuccessful = true;
        this.n = n;
        this.eventMatches = eventMatches;
        this.bindingCandidates = collectCandidates();
        this.score = computeScore();
    }

    public double computeScore() {
        score = 0.0;
        for (Event e : eventMatches.keySet()) {
            try {
                score = score + eventMatches.get(e).getScore();
            } catch (Exception ex) {

            }
        }
        score = score / eventMatches.size();
        return score;
    }

    public HashSet<DataSet> collectCandidates() {
        HashSet<DataSet> candidates = new HashSet<>();
        for (Event e : eventMatches.keySet()) {
            try {
                candidates.add(eventMatches.get(e).getA().getDs());
            } catch (Exception ex) {

            }
        }
        return candidates;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public Attribute getMatchedAttribute(Event e) {
        return eventMatches.get(e).getA();
    }

    public HashSet<DataSet> getBindingCandidates() {
        return bindingCandidates;
    }

    public Narrative getN() {
        return n;
    }

    public HashMap<Event, EventMatch> getEventMatches() {
        return eventMatches;
    }

    public double getScore() {
        return score;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }
}
