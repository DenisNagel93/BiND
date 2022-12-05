package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import io.Embedding;
import matches.EventMatch;
import matches.FactMatch;
import matches.PropertyMatch;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.util.ArrayList;
import java.util.HashMap;

public class EventRefinement {

    ArrayList<EventMatch> em;
    Embedding factEmbedding,valueEmbedding,titleEmbedding;
    double tPM;
    double tEM;

    public EventRefinement(ArrayList<EventMatch> em, Embedding factEmbedding, Embedding valueEmbedding, Embedding titleEmbedding, double tPM, double tEM) {
        this.em = em;
        this.factEmbedding = factEmbedding;
        this.valueEmbedding = valueEmbedding;
        this.titleEmbedding = titleEmbedding;
        this.tPM = tPM;
        this.tEM = tEM;
        //System.out.println("#EventMatches: " + em.size());
        for (int i = 0; i < em.size();i++) {
            EventMatch s = em.get(i);
            if (s.getA().isPlaceholder()) {
                replacePlaceholder(s);
                if (!s.checkConstraints()) {
                    em.remove(s);
                }
            }
            refineEvent(s);
            if (!s.isRefinedSuccessful() || s.getRefinedScore() < tPM) {
                em.remove(s);
                i--;
            }
        }
    }

    public void replacePlaceholder(EventMatch s) {
        double max = 0.0;
        Attribute maxA = null;
        EventProperty maxP = null;
        for (EventProperty ep : s.getE().getProperties()) {
            for (Attribute a : s.getD().getAttributes()) {
                //Full embeddings needed
                if (valueEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()) != null) {
                    if (valueEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()).get(a.getTitle().replace("\"", "")) != null) {
                        double toCompare = valueEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()).get(a.getTitle().replace("\"", ""));
                        //if (toCompare >= t && toCompare >= max) {
                        if (toCompare >= max) {
                            max = toCompare;
                            maxA = a;
                            maxP = ep;
                        }
                    }
                }
            }
        }
        double newScore = (max + s.getScore()) / 2;
        if (newScore >= tEM && max > 0.0) {
            s.setScore(newScore);
            s.setA(maxA);
            s.getIntegrated().add(maxP);
        }
    }

    public void refineEvent(EventMatch s) {
        HashMap<EventProperty,PropertyMatch> properties = collectPropertyMatches(s);
        HashMap<FactualRelation,FactMatch> facts = collectFactMatches(s);
        s.setPm(properties);
        s.setFm(facts);
        if (s.isRefinedSuccessful()) {
            s.setRefinedScore(computeTotalScore(s));
        }
    }

    public double computeTotalScore(EventMatch s) {
        double baseScore = s.getScore();
        double propertyScore = 0.0;
        int count = 0;
        for (PropertyMatch p : s.getPm().values()) {
            count++;
            propertyScore = propertyScore + p.getScore();
        }
        for (FactMatch f : s.getFm().values()) {
            count++;
            propertyScore = propertyScore + f.getScore();
        }
        if (count > 0) {
            return (baseScore + (propertyScore / (double) count)) / 2;
        }
        return baseScore;
    }

    public PropertyMatch checkTitle(EventProperty ep, DataSet d) {
        //Full embeddings needed
        if (titleEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()) != null) {
            if (titleEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()).get(d.getTitle()) != null) {
                Attribute ph = new Attribute(d.getTitle() + "_Title", d);
                PropertyMatch pm = new PropertyMatch(ep, ph, titleEmbedding.getSimilarityScores().get(ep.getNodeB().getLabel()).get(d.getTitle()));
                return pm;
            }
        }
        return null;
    }

    public HashMap<EventProperty,PropertyMatch> collectPropertyMatches(EventMatch s) {
        HashMap<EventProperty,PropertyMatch> matches = new HashMap<>();
        for (EventProperty ep : s.getE().getProperties()) {
            if (!s.getIntegrated().contains(ep)) {
                PropertyMatch pR = findPropertyMatch(ep,s.getD());
                PropertyMatch pT = checkTitle(ep,s.getD());
                if (pR == null) {
                    if (pT == null) {
                        s.setRefinedSuccessful(false);
                    } else {
                        pT.setTitleMatch(true);
                        if (pT.getScore() >= 0.9) {
                            pT.setPerfectTitleMatch(true);
                        } else {
                            s.setFullMatch(false);
                        }
                        matches.put(ep,pT);
                    }
                } else {
                    if (pT == null) {
                        matches.put(ep,pR);
                    } else {
                        if (pR.getScore() >= pT.getScore() - 0.05) {
                            matches.put(ep,pR);
                        } else {
                            pT.setTitleMatch(true);
                            if (pT.getScore() >= 0.9) {
                                pT.setPerfectTitleMatch(true);
                            } else {
                                s.setFullMatch(false);
                            }
                            matches.put(ep,pT);
                        }
                    }
                }
            }
        }
        return matches;
    }

    /*public HashMap<EventProperty,PropertyMatch> collectPropertyMatchesOld(EventMatch s) {
        HashMap<EventProperty,PropertyMatch> matches = new HashMap<>();
        for (EventProperty ep : s.getE().getProperties()) {
            if (!s.getIntegrated().contains(ep)) {
                PropertyMatch p = findPropertyMatch(ep,s.getD());
                if (p == null) {
                    p = checkTitle(ep,s.getD());
                    if (p == null) {
                        s.setRefinedSuccessful(false);
                    }
                }
                matches.put(ep,p);
            }
        }
        return matches;
    }*/

    public HashMap<FactualRelation,FactMatch> collectFactMatches(EventMatch s) {
        HashMap<FactualRelation,FactMatch> matches = new HashMap<>();
        for (EventProperty ep : s.getE().getProperties()) {
            for (FactualRelation fr : ep.getNodeB().getProperties()) {
                FactMatch f = findFactMatch(fr,s.getE(),s.getD(),this.tPM);
                if (f == null) {
                    s.setRefinedSuccessful(false);
                }
                matches.put(fr,f);
            }
        }
        return matches;
    }

    public PropertyMatch findPropertyMatch(EventProperty rel, DataSet d) {
        double max = 0.0;
        PropertyMatch bestMatch = null;
        for (Attribute a : d.getAttributes()) {
            //Full embeddings needed
            if (factEmbedding.getSimilarityScores().get(rel.getLabel()).get(a.getTitle().replace("\"","")) != null) {
                double toCompare = factEmbedding.getSimilarityScores().get(rel.getLabel()).get(a.getTitle().replace("\"",""));
                //if (toCompare >= t && toCompare >= max) {
                if (toCompare >= max) {
                    max = toCompare;
                    bestMatch = new PropertyMatch(rel,a,toCompare);
                }
            }
        }
        return bestMatch;
    }

    public FactMatch findFactMatch(FactualRelation rel, Event e, DataSet d, double t) {
        double max = 0.0;
        FactMatch bestMatch = null;
        for (Attribute a : d.getAttributes()) {
            double toCompare = factEmbedding.getSimilarityScores().get(rel.getLabel()).get(a.getTitle().replace("\"", ""));
            if (toCompare >= t && toCompare >= max) {
                max = toCompare;
                bestMatch = new FactMatch(rel, e, a, toCompare);
            }
        }
        return bestMatch;
    }

    public ArrayList<EventMatch> getEventMatching() {
        return em;
    }
}
