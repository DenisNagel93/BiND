package debug;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import io.LoadDataSets;
import io.LoadNarratives;
import matches.EventMatch;
import matches.InstanceMatch;
import matches.NarrativeBinding;
import narrativeComponents.*;
import system.EventMatching;
import system.EventRefinement;
import system.InstanceMatching;
import system.RelationAssessment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AblationStudy {

    static HashSet<DataSet> dataSets;
    static HashSet<Narrative> narratives;

    public static void relationAssessmentEvaluationNoIM(String path, double tEM) throws IOException {
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
                EventRefinement er = new EventRefinement(matches.get(e), new File(path + "SBERT/FactEmbeddingScores.txt"));

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

    public static void noEventMatch(String path) {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        long starttime = System.nanoTime();
        for (Narrative n : narratives) {
            for (Event e : n.getEvents()) {
                for (DataSet d : dataSets) {
                    for (Attribute a : d.getAttributes()) {
                        EventMatch em = new EventMatch(e,a,0.0);
                    }
                }
            }
        }
        long endtime = System.nanoTime();
        runtime = (endtime - starttime);
        System.out.println("Runtime = " + runtime);
    }



    public static void relationAssessmentEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        long runtime = 0;
        for (Narrative n : narratives) {
            System.out.println("Narrative: " + n.getSrc());
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e, dataSets, new File(path + "SBERT/embeddingScores.txt"));
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
            bw.close();
        }
        System.out.println("Runtime: " + runtime);
    }

    public static void instanceMatchEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        int totalCount = 0;
        int reducedCount = 0;
        long runtime = 0;
        for (Narrative n : narratives) {
            HashSet<DataSet> fullData = new HashSet<>();
            HashSet<DataSet> reducedData = new HashSet<>();
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,new File(path + "SBERT/embeddingScores.txt"));
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
            for (Event e : matches.keySet()) {
                for (EventMatch m : matches.get(e)) {
                    fullData.add(m.getD());
                    boolean fail = false;
                    for (InstanceMatch i : ims.get(e)) {
                        try {
                            i.getReductionByEvent(i.getE().getE()).size();
                            if (i.getNoMatch().contains(m.getA())) {
                                fail = true;
                            }
                        } catch (Exception ex) {
                            fail = true;
                        }
                    }
                    if (!fail) {
                        reducedData.add(m.getD());
                    }
                }

            }
            totalCount = totalCount + fullData.size();
            reducedCount = reducedCount + reducedData.size();
        }
        System.out.println("Total Count = " + totalCount);
        System.out.println("Reduced Count = " + reducedCount);
        System.out.println("Runtime: " + runtime);
    }


    public static void propertyMatchEvaluation(String path, double tEM) throws IOException {
        loadInput(new File(path + "Data"), new File(path + "Narratives"));
        int totalCount = 0;
        int reducedCount = 0;
        long runtime = 0;
        for (Narrative n : narratives) {
            HashSet<DataSet> fullData = new HashSet<>();
            HashSet<DataSet> reducedData = new HashSet<>();
            HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
            for (Event e : n.getEvents()) {
                EventMatching em = new EventMatching(e,dataSets,new File(path + "SBERT/embeddingScores.txt"));
                matches.put(e,em.getThreshold(tEM));
            }
            long starttime = System.nanoTime();
            for (Event e : matches.keySet()) {
                EventRefinement er = new EventRefinement(matches.get(e),new File(path + "SBERT/FactEmbeddingScoresReduced.txt"));
            }
            long endtime = System.nanoTime();
            runtime = runtime + (endtime - starttime);
            for (Event e : matches.keySet()) {
                for (EventMatch m : matches.get(e)) {
                    fullData.add(m.getD());
                    boolean fail = false;
                    for (EventProperty ep : m.getE().getProperties()) {
                        try {
                            m.getPm().get(ep).getScore();
                            m.getPm().get(ep).getA().getTitle();
                            m.getPm().get(ep).getA().getDs().getSrc();
                        } catch (Exception ex) {
                            fail = true;
                        }
                        for (FactualRelation fr : ep.getNodeB().getProperties()) {
                            try {
                                m.getFm().get(fr).getScore();
                                m.getFm().get(fr).getA().getTitle();
                                m.getFm().get(fr).getA().getDs().getSrc();
                            } catch (Exception ex) {
                                fail = true;
                            }
                        }
                    }
                    if (!fail) {
                        reducedData.add(m.getD());
                    }
                }

            }
            totalCount = totalCount + fullData.size();
            reducedCount = reducedCount + reducedData.size();
        }
        System.out.println("Total Count = " + totalCount);
        System.out.println("Reduced Count = " + reducedCount);
        System.out.println("Runtime: " + runtime);
    }



    public static void loadInput(File dataSrc, File narrativeSrc) {
        dataSets = LoadDataSets.loadDataSetsFromFolder(dataSrc);
        narratives = LoadNarratives.loadNarrativesFromFolder(narrativeSrc);
    }
}
