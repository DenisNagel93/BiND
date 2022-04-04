package io;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import narrativeComponents.Narrative;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LoadDataSets {

    //Loads data sets from file
    public static HashSet<DataSet> loadDataSetsFromFolder(File inputFile) {
        HashSet<DataSet> output = new HashSet<>();
        for (File entry : inputFile.listFiles()) {
            System.out.println("Load DS: " + entry);
            output.add(loadDataSetsFromFile(entry));
        }
        return output;
    }

    //Loads data sets from file
    public static DataSet loadDataSetsFromFile(File inputFile) {
        HashSet<Attribute> attributes = new HashSet<>();
        ArrayList<Attribute> list = new ArrayList<>();
        Parser ln = new Parser(inputFile);
        DataSet ds = new DataSet(inputFile);
        for (String entry : ln.fileContent.get(0)) {
            Attribute a = new Attribute(entry,ds);
            attributes.add(a);
            list.add(a);
        }
        ds.setAttributes(attributes);
        ds.setAttributeList(list);

        for (int i = 1; i < ln.fileContent.size(); i++) {
            HashMap<Attribute, String> entries = new HashMap<>();
            for (int j = 0; j < ln.fileContent.get(i).size(); j++) {
                entries.put(list.get(j),ln.fileContent.get(i).get(j));
            }
            Record r = new Record(entries);
            ds.addRecord(r);
        }
        return ds;
    }
}
