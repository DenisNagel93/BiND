package io;

import narrativeComponents.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class LoadNarratives {

    //Loads narratives from folder
    public static HashSet<Narrative> loadNarrativesFromFolder(File inputFile) {
        HashSet<Narrative> output = new HashSet<>();
        for (File entry : inputFile.listFiles()) {
            System.out.println("Load N: " + entry);
            output.add(loadNarrativeFromFile(entry));
        }
        return output;
    }

    //Loads narrative from file
    public static Narrative loadNarrativeFromFile(File inputFile) {
        Narrative n = new Narrative(inputFile);
        Parser ln = new Parser(inputFile);
        for (ArrayList<String> line : ln.fileContent) {
            if (line.get(0).equals("ev")) {
                Event v1 = n.assignEvent(line.get(1),line.get(2));
                n.addEvent(v1);
            } else {
                if (line.get(0).equals("fr")) {
                    Entity v1 = n.assignEntity(line.get(1));
                    Entity v2 = n.assignEntity(line.get(3));
                    FactualRelation fr = new FactualRelation(line.get(2), v1, v2);
                    v1.addEdge(fr);
                    n.addFactualRelation(fr);
                } else {
                    if (line.get(0).equals("nr")) {
                        Event v1 = n.getEvent(line.get(1));
                        Event v2 = n.getEvent(line.get(3));
                        NarrativeRelation nr = new NarrativeRelation(line.get(2), v1, v2);
                        v1.addEdge(nr);
                        n.addNarrativeRelation(nr);
                    } else {
                        Event v1 = n.getEvent(line.get(1));
                        Entity v2 = n.assignEntity(line.get(3));
                        EventProperty ep = new EventProperty(line.get(2), v1, v2);
                        v1.addEdge(ep);
                        n.addEventProperty(ep);
                    }
                }
            }
        }
        return n;
    }

}
