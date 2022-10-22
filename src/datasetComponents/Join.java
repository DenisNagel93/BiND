package datasetComponents;

import matches.InstanceMatch;

import java.util.HashMap;
import java.util.HashSet;

//A join between two data sets
public class Join {

    //The joined data sets
    DataSet dA,dB;
    //Join Attributes
    HashMap<Attribute,Attribute> joinAttributes;
    //Records of the joined table
    HashMap<Record,HashSet<Record>> joinMatches;
    //Attributes that are explicitly excluded from being considered as join attributes
    HashSet<Attribute> constrA,constrB;
    //Instance Match for each Data Set
    InstanceMatch iA,iB;

    //Constructor(Given:Instance Matches)
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

    //Constructor(Given: Data Sets/No Constraints)
    public Join(DataSet dA, DataSet dB, HashMap<Attribute,Attribute> joinAttributes) {
        this.dA = dA;
        this.dB = dB;
        this.joinAttributes = joinAttributes;
        this.constrA = new HashSet<>();
        this.constrB = new HashSet<>();
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    //Removes constraint attributes from join candidates
    public HashMap<Attribute,Attribute> prepareJoinAttributes() {
        HashMap<Attribute,Attribute> attributes = new HashMap<>();
        for (Attribute a : joinAttributes.keySet()) {
            if (!constrA.contains(a) && !constrB.contains(joinAttributes.get(a)) /*&& !a.getTitle().equals("Comments")*/) {
                attributes.put(a,joinAttributes.get(a));
            }
        }
        return attributes;
    }

    //Computes the Join
    public HashMap<Record,HashSet<Record>> computeJoin() {
        HashMap<Record,HashSet<Record>> matches = new HashMap<>();
        int countA = 0;
        int countB = 0;
        for (Record rA : iA.getEventReduction()) {
            HashSet<Record> joinPartner = new HashSet<>();
            countA++;
            for (Record rB : iB.getEventReduction()) {
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
                        matches.put(rB,joinPartnerB);
                    }
                }
                if (countB == 1000) {
                    countB = 0;
                    break;
                }
            }
            matches.put(rA,joinPartner);
            if (countA == 1000) {
                break;
            }
        }
        return matches;
    }

    //--------Getter/Setter--------

    public HashMap<Record, HashSet<Record>> getJoinMatches() {
        return joinMatches;
    }

}
