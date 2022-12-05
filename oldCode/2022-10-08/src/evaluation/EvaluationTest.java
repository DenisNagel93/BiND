package evaluation;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import io.Embedding;
import io.LoadDataSets;
import io.LoadNarratives;
import matches.EventMatch;
import matches.FactualBinding;
import matches.InstanceMatch;
import matches.NarrativeBinding;
import narrativeComponents.*;
import system.*;
import ui.applicationSelect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class EvaluationTest {

    static HashSet<DataSet> dataSets;
    static HashSet<Narrative> narratives;

    static Embedding attributeEmbedding, titleEmbedding, vtEmbedding, factEmbedding, valueEmbedding;

    static HashMap<Narrative,HashSet<DataSet>> results = new HashMap<>();
    static HashMap<Event,ArrayList<EventMatch>> completeMatches = new HashMap<>();

    static HashMap<Narrative,HashMap<EventMatch,HashMap<EventMatch,HashSet<NarrativeBinding>>>> nBindings = new HashMap<>();
    static HashMap<Narrative,HashMap<EventMatch,HashSet<FactualBinding>>> fBindings = new HashMap<>();

    public static HashMap<String,HashSet<DataSet>> atrIndex = new HashMap<>();
    public static HashMap<String,DataSet> titleIndex = new HashMap<>();
    public static HashMap<String,Narrative> narrativeTitleIndex = new HashMap<>();

    public static void relationAssessmentEvaluation(double tEM,double tPM,double tRA,boolean p) throws IOException {
        long emRuntime = 0;
        long pmRuntime = 0;
        long imRuntime = 0;
        long raRuntime = 0;
        for (Narrative n : narratives) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashSet<NarrativeBinding> successful = new HashSet<>();
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,attributeEmbedding,titleEmbedding,tEM);
                matches.put(e, em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            HashMap<Event, InstanceMatching> ims = new HashMap<>();
            for (Event e : matches.keySet()) {
                startRuntime = System.nanoTime();
                EventRefinement er = new EventRefinement(matches.get(e),factEmbedding,valueEmbedding,vtEmbedding,tPM,tEM);
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
                    if (cl.isBinary()) {
                        FactChecking fc = new FactChecking(ims.get(cl.getNodeA()),ims.get(cl.getEventB()),cl);
                        fcs.put(cl,fc);
                    } else {
                        FactChecking fc = new FactChecking(ims.get(cl.getNodeA()),cl);
                        fcs.put(cl,fc);
                    }
                }
            }


            HashMap<NarrativeRelation, RelationAssessment> ras = new HashMap<>();
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                RelationAssessment ra = new RelationAssessment(nr,ims.get(nr.getNodeA()),ims.get(nr.getNodeB()),new File(applicationSelect.getEmbeddingPath() + "/DataEmbeddingScoresReduced.txt"),tRA);
                ras.put(nr,ra);
                nBindings.put(n,ra.getMapping());
            }
            raRuntime = raRuntime + (System.nanoTime() - startRuntime);

            results.put(n,bindings);

            HashMap<EventMatch,HashSet<FactualBinding>> bindingResults = new HashMap<>();
            for (FactChecking fc : fcs.values()) {
                 for (EventMatch em : fc.getResults().keySet()) {
                     if (bindingResults.keySet().contains(em)) {
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
        File outfile = new File(applicationSelect.getOutputPath() + "/metrics_" + tRA + ".txt");
        eRun.printRAResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
        System.out.println("Instance Matching took: " + (double)imRuntime / 1000000000 + " seconds");
        System.out.println("Relation Assessment took: " + (double)raRuntime / 1000000000 + " seconds");
    }

    public static void printRAEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, InstanceMatching> ims,
                                   HashMap<NarrativeRelation, RelationAssessment> ras,HashMap<Claim, FactChecking> fcs,HashSet<NarrativeBinding> successful) throws IOException {
        File outfile = new File(applicationSelect.getOutputPath() + "/RelationAssessment/" + n.getSrc().getName());
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
        for (Narrative n : narratives) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,attributeEmbedding,titleEmbedding,tEM);
                matches.put(e,em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            HashMap<Event, ArrayList<InstanceMatch>> ims = new HashMap<>();
            for (Event e : matches.keySet()) {
                startRuntime = System.nanoTime();
                EventRefinement er = new EventRefinement(matches.get(e),factEmbedding,valueEmbedding,vtEmbedding,tPM,tEM);
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
        EvaluationRun eRun = new EvaluationRun(results, dataSets.size(),completeMatches);

        File outfile = new File(applicationSelect.getOutputPath() + "/metrics_" + tPM + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
        System.out.println("Instance Matching took: " + (double)imRuntime / 1000000000 + " seconds");
    }

    public static void printIMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, ArrayList<InstanceMatch>> ims) throws IOException {
        File outfile = new File(applicationSelect.getOutputPath() + "/InstanceMatching/" + n.getSrc().getName());
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
                    if (e.hasValue()) {
                        //bw.write(" Verified: " + FactChecking.checkValue(m));
                    }
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
        for (Narrative n : narratives) {
            HashSet<DataSet> bindings = new HashSet<>();
            long startRuntime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,attributeEmbedding,titleEmbedding,tEM);
                matches.put(e,em.getMatching());
            }
            emRuntime = emRuntime + (System.nanoTime() - startRuntime);
            //System.out.println("Event Matching: Done");
            startRuntime = System.nanoTime();
            for (Event e : matches.keySet()) {
                EventRefinement er = new EventRefinement(matches.get(e),factEmbedding,valueEmbedding,vtEmbedding,tPM,tEM);
                er.getEventMatching().sort(Collections.reverseOrder());
                for (EventMatch re : er.getEventMatching()) {
                    bindings.add(re.getD());
                }
                completeMatches.put(e,er.getEventMatching());
            }
            pmRuntime = pmRuntime + (System.nanoTime() - startRuntime);

            //System.out.println("Property Matching: Done");

            results.put(n,bindings);

            if (p) {
                printPMEval(n,matches);
            }

        }
        EvaluationRun eRun = new EvaluationRun(results, dataSets.size(),completeMatches);

        File outfile = new File(applicationSelect.getOutputPath() + "/metrics_" + tPM + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
        System.out.println("Property Matching took: " + (double)pmRuntime / 1000000000 + " seconds");
    }

    public static void printPMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches) throws IOException {
        File outfile = new File(applicationSelect.getOutputPath() + "/EventRefinement/" + n.getSrc().getName());
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
        for (Narrative n : narratives) {
            HashSet<DataSet> bindings = new HashSet<>();
            long emStarttime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,attributeEmbedding,titleEmbedding,t);
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
        EvaluationRun eRun = new EvaluationRun(results, dataSets.size(),completeMatches);
        File outfile = new File(applicationSelect.getOutputPath() + "/metrics_" + t + ".txt");
        eRun.printResults(outfile);

        System.out.println("Event Matching took: " + (double)emRuntime / 1000000000 + " seconds");
    }

    public static void printEMEval(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches) throws IOException {
        File outfile = new File(applicationSelect.getOutputPath() + "/EventMatching/" + n.getSrc().getName());
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

    public static void prepareEmbedding(String delim) throws IOException {
        long starttime = System.nanoTime();
        loadInput(new File(applicationSelect.getDataPath()), new File(applicationSelect.getNarrativePath()),delim);
        writeEvents(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingEvents.txt"));
        writeAttributes(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingAttributes.txt"));
        writeProperties(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingProperties.txt"));
        writeFacts(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingFacts.txt"));
        writeTitles(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingTitles.txt"));
        writeValues(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingValues.txt"));
        long endtime = System.nanoTime();
        double runtime = ((double)endtime - (double)starttime) / 1000000000;
        System.out.println("Preparing Embeddings took " + runtime + " seconds");
    }

    public static void loadEmbeddings(String delim) {
        attributeEmbedding = new Embedding(new File(applicationSelect.getEmbeddingPath() + "/EmbeddingScoresReduced.txt"),delim);
        titleEmbedding = new Embedding(new File(applicationSelect.getEmbeddingPath() + "/TitleEmbeddingScoresReduced.txt"),delim);
        vtEmbedding = new Embedding(new File(applicationSelect.getEmbeddingPath() + "/VTEmbeddingScoresReduced.txt"),delim);
        factEmbedding = new Embedding(new File(applicationSelect.getEmbeddingPath() + "/FactEmbeddingScoresReduced.txt"),delim);
        valueEmbedding = new Embedding(new File(applicationSelect.getEmbeddingPath() + "/ValueEmbeddingScoresReduced.txt"),delim);
    }

    public static void writeValues(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (Narrative n : narratives) {
            for (EventProperty ep : n.getEventProperties()) {
                toWrite.add(ep.getNodeB().getLabel());
            }
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void writeTitles(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (DataSet d : dataSets) {
            toWrite.add(d.getTitle());
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void writeFacts(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (Narrative n : narratives) {
            for (FactualRelation fr : n.getFactualRelations()) {
                toWrite.add(fr.getLabel());
            }
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void writeProperties(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (Narrative n : narratives) {
            for (EventProperty ep : n.getEventProperties()) {
                toWrite.add(ep.getLabel());
            }
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void writeEvents(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (Narrative n : narratives) {
            for (Event e : n.getEvents()) {
                toWrite.add(e.getCaption());
            }
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void writeAttributes(File output) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        HashSet<String> toWrite = new HashSet<>();
        for (DataSet d : dataSets) {
            for (Attribute a : d.getAttributes()) {
                toWrite.add(a.getTitle().replace("\"",""));
            }
        }
        for (String s : toWrite) {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }

    public static void buildIndexes() {
        for (DataSet d : dataSets) {
            titleIndex.put(d.getTitle(),d);
            for (Attribute a : d.getAttributes()) {
                String atrTitle = a.getTitle().replace("\"","");
                if (atrIndex.containsKey(atrTitle)) {
                    atrIndex.get(atrTitle).add(d);
                } else {
                    HashSet<DataSet> ds = new HashSet<>();
                    ds.add(d);
                    atrIndex.put(atrTitle,ds);
                }
            }
        }
        for (Narrative n : narratives) {
            narrativeTitleIndex.put(n.getSrc().getName(),n);
        }
    }

    public static void loadInput(File dataSrc, File narrativeSrc,String delim) {
        dataSets = LoadDataSets.loadDataSetsFromFolder(dataSrc,delim);
        narratives = LoadNarratives.loadNarrativesFromFolder(narrativeSrc,delim);
        buildIndexes();
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
