package matches;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.util.HashMap;
import java.util.HashSet;

public class InstanceMatch implements Comparable<InstanceMatch> {

    EventMatch e;
    HashMap<FactMatch,HashSet<Record>> factReduction;
    HashMap<PropertyMatch,HashSet<Record>> propertyReduction;
    HashSet<Record> eventReduction;
    HashSet<Record> irrelevant;

    HashSet<Attribute> noMatch;
    boolean isReduced;
    boolean emptyMatch;

    public InstanceMatch(EventMatch e) {
        this.isReduced = true;
        this.emptyMatch = false;
        this.e = e;
        this.noMatch = new HashSet<>();
        this.factReduction = new HashMap<>();
        this.propertyReduction = new HashMap<>();
        this.eventReduction = getRelevantRecords();
        this.irrelevant = new HashSet<>(this.getE().getD().getRecords());
        this.irrelevant.removeAll(eventReduction);
    }

    public HashSet<Record> getRelevantRecords() {
        DataSet d = this.getE().getD();
        HashSet<Record> result = new HashSet<>(d.getRecords());
        for (EventProperty ep : this.getE().getE().getProperties()) {
            if (!this.getE().getIntegrated().contains(ep)) {
                //System.out.println("getRelRec");
                //System.out.println("Ev: " + this.getE().getE().getCaption());
                //System.out.println("Data: " + this.getE().getD().getTitle());
                //Fix later
                if (this.getE().getPm().get(ep) != null) {
                    HashSet<Record> temp = findRecords(this.getE().getPm().get(ep));
                    if (temp.size() > 0) {
                        result.retainAll(temp);
                    } else {
                        if (!this.getE().getPm().get(ep).isTitleMatch()) {
                            this.addNoMatch(this.getE().getPm().get(ep).getA());
                            isReduced = false;
                        }
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        temp = findRecords(this.getE().getFm().get(fr));
                        if (temp.size() > 0) {
                            result.retainAll(temp);
                        } else {
                            this.addNoMatch(this.getE().getFm().get(fr).getA());
                            isReduced = false;
                        }
                    }
                }
            }
        }
        if (result.size() == 0) {
            this.emptyMatch = true;
            //isReduced = false;
        }
        return result;
    }

    public HashSet<Record> findRecords(PropertyMatch p) {
        HashSet<Record> result = new HashSet<>();
        //System.out.println("Ev: " + p.getE().getCaption());
        //System.out.println("Rel: " + p.getEP().getLabel());
        //System.out.println("Atr: " + p.getA().getTitle());
        //System.out.println("Data: " + p.getDS().getTitle());
        for (Record r : p.getDS().getRecords()) {
            try {
                if (p.getEP().getNodeB().hasOperator()) {
                    switch (p.getEP().getNodeB().getOperator()) {
                        case "<" :
                            if (Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim()) < Double.parseDouble(p.getEP().getNodeB().getLabel().trim())) {
                                result.add(r);
                            }
                            break;
                        case ">" :
                            if (Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim()) > Double.parseDouble(p.getEP().getNodeB().getLabel().trim())) {
                                result.add(r);
                            }
                            break;
                        case "not" :
                            if (!r.getEntry(p.getA()).replace("\"","").trim().contains(p.getEP().getNodeB().getLabel().trim())) {
                                result.add(r);
                            }
                            break;
                        case "or" :
                            String[] options = p.getEP().getNodeB().getLabel().trim().split(":");
                            if (r.getEntry(p.getA()).replace("\"","").trim().contains(options[0])
                                    || r.getEntry(p.getA()).replace("\"","").trim().contains(options[1])) {
                                result.add(r);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    try {
                        double v1 = Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim());
                        double v2 = Double.parseDouble(p.getEP().getNodeB().getLabel().trim());
                        if (v1 == v2) {
                            result.add(r);
                        }
                    } catch (Exception e) {
                        if (r.getEntry(p.getA()).replace("\"","").trim().contains(p.getEP().getNodeB().getLabel().trim())) {
                            result.add(r);
                        }
                    }
                }
            } catch (Exception e) {
                //Remove try later!!
            }
        }
        if (result.size() == 0) {
            try {
                Double.parseDouble(p.getEP().getNodeB().getLabel().trim());
            } catch (Exception e) {
                if (p.getDS().getTitle().contains(p.getEP().getNodeB().getLabel().trim())) {
                    return p.getDS().getRecords();
                    //Change Matched Attribute to Title
                }
            }
        }

        return result;
    }

    public HashSet<Record> findRecords(FactMatch f) {
        HashSet<Record> result = new HashSet<>();
        for (Record r : f.getDS().getRecords()) {
            if (r.getEntry(f.getA()).replace("\"","").trim().contains(f.getFR().getNodeB().getLabel().trim())) {
                result.add(r);
            }
        }
        return result;
    }

    public HashSet<Record> getEventReduction() {
        return this.eventReduction;
    }

    public HashSet<Record> getIrrelevant() {
        return irrelevant;
    }

    public void addNoMatch(Attribute a) {
        this.noMatch.add(a);
    }

    public boolean isReduced() {
        return isReduced;
    }

    public boolean isEmptyMatch() {
        return emptyMatch;
    }

    public EventMatch getE() {
        return e;
    }

    public HashSet<Attribute> getNoMatch() {
        return noMatch;
    }

    public void setFactReduction(HashMap<FactMatch, HashSet<Record>> factReduction) {
        this.factReduction = factReduction;
    }

    public void setPropertyReduction(HashMap<PropertyMatch, HashSet<Record>> propertyReduction) {
        this.propertyReduction = propertyReduction;
    }

    public void setReduced(boolean reduced) {
        isReduced = reduced;
    }

    @Override
    public int compareTo(InstanceMatch o) {
        return Double.compare(this.getE().refinedScore, o.getE().getRefinedScore());
    }
}
