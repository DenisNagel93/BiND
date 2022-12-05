package evaluation;

import datasetComponents.DataSet;
import matches.EventMatch;
import matches.FactualBinding;
import matches.NarrativeBinding;
import narrativeComponents.Claim;
import narrativeComponents.Event;
import narrativeComponents.Narrative;
import narrativeComponents.NarrativeRelation;
import ui.applicationSelect;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class EvaluationRun {

    HashMap<String,Double> avgPrecision;
    HashMap<String,Double> avgRecall;
    HashMap<String,Double> avgF1;

    HashMap<String,Double> avgHitsAt1;
    HashMap<String,Double> avgHitsAt10;

    double avgAccuracyAt1;
    double avgAccuracyAt2;
    double avgAccuracyAt3;
    double avgAccuracyAt5;
    double avgAccuracyAt10;
    double avgAccuracy;

    double avgClaimAccuracyAt1;
    double avgNRAccuracyAt1;

    double avgReduction;
    int nrData;

    HashMap<Narrative, HashSet<DataSet>> results;
    HashMap<String,HashMap<String,HashSet<String>>> correctMatches;
    HashMap<Event,ArrayList<EventMatch>> completeMatches;

    HashMap<Narrative,HashMap<EventMatch,HashMap<EventMatch,HashSet<NarrativeBinding>>>> nBindings;
    HashMap<Narrative,HashMap<EventMatch,HashSet<FactualBinding>>> fBindings;
    HashMap<Narrative,HashMap<Claim,String>> claimsGT;
    HashMap<Narrative,HashMap<NarrativeRelation,String>> nrGT;

    public EvaluationRun(HashMap<Narrative,HashSet<DataSet>> results, int nrData, HashMap<Event,ArrayList<EventMatch>> completeMatches) throws IOException {
        this.results = results;
        this.nrData = nrData;
        this.correctMatches = loadGroundTruth();
        this.completeMatches = completeMatches;

        this.avgPrecision = computeAvgPrecision();
        this.avgRecall = computeAvgRecall();
        this.avgF1 = computeAvgF1();

        this.avgHitsAt1 = computeHitsAtK(1);
        this.avgHitsAt10 = computeHitsAtK(10);

        this.avgReduction = computeAvgReduction();
    }

    public EvaluationRun(HashMap<Narrative,HashSet<DataSet>> results,HashMap<Event,ArrayList<EventMatch>> completeMatches,
                         HashMap<Narrative,HashMap<EventMatch,HashMap<EventMatch,HashSet<NarrativeBinding>>>> nBindings,
                         HashMap<Narrative,HashMap<EventMatch,HashSet<FactualBinding>>> fBindings) throws IOException {
        this.results = results;
        this.completeMatches = completeMatches;
        this.nBindings = nBindings;
        this.fBindings = fBindings;
        claimsGT = new HashMap<>();
        nrGT = new HashMap<>();
        loadGroundTruthBindings();

        this.avgClaimAccuracyAt1 = computeClaimAccuracyAtK(1);
        this.avgNRAccuracyAt1 = computeNRAccuracyAtK(1);

        this.avgAccuracyAt1 = computeAccuracyAtK(1);
        this.avgAccuracyAt2 = computeAccuracyAtK(2);
        this.avgAccuracyAt3 = computeAccuracyAtK(3);
        this.avgAccuracyAt5 = computeAccuracyAtK(5);
        this.avgAccuracyAt10 = computeAccuracyAtK(10);

        this.avgAccuracy = computeAccuracy();
    }

    public void printRAResults(File outfile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        bw.write("Avg. Accuracy at 1: " + avgAccuracyAt1);
        bw.newLine();
        bw.write("Avg. Accuracy at 2: " + avgAccuracyAt2);
        bw.newLine();
        bw.write("Avg. Accuracy at 3: " + avgAccuracyAt3);
        bw.newLine();
        bw.write("Avg. Accuracy at 5: " + avgAccuracyAt5);
        bw.newLine();
        bw.write("Avg. Accuracy at 10: " + avgAccuracyAt10);
        bw.newLine();
        bw.write("Avg. Accuracy: " + avgAccuracy);
        bw.newLine();
        bw.newLine();
        bw.write("Avg. Claim Accuracy at 1: " + avgClaimAccuracyAt1);
        bw.newLine();
        bw.write("Avg. NR Accuracy at 1: " + avgNRAccuracyAt1);
        bw.newLine();
        bw.close();
    }

    public void printResults (File outfile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (String s : avgPrecision.keySet()) {
            bw.write("GroundTruth: " + s);
            bw.newLine();
            bw.newLine();
            bw.write("Avg. Precision: " + avgPrecision.get(s));
            bw.newLine();
            bw.write("Avg. Recall: " + avgRecall.get(s));
            bw.newLine();
            bw.write("Avg. F1: " + avgF1.get(s));
            bw.newLine();
            bw.newLine();
            bw.write("Avg. Hits at 1: " + avgHitsAt1.get(s));
            bw.newLine();
            bw.write("Avg. Hits at 10: " + avgHitsAt10.get(s));
            bw.newLine();
            bw.newLine();
            bw.write("Avg. Red. Factor: " + avgReduction);
            bw.newLine();
        }
        bw.close();
    }

    public void loadGroundTruthBindings() throws IOException {
        for (File entry : new File(applicationSelect.getGroundTruthPath()).listFiles()) {
            Narrative n = EvaluationTest.narrativeTitleIndex.get(entry.getName());
            HashMap<Claim,String> cls = new HashMap<>();
            HashMap<NarrativeRelation,String> nrs = new HashMap<>();

            boolean readClaims = false;
            BufferedReader br = new BufferedReader(new FileReader(entry));
            String line;
            while ((line = br.readLine()) != null) {
                if (readClaims) {
                    String[] sub = line.split(",");
                    if (sub[0].equals("cl") || sub[0].equals("cl2")) {
                        for (Claim c : n.getClaims()) {
                            if (c.getNodeA().getLabel().equals(sub[1]) && c.getLabel().equals(sub[2])) {
                                cls.put(c,sub[4]);
                            }
                        }
                    }
                    if (sub[0].equals("nr")) {
                        for (NarrativeRelation nr : n.getNarrativeRelations()) {
                            if (nr.getNodeA().getLabel().equals(sub[1]) && nr.getLabel().equals(sub[2]) && nr.getNodeB().getLabel().equals(sub[3])) {
                                nrs.put(nr,sub[4]);
                            }
                        }
                    }
                } else {
                    if (line.startsWith("{")) {
                        readClaims = true;
                    }
                }
            }
            claimsGT.put(n,cls);
            nrGT.put(n,nrs);
        }
    }

    public HashMap<String,HashMap<String, HashSet<String>>> loadGroundTruth() throws IOException {
        HashMap<String,HashMap<String,HashSet<String>>> correctMatches = new HashMap<>();
        for (File entry : new File(applicationSelect.getGroundTruthPath()).listFiles()) {
            BufferedReader br = new BufferedReader(new FileReader(entry));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("{")) {
                    break;
                }
                String[] sub = line.split(",");
                for (int i = 0; i < sub.length - 1;i++) {
                    if (!correctMatches.containsKey(sub[i])) {
                        HashMap<String,HashSet<String>> map = new HashMap<>();
                        HashSet<String> bindings = new HashSet<>();
                        bindings.add(sub[sub.length-1]);
                        map.put(entry.getName(),bindings);
                        correctMatches.put(sub[i],map);
                    } else {
                        if (!correctMatches.get(sub[i]).containsKey(entry.getName())) {
                            HashSet<String> bindings = new HashSet<>();
                            bindings.add(sub[sub.length-1]);
                            correctMatches.get(sub[i]).put(entry.getName(),bindings);
                        } else {
                            correctMatches.get(sub[i]).get(entry.getName()).add(sub[sub.length-1]);
                        }
                    }
                }
            }
        }
        return correctMatches;
    }

    public HashMap<String,Double> computeHitsAtK(int k) {
        HashMap<String,Double> scores = new HashMap<>();
        for (String gt : correctMatches.keySet()) {
            double temp = 0.0;
            for (Narrative n : results.keySet()) {
                int total = correctMatches.get(gt).get(n.getSrc().getName()).size();
                int correct = 0;
                HashSet<String> checked = new HashSet<>();
                for (Event e : n.getEvents()) {
                    for (int i = 0; i < completeMatches.get(e).size() && i < k; i++) {
                        EventMatch current = completeMatches.get(e).get(i);
                        if (correctMatches.get(gt).get(n.getSrc().getName()).contains(current.getD().getSrc().getName())
                                && !checked.contains(current.getD().getSrc().getName())) {
                            correct++;
                            checked.add(current.getD().getSrc().getName());
                        }
                    }
                }
                if (correct != 0) {
                    temp = temp + ((double)correct / (double)total);
                }
            }
            if (temp == 0.0) {
                scores.put(gt,0.0);
            } else {
                scores.put(gt,temp / results.keySet().size());
            }
        }
        return scores;
    }

    public double computeNRAccuracyAtK(int k) {
        double tempC = 0.0;
        int empty = 0;
        for (Narrative n : results.keySet()) {
            double tempN;
            int total = 0;
            int correct = 0;
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                for (int i = 0; i < completeMatches.get(nr.getNodeA()).size() && i < k; i++) {
                    for (int j = 0; j < completeMatches.get(nr.getNodeB()).size() && j < k; j++) {
                        EventMatch currentA = completeMatches.get(nr.getNodeA()).get(i);
                        EventMatch currentB = completeMatches.get(nr.getNodeB()).get(j);
                        try {
                            for (NarrativeBinding nb : nBindings.get(n).get(currentA).get(currentB)) {
                                total++;
                                if (nrGT.get(n).get(nr).equals(nb.getSupport())) {
                                    correct++;
                                }
                            }
                        } catch (Exception ex) {

                        }

                    }
                }
            }
            if (total == 0) {
                empty++;
            }
            if (correct != 0) {
                tempN = ((double)correct / (double)total);
                tempC = tempC + tempN;
            }
        }
        if (tempC == 0.0) {
            return 0.0;
        }
        return tempC / (double)(results.keySet().size() - empty);
    }

    public double computeClaimAccuracyAtK(int k) {
        double tempC = 0.0;
        int empty = 0;
        for (Narrative n : results.keySet()) {
            double tempN;
            int total = 0;
            int correct = 0;
            for (Event e : n.getEvents()) {
                for (int i = 0; i < completeMatches.get(e).size() && i < k; i++) {
                    EventMatch current = completeMatches.get(e).get(i);
                    try {
                        for (FactualBinding fb : fBindings.get(n).get(current)) {
                            total++;
                            if (claimsGT.get(n).get(fb.getCl()).equals(fb.isValid())) {
                                correct++;
                            }
                        }
                    } catch (Exception ex) {

                    }
                }

            }
            if (total == 0) {
                empty++;
            }
            if (correct != 0) {
                tempN = ((double)correct / (double)total);
                tempC = tempC + tempN;
            }
        }
        if (tempC == 0.0) {
            return 0.0;
        }
        return tempC / (double)(results.keySet().size() - empty);
    }


    public double computeAccuracy() {
        int max = 0;
        for (Event e : completeMatches.keySet()) {
            if (completeMatches.get(e).size() > max) {
                max = completeMatches.get(e).size();
            }
        }
        return computeAccuracyAtK(max);
    }

    public double computeAccuracyAtK(int k) {
        double tempC = 0.0;
        int empty = 0;
        for (Narrative n : results.keySet()) {
            double tempN;
            int total = 0;
            int correct = 0;
            for (Event e : n.getEvents()) {
                for (int i = 0; i < completeMatches.get(e).size() && i < k; i++) {
                    EventMatch current = completeMatches.get(e).get(i);
                    try {
                        for (FactualBinding fb : fBindings.get(n).get(current)) {
                            total++;
                            if (claimsGT.get(n).get(fb.getCl()).equals(fb.isValid())) {
                                correct++;
                            }
                        }
                    } catch (Exception ex) {

                    }
                }

            }
            for (NarrativeRelation nr : n.getNarrativeRelations()) {
                for (int i = 0; i < completeMatches.get(nr.getNodeA()).size() && i < k; i++) {
                    for (int j = 0; j < completeMatches.get(nr.getNodeB()).size() && j < k; j++) {
                        EventMatch currentA = completeMatches.get(nr.getNodeA()).get(i);
                        EventMatch currentB = completeMatches.get(nr.getNodeB()).get(j);
                        try {
                            for (NarrativeBinding nb : nBindings.get(n).get(currentA).get(currentB)) {
                                total++;
                                if (nrGT.get(n).get(nr).equals(nb.getSupport())) {
                                    correct++;
                                }
                            }
                        } catch (Exception ex) {

                        }

                    }
                }
            }
            if (total == 0) {
                empty++;
            }
            if (correct != 0) {
                tempN = ((double)correct / (double)total);
                tempC = tempC + tempN;
            }
        }
        if (tempC == 0.0) {
            return 0.0;
        }
        return tempC / (double)(results.keySet().size() - empty);
    }

    public double computeAvgReduction() {
        double reductionFactorSum = 0.0;
        for (Narrative n : results.keySet()) {
            reductionFactorSum = reductionFactorSum + ((double)results.get(n).size() / (double)nrData);
        }
        return 1.0 - (reductionFactorSum / (double)results.keySet().size());
    }

    public HashMap<String,Double> computeAvgPrecision() {
        HashMap<String,Double> scores = new HashMap<>();
        for (String gt : correctMatches.keySet()) {
            double temp = 0.0;
            for (Narrative n : results.keySet()) {
                int tp = 0;
                int total = 0;
                for (String sn : correctMatches.get(gt).keySet()) {
                    if (sn.equals(n.getSrc().getName())) {
                        for (DataSet d : results.get(n)) {
                            total++;
                            for (String sd : correctMatches.get(gt).get(sn)) {
                                if (sd.equals(d.getSrc().getName())) {
                                    tp++;
                                }
                            }
                        }
                        if (total == 0) {
                            if (tp == 0) {
                                temp = temp + 1.0;
                            } else {
                                temp = temp + 0.0;
                            }
                        } else {
                            temp = temp + ((double) tp / (double) total);
                        }
                    }
                }
            }
            scores.put(gt,temp / (double)results.keySet().size());
        }
        return scores;
    }

    /*public HashMap<String,Double> computeAvgRecallNew() {
        HashMap<String,Double> scores = new HashMap<>();
        for (String gt : correctMatches.keySet()) {
            double temp = 0.0;
            for (String sn : correctMatches.get(gt).keySet()) {
                int tp = 0;
                int total = 0;
                for (String sd : correctMatches.get(gt).get(sn)) {
                    total++;
                    //Discrepancy in Title Index
                    if (results.get(EvaluationTest.narrativeTitleIndex.get(sn))
                            .contains(EvaluationTest.titleIndex.get(sd))) {
                        tp++;
                    }
                }
                if (total == 0) {
                    temp = temp + 1.0;
                } else {
                    temp = temp + ((double) tp / (double) total);
                }
            }
            scores.put(gt,temp / (double)correctMatches.get(gt).size());
        }
        return scores;
    }*/

    public HashMap<String,Double> computeAvgRecall() {
        HashMap<String,Double> scores = new HashMap<>();
        for (String gt : correctMatches.keySet()) {
            double temp = 0.0;
            for (String sn : correctMatches.get(gt).keySet()) {
                int tp = 0;
                int total = 0;
                for (Narrative n : results.keySet()) {
                    if (sn.equals(n.getSrc().getName())) {
                        for (String sd : correctMatches.get(gt).get(sn)) {
                            total++;
                            for (DataSet d : results.get(n)) {
                                if (sd.equals(d.getSrc().getName())) {
                                    tp++;
                                }
                            }
                        }
                        if (total == 0) {
                            temp = temp + 1.0;
                        } else {
                            temp = temp + ((double) tp / (double) total);
                        }
                    }
                }
            }
            scores.put(gt,temp / (double)correctMatches.get(gt).size());
        }
        return scores;
    }

    public HashMap<String,Double> computeAvgF1() {
        HashMap<String,Double> scores = new HashMap<>();
        for (String gt : correctMatches.keySet()) {
            double f1 = 2 * ((this.avgPrecision.get(gt) * this.avgRecall.get(gt)) / (this.avgPrecision.get(gt) + this.avgRecall.get(gt)));
            scores.put(gt,f1);
        }
        return scores;
    }

    public HashMap<String,Double> getAvgPrecision() {
        return avgPrecision;
    }

    public double getAvgReduction() {
        return avgReduction;
    }

    public HashMap<String, HashMap<String, HashSet<String>>> getCorrectMatches() {
        return correctMatches;
    }

}
