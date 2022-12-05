package evaluation;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import io.LoadDataSets;
import io.Vars;
import matches.EventMatch;
import matches.FactualBinding;
import matches.InstanceMatch;
import matches.NarrativeBinding;
import narrativeComponents.*;
import system.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EvaluationTest {

    static HashMap<Narrative,HashSet<DataSet>> results = new HashMap<>();
    static HashMap<Event,ArrayList<EventMatch>> completeMatches = new HashMap<>();

    static HashMap<Narrative,HashMap<EventMatch,HashMap<EventMatch,HashSet<NarrativeBinding>>>> nBindings = new HashMap<>();
    static HashMap<Narrative,HashMap<EventMatch,HashSet<FactualBinding>>> fBindings = new HashMap<>();

    public static void relationAssessmentEvaluation(double tEM,double tPM,double tRA,boolean p) throws IOException {
        long emRuntime = 0;
        long pmRuntime = 0;
        long imRuntime = 0;
        long raRuntime = 0;
        for (Narrative n : Vars.getNarratives()) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashSet<NarrativeBinding> successful = new HashSet<>();
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,Vars.getDataSets(), Vars.attributeEmbedding,Vars.titleEmbedding,tEM);
                matches.put(e, em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            HashMap<Event, InstanceMatching> ims = new HashMap<>();
            for (Event e : matches.keySet()) {
                startRuntime = System.nanoTime();
                EventRefinement er = new EventRefinement(matches.get(e),Vars.factEmbedding,Vars.valueEmbedding,Vars.vtEmbedding,tPM,tEM);
                pmRuntime = pmRuntime + (System.nanoTime() - startRuntime);
                startRuntime = System.nanoTime();
                InstanceMatching i = new InstanceMatching(er);
                ims.put(e, i);
                imRuntime = imRuntime + (System.nanoTime() - startRuntime);
                ArrayList<EventMatch> orderedList = new ArrayList<>();
                i.getMatching().sort(Collections.reverseOrder());
                for (InstanceMatch ri : i.getMatching()) {
                    bindings.add(ri.getE().getD());
                    orderedList.add(ri.getE());
                }
                completeMatches.put(e,orderedList);
            }
            startRuntime = System.nanoTime();
            HashMap<Claim, FactChecking> fcs = new HashMap<>();
            for (Event e : matches.keySet()) {
                for (Claim cl : e.getClaims()) {
                    FactChecking fc;
                    if (cl.isBinary()) {
                        fc = new FactChecking(ims.get(cl.getNodeA()), ims.get(cl.getEventB()), cl);
                    } else {
                        fc = new FactChecking(ims.get(cl.getNodeA()), cl);
                    }
                    fcs.put(cl,fc);
                }
            }


            HashMap<NarrativeRelation, RelationAssessment> ras = new HashMap<>();
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                RelationAssessment ra = new RelationAssessment(nr,ims.get(nr.getNodeA()),ims.get(nr.getNodeB()),new File(Vars.getEmbeddingPath() + "/DataEmbeddingScoresReduced.txt"),tRA);
                ras.put(nr,ra);
                nBindings.put(n,ra.getMapping());
            }
            raRuntime = raRuntime + (System.nanoTime() - startRuntime);

            results.put(n,bindings);

            HashMap<EventMatch,HashSet<FactualBinding>> bindingResults = new HashMap<>();
            for (FactChecking fc : fcs.values()) {
                 for (EventMatch em : fc.getResults().keySet()) {
                     if (bindingResults.containsKey(em)) {
                         bindingResults.get(em).add(fc.getResults().get(em));
                     } else {
                         HashSet<FactualBinding> fbSet = new HashSet<>();
                         fbSet.add(fc.getResults().get(em));
                         bindingResults.put(em,fbSet);
                     }
                 }
            }
            fBindings.put(n,bindingResults);

            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                    try {
                        if (nb.getScore().getScore() >= tRA) {
                            successful.add(nb);
                            successful.add(nb);
                        }
                    } catch (Exception ex) {

                    }
                }
            }
            if (p) {
                printRAEval(n,matches,ims,ras,fcs,successful);
            }

        }
        EvaluationRun eRun = new EvaluationRun(results,completeMatches,nBindings,fBindings);
        File outfile = new File(Vars.getOutputPath() + "/metrics_" + tRA + ".txt");
        eRun.printRAResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
        System.out.println("Instance Matching took: " + (double)imRuntime / 1000000000 + " seconds");
        System.out.println("Relation Assessment took: " + (double)raRuntime / 1000000000 + " seconds");
    }

    public static void printRAEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, InstanceMatching> ims,
                                   HashMap<NarrativeRelation, RelationAssessment> ras,HashMap<Claim, FactChecking> fcs,HashSet<NarrativeBinding> successful) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/RelationAssessment/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

        for (Event e : matches.keySet()) {
            bw.write(e.getLabel());
            bw.newLine();
            for (InstanceMatch m : ims.get(e).getMatching()) {
                try {
                    bw.write("(" + m.getE().getRefinedScore() + "/" + m.getE().getScore() + ") "
                            + e.getCaption()
                            + "-> " + m.getE().getA().getTitle()
                            + " (" + m.getE().getA().getDs().getSrc() + ")"
                            + " (" + m.getEventReduction().size() + ") ");
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write(" -> No match found!");
                    bw.newLine();
                }
                for (EventProperty ep : e.getProperties()) {
                    if (!m.getE().getIntegrated().contains(ep)) {
                        bw.write("-" + ep.getLabel());
                        try {
                            bw.write("(" + m.getE().getPm().get(ep).getScore() + ") "
                                    + "-> " + m.getE().getPm().get(ep).getA().getTitle()
                                    + " (" + m.getE().getPm().get(ep).getA().getDs().getSrc()
                                    + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + m.getE().getFm().get(fr).getScore() + ") "
                                    + "-> " + m.getE().getFm().get(fr).getA().getTitle()
                                    + " (" + m.getE().getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
                for (Claim cl : e.getClaims()) {
                    if (cl.isBinary()) {
                        bw.write("-" + cl.getLabel());
                        try {
                            bw.write("(" + cl.getEventB().getLabel() + ") "
                                    + "-> " + fcs.get(cl).getResults().get(m.getE()).isValid());
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> Unknown!");
                            bw.newLine();
                        }
                    } else {
                        bw.write("-" + cl.getLabel());
                        try {
                            bw.write("(" + cl.getNodeB().getLabel() + ") "
                                    + "-> " + fcs.get(cl).getResults().get(m.getE()).isValid());
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> Unknown!");
                            bw.newLine();
                        }
                    }
                }
            }
            bw.newLine();
        }

        for (NarrativeRelation nr : n.getNarrativeRelations()) {
            for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                try {
                    bw.write(nb.getImA().getE().getA().getTitle() + " (" + nb.getImA().getEventReduction().size()
                            + ") " + nr.getLabel() + " "
                            + nb.getImB().getE().getA().getTitle() + " (" + nb.getImB().getEventReduction().size()
                            + ") -> " + nb.getScore().getScore());
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write("No successful binding!");
                    bw.newLine();
                }
            }
        }
        for (NarrativeBinding nb : successful) {
            bw.write(nb.getImA().getE().getD().getTitle() + " <--> " + nb.getImB().getE().getD().getTitle());
            bw.newLine();
        }
        bw.close();
    }

    public static void instanceMatchEvaluation(double tEM,double tPM,boolean p) throws IOException {
        long emRuntime = 0;
        long pmRuntime = 0;
        long imRuntime = 0;
        for (Narrative n : Vars.getNarratives()) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,Vars.getDataSets(),Vars.attributeEmbedding,Vars.titleEmbedding,tEM);
                matches.put(e,em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            HashMap<Event, ArrayList<InstanceMatch>> ims = new HashMap<>();
            for (Event e : matches.keySet()) {
                startRuntime = System.nanoTime();
                EventRefinement er = new EventRefinement(matches.get(e),Vars.factEmbedding,Vars.valueEmbedding,Vars.vtEmbedding,tPM,tEM);
                pmRuntime = pmRuntime + (System.nanoTime() - startRuntime);
                startRuntime = System.nanoTime();
                InstanceMatching i = new InstanceMatching(er);
                ims.put(e,i.getMatching());
                imRuntime = imRuntime + (System.nanoTime() - startRuntime);
                i.getMatching().sort(Collections.reverseOrder());
                ArrayList<EventMatch> orderedList = new ArrayList<>();
                for (InstanceMatch ri : i.getMatching()) {
                    bindings.add(ri.getE().getD());
                    orderedList.add(ri.getE());
                }
                completeMatches.put(e,orderedList);
            }
            results.put(n,bindings);

            if (p) {
                printIMEval(n,matches,ims);
            }

        }
        System.out.println("GroundTruth");
        EvaluationRun eRun = new EvaluationRun(results, Vars.getDataSets().size(),completeMatches);

        File outfile = new File(Vars.getOutputPath() + "/metrics_" + tPM + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
        System.out.println("Instance Matching took: " + (double)imRuntime / 1000000000 + " seconds");
    }

    public static void printIMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, ArrayList<InstanceMatch>> ims) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/InstanceMatching/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (Event e : matches.keySet()) {
            bw.write(e.getLabel());
            bw.newLine();
            for (InstanceMatch m : ims.get(e)) {
                try {
                    bw.write("(" + m.getE().getRefinedScore() + "/" + m.getE().getScore() + ") "
                            + m.getE().getE().getCaption()
                            + "-> " + m.getE().getA().getTitle()
                            + " (" + m.getE().getA().getDs().getSrc() + ")"
                            + " (" + m.getEventReduction().size() + ") " + m.isReduced());
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write(" -> No match found!");
                    bw.newLine();
                }
                for (EventProperty ep : m.getE().getE().getProperties()) {
                    if (!m.getE().getIntegrated().contains(ep)) {
                        bw.write("-" + ep.getLabel());
                        try {
                            bw.write("(" + m.getE().getPm().get(ep).getScore() + ") "
                                    + "-> " + m.getE().getPm().get(ep).getA().getTitle()
                                    + " (" + m.getE().getPm().get(ep).getA().getDs().getSrc()
                                    + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + m.getE().getFm().get(fr).getScore() + ") "
                                    + "-> " + m.getE().getFm().get(fr).getA().getTitle()
                                    + " (" + m.getE().getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void propertyMatchEvaluation(double tEM,double tPM,boolean p) throws IOException {
        long emRuntime = 0;
        long pmRuntime = 0;
        for (Narrative n : Vars.getNarratives()) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,Vars.getDataSets(),Vars.attributeEmbedding,Vars.titleEmbedding,tEM);
                matches.put(e,em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            startRuntime = System.nanoTime();
            for (Event e : matches.keySet()) {
                EventRefinement er = new EventRefinement(matches.get(e),Vars.factEmbedding,Vars.valueEmbedding,Vars.vtEmbedding,tPM,tEM);
                er.getEventMatching().sort(Collections.reverseOrder());
                for (EventMatch re : er.getEventMatching()) {
                    bindings.add(re.getD());
                }
                completeMatches.put(e,er.getEventMatching());
            }
            pmRuntime = pmRuntime + (System.nanoTime() - startRuntime);

            results.put(n,bindings);

            if (p) {
                printPMEval(n,matches);
            }

        }
        EvaluationRun eRun = new EvaluationRun(results, Vars.getDataSets().size(),completeMatches);

        File outfile = new File(Vars.getOutputPath() + "/metrics_" + tPM + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
    }

    public static void printPMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/EventRefinement/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (Event e : matches.keySet()) {
            bw.write(e.getLabel());
            bw.newLine();
            for (EventMatch m : matches.get(e)) {
                bw.write("(" + m.getRefinedScore() + "/" + m.getScore() + ") "
                        + m.getE().getCaption()
                        + " -> " + m.getA().getTitle()
                        + " (" + m.getA().getDs().getSrc() + ")");
                bw.newLine();
                for (EventProperty ep : m.getE().getProperties()) {
                    if (!m.getIntegrated().contains(ep)) {
                        bw.write("-" + ep.getLabel());
                        try {
                            bw.write("(" + m.getPm().get(ep).getScore() + ") "
                                    + "-> " + m.getPm().get(ep).getA().getTitle()
                                    + " (" + m.getPm().get(ep).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + m.getFm().get(fr).getScore() + ") "
                                    + "-> " + m.getFm().get(fr).getA().getTitle()
                                    + " (" + m.getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void eventMatchEvaluation(double t,boolean p) throws IOException {
        long emRuntime = 0;
        for (Narrative n : Vars.getNarratives()) {
            HashSet<DataSet> bindings = new HashSet<>();
            long emStarttime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,Vars.getDataSets(),Vars.attributeEmbedding,Vars.titleEmbedding,t);
                matches.put(e,em.getMatching());
                em.getMatching().sort(Collections.reverseOrder());
                for (EventMatch re : em.getMatching()) {
                    bindings.add(re.getD());
                }
                completeMatches.put(e,em.getMatching());
            }
            long emEndtime = System.nanoTime();
            emRuntime = emRuntime + (emEndtime - emStarttime);

            results.put(n,bindings);
            if (p) {
                printEMEval(n,matches);
            }
        }
        EvaluationRun eRun = new EvaluationRun(results, Vars.getDataSets().size(),completeMatches);
        File outfile = new File(Vars.getOutputPath() + "/metrics_" + t + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
    }

    public static void printEMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/EventMatching/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (Event e : matches.keySet()) {
            bw.write(e.getLabel());
            bw.newLine();
            for (EventMatch m : matches.get(e)) {
                bw.write("(" + m.getScore() + ") "
                        + m.getE().getCaption()
                        + " -> " + m.getA().getTitle()
                        + " (" + m.getA().getDs().getSrc() + ")");
                bw.newLine();
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void prepareHEAT() throws IOException {
        DataSet heat = LoadDataSets.loadDataSetsFromFile(new File("C:\\Users\\Denis\\Desktop\\HEAT\\CSV\\HEATPlus-repository-WASH.csv"),",");
        HashMap<String,HashSet<Record>> subSets = new HashMap<>();
        for (Record r : heat.getRecords()) {
            String key = r.getEntry(heat.getAttribute("indicator_name")) + r.getEntry(heat.getAttribute("dimension"));
            if(subSets.containsKey(key)) {
                subSets.get(key).add(r);
            } else {
                HashSet<Record> data = new HashSet<>();
                data.add(r);
                subSets.put(key,data);
            }
        }
        HashSet<DataSet> heatData = new HashSet<>();
        int count = 0;
        for (HashSet<Record> data : subSets.values()) {
            String indicator = "";
            String dimension = "";
            for (Record r : data) {
                indicator = r.getEntry(heat.getAttribute("indicator_name"));
                dimension = r.getEntry(heat.getAttribute("dimension"));
                break;
            }
            count++;
            File f = new File("C:\\Users\\Denis\\Desktop\\HEAT\\Output\\" + "HEATPlus-repository-WASH_" + count + ".csv");
            DataSet d = new DataSet(f);
            HashSet<Attribute> attributes = new HashSet<>();
            ArrayList<Attribute> list = new ArrayList<>();
            Attribute country = new Attribute("country",d);
            Attribute year = new Attribute("year",d);
            Attribute ind = new Attribute(indicator,d);
            Attribute dim = new Attribute(dimension,d);
            attributes.add(country);
            attributes.add(year);
            attributes.add(dim);
            attributes.add(ind);
            list.add(country);
            list.add(year);
            list.add(dim);
            list.add(ind);
            d.setAttributes(attributes);
            d.setAttributeList(list);
            for (Record r : data) {
                HashMap<Attribute,String> newEntries = new HashMap<>();
                for (Attribute a : r.getEntries().keySet()) {
                    if (a.getTitle().endsWith("setting")) {
                        newEntries.put(country,r.getEntry(a));
                    }
                    if (a.getTitle().equals("year")) {
                        newEntries.put(year,r.getEntry(a));
                    }
                    if (a.getTitle().equals("subgroup")) {
                        newEntries.put(dim,r.getEntry(a));
                    }
                    if (a.getTitle().equals("estimate")) {
                        newEntries.put(ind,r.getEntry(a));
                    }
                }
                Record newRecord = new Record(newEntries);
                d.addRecord(newRecord);
            }
            heatData.add(d);
        }
        for (DataSet ds : heatData) {
            File outfile = ds.getSrc();
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            for (Attribute a : ds.getAttributeList()) {
                bw.write(a.getTitle() + ",");
            }
            bw.newLine();
            boolean first = true;
            for (Record r : ds.getRecords()) {
                if (!first) {
                    bw.newLine();
                } else {
                    first = false;
                }
                for (int i = 0; i < ds.getAttributeList().size(); i++) {
                    bw.write(r.getEntry(ds.getAttribute(i)) + ",");
                }
            }
            bw.close();
        }
    }
}
