package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import matches.*;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InstanceMatching {

    SchemaMatching sm;
    HashSet<InstanceMatch> matching;

    public InstanceMatching(SchemaMatching sm) {
        this.sm = sm;
        this.matching = new HashSet<>();
        for (SchemaMatch s : sm.getMatching()) {
            InstanceMatch im = new InstanceMatch(s);
            if (s.isSuccessful()) {
                HashMap<FactMatch,HashSet<Record>> factReduction = new HashMap<>();
                HashMap<PropertyMatch,HashSet<Record>> propertyReduction = new HashMap<>();
                HashMap<EventMatch,HashSet<Record>> eventReduction = new HashMap<>();
                for (EventMatch m : s.getEventMatches().values()) {
                    for (PropertyMatch p : m.getPm().values()) {
                        HashSet<Record> red = findRecords(p);
                        propertyReduction.put(p,red);
                        if (red.size() > 0) {
                            im.setReduced(true);
                        }
                    }
                    for (FactMatch f : m.getFm().values()) {
                        HashSet<Record> red = findRecords(f);
                        factReduction.put(f,red);
                        if (red.size() > 0) {
                            im.setReduced(true);
                        }
                    }
                }
                im.setPropertyReduction(propertyReduction);
                im.setFactReduction(factReduction);
                for (Event e : s.getEventMatches().keySet()) {
                    eventReduction.put(s.getEventMatches().get(e), getRelevantRecords(im,e));
                }
                im.setEventReduction(eventReduction);
            }
            matching.add(im);
        }
    }

    public InstanceMatching(EventRefinement em) {
        this.matching = new HashSet<>();
        for (EventMatch m : em.getEventMatching()) {
            if (m.isRefinedSuccessful()) {
                InstanceMatch im = new InstanceMatch(m);
                HashMap<FactMatch,HashSet<Record>> factReduction = new HashMap<>();
                HashMap<PropertyMatch,HashSet<Record>> propertyReduction = new HashMap<>();
                HashMap<EventMatch,HashSet<Record>> eventReduction = new HashMap<>();
                for (PropertyMatch p : m.getPm().values()) {
                    HashSet<Record> red = findRecords(p);
                    propertyReduction.put(p,red);
                    if (red.size() > 0) {
                        im.setReduced(true);
                    }
                }
                for (FactMatch f : m.getFm().values()) {
                    HashSet<Record> red = findRecords(f);
                    factReduction.put(f,red);
                    if (red.size() > 0) {
                        im.setReduced(true);
                    }
                }
                im.setPropertyReduction(propertyReduction);
                im.setFactReduction(factReduction);
                eventReduction.put(m, getRelevantRecords(im,m));
                //eventReduction.put(m, getRelevantRecordsTotal(im,m));
                im.setEventReduction(eventReduction);
                matching.add(im);
            }
        }
    }

    public void printResult(String outputPath) throws IOException {
        File outfile = new File(outputPath + "/InstanceMatching/" + sm.getN().getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (InstanceMatch im : matching) {
            for (EventMatch e : im.getEventReduction().keySet()) {
                bw.write(e.getE().getLabel() + "-> " + im.getEventReduction().get(e).size() + " (" + e.getD().getSrc() + ")");
                bw.newLine();
            }
        }
        bw.close();
    }

    public HashSet<Record> getRelevantRecords(InstanceMatch i,EventMatch e) {
        DataSet d = e.getD();
        HashSet<Record> result = new HashSet<>();
        result.addAll(d.getRecords());
        //System.out.println("Original Size: " + result.size());
        for (EventProperty ep : e.getE().getProperties()) {
            HashSet<Record> temp = findRecords(e.getPm().get(ep));
            if (temp.size() > 0) {
                result.retainAll(temp);
            } else {
                i.addNoMatch(e.getPm().get(ep).getA());
            }
            for (FactualRelation fr : ep.getNodeB().getProperties()) {
                temp = findRecords(e.getFm().get(fr));
                if (temp.size() > 0) {
                    result.retainAll(temp);
                } else {
                    i.addNoMatch(e.getFm().get(fr).getA());
                }
            }
        }
        //System.out.println("Reduced Size: " + result.size());
        return result;
    }

    public HashSet<Record> getRelevantRecords(InstanceMatch i,Event e) {
        DataSet d = i.getS().getEventMatches().get(e).getD();
        HashSet<Record> result = new HashSet<>();
        result.addAll(d.getRecords());
        //System.out.println("Original Size: " + result.size());
        for (EventProperty ep : e.getProperties()) {
            HashSet<Record> temp = findRecords(i.getS().getEventMatches().get(e).getPm().get(ep));
            if (temp.size() > 0) {
                result.retainAll(temp);
            } else {
                i.addNoMatch(i.getS().getEventMatches().get(e).getPm().get(ep).getA());
            }
            for (FactualRelation fr : ep.getNodeB().getProperties()) {
                temp = findRecords(i.getS().getEventMatches().get(e).getFm().get(fr));
                if (temp.size() > 0) {
                    result.retainAll(temp);
                } else {
                    i.addNoMatch(i.getS().getEventMatches().get(e).getFm().get(fr).getA());
                }
            }
        }
        //System.out.println("Reduced Size: " + result.size());
        return result;
    }

    public HashSet<Record> getRelevantRecordsTotal(InstanceMatch i,EventMatch e) {
        DataSet d = e.getD();
        HashSet<Record> result = new HashSet<>();
        result.addAll(d.getRecords());
        //System.out.println("Original Size: " + result.size());
        for (EventProperty ep : e.getE().getProperties()) {
            HashSet<Record> temp = new HashSet<>();
            for (Attribute a : i.getE().getD().getAttributes()) {
                PropertyMatch pn = new PropertyMatch(ep,a,1.0);
                HashSet<Record> newRecords = findRecords(pn);
                if (newRecords.isEmpty()) {
                    i.addNoMatch(pn.getA());
                } else {
                    temp.addAll(newRecords);
                }

            }
            if (temp.size() > 0) {
                result.retainAll(temp);
            }
            for (FactualRelation fr : ep.getNodeB().getProperties()) {
                for (Attribute a : i.getE().getD().getAttributes()) {
                    FactMatch fn = new FactMatch(fr,e.getE(),a,1.0);
                    HashSet<Record> newRecords = findRecords(fn);
                    if (newRecords.isEmpty()) {
                        i.addNoMatch(fn.getA());
                    } else {
                        temp.addAll(newRecords);
                    }
                }
                if (temp.size() > 0) {
                    result.retainAll(temp);
                }
            }
        }
        //System.out.println("Reduced Size: " + result.size());
        return result;
    }

    public HashSet<Record> getRelevantRecords(SchemaMatch s,DataSet d) {
        HashSet<Record> result = d.getRecords();
        for (EventMatch m : s.getEventMatches().values()) {
            for (EventProperty ep : m.getPm().keySet()) {
                if (m.getPm().get(ep).getDS() == d) {
                    HashSet<Record> temp = findRecords(m.getPm().get(ep));
                    if (temp.size() > 0) {
                        result.retainAll(temp);
                    }
                }
            }
            for (FactualRelation fr : m.getFm().keySet()) {
                if (m.getFm().get(fr).getDS() == d) {
                    HashSet<Record> temp = findRecords(m.getFm().get(fr));
                    if (temp.size() > 0) {
                        result.retainAll(temp);
                    }
                }
            }
        }
        return result;
    }

    public HashSet<Record> findRecords(PropertyMatch p) {
        HashSet<Record> result = new HashSet<>();
        for (Record r : p.getDS().getRecords()) {
            try {
                if (r.getEntry(p.getA()).replace("\"","").trim().contains(p.getEP().getNodeB().getLabel().trim())) {
                    result.add(r);
                }
            } catch (Exception e) {
                //Remove try later!!
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

    public HashSet<InstanceMatch> getMatching() {
        return matching;
    }

    public SchemaMatching getSm() {
        return sm;
    }
}
