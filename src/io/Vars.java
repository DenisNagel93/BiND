package io;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;
import narrativeComponents.Narrative;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class Vars {

    static HashSet<DataSet> dataSets;
    static HashSet<Narrative> narratives;

    static String dataPath;
    static String narrativePath;
    static String outputPath;
    static String embeddingPath;
    static String groundTruthPath;

    static double tEM,tPM,tRA;

    static String delim;

    public static Embedding attributeEmbedding, titleEmbedding, vtEmbedding, factEmbedding, valueEmbedding;

    public static HashMap<String, HashSet<DataSet>> atrIndex = new HashMap<>();
    public static HashMap<String,DataSet> titleIndex = new HashMap<>();
    public static HashMap<String, Narrative> narrativeTitleIndex = new HashMap<>();

    public static void initBiND(File config) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(config));
        String line;
        while ((line = br.readLine()) != null) {
            String[] sub = line.split("=");
            switch (sub[0]) {
                case "narratives_path": narrativePath = sub[1];
                    break;
                case "data_path": dataPath = sub[1];
                    break;
                case "groundtruth_path": groundTruthPath = sub[1];
                    break;
                case "output_path": outputPath = sub[1];
                    break;
                case "embeddings_path": Vars.embeddingPath = sub[1];
                    break;
                case "csv_delimiter": delim = sub[1];
                    break;
                case "event_type_matching_threshold": tEM = Double.parseDouble(sub[1]);
                    break;
                case "constraint_matching_threshold": tPM = Double.parseDouble(sub[1]);
                    break;
                case "relation_assessment_threshold": tRA = Double.parseDouble(sub[1]);
                    break;
                default:
            }
        }
        br.close();
    }

    public static void loadInput(File dataSrc, File narrativeSrc,String delim) {
        dataSets = LoadDataSets.loadDataSetsFromFolder(dataSrc,delim);
        narratives = LoadNarratives.loadNarrativesFromFolder(narrativeSrc,delim);
        buildIndexes();
    }

    public static void buildIndexes() {
        for (DataSet d : dataSets) {
            Vars.titleIndex.put(d.getTitle(),d);
            for (Attribute a : d.getAttributes()) {
                String atrTitle = a.getTitle().replace("\"","");
                if (Vars.atrIndex.containsKey(atrTitle)) {
                    Vars.atrIndex.get(atrTitle).add(d);
                } else {
                    HashSet<DataSet> ds = new HashSet<>();
                    ds.add(d);
                    Vars.atrIndex.put(atrTitle,ds);
                }
            }
        }
        for (Narrative n : narratives) {
            Vars.narrativeTitleIndex.put(n.getSrc().getName(),n);
        }
    }

    public static void prepareEmbedding(String delim) throws IOException {
        long starttime = System.nanoTime();
        loadInput(new File(Vars.getDataPath()), new File(narrativePath),delim);
        writeEvents(new File(embeddingPath + "/EmbeddingEvents.txt"));
        writeAttributes(new File(embeddingPath + "/EmbeddingAttributes.txt"));
        writeProperties(new File(embeddingPath + "/EmbeddingProperties.txt"));
        writeFacts(new File(embeddingPath + "/EmbeddingFacts.txt"));
        writeTitles(new File(embeddingPath + "/EmbeddingTitles.txt"));
        writeValues(new File(embeddingPath + "/EmbeddingValues.txt"));
        long endtime = System.nanoTime();
        double runtime = ((double)endtime - (double)starttime) / 1000000000;
        System.out.println("Preparing Embeddings took " + runtime + " seconds");
    }

    public static void loadEmbeddings(String delim) {
        attributeEmbedding = new Embedding(new File(embeddingPath + "/EmbeddingScoresReduced.txt"),delim);
        titleEmbedding = new Embedding(new File(embeddingPath + "/TitleEmbeddingScoresReduced.txt"),delim);
        vtEmbedding = new Embedding(new File(embeddingPath + "/VTEmbeddingScoresReduced.txt"),delim);
        factEmbedding = new Embedding(new File(embeddingPath + "/FactEmbeddingScoresReduced.txt"),delim);
        valueEmbedding = new Embedding(new File(embeddingPath + "/ValueEmbeddingScoresReduced.txt"),delim);
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

    public Narrative selectNarrative(String filename) {
        for (Narrative n : narratives) {
            if (filename.equals(n.getSrc().getName())) {
                return n;
            }
        }
        return null;
    }

    //--------Getter/Setter--------


    public static HashSet<DataSet> getDataSets() {
        return dataSets;
    }

    public static HashSet<Narrative> getNarratives() {
        return narratives;
    }

    public static String getGroundTruthPath() {
        return groundTruthPath;
    }

    public static String getDataPath() {
        return dataPath;
    }

    public static String getEmbeddingPath() {
        return embeddingPath;
    }

    public static String getNarrativePath() {
        return narrativePath;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static double getTEM() {
        return tEM;
    }

    public static double getTPM() {
        return tPM;
    }

    public static double getTRA() {
        return tRA;
    }

    public static String getDelim() {
        return delim;
    }
}
