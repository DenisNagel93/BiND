package datasetComponents;

import matches.InstanceMatch;
import narrativeComponents.NarrativeRelation;
import system.Helper;
import system.InstanceMatching;

import java.util.ArrayList;
import java.util.HashSet;

public class ColumnPair {

    ArrayList<Record> columnA, columnB;
    HashSet<Record> relevantA, relevantB;
    Attribute a,b;
    double avgA,avgB;

    public ColumnPair(NarrativeRelation nr, Attribute a, Attribute b, DataSet d, InstanceMatch im) {
        this.columnA = new ArrayList<>();
        this.columnB = new ArrayList<>();
        this.relevantA = new HashSet<>();
        this.relevantB = new HashSet<>();
        for (Record r : d.getRecords()) {
            columnA.add(r);
            columnB.add(r);
            if (im.getReductionByEvent(nr.getNodeA()).contains(r)
                    && im.getReductionByEvent(nr.getNodeB()).contains(r)) {
                relevantA.add(r);
                relevantB.add(r);
            }
        }
        this.a = a;
        this.b = b;
        this.avgA = Helper.computeAvg(a,relevantA);
        this.avgB = Helper.computeAvg(b,relevantB);
    }

    public ColumnPair(NarrativeRelation nr, Attribute a, Attribute b, Join j, InstanceMatch imA, InstanceMatch imB) {
        this.columnA = new ArrayList<>();
        this.columnB = new ArrayList<>();
        this.relevantA = new HashSet<>();
        this.relevantB = new HashSet<>();
        for (Record rA : j.getJoinMatches().keySet()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                columnA.add(rA);
                columnB.add(rB);
                if ((!imA.getNoMatch().contains(a) && !imB.getNoMatch().contains(b))
                        && imA.getReductionByEvent(nr.getNodeA()) != null && imB.getReductionByEvent(nr.getNodeB()) != null) {
                    if (imA.getReductionByEvent(nr.getNodeA()).contains(rA)) {
                        relevantA.add(rA);
                    }
                    if (imB.getReductionByEvent(nr.getNodeB()).contains(rB)) {
                        relevantB.add(rB);
                    }
                }
            }
        }
        //System.out.println("Relevant A: " + relevantA.size());
        //System.out.println("Relevant B: " + relevantB.size());
        this.a = a;
        this.b = b;
        this.avgA = Helper.computeAvg(a,relevantA);
        this.avgB = Helper.computeAvg(b,relevantB);
    }

    public ColumnPair(NarrativeRelation nr, Attribute a, Attribute b, Join j, InstanceMatch im) {
        this.columnA = new ArrayList<>();
        this.columnB = new ArrayList<>();
        this.relevantA = new HashSet<>();
        this.relevantB = new HashSet<>();
        for (Record rA : j.getJoinMatches().keySet()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                columnA.add(rA);
                columnB.add(rB);
                if ((!im.getNoMatch().contains(a) && !im.getNoMatch().contains(b))) {
                    if (im.getReductionByEvent(nr.getNodeA()).contains(rA)) {
                        relevantA.add(rA);
                    }
                    if (im.getReductionByEvent(nr.getNodeB()).contains(rB)) {
                        relevantB.add(rB);
                    }
                }
            }
        }
        System.out.println("Relevant A: " + relevantA.size());
        System.out.println("Relevant B: " + relevantB.size());
        this.a = a;
        this.b = b;
        this.avgA = Helper.computeAvg(a,relevantA);
        this.avgB = Helper.computeAvg(b,relevantB);
    }

    public ArrayList<Record> getColumnA() {
        return columnA;
    }

    public ArrayList<Record> getColumnB() {
        return columnB;
    }

    public HashSet<Record> getRelevantA() {
        return relevantA;
    }

    public HashSet<Record> getRelevantB() {
        return relevantB;
    }

    public Attribute getA() {
        return a;
    }

    public Attribute getB() {
        return b;
    }

    public double getAvgA() {
        return avgA;
    }

    public double getAvgB() {
        return avgB;
    }
}
