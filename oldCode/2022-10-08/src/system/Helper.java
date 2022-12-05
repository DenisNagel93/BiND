package system;

import datasetComponents.Attribute;
import datasetComponents.Record;

import java.util.*;

public class Helper {

    public static double computeAvg(Attribute a,HashSet<Record> records) {
        double sum = 0.0;
        int errors = 0;
        for (Record r : records) {
            try {
                sum = sum + Double.parseDouble(r.getEntry(a));
            } catch (Exception e) {
                errors++;
            }
        }
        return sum / (double)(records.size() - errors);
    }

    public static double prepareForDouble(String s) {
        s = s.replaceAll("\"","");
        String[] sub = s.split("\\[");
        return Double.parseDouble(sub[0]);
    }
}
