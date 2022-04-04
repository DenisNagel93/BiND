package system;

import datasetComponents.*;
import datasetComponents.Record;
import matches.*;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;
import narrativeComponents.NarrativeRelation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class RelationAssessment {

    InstanceMatching im;
    HashSet<NarrativeBinding> bindings;
    File embedding;

    InstanceMatching imA,imB;

    public RelationAssessment(InstanceMatching im, File embedding) throws IOException {
        this.im = im;
        this.bindings = new HashSet<>();
        this.embedding = embedding;
        for (InstanceMatch i : im.getMatching()) {
            HashMap<NarrativeRelation,RelationScore> scores = new HashMap<>();
            if (i.getS().isSuccessful()) {
                for (NarrativeRelation nr : i.getS().getN().getNarrativeRelations()) {
                    scores.put(nr,checkRelation(i,nr));
                }
            }
            NarrativeBinding nb = new NarrativeBinding(i,scores);
            bindings.add(nb);
        }
    }

    public RelationAssessment(NarrativeRelation nr, InstanceMatching imA, InstanceMatching imB, File embedding) throws IOException {
        this.imA = imA;
        this.imB = imB;
        this.bindings = new HashSet<>();
        this.embedding = embedding;
        for (InstanceMatch iA : imA.getMatching()) {
            //System.out.println("A1: " + iA.getE().getA().getTitle());
            for (InstanceMatch iB : imB.getMatching()) {
                //if (iA.getE().getA() != iB.getE().getA()) {
                    //System.out.println("--A2: " + iB.getE().getA().getTitle());
                    if (iA.getE().isRefinedSuccessful() && iB.getE().isRefinedSuccessful()) {
                        RelationScore sc = checkRelation(iA,iB,nr);
                        NarrativeBinding nb = new NarrativeBinding(iA,iB,sc);
                        bindings.add(nb);
                    }
                //}
            }
        }
    }

    public void printResult(String outputPath) throws IOException {
        File outfile = new File(outputPath + "/RelationAssessment/" + im.getSm().getN().getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (NarrativeBinding nb : bindings) {
            SchemaMatch sm = nb.getIm().getS();
            bw.write("MatchScore: " + sm.getScore());
            bw.newLine();
            for (Event e : sm.getEventMatches().keySet()) {
                bw.write(e.getLabel());
                try {
                    bw.write("(" + sm.getEventMatches().get(e).getScore() + ") "
                            + "-> " + sm.getEventMatches().get(e).getA().getTitle()
                            + " (" + sm.getEventMatches().get(e).getA().getDs().getSrc() + ")");
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write(" -> No match found!");
                    bw.newLine();
                }
                for (EventProperty ep : e.getProperties()) {
                    bw.write("-" + ep.getLabel());
                    try {
                        bw.write("(" + sm.getEventMatches().get(e).getPm().get(ep).getScore() + ") "
                                + "-> " + sm.getEventMatches().get(e).getPm().get(ep).getA().getTitle()
                                + " (" + sm.getEventMatches().get(e).getPm().get(ep).getA().getDs().getSrc() + ")");
                        bw.newLine();
                    } catch (Exception ex) {
                        bw.write(" -> No match found!");
                        bw.newLine();
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + sm.getEventMatches().get(e).getFm().get(fr).getScore() + ") "
                                    + "-> " + sm.getEventMatches().get(e).getFm().get(fr).getA().getTitle()
                                    + " (" + sm.getEventMatches().get(e).getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
                bw.newLine();
            }
            for (NarrativeRelation nr : sm.getN().getNarrativeRelations()) {
                try {
                    bw.write(nr.getNodeA().getCaption() + "(" + nb.getIm().getReductionByEvent(nr.getNodeA()).size() + "/" + nb.getIm().getS().getEventMatches().get(nr.getNodeA()).getD().getRecords().size()
                            + ") " + nr.getLabel() + " "
                            + nr.getNodeB().getCaption() + "(" + nb.getIm().getReductionByEvent(nr.getNodeB()).size() + "/" + nb.getIm().getS().getEventMatches().get(nr.getNodeB()).getD().getRecords().size()
                            + ") -> " + nb.getScores().get(nr).getScore());
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write("No successful binding!");
                    bw.newLine();
                }
            }
            bw.write("----------------");
            bw.newLine();
        }
        bw.close();
    }

    public RelationScore checkRelation(InstanceMatch iA, InstanceMatch iB, NarrativeRelation nr) throws IOException {
        ColumnPair cp;
        EventMatch eA = iA.getE();
        EventMatch eB = iB.getE();
        if (eA.getD().getJoinCandidates(eB.getD()).size() > 0) {
            //System.out.println("--------Prepare Join!");
            HashSet<Attribute> constrA = eA.getAssociatedAttributes();
            constrA = refineJoinConstraints(iA,nr,constrA);
            //System.out.println("Constr: " + constrA.size());
            HashSet<Attribute> constrB = eB.getAssociatedAttributes();
            constrB = refineJoinConstraints(iB,nr,constrB);
            //System.out.println("JoinCand: " + eA.getD().getJoinCandidates(eB.getD()).size());
            Join j = new Join(iA, iB, eA.getD().getJoinCandidates(eB.getD()),constrA,constrB);
            cp = new ColumnPair(nr,eA.getA(), eB.getA(), j,iA,iB);
            RelationScore rs = new RelationScore(nr,cp,computeMetric(nr,cp,j));
            return rs;
        }
        return null;
    }

    public RelationScore checkRelation(InstanceMatch i, NarrativeRelation nr) throws IOException {
        ColumnPair cp;
        EventMatch eA = i.getS().getEventMatches().get(nr.getNodeA());
        EventMatch eB = i.getS().getEventMatches().get(nr.getNodeB());
        /*if (eA.getD() == eB.getD()) {
            cp = new ColumnPair(nr,eA.getA(),eB.getA(),eA.getD(),i);
            RelationScore rs = new RelationScore(nr,cp,computeMetric(nr,cp));
            return rs;
        } else {*/
            if (eA.getD().getJoinCandidates(eB.getD()).size() > 0) {
                HashSet<Attribute> constrA = eA.getAssociatedAttributes();
                constrA = refineJoinConstraints(i,nr,constrA);
                HashSet<Attribute> constrB = eB.getAssociatedAttributes();
                constrB = refineJoinConstraints(i,nr,constrB);
                Join j = new Join(eA.getD(), eB.getD(), eA.getD().getJoinCandidates(eB.getD()),constrA,constrB);
                cp = new ColumnPair(nr,eA.getA(), eB.getA(), j,i);
                RelationScore rs = new RelationScore(nr,cp,computeMetric(nr,cp,j));
                return rs;
            }
        //}
        return null;
    }

    public HashSet<Attribute> refineJoinConstraints(InstanceMatch i, NarrativeRelation nr, HashSet<Attribute> constr) {
        HashSet<Attribute> newConstr = new HashSet<>();
        for (Attribute a : constr) {
            if (!(i.getNoMatch().contains(a))) {
                newConstr.add(a);
            }
        }
        return newConstr;
    }

    public double computeMetric(NarrativeRelation nr, ColumnPair cp, Join j) throws IOException {

        //System.out.println("Type: " + nr.getLabel());
        switch (nr.getLabel()) {
            case "causes" :
            case "prevents" :
                return computeAvgWCAL(cp,j);
            case "correlates" :
            case "reduces" :
                return computePearson(cp,j);
            case "lower probability" :
                return computeRelativeDifference(cp,j,true);
            case "higher probability" :
                return computeRelativeDifference(cp,j,false);
            case "higher" :
                return computeDifference(cp,j,false);
            case "lower" :
                return computeDifference(cp,j,true);
            default:
                System.out.println("No suitable relation type!");
                break;
        }
        return -1.0;
    }

    public Double computeAvgWCAL(ColumnPair cp,Join j) throws IOException {
        System.out.println("----Start Computation");
        HashSet<Attribute> contexts = gatherContext(cp.getB());
        double sum = 0.0;
        int count = 0;
        for (Attribute c : contexts) {
            sum = sum + computeWCAL(cp,j,c);
            count++;
        }
        return sum / (double)count;
    }

    public double computeRelativeDifference(ColumnPair cp, Join j, boolean negative) {
        System.out.println("ComputeRelative!");
        if (cp.getA().isRelative && cp.getB().isRelative) {
            System.out.println("Attributes fit!");
            return computeDifference(cp,j,negative);
        }
        return -1.0;
    }

    //computation of the Pearson correlation coefficient for the match
    public Double computeDifference(ColumnPair cp, Join j, boolean negative) {
        int valid = 0;
        int countHigher = 0;
        int errors = 0;
        //System.out.println("Relevant Size: " + cp.getRelevantA().size());
        for (Record rA : cp.getRelevantA()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                if (!(rA == rB && cp.getA() == cp.getB())) {
                    if (cp.getRelevantB().contains(rB)) {
                        valid++;

                        //System.out.print("Compare A: " + rA.getEntry(cp.getA()) + " <-> ");
                        //System.out.println("Compare B: " + rB.getEntry(cp.getB()));
                        try {
                            if (Helper.prepareForDouble(rA.getEntry(cp.getA())) > Helper.prepareForDouble(rB.getEntry(cp.getB()))) {
                                //System.out.println("Count Up!");
                                countHigher++;
                            }
                        } catch (Exception e) {
                            errors++;
                        }
                    }
                }
            }
        }
        double percentage = 0.0;
        //System.out.println("#Count: " + countHigher);
        //System.out.println("#Entries: " + cp.getRelevantA().size());
        //System.out.println("#Errors: " + errors);
        if (valid > 0) {
            if (cp.getRelevantA().size() - errors != 0.0) {
                percentage = ((double)countHigher / (double)(valid - errors));
                //System.out.println("Percentage: " + percentage);
            }
        } else {
            return -1.0;
        }
        if (negative) {
            System.out.println("Score: " + percentage);
            return 1.0 - percentage;
        }
        return percentage;
    }

    //computation of the Pearson correlation coefficient for the match
    public Double computePearson(ColumnPair cp, Join j) {
        double sumA = 0.0;
        double sumB = 0.0;
        int errors = 0;
        for (Record rA : cp.getRelevantA()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                if (cp.getRelevantB().contains(rB)) {
                    try {
                        sumA = sumA + Helper.prepareForDouble(rA.getEntry(cp.getA()));
                        sumB = sumB + Helper.prepareForDouble(rB.getEntry(cp.getB()));
                    } catch (Exception e) {
                        errors++;
                    }
                }
            }
        }
        double avgA = sumA / (double)(cp.getRelevantA().size() - errors);
        double avgB = sumB / (double)(cp.getRelevantB().size() - errors);

        double cov = 0.0;
        double varA = 0.0;
        double varB = 0.0;
        for (Record rA : cp.getRelevantA()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                if (cp.getRelevantB().contains(rB)) {
                    try {
                        cov = cov + ((Helper.prepareForDouble(rA.getEntry(cp.getA())) - avgA) * (Helper.prepareForDouble(rB.getEntry(cp.getB())) - avgB));
                        varA = varA + Math.pow(Helper.prepareForDouble(rA.getEntry(cp.getA())) - avgA, 2);
                        varB = varB + Math.pow(Helper.prepareForDouble(rB.getEntry(cp.getB())) - avgB, 2);
                    } catch (Exception e) {

                    }
                }
            }
        }
        return cov / (Math.sqrt(varA) * Math.sqrt(varB));
    }

    public HashSet<Attribute> gatherContext(Attribute b) throws IOException {
        System.out.println("--Gather Contexts");
        HashSet<Attribute> context = new HashSet<>();
        BufferedReader br = new BufferedReader(new FileReader(embedding));
        String line;
        while ((line = br.readLine()) != null && context.size() <= 5) {
            String[] subString = line.split(",");
            if (b.getTitle().replace("\"","").equals(subString[0])) {
                for (DataSet d : im.getSm().getRepository()) {
                    for (Attribute a : d.getAttributes()) {
                        if (a.getTitle().replace("\"","").equals(subString[1])) {
                            context.add(a);
                        }
                    }
                }
            }
        }
        return context;
    }

    public void assignToGroups(HashSet<Record> base, Attribute a, HashSet<Record> eg, HashSet<Record> cg) {
        for (Record r : base) {
            try {
                if (Double.parseDouble(r.getEntry(a)) > Helper.computeAvg(a,base)) {
                    eg.add(r);
                } else {
                    cg.add(r);
                }
            } catch (Exception ex) {
                System.out.println("No double");
            }
        }
    }

    public Double computeCAL(ColumnPair cp,Join j,Attribute context) {
        Attribute c = cp.getA();
        Attribute e = cp.getB();
        Join contextJoinE = new Join(e.getDs(), context.getDs(), e.getDs().getJoinCandidates(context.getDs()));
        Join contextJoinC = new Join(c.getDs(), context.getDs(), c.getDs().getJoinCandidates(context.getDs()));
        HashSet<Record> expGrpE = new HashSet<>();
        HashSet<Record> ctrlGrpE = new HashSet<>();
        assignToGroups(cp.getRelevantB(),cp.getB(),expGrpE,ctrlGrpE);
        HashSet<Record> expGrpC = new HashSet<>();
        HashSet<Record> ctrlGrpC = new HashSet<>();
        assignToGroups(cp.getRelevantA(),cp.getA(),expGrpC,ctrlGrpC);
        HashSet<Record> expGrpCon = new HashSet<>();
        HashSet<Record> ctrlGrpCon = new HashSet<>();
        assignToGroups(context.getDs().getRecords(),context,expGrpCon,ctrlGrpCon);
        int pYbXZ = 0;
        int tYbXZ = 0;
        int pYbZ = 0;
        int tYbZ = 0;
        for (Record rE : cp.getRelevantB()) {
            if (expGrpE.contains(rE)) {
                for (Record rCon : contextJoinE.getJoinMatches().get(rE)) {
                    tYbZ++;
                    if (expGrpCon.contains(rCon)) {
                        pYbZ++;
                        for (Record rC : j.getJoinMatches().get(rE)) {
                            tYbXZ++;
                            if (expGrpC.contains(rC)
                                && contextJoinC.getJoinMatches().get(rC).contains(rCon)) {
                                pYbXZ++;
                            }
                        }
                    }
                }
            }
        }
        double score = ((double)pYbXZ / (double)tYbXZ) - ((double)pYbZ / (double)tYbZ);
        return score;
    }

    public Double computePartCAL(ColumnPair cp, Join j, HashSet<Record> expGrpE, HashSet<Record> expGrpC, HashSet<Record> expGrpCon, Join contextJoinC, Join contextJoinE) {
        System.out.println("Compute PartCAL");
        int pYbXZ = 0;
        int tYbXZ = 0;
        int pYbZ = 0;
        int tYbZ = 0;
        for (Record rE : cp.getRelevantB()) {
            if (expGrpE.contains(rE)) {
                for (Record rCon : contextJoinE.getJoinMatches().get(rE)) {
                    tYbZ++;
                    if (expGrpCon.contains(rCon)) {
                        pYbZ++;
                        for (Record rC : j.getJoinMatches().get(rE)) {
                            tYbXZ++;
                            if (expGrpC.contains(rC)
                                    && contextJoinC.getJoinMatches().get(rC).contains(rCon)) {
                                pYbXZ++;
                            }
                        }
                    }
                }
            }
        }
        double score = ((double)pYbXZ / (double)tYbXZ) - ((double)pYbZ / (double)tYbZ);
        return score;
    }

    public Double computeWCAL(ColumnPair cp,Join j,Attribute context) {
        System.out.println("--Compute WCAL -> " + cp.getA().getTitle() + " " + cp.getB().getTitle() + " | " + context.getTitle());
        Attribute c = cp.getA();
        Attribute e = cp.getB();
        System.out.println("-Computing Joins");
        Join contextJoinE = new Join(e.getDs(), context.getDs(), e.getDs().getJoinCandidates(context.getDs()));
        Join contextJoinC = new Join(c.getDs(), context.getDs(), c.getDs().getJoinCandidates(context.getDs()));
        System.out.println("-Assigning Groups");
        HashSet<Record> expGrpE = new HashSet<>();
        HashSet<Record> ctrlGrpE = new HashSet<>();
        assignToGroups(cp.getRelevantB(),cp.getB(),expGrpE,ctrlGrpE);
        HashSet<Record> expGrpC = new HashSet<>();
        HashSet<Record> ctrlGrpC = new HashSet<>();
        assignToGroups(cp.getRelevantA(),cp.getA(),expGrpC,ctrlGrpC);
        HashSet<Record> expGrpCon = new HashSet<>();
        HashSet<Record> ctrlGrpCon = new HashSet<>();
        assignToGroups(context.getDs().getRecords(),context,expGrpCon,ctrlGrpCon);
        int pZbX = 0;
        int tZbX = 0;
        int pnZbX = 0;
        System.out.println("-Computing Probabilities");
        for (Record rC : cp.getRelevantA()) {
            if (expGrpC.contains(rC)) {
                for (Record rCon : contextJoinC.getJoinMatches().get(rC)) {
                    tZbX++;
                    if (expGrpCon.contains(rCon)) {
                        pZbX++;
                    }
                    if (ctrlGrpCon.contains(rCon)) {
                        pnZbX++;
                    }
                }
            }
        }
        System.out.println("-Computing Metric");
        double calscore = ((double)pZbX / (double)tZbX) * computePartCAL(cp,j,expGrpE,expGrpC,expGrpCon,contextJoinC,contextJoinE);
        double calnscore = ((double)pnZbX / (double)tZbX) * computePartCAL(cp,j,expGrpE,expGrpC,ctrlGrpCon,contextJoinC,contextJoinE);
        double score = calscore + calnscore;
        return score;
    }

    public HashSet<NarrativeBinding> getBindings() {
        return bindings;
    }
}
