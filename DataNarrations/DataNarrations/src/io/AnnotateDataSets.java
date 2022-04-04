package io;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class AnnotateDataSets {

    public static void annotateJson(DataSet ds, File inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;
        boolean inDataSet = false;
        boolean inRecord = false;
        boolean inCell = false;
        boolean inEntity = false;
        boolean inDefinitions = false;
        boolean isAttribute = true;
        HashSet<String> annotations = null;
        Attribute a = null;
        Record r = null;
        int index = -1;
        int recordNr = -1;
        while ((line = br.readLine()) != null) {
            if (line.trim().equals("[") || line.trim().equals("{") || line.trim().equals("\"definitions\": [")) {
                if (!inDataSet) {
                    inDataSet = true;
                } else {
                    if (!inRecord) {
                        inRecord = true;
                        if (!isAttribute) {
                            recordNr++;
                            r = ds.getRecord(recordNr);
                        }
                    } else {
                        if (!inCell) {
                            inCell = true;
                            index++;
                            if (isAttribute) {
                                a = ds.getAttribute(index);
                            }
                        } else {
                            if (!inEntity) {
                                inEntity = true;
                                annotations = new HashSet<>();
                            } else {
                                if (!inDefinitions) {
                                    inDefinitions = true;
                                }
                            }
                        }
                    }
                }
            } else {
                if (line.trim().equals("]") || line.trim().equals("],") || line.trim().equals("}") || line.trim().equals("},")) {
                    if (inDefinitions) {
                        inDefinitions = false;
                    } else {
                        if (inEntity) {
                            inEntity = false;
                            if (isAttribute) {
                                a.addConcepts(annotations);
                            } else {
                                r.addConcepts(annotations);
                            }
                        } else {
                            if (inCell) {
                                inCell = false;
                                if (isAttribute) {
                                    a = null;
                                }
                            } else {
                                if (inRecord) {
                                    inRecord = false;
                                    if (isAttribute) {
                                        isAttribute = false;
                                    }
                                    r = null;
                                } else {
                                    if (inDataSet) {
                                        inDataSet = false;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (inDefinitions) {
                        annotations.add(line.trim().replace("\"","").replace(",",""));
                    }
                }
            }
        }
    }

    //Processes files produced as output of an entity linking for the data sets
    public static void annotate(DataSet ds, File inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] sub1 = line.split(";");
            //attribute

            if (sub1.length > 1) {
                String attributeName = sub1[0];
                boolean append = false;
                int i = 1;
                if (attributeName.startsWith("\"") && !attributeName.endsWith("\"")) {
                    append = true;
                }
                while (append) {
                    attributeName = attributeName.concat(";" + sub1[i]);
                    if (sub1[i].endsWith("\"")) {
                        append = false;
                    }
                    i++;
                }
                if (sub1.length > i) {
                    Attribute a = ds.getAttribute(attributeName);
                    String[] sub2 = sub1[i].split("\\.");
                    //entities
                    for (String e : sub2) {
                        HashSet<String> annotations = new HashSet<>();
                        String[] sub3 = e.split(",");
                        //annotations
                        annotations.addAll(Arrays.asList(sub3));
                        a.addConcepts(annotations);
                    }
                }
            }
        }
        br.close();
    }
}
