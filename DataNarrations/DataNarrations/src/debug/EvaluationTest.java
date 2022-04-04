package debug;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import io.AnnotateDataSets;
import io.AnnotateNarratives;
import io.LoadDataSets;
import io.LoadNarratives;
import matches.EventMatch;
import matches.InstanceMatchPair;
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

    static HashSet<DataSet> dataSets;
    static HashSet<Narrative> narratives;
    //static DataSet populationData;

    public static void testEvalPipeline(String path) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        //populationData = LoadDataSets.loadDataSetsFromFile(new File(path + "ExternalData/FAOSTAT_Population.csv"));
        //annotateInput(dataSets,narratives,path + "NarrativesAnnotations",path + "DataAnnotations");
        //prepareEmbedding(path);
        for (Narrative n : narratives) {
            System.out.println("Narrative: " + n.getSrc() + " (" + (System.currentTimeMillis() / 3600000) + ")");
            System.out.println("----Run Schema Matching");
            SchemaMatching sm = new SchemaMatching(n,dataSets,new File(path + "SBERT/embeddingScoresReduced.txt"),new File(path + "SBERT/FactEmbeddingScoresReduced.txt"));
            sm.printResult(path + "Output");
            System.out.println("----Run Instance Matching");
            InstanceMatching im = new InstanceMatching(sm);
            im.printResult(path + "Output");
            System.out.println("----Run Relation Assessment");
            RelationAssessment ra = new RelationAssessment(im,new File(path + "SBERT/ReducedDataEmbeddingScores.txt"));
            ra.printResult(path + "Output");
        }
        //prepareHEAT();
    }

    public static ArrayList<InstanceMatchPair> prepareOrdering(NarrativeRelation nr, HashMap<Event, InstanceMatching> matches) {
        HashMap<InstanceMatchPair,Double> scores = new HashMap<>();
        for (InstanceMatch e1 : matches.get(nr.getNodeA()).getMatching()) {
            for (InstanceMatch e2 : matches.get(nr.getNodeB()).getMatching()) {
                InstanceMatchPair ep = new InstanceMatchPair(e1,e2);
                double sc = (e1.getE().getRefinedScore() + e2.getE().getRefinedScore()) / 2;
                scores.put(ep,sc);
            }
        }
        ArrayList<InstanceMatchPair> sorted = new ArrayList<>();
        LinkedHashMap<InstanceMatchPair, Double> ranking = Helper.sortHashMapByValues(scores);
        for (InstanceMatchPair ep : ranking.keySet()) {
            sorted.add(ep);
        }
        return sorted;
    }

    public static void relationAssessmentEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        for (Narrative n : narratives) {
            HashSet<Attribute> successful = new HashSet<>();
            System.out.println("Narrative: " + n.getSrc());
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e, dataSets, new File(path + "SBERT/embeddingScoresReduced.txt"));
                matches.put(e, em.getThreshold(tEM));
            }
            HashMap<Event, InstanceMatching> ims = new HashMap<>();
            for (Event e : matches.keySet()) {
                EventRefinement er = new EventRefinement(matches.get(e), new File(path + "SBERT/FactEmbeddingScoresReduced.txt"));

                InstanceMatching i = new InstanceMatching(er);
                //System.out.println("IM Size: " + i.getMatching().size());
                ims.put(e, i);
            }
            HashMap<NarrativeRelation, RelationAssessment> ras = new HashMap<>();
            long starttime = System.nanoTime();
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                //System.out.println("Start Relation Assessment!");
                RelationAssessment ra = new RelationAssessment(nr,ims.get(nr.getNodeA()),ims.get(nr.getNodeB()),new File(path + "SBERT/ReducedDataEmbeddingScores.txt"));
                ras.put(nr,ra);
            }
            long endtime = System.nanoTime();
            runtime = runtime + (endtime - starttime);
            File outfile = new File(path + "Output/RelationAssessment/" + n.getSrc().getName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                    try {
                        if (nb.getScore().getScore() >= 0.7) {
                            successful.add(nb.getImA().getE().getA());
                            successful.add(nb.getImB().getE().getA());
                        }
                        bw.write(nb.getImA().getE().getA().getTitle() + " (" + nb.getImA().getReductionByEvent(nr.getNodeA()).size()
                                + ") " + nr.getLabel() + " "
                                + nb.getImB().getE().getA().getTitle() + " (" + nb.getImB().getReductionByEvent(nr.getNodeB()).size()
                                + ") -> " + nb.getScore().getScore());
                        bw.newLine();
                    } catch (Exception ex) {
                        bw.write("No successful binding!");
                        bw.newLine();
                    }
                }
            }
            for (Attribute a : successful) {
                bw.write(a.getTitle() + " " + a.getDs().getSrc());
                bw.newLine();
            }
            bw.close();
        }
        System.out.println("Runtime: " + runtime);
    }

    public static void instanceMatchEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        for (Narrative n : narratives) {
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,new File(path + "SBERT/embeddingScoresReduced.txt"));
                matches.put(e,em.getThreshold(tEM));
            }
            HashMap<Event, HashSet<InstanceMatch>> ims = new HashMap<>();

            for (Event e : matches.keySet()) {
                ArrayList<EventMatch> success = new ArrayList<>();
                EventRefinement er = new EventRefinement(matches.get(e),new File(path + "SBERT/FactEmbeddingScoresReduced.txt"));
                long starttime = System.nanoTime();
                InstanceMatching i = new InstanceMatching(er);
                ims.put(e,i.getMatching());
                long endtime = System.nanoTime();
                runtime = runtime + (endtime - starttime);
            }
            File outfile = new File(path + "Output/InstanceMatching/" + n.getSrc().getName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            for (Event e : matches.keySet()) {
                bw.write(e.getLabel());
                bw.newLine();
                for (InstanceMatch m : ims.get(e)) {
                    try {
                        bw.write("(" + m.getE().getScore() + ") "
                                + "-> " + m.getE().getA().getTitle()
                                + " (" + m.getE().getA().getDs().getSrc() + ")"
                                + " (" + m.getReductionByEvent(m.getE().getE()).size() + ") " + m.isReduced());
                        bw.newLine();
                    } catch (Exception ex) {
                        bw.write(" -> No match found!");
                        bw.newLine();
                    }
                    for (EventProperty ep : m.getE().getE().getProperties()) {
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
        System.out.println("Runtime: " + runtime);
    }

    public static void propertyMatchEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        for (Narrative n : narratives) {
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,new File(path + "SBERT/embeddingScoresReduced.txt"));
                matches.put(e,em.getThreshold(tEM));
            }
            long starttime = System.nanoTime();
            for (Event e : matches.keySet()) {
                EventRefinement er = new EventRefinement(matches.get(e),new File(path + "SBERT/FactEmbeddingScores.txt"));
            }
            long endtime = System.nanoTime();
            runtime = runtime + (endtime - starttime);

            File outfile = new File(path + "Output/EventRefinement/" + n.getSrc().getName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            for (Event e : matches.keySet()) {
                bw.write(e.getLabel());
                bw.newLine();
                for (EventMatch m : matches.get(e)) {
                    bw.write("(" + m.getScore() + ") "
                            + "-> " + m.getA().getTitle()
                            + " (" + m.getA().getDs().getSrc() + ")");
                    bw.newLine();
                    for (EventProperty ep : m.getE().getProperties()) {
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
        System.out.println("Runtime: " + runtime);
    }

    public static void eventMatchEvaluation(String path, double t) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        for (Narrative n : narratives) {
            long starttime = System.nanoTime();
            HashMap<Event,ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,new File(path + "SBERT/embeddingScoresReduced.txt"));
                matches.put(e,em.getThreshold(t));
            }
            long endtime = System.nanoTime();
            runtime = runtime + (endtime - starttime);
            File outfile = new File(path + "Output/EventMatching/" + n.getSrc().getName());
            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
            for (Event e : matches.keySet()) {
                bw.write(e.getLabel());
                bw.newLine();
                for (EventMatch m : matches.get(e)) {
                    bw.write("(" + m.getScore() + ") "
                            + "-> " + m.getA().getTitle()
                            + " (" + m.getA().getDs().getSrc() + ")");
                    bw.newLine();
                }
                bw.newLine();
            }
            bw.close();
        }
        System.out.println("Runtime: " + runtime);
    }

    public static void prepareEmbedding(String path) throws IOException {
        writeEvents(new File(path + "SBERT/EmbeddingEvents.txt"));
        writeAttributes(new File(path + "SBERT/EmbeddingAttributes.txt"));
        writeProperties(new File(path + "SBERT/EmbeddingProperties.txt"));
        writeFacts(new File(path + "SBERT/EmbeddingFacts.txt"));
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

    public static void annotateInput(HashSet<DataSet> dataSets, HashSet<Narrative> narratives, String annotationsNarratives, String annotationsData) throws IOException {
        for (Narrative n : narratives) {
            System.out.println("Annotate N: " + n.getSrc());
            File annotation = new File(annotationsNarratives + "/" + n.getSrc().getName());
            AnnotateNarratives.annotate(n,annotation);
        }
        for (DataSet d : dataSets) {
            System.out.println("Annotate DS: " + d.getSrc());
            File annotation = new File(annotationsData + "/" + d.getSrc().getName());
            if (annotation.getName().split("\\.")[1].equals("json")) {
                AnnotateDataSets.annotateJson(d,annotation);
            } else {
                AnnotateDataSets.annotate(d,annotation);
            }
        }
    }

    public static void loadInput(File dataSrc, File narrativeSrc) {
        dataSets = LoadDataSets.loadDataSetsFromFolder(dataSrc);
        narratives = LoadNarratives.loadNarrativesFromFolder(narrativeSrc);
    }

    public static void prepareHEAT() throws IOException {
        DataSet heat = LoadDataSets.loadDataSetsFromFile(new File("C:\\Users\\Denis\\Desktop\\HEAT\\CSV\\HEATPlus-repository-WASH.csv"));
        String outputPath = "C:\\Users\\Denis\\Desktop\\HEAT\\Output\\";
        HashMap<String,HashSet<Record>> subSets = new HashMap<>();
        for (Record r : heat.getRecords()) {
            String key = r.getEntry(heat.getAttribute("indicator_name")) + r.getEntry(heat.getAttribute("dimension"));
            if(subSets.keySet().contains(key)) {
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
