package datasetComponents;

import matches.InstanceMatch;

import java.util.HashMap;
import java.util.HashSet;

public class Join {

    DataSet dA,dB;
    HashMap<Attribute,Attribute> joinAttributes;
    HashMap<Record,HashSet<Record>> joinMatches;
    HashSet<Attribute> constrA,constrB;

    InstanceMatch iA,iB;

    public Join(InstanceMatch iA, InstanceMatch iB, HashMap<Attribute,Attribute> joinAttributes, HashSet<Attribute> constrA, HashSet<Attribute> constrB) {
        this.iA = iA;
        this.iB = iB;
        this.dA = iA.getE().getD();
        this.dB = iB.getE().getD();
        this.joinAttributes = joinAttributes;
        this.constrA = constrA;
        this.constrB = constrB;
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    public Join(DataSet dA, DataSet dB, HashMap<Attribute,Attribute> joinAttributes, HashSet<Attribute> constrA, HashSet<Attribute> constrB) {
        this.dA = dA;
        this.dB = dB;
        this.joinAttributes = joinAttributes;
        this.constrA = constrA;
        this.constrB = constrB;
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    public Join(DataSet dA, DataSet dB, HashMap<Attribute,Attribute> joinAttributes) {
        this.dA = dA;
        this.dB = dB;
        this.joinAttributes = joinAttributes;
        this.constrA = new HashSet<>();
        this.constrB = new HashSet<>();
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    public HashMap<Attribute,Attribute> prepareJoinAttributes() {
        HashMap<Attribute,Attribute> attributes = new HashMap<>();
        /*for (Attribute test : constrA) {
            System.out.println("CstrA: " + test.getTitle());
        }
        for (Attribute test : constrB) {
            System.out.println("CstrB: " + test.getTitle());
        }*/
        for (Attribute a : joinAttributes.keySet()) {
            if (!constrA.contains(a) && !constrB.contains(joinAttributes.get(a).getTitle())) {
                attributes.put(a,joinAttributes.get(a));
                //System.out.println("Join über: " + a + " " + joinAttributes.get(a).getTitle());
            }
        }
        return attributes;
    }

    public HashMap<Record,HashSet<Record>> computeJoin() {
        //System.out.println("RelA: " + iA.getReductionByEvent(iA.getE().getE()).size());
        //System.out.println("RelB: " + iB.getReductionByEvent(iB.getE().getE()).size());
        HashMap<Record,HashSet<Record>> matches = new HashMap<>();
        int countA = 0;
        int countB = 0;
        for (Record rA : iA.getReductionByEvent(iA.getE().getE())) {
            HashSet<Record> joinPartner = new HashSet<>();
            countA++;
            for (Record rB : iB.getReductionByEvent(iB.getE().getE())) {
                countB++;
                boolean compatible = true;
                for (Attribute a : joinAttributes.keySet()) {
                    try {
                        if (!(rA.getEntry(a).equals(rB.getEntry(joinAttributes.get(a))))) {
                            compatible = false;
                        }
                    } catch (Exception ex) {
                        compatible = false;
                    }
                }
                if (compatible) {
                    joinPartner.add(rB);
                }
                if (matches.containsKey(rB)) {
                    if (compatible) {
                        matches.get(rB).add(rA);
                    }
                } else {
                    HashSet<Record> joinPartnerB = new HashSet<>();
                    if (compatible) {
                        joinPartnerB.add(rA);
                    }
                    matches.put(rB,joinPartnerB);
                }
                if (countB == 1000) {
                    countB = 0;
                    break;
                }
            }
            matches.put(rA,joinPartner);
            if (countA == 1000) {
                countA = 0;
                break;
            }
        }
        //System.out.println("Join Matches: " + matches.size());
        return matches;
    }

    public DataSet getdA() {
        return dA;
    }

    public DataSet getdB() {
        return dB;
    }

    public HashMap<Record, HashSet<Record>> getJoinMatches() {
        return joinMatches;
    }

    public HashMap<Attribute,Attribute> getJoinAttributes() {
        return joinAttributes;
    }
}
