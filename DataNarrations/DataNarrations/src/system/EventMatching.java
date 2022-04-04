package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import matches.EventMatch;
import narrativeComponents.Event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class EventMatching {

    Event e;
    HashSet<DataSet> repository;
    File embedding;
    ArrayList<EventMatch> matching;

    public EventMatching(Event e, HashSet<DataSet> repository, File embedding) throws IOException {
        this.e = e;
        this.repository = repository;
        this.embedding = embedding;
        this.matching = readEmbedding();
    }

    public ArrayList<EventMatch> readEmbedding() throws IOException {
        ArrayList<EventMatch> matches = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(embedding));
        String line;
        //System.out.println("EmbeddingFile: " + embedding);
        while ((line = br.readLine()) != null) {
            String[] subString = line.split(",");
            //System.out.println("Compare: " + e.getLabel() + " -> " + subString[0]);
            if (e.getCaption().equals(subString[0])) {
                for (DataSet d : repository) {
                    for (Attribute a : d.getAttributes()) {
                        if (a.getTitle().replace("\"","").equals(subString[1])) {
                            EventMatch em = new EventMatch(e,a,Double.parseDouble(subString[2]));
                            matches.add(em);
                        }
                    }
                }
            }
        }
        return matches;
    }

    public ArrayList<EventMatch> getThreshold(double k) {
        ArrayList<EventMatch> output = new ArrayList<>();
        for (EventMatch em : matching) {
            if (em.getScore() >= k) {
                output.add(em);
            } else {
                break;
            }
        }
        return output;
    }

    public ArrayList<EventMatch> getTopResults(int k) {
        ArrayList<EventMatch> output = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            output.add(matching.get(i));
        }
        return output;
    }

    public Event getE() {
        return e;
    }

    public HashSet<DataSet> getRepository() {
        return repository;
    }

    public ArrayList<EventMatch> getMatching() {
        return matching;
    }
}
