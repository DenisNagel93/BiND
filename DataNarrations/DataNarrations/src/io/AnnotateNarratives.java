package io;

import narrativeComponents.Edge;
import narrativeComponents.Narrative;
import narrativeComponents.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class AnnotateNarratives {

    //Processes files produced as output of an entity linking for the candidate narratives
    public static void annotate(Narrative n, File inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] sub1 = line.split(";");
            String type = sub1[0];
            if (type.equals("Node") && sub1.length > 2) {
                //node
                Node v = n.getNode(sub1[1]);
                String[] sub2 = sub1[2].split("\\.");
                //entities
                for (String e : sub2) {
                    String[] sub3 = e.split(",");
                    HashSet<String> annotations = new HashSet<>(Arrays.asList(sub3));
                    //annotations
                    v.addConcepts(annotations);
                }
            }
            if (type.equals("Label") && sub1.length > 2) {
                for (Edge r : n.getRelations()) {
                    if (r.getLabel().equals(sub1[1])) {
                        String[] sub2 = sub1[2].split("\\.");
                        //entities
                        for (String e : sub2) {
                            String[] sub3 = e.split(",");
                            HashSet<String> annotations = new HashSet<>(Arrays.asList(sub3));
                            //annotations
                            r.addConcepts(annotations);
                        }
                    }
                }
            }
        }
    }
}
