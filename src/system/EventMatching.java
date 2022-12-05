package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import evaluation.EvaluationTest;
import io.Embedding;
import io.Vars;
import matches.EventMatch;
import narrativeComponents.Event;

import java.util.ArrayList;
import java.util.HashSet;

public class EventMatching {

    Event e;
    HashSet<DataSet> repository;
    Embedding attributeEmbedding,titleEmbedding;
    ArrayList<EventMatch> matching;
    HashSet<DataSet> matchedData;

    public EventMatching(Event e, HashSet<DataSet> repository, Embedding attributeEmbedding, Embedding titleEmbedding, double t) {
        this.e = e;
        this.repository = repository;
        this.attributeEmbedding = attributeEmbedding;
        this.titleEmbedding = titleEmbedding;
        this.matchedData = new HashSet<>();
        this.matching = readEmbedding(t);
        setPlaceholderMatches(t);
    }


    public ArrayList<EventMatch> readEmbedding(double t) {
        ArrayList<EventMatch> matches = new ArrayList<>();
        for (ArrayList<String> line : attributeEmbedding.getRankedEmbedding().get(e.getCaption())) {
            if (Double.parseDouble(line.get(1)) >= t) {
                for (DataSet d : Vars.atrIndex.get(line.get(0))) {
                    if (!matchedData.contains(d)) {
                        for (Attribute a : d.getAttributes()) {
                            if (a.getTitle().replace("\"", "").equals(line.get(0))) {
                                EventMatch em = new EventMatch(e, a, Double.parseDouble(line.get(1)));
                                if (em.checkConstraints()) {
                                    matches.add(em);
                                    matchedData.add(d);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                break;
            }
        }
        return matches;
    }

    public void setPlaceholderMatches(double t) {
        for (ArrayList<String> line : titleEmbedding.getRankedEmbedding().get(e.getCaption())) {
            DataSet d = Vars.titleIndex.get(line.get(0));
            if (Double.parseDouble(line.get(1)) >= t) {
                if (!matchedData.contains(d)) {
                    Attribute ph = new Attribute(e.getCaption() + "_Placeholder",d,true);
                    EventMatch em = new EventMatch(e,ph,Double.parseDouble(line.get(1)));
                    this.matching.add(em);
                    matchedData.add(d);
                }
            }
        }
    }

    public Event getE() {
        return e;
    }

    public ArrayList<EventMatch> getMatching() {
        return matching;
    }
}
