package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import matches.EventMatch;
import matches.FactMatch;
import matches.PropertyMatch;
import matches.SchemaMatch;
import narrativeComponents.Edge;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EventRefinement {

    ArrayList<EventMatch> em;
    File path;

    public EventRefinement(ArrayList<EventMatch> em, File path) throws IOException {
        this.em = em;
        this.path = path;
        for (EventMatch s : em) {
            refineEvent(s);
        }
    }

    public void refineEvent(EventMatch s) throws IOException {
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
        int count = 1;
        for (PropertyMatch p : s.getPm().values()) {
            count++;
            baseScore = baseScore + p.getScore();
        }
        for (FactMatch f : s.getFm().values()) {
            count++;
            baseScore = baseScore + f.getScore();
        }
        return baseScore / (double) count;
    }

    public HashMap<EventProperty,PropertyMatch> collectPropertyMatches(EventMatch s) throws IOException {
        HashMap<EventProperty,PropertyMatch> matches = new HashMap<>();
        for (EventProperty ep : s.getE().getProperties()) {
            PropertyMatch p = findPropertyMatch(ep,s.getD(),0.5);
            if (p == null) {
                s.setRefinedSuccessful(false);
            }
            matches.put(ep,p);
        }
        return matches;
    }

    public HashMap<FactualRelation,FactMatch> collectFactMatches(EventMatch s) throws IOException {
        HashMap<FactualRelation,FactMatch> matches = new HashMap<>();
        for (EventProperty ep : s.getE().getProperties()) {
            for (FactualRelation fr : ep.getNodeB().getProperties()) {
                FactMatch f = findFactMatch(fr,s.getE(),s.getD(),0.5);
                if (f == null) {
                    s.setRefinedSuccessful(false);
                }
                matches.put(fr,f);
            }
        }
        return matches;
    }

    public PropertyMatch findPropertyMatch(EventProperty rel, DataSet d, double t) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            String[] subString = line.split(",");
            if (rel.getLabel().equals(subString[0])) {
                for (Attribute a : d.getAttributes()) {
                    if (a.getTitle().replace("\"","").equals(subString[1]) && Double.parseDouble(subString[2]) > t) {
                        PropertyMatch pm = new PropertyMatch(rel,a,Double.parseDouble(subString[2]));
                        return pm;
                    }
                }
            }
        }
        return null;
    }

    public FactMatch findFactMatch(FactualRelation rel, Event e, DataSet d, double t) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            String[] subString = line.split(",");
            if (rel.getLabel().equals(subString[0])) {
                for (Attribute a : d.getAttributes()) {
                    if (a.getTitle().replace("\"","").equals(subString[1]) && Double.parseDouble(subString[2]) > t) {
                        FactMatch fm = new FactMatch(rel,e,a,Double.parseDouble(subString[2]));
                        return fm;
                    }
                }
            }
        }
        return null;
    }

    /*public void printResult(String outputPath) throws IOException {
        File outfile = new File(outputPath + "/EventRefinement/" + em.getN().getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (SchemaMatch sm : sm.getMatching()) {
            bw.write("MatchScore: " + sm.getScore());
            bw.newLine();
            for (Event e : sm.getEventMatches().keySet()) {
                bw.write(e.getLabel());
                bw.write("(" + sm.getEventMatches().get(e).getScore() + ") "
                        + "-> " + sm.getEventMatches().get(e).getA().getTitle()
                        + " (" + sm.getEventMatches().get(e).getA().getDs().getSrc() + ")");
                bw.newLine();
                for (EventProperty ep : e.getProperties()) {
                    bw.write("-" + ep.getLabel());
                    try {
                        bw.write("(" + sm.getPm().get(ep).getScore() + ") "
                                + "-> " + sm.getPm().get(ep).getA().getTitle()
                                + " (" + sm.getPm().get(ep).getA().getDs().getSrc() + ")");
                        bw.newLine();
                    } catch (Exception ex) {
                        bw.write(" -> No match found!");
                        bw.newLine();
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + sm.getFm().get(fr).getScore() + ") "
                                    + "-> " + sm.getFm().get(fr).getA().getTitle()
                                    + " (" + sm.getFm().get(fr).getA().getDs().getSrc() + ")");
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
    }*/

    public ArrayList<EventMatch> getEventMatching() {
        return em;
    }
}
