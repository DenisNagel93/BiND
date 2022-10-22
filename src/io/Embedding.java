package io;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Embedding {

    File embeddingFile;
    String delim;
    HashMap<String, HashMap<String,Double>> similarityScores;
    HashMap<String,ArrayList<ArrayList<String>>> rankedEmbedding;

    public Embedding(File embeddingFile, String delim) {
        this.embeddingFile = embeddingFile;
        this.delim = delim;
        loadEmbeddingFromFile(embeddingFile,delim);
    }

    public void loadEmbeddingFromFile(File inputFile, String delim) {
        this.similarityScores = new HashMap<>();
        this.rankedEmbedding = new HashMap<>();
        Parser ln = new Parser(inputFile,delim);
        for (int i = 0; i < ln.fileContent.size(); i++) {

            StringBuilder aTitle = new StringBuilder(ln.fileContent.get(i).get(1));
            if (ln.fileContent.get(i).size() > 3) {
                for (int j = 2; j < ln.fileContent.get(i).size() - 1; j++) {
                    aTitle.append(delim).append(ln.fileContent.get(i).get(j));
                }
            }

            if (similarityScores.containsKey(ln.fileContent.get(i).get(0))) {
                similarityScores.get(ln.fileContent.get(i).get(0)).put(aTitle.toString(),Double.parseDouble(ln.fileContent.get(i).get(ln.fileContent.get(i).size() - 1)));
            } else {
                HashMap<String, Double> score = new HashMap<>();
                score.put(aTitle.toString(),Double.parseDouble(ln.fileContent.get(i).get(ln.fileContent.get(i).size() - 1)));
                similarityScores.put(ln.fileContent.get(i).get(0),score);
            }
            ArrayList<String> line = new ArrayList<>();
            line.add(aTitle.toString());
            line.add(ln.fileContent.get(i).get(ln.fileContent.get(i).size() - 1));
            if (rankedEmbedding.containsKey(ln.fileContent.get(i).get(0))) {
                rankedEmbedding.get(ln.fileContent.get(i).get(0)).add(line);
            } else {
                ArrayList<ArrayList<String>> ranking = new ArrayList<>();
                ranking.add(line);
                rankedEmbedding.put(ln.fileContent.get(i).get(0),ranking);
            }
        }
    }

    public HashMap<String, HashMap<String, Double>> getSimilarityScores() {
        return similarityScores;
    }

    public HashMap<String,ArrayList<ArrayList<String>>> getRankedEmbedding() {
        return rankedEmbedding;
    }
}
