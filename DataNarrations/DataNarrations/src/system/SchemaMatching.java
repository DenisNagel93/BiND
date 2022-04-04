package system;

import datasetComponents.DataSet;
import matches.EventMatch;
import matches.SchemaMatch;
import narrativeComponents.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SchemaMatching {

    Narrative n;
    HashSet<DataSet> repository;
    HashSet<SchemaMatch> matching;
    File eventEmbedding,factEmbedding;

    //Schema Matching via Entity Linking
    public SchemaMatching(Narrative n, HashSet<DataSet> repository, File eventEmbedding, File factEmbedding) throws IOException {
        this.n = n;
        this.repository = repository;
        this.eventEmbedding = eventEmbedding;
        this.factEmbedding = factEmbedding;
        this.matching = buildSchemaMatch();
    }

    public ArrayList<EventMatch> collectEventMatches(Event e) throws IOException {
        System.out.println("--------Run Event Matching");
        EventMatching em = new EventMatching(e,repository,eventEmbedding);
        ArrayList<EventMatch> match = em.getThreshold(0.65);
        System.out.println("--------Run Event Refinement");
        EventRefinement er = new EventRefinement(match,factEmbedding);
        return match;
    }

    public List<List<EventMatch>> collectEventMatches() throws IOException {
        List<List<EventMatch>> matchings = new ArrayList<>();
        for (Event e : n.getEvents()) {
            EventMatching em = new EventMatching(e,repository,eventEmbedding);
            //ArrayList<EventMatch> match = em.getMatching();
            //ArrayList<EventMatch> match = em.getTopResults(1);
            ArrayList<EventMatch> match = em.getThreshold(0.65);
            matchings.add(match);
        }
        return matchings;
    }

    public EventMatch getBestMatch(Event e,ArrayList<EventMatch> matches) {
        boolean needsRelativeValues = false;
        HashSet<NarrativeRelation> toCheck = new HashSet<>();
        toCheck.addAll(e.getRelations());
        toCheck.addAll(e.getIngoing());
        for (NarrativeRelation nr : toCheck) {
            if (nr.getLabel().equals("higher probability") || nr.getLabel().equals("lower probability")) {
                needsRelativeValues = true;
                break;
            }
        }
        EventMatch bestMatch = null;
        double eventScore = 0.0;
        boolean firstFound = false;
        for (int i = 0; i < matches.size(); i++) {
            //if (firstFound) {
            //    System.out.println("Best Match: " + bestMatch.getA().getTitle() + " -> " + bestMatch.getRefinedScore());
            //}
            //System.out.println("Check Match: " + matches.get(i).getA().getTitle() + " -> " + matches.get(i).getRefinedScore());
            if (matches.get(i).getScore() < 0.9 * eventScore) {
                break;
            } else {
                if (matches.get(i).isRefinedSuccessful()) {
                    if (!needsRelativeValues) {
                        if (!firstFound) {
                            eventScore = matches.get(i).getScore();
                            bestMatch = matches.get(i);
                            firstFound = true;
                        } else {
                            if (matches.get(i).getRefinedScore() > bestMatch.getRefinedScore()) {
                                bestMatch = matches.get(i);
                            }
                        }
                    } else {
                        if (matches.get(i).getA().isRelative()) {
                            if (!firstFound) {
                                eventScore = matches.get(i).getScore();
                                bestMatch = matches.get(i);
                                firstFound = true;
                            } else {
                                if (matches.get(i).getRefinedScore() > bestMatch.getRefinedScore()) {
                                    bestMatch = matches.get(i);
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestMatch;
    }

    public HashSet<SchemaMatch> buildSchemaMatch() throws IOException {
        HashSet<SchemaMatch> schemaMatches = new HashSet<>();
        HashMap<Event,EventMatch> matches = new HashMap<>();
        boolean success = true;
        for (Event e : n.getEvents()) {
            EventMatch bestMatch = getBestMatch(e,collectEventMatches(e));
            if (bestMatch == null) {
                success = false;
            }
            matches.put(e,bestMatch);
        }
        SchemaMatch sm = new SchemaMatch(n,matches);
        if (!success) {
            sm.setSuccessful(false);
        }
        schemaMatches.add(sm);
        return schemaMatches;
    }

    public HashSet<SchemaMatch> buildSchemaMatches() throws IOException {
        HashSet<SchemaMatch> matches = new HashSet<>();
        List<List<EventMatch>> eventMatches = collectEventMatches();
        //System.out.println("EventMatches Size: " + eventMatches.size());
        List<List<EventMatch>> cartesian = Helper.calculateCartesianProduct(eventMatches);
        for (int i = 0; i < cartesian.size(); i++) {
            HashMap<Event,EventMatch> map = new HashMap<>();
            for (EventMatch em : cartesian.get(i)) {
                map.put(em.getE(),em);
            }
            SchemaMatch sm = new SchemaMatch(n,map);
            matches.add(sm);
        }
        return matches;
    }

    public void printResult(String outputPath) throws IOException {
        File outfile = new File(outputPath + "/SchemaMatching/" + this.getN().getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (SchemaMatch sm : this.getMatching()) {
            bw.write("MatchScore: " + sm.getScore());
            bw.newLine();
            for (Event e : sm.getEventMatches().keySet()) {
                bw.write(e.getLabel());
                try {
                    bw.write("(" + sm.getEventMatches().get(e).getScore() + ") "
                            + "-> " + sm.getEventMatches().get(e).getA().getTitle()
                            + " (" + sm.getEventMatches().get(e).getA().getDs().getSrc() + ")");
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write(" -> No match found!");
                    bw.newLine();
                }
                for (EventProperty ep : e.getProperties()) {
                    bw.write("-" + ep.getLabel());
                    try {
                        bw.write("(" + sm.getEventMatches().get(e).getPm().get(ep).getScore() + ") "
                                + "-> " + sm.getEventMatches().get(e).getPm().get(ep).getA().getTitle()
                                + " (" + sm.getEventMatches().get(e).getPm().get(ep).getA().getDs().getSrc() + ")");
                        bw.newLine();
                    } catch (Exception ex) {
                        bw.write(" -> No match found!");
                        bw.newLine();
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + sm.getEventMatches().get(e).getFm().get(fr).getScore() + ") "
                                    + "-> " + sm.getEventMatches().get(e).getFm().get(fr).getA().getTitle()
                                    + " (" + sm.getEventMatches().get(e).getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
                bw.newLine();
            }
            bw.write("----------------");
            bw.newLine();
        }
        bw.close();
    }

    public HashSet<SchemaMatch> getMatching() {
        return matching;
    }

    public HashSet<DataSet> getCandidates(Event e) {
        return repository;
    }

    public HashSet<DataSet> getRepository() {
        return repository;
    }

    public Narrative getN() {
        return n;
    }
}
