package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import io.LoadDataSets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CausalityAssessment {

    static HashMap<Attribute, HashMap<Attribute,Double>> results = new HashMap<>();

    public static void computeFullALift() {
        DataSet ds = LoadDataSets.loadDataSetsFromFile(new File("/home/nagel/UCIData/student-por.csv"),";");
        String aAText = "";
        String aBText = "";
        double aAAvg;
        double aBAvg;
        for (Attribute aA : ds.getAttributes()) {
            HashMap<Attribute,Double> res = new HashMap<>();
            aAAvg = aA.computeAvg();
            if (aAAvg == 0.0) {
                for (Record r : ds.getRecords()) {
                    aAText = r.getEntry(aA);
                    break;
                }
            }
            for (Attribute aB : ds.getAttributes()) {
                aBAvg = aB.computeAvg();
                if (aBAvg == 0.0) {
                    for (Record r : ds.getRecords()) {
                        aBText = r.getEntry(aB);
                        break;
                    }
                }
                int pYX = 0;
                int pY = 0;
                int pX = 0;
                for (Record r : ds.getRecords()) {
                    boolean inSetA = false;
                    boolean inSetB = false;
                    try {
                        if (Double.parseDouble(r.getEntry(aA)) > aAAvg) {
                            inSetA = true;
                        }
                    } catch (Exception e) {
                        if (r.getEntry(aA).equals(aAText)) {
                            inSetA = true;
                        }
                    }
                    try {
                        if (Double.parseDouble(r.getEntry(aB)) > aBAvg) {
                            inSetB = true;
                        }
                    } catch (Exception e) {
                        if (r.getEntry(aB).equals(aBText)) {
                            inSetB = true;
                        }
                    }
                    if (inSetA) {
                        pX++;
                    }
                    if (inSetB) {
                        pY++;
                        if (inSetA) {
                            pYX++;
                        }
                    }
                }
                res.put(aB,((double)pYX / (double)pX) - ((double)pY / (double)ds.getRecords().size()));
            }
            results.put(aA,res);
        }
    }

    public static void printOut() throws IOException {
        File outfile = new File("/home/nagel/UCIData/out.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (Attribute a : results.keySet()) {
            String aVal = "";
            for (Record r : a.getDs().getRecords()) {
                aVal = r.getEntry(a);
                break;
            }
            bw.newLine();
            for (Attribute b : results.get(a).keySet()) {
                String bVal = "";
                for (Record r : b.getDs().getRecords()) {
                    bVal = r.getEntry(b);
                    break;
                }
                bw.write(a.getTitle() + " (" + aVal + ") -> " + b.getTitle() + " (" + bVal + ") : " + results.get(a).get(b));
                bw.newLine();
            }
        }
        bw.close();

    }
}
