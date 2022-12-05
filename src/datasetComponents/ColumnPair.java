package datasetComponents;

import matches.InstanceMatch;
import system.Helper;

import java.util.ArrayList;
import java.util.HashSet;

//A pair of data set columns to compare
public class ColumnPair {

    //The set of records each column consists of
    ArrayList<Record> columnA, columnB;
    //The relevant records of each column (according to previous instance matching)
    HashSet<Record> relevantA, relevantB;
    //Attribute represented in each column
    Attribute a,b;
    //Average data value over the whole column
    double avgA,avgB;

    //Constructor for two columns of different data sets (new)
    public ColumnPair(Attribute a, Attribute b, Join j, InstanceMatch imA, InstanceMatch imB) {
        this.columnA = new ArrayList<>();
        this.columnB = new ArrayList<>();
        this.relevantA = new HashSet<>();
        this.relevantB = new HashSet<>();
        for (Record rA : j.getJoinMatches().keySet()) {
            for (Record rB : j.getJoinMatches().get(rA)) {
                columnA.add(rA);
                columnB.add(rB);
                if ((!imA.getNoMatch().contains(a) && !imB.getNoMatch().contains(b))
                        && imA.getEventReduction() != null && imB.getEventReduction() != null) {
                    if (imA.getEventReduction().contains(rA)) {
                        relevantA.add(rA);
                    }
                    if (imB.getEventReduction().contains(rB)) {
                        relevantB.add(rB);
                    }
                }
            }
        }
        this.a = a;
        this.b = b;
        this.avgA = Helper.computeAvg(a,relevantA);
        this.avgB = Helper.computeAvg(b,relevantB);
    }

    //--------Getter/Setter--------

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

}
