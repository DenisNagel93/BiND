package system;

import datasetComponents.*;
import datasetComponents.Record;
import matches.*;
import narrativeComponents.NarrativeRelation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class RelationAssessment {

    HashMap<EventMatch, HashMap<EventMatch,HashSet<NarrativeBinding>>> mapping;
    HashSet<NarrativeBinding> bindings;
    File embedding;

    InstanceMatching imA,imB;

    public RelationAssessment(NarrativeRelation nr, InstanceMatching imA, InstanceMatching imB, File embedding, double tRA) {
        this.imA = imA;
        this.imB = imB;
        this.bindings = new HashSet<>();
        this.mapping = new HashMap<>();
        this.embedding = embedding;
        for (InstanceMatch iA : imA.getMatching()) {
            HashMap<EventMatch,HashSet<NarrativeBinding>> mapPart = new HashMap<>();
            for (InstanceMatch iB : imB.getMatching()) {
                HashSet<NarrativeBinding> mapped = new HashSet<>();
                if (iA.getE().isRefinedSuccessful() && iB.getE().isRefinedSuccessful()) {
                    if (!(iA.getE().getE().getCaption().equals(iB.getE().getE().getCaption()) && iA.getE().getA() != iB.getE().getA())) {
                        RelationScore sc = checkRelation(iA,iB,nr);
                        NarrativeBinding nb;
                        if (sc != null) {
                            if (sc.getScore() >= tRA) {
                                nb = new NarrativeBinding(iA,iB,sc,"True");
                            } else {
                                nb = new NarrativeBinding(iA,iB,sc,"False");
                            }
                            bindings.add(nb);
                            mapped.add(nb);
                        }
                    }
                }
                mapPart.put(iB.getE(),mapped);
            }
            mapping.put(iA.getE(),mapPart);
        }
    }

    public RelationScore checkRelation(InstanceMatch iA, InstanceMatch iB, NarrativeRelation nr) {
        ColumnPair cp;
        EventMatch eA = iA.getE();
        EventMatch eB = iB.getE();
        if (eA.getD().getJoinCandidates(eB.getD()).size() > 0) {
            HashSet<Attribute> constrA = eA.getAssociatedAttributes();
            constrA = refineJoinConstraints(iA,constrA);
            HashSet<Attribute> constrB = eB.getAssociatedAttributes();
            constrB = refineJoinConstraints(iB,constrB);
            Join j = new Join(iA, iB, eA.getD().getJoinCandidates(eB.getD()),constrA,constrB);
            if (j.getJoinMatches().size() == 0) {
                return null;
            }
            cp = new ColumnPair(eA.getA(), eB.getA(), j,iA,iB);
            return new RelationScore(nr,cp,computeMetric(nr,cp,j));
        }
        return null;
    }

    public HashSet<Attribute> refineJoinConstraints(InstanceMatch i, HashSet<Attribute> constr) {
        HashSet<Attribute> newConstr = new HashSet<>();
        for (Attribute a : constr) {
            if (!(i.getNoMatch().contains(a))) {
                newConstr.add(a);
            }
        }
        return newConstr;
    }

    public double computeMetric(NarrativeRelation nr, ColumnPair cp, Join j) {

        //System.out.println("Type: " + nr.getLabel());
        switch (nr.getLabel().trim()) {
            case "causes" :
            case "prevents" :
                //return computeAvgWCAL(cp,j);
            case "correlates" :
                return computePearson(cp,j,false);
            case "reduces" :
                return computePearson(cp,j,true);
            case "lower probability" :
                return computeRelativeDifference(cp,j,"lower");
            case "higher probability" :
                return computeRelativeDifference(cp,j,"higher");
            case "higher" :
                return computeDifference(cp,j,"higher");
            case "lower" :
                return computeDifference(cp,j,"lower");
            case "equal" :
                return computeDifference(cp,j,"equal");
            default:
                System.out.println("No suitable relation type!");
                break;
        }
        return -1.0;
    }

    public double computeRelativeDifference(ColumnPair cp, Join j, String type) {
        if (cp.getA().isRelative && cp.getB().isRelative) {
            return computeDifference(cp,j,type);
        }
        return -1.0;
    }

    //computation of the Pearson correlation coefficient for the match
    public Double computeDifference(ColumnPair cp, Join j, String type) {
        int valid = 0;
        int countHigher = 0;
        int errors = 0;
        int countEqual = 0;
        for (Record rA : cp.getRelevantA()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                if (!(rA == rB && cp.getA() == cp.getB())) {
                    if (cp.getRelevantB().contains(rB)) {
                        valid++;

                        try {
                            if (Helper.prepareForDouble(rA.getEntry(cp.getA())) > Helper.prepareForDouble(rB.getEntry(cp.getB()))) {
                                countHigher++;
                            } else {
                                if (Helper.prepareForDouble(rA.getEntry(cp.getA())) == Helper.prepareForDouble(rB.getEntry(cp.getB()))) {
                                    countEqual++;
                                }
                            }
                        } catch (Exception e) {
                            errors++;
                        }
                    }
                }
            }
        }
        double higherPercentage = 0.0;
        double equalPercentage = 0.0;

        double result;

        if (valid > 0) {
            if (cp.getRelevantA().size() - errors != 0.0) {
                higherPercentage = ((double)countHigher / (double)(valid - errors));
                equalPercentage = ((double)countEqual / (double)(valid - errors));
            }
        } else {
            return -1.0;
        }
        if (type.equals("lower")) {
            result = 1.0 - (higherPercentage + equalPercentage);
        } else {
            if (type.equals("equal")) {
                result = equalPercentage;
            } else {
                result = higherPercentage;
            }
        }

        /*if (percentage < 0.5) {
            return 0.0;
        }*/
        return (result - 0.5) * 2;
    }

    public Double computePearson(ColumnPair cp, Join j, boolean negative) {
        double sA = 0.0;
        double sB = 0.0;
        double sAA = 0.0;
        double sBB = 0.0;
        double sAB = 0.0;
        int errors = 0;
        int total = 0;
        for (Record rA : cp.getRelevantA()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                if (cp.getRelevantB().contains(rB)) {
                    total++;
                    try {
                        double a = Helper.prepareForDouble(rA.getEntry(cp.getA()));
                        double b = Helper.prepareForDouble(rB.getEntry(cp.getB()));
                        sA = sA + a;
                        sB = sB + b;
                        sAA = sAA + (a * a);
                        sBB = sBB + (b * b);
                        sAB = sAB + (a * b);
                    } catch (Exception e) {
                        errors++;
                    }
                }
            }
        }
        double n = (double)total - (double)errors;
        double cov = (sAB / n) - (sA * sB) / n / n;
        double sigmaA = Math.sqrt(sAA / n - sA * sA / n / n);
        double sigmaB = Math.sqrt(sBB / n - sB * sB / n / n);
        if (negative) {
            return (cov / sigmaA / sigmaB) * -1;
        }
        return cov / sigmaA / sigmaB;
    }

    /*public Double computeAvgWCAL(ColumnPair cp,Join j) throws IOException {
        System.out.println("----Start Computation");
        HashSet<Attribute> contexts = gatherContext(cp.getB());
        double sum = 0.0;
        int count = 0;
        for (Attribute c : contexts) {
            sum = sum + computeWCAL(cp,j,c);
            count++;
        }
        return sum / (double)count;
    }*/

    /*public HashSet<Attribute> gatherContext(Attribute b) throws IOException {
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
    }*/

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
        return ((double)pYbXZ / (double)tYbXZ) - ((double)pYbZ / (double)tYbZ);
    }

    public Double computePartCAL(ColumnPair cp, Join j, HashSet<Record> expGrpE, HashSet<Record> expGrpC, HashSet<Record> expGrpCon, Join contextJoinC, Join contextJoinE) {
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
        return ((double)pYbXZ / (double)tYbXZ) - ((double)pYbZ / (double)tYbZ);
    }

    public Double computeWCAL(ColumnPair cp,Join j,Attribute context) {
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
        int pZbX = 0;
        int tZbX = 0;
        int pnZbX = 0;
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
        double calscore = ((double)pZbX / (double)tZbX) * computePartCAL(cp,j,expGrpE,expGrpC,expGrpCon,contextJoinC,contextJoinE);
        double calnscore = ((double)pnZbX / (double)tZbX) * computePartCAL(cp,j,expGrpE,expGrpC,ctrlGrpCon,contextJoinC,contextJoinE);
        return calscore + calnscore;
    }

    public HashSet<NarrativeBinding> getBindings() {
        return bindings;
    }

    public HashMap<EventMatch, HashMap<EventMatch, HashSet<NarrativeBinding>>> getMapping() {
        return mapping;
    }
}
