package debug;

import datasetComponents.Record;
import datasetComponents.Attribute;
import datasetComponents.DataSet;
import io.LoadDataSets;
import io.LoadNarratives;
import narrativeComponents.Edge;
import narrativeComponents.Narrative;
import narrativeComponents.Node;

import java.io.File;
import java.util.HashSet;

public class ParserTest {

    public static void testReadDataSet(File src) {
        DataSet d = LoadDataSets.loadDataSetsFromFile(src);
        System.out.println("----Attributes:");
        for (Attribute a : d.getAttributes()) {
            System.out.println(a.getTitle());
        }
        System.out.println("----Records:");
        int count = 1;
        for (Record r : d.getRecords()) {
            System.out.println("----Record #" + count);
            count++;
            for (Attribute a : r.getEntries().keySet()) {
                System.out.println(a.getTitle() + " -> " + r.getEntries().get(a));
            }
        }
    }

    public static void testReadNarrative(File src) {
        Narrative n = LoadNarratives.loadNarrativeFromFile(src);
        System.out.println("----Events:");
        for (Node e : n.getEvents()) {
            System.out.println(e.getLabel());
        }
        System.out.println("----Entities:");
        for (Node e : n.getEntities()) {
            System.out.println(e.getLabel());
        }
        System.out.println("----NarrativeRelations:");
        for (Edge e : n.getNarrativeRelations()) {
            System.out.println("<" + e.getNodeA().getLabel() + "> <" + e.getLabel() + "> <" + e.getNodeB().getLabel() + ">");
        }
        System.out.println("----EventProperties:");
        for (Edge e : n.getEventProperties()) {
            System.out.println("<" + e.getNodeA().getLabel() + "> <" + e.getLabel() + "> <" + e.getNodeB().getLabel() + ">");
        }
        System.out.println("----FactualRelations:");
        for (Edge e : n.getFactualRelations()) {
            System.out.println("<" + e.getNodeA().getLabel() + "> <" + e.getLabel() + "> <" + e.getNodeB().getLabel() + ">");
        }
    }
}
