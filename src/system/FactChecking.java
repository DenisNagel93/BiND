package system;

import datasetComponents.Record;
import matches.EventMatch;
import matches.FactualBinding;
import matches.InstanceMatch;
import narrativeComponents.Claim;

import java.util.HashMap;
import java.util.HashSet;

public class FactChecking {

    InstanceMatching im;
    InstanceMatching imB;
    Claim c;

    HashMap<EventMatch,FactualBinding> results;
    HashSet<FactualBinding> resultSet;

    public FactChecking(InstanceMatching im,Claim c) {
        this.im = im;
        this.c = c;
        this.results = new HashMap<>();
        this.resultSet = new HashSet<>();
        for (InstanceMatch i : im.getMatching()) {
            FactualBinding fb = new FactualBinding(i,c,checkClaim(i));
            results.put(i.getE(),fb);
            resultSet.add(fb);
        }
    }

    public FactChecking(InstanceMatching im,InstanceMatching imB,Claim c) {
        this.im = im;
        this.imB = imB;
        this.c = c;
        this.results = new HashMap<>();
        this.resultSet = new HashSet<>();
        for (InstanceMatch i : im.getMatching()) {
            for (InstanceMatch iB : imB.getMatching()) {
                FactualBinding fb = new FactualBinding(i,c,checkClaim(i,iB));
                results.put(i.getE(),fb);
                resultSet.add(fb);
            }
        }
    }

    public String checkClaim(InstanceMatch i, InstanceMatch iB) {
        switch (this.c.getLabel()) {
            case "higher":
                return checkDifference(i,iB,"higher");
            case "lower":
                return checkDifference(i,iB,"lower");
            case "equal":
                return checkDifference(i,iB,"equal");
            default:
                break;
        }
        return "Unknown Claim Type";
    }

    public String checkClaim(InstanceMatch i) {
        switch (this.c.getLabel()) {
            case "value":
                return checkValue(i);
            case "exists":
                return checkExistence(i);
            case "max":
                return checkMax(i,false);
            case "maxVal":
                return checkMax(i,true);
            case "min":
                return checkMin(i,false);
            case "minVal":
                return checkMin(i,true);
            case "count":
                return checkCount(i);
            case "tie":
                return checkTie(i);
            default:
                break;
        }
        return "Unknown Claim Type";
    }

    public String checkDifference(InstanceMatch i, InstanceMatch iB, String type) {
        if (i.getE().isRefinedSuccessful() && iB.getE().isRefinedSuccessful()) {
            String val = "";
            for (Record r : i.getEventReduction()) {
                try {
                    val = r.getEntry(i.getE().getA());
                } catch (Exception e) {

                }
            }
            String valB = "";
            for (Record r : iB.getEventReduction()) {
                try {
                    valB = r.getEntry(iB.getE().getA());
                } catch (Exception e) {

                }
            }
            if (type.equals("higher")) {
                if (val.compareTo(valB) > 0) {
                    return "True";
                } else {
                    return "False";
                }
            }
            if (type.equals("lower")) {
                if (val.compareTo(valB) < 0) {
                    return "True";
                } else {
                    return "False";
                }
            }
            if (type.equals("equal")) {
                if (val.compareTo(valB) == 0) {
                    return "True";
                } else {
                    return "False";
                }
            }
        }
        return "Unknown";
    }

    public String checkTie(InstanceMatch i) {
        if (i.getE().isRefinedSuccessful()) {
            double val = 0.0;
            for (Record r : i.getEventReduction()) {
                try {
                    val = Double.parseDouble(r.getEntry(i.getE().getA()));
                } catch (Exception e) {

                }
            }
            int count = 0;
            for (Record r : i.getIrrelevant()) {
                try {
                    if (Double.parseDouble(r.getEntry(i.getE().getA())) == val) {
                        count++;
                    }
                } catch (Exception e) {

                }
            }
            if (c.getNodeB().hasOperator()) {
                if (c.getNodeB().getOperator().equals(">")) {
                    if (count > Integer.parseInt(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
                if (c.getNodeB().getOperator().equals("<")) {
                    if (count < Integer.parseInt(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
            } else {
                if (count == Integer.parseInt(c.getNodeB().getLabel())) {
                    return "True";
                } else {
                    return "False";
                }
            }
        }
        return "Unknown";
    }

    public String checkCount(InstanceMatch i) {
        if (i.getE().isRefinedSuccessful()) {
            if (c.getNodeB().hasOperator()) {
                if (c.getNodeB().getOperator().equals(">")) {
                    if (i.getEventReduction().size() > Double.parseDouble(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
                if (c.getNodeB().getOperator().equals("<")) {
                    if (i.getEventReduction().size() < Double.parseDouble(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
            } else {
                if (i.getEventReduction().size() == Double.parseDouble(c.getNodeB().getLabel())) {
                    return "True";
                } else {
                    return "False";
                }
            }
        }
        if (i.isEmptyMatch()) {
            if (Double.parseDouble(c.getNodeB().getLabel()) == 0) {
                return "True";
            } else {
                return "False";
            }
        }
        return "Unknown";
    }

    public String checkMin(InstanceMatch i,boolean val) {
        boolean checked = false;
        double minR = -1.0;
        boolean first = true;
        if (i.getE().isRefinedSuccessful()) {
            for (Record r : i.getEventReduction()) {
                try {
                    checked = true;
                    double toCompare = Double.parseDouble(r.getEntry(i.getE().getA()));
                    if (first) {
                        minR = toCompare;
                        first = false;
                    } else {
                        if (toCompare <= minR) {
                            minR = toCompare;
                        }
                    }
                } catch (Exception e) {

                }
                if (val) {
                    if (minR == Double.parseDouble(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
            }
            for (Record r : i.getIrrelevant()) {
                try {
                    if (Double.parseDouble(r.getEntry(i.getE().getA())) < minR) {
                        if (c.getNodeB().getLabel().equals("false")) {
                            return "True";
                        } else {
                            return "False";
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        if (checked) {
            if (c.getNodeB().getLabel().equals("false")) {
                return "False";
            } else {
                return "True";
            }

        }
        return "Unknown";
    }

    public String checkMax(InstanceMatch i,boolean val) {
        boolean checked = false;
        double maxR = 0.0;
        if (i.getE().isRefinedSuccessful()) {
            for (Record r : i.getEventReduction()) {
                try {
                    checked = true;
                    double toCompare = Double.parseDouble(r.getEntry(i.getE().getA()));
                    if (toCompare >= maxR) {
                        maxR = toCompare;
                    }
                } catch (Exception e) {

                }
                if (val) {
                    if (maxR == Double.parseDouble(c.getNodeB().getLabel())) {
                        return "True";
                    } else {
                        return "False";
                    }
                }
            }
            for (Record r : i.getIrrelevant()) {
                try {
                    if (Double.parseDouble(r.getEntry(i.getE().getA())) > maxR) {
                        if (c.getNodeB().getLabel().equals("false")) {
                            return "True";
                        } else {
                            return "False";
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        if (checked) {
            if (c.getNodeB().getLabel().equals("false")) {
                return "False";
            } else {
                return "True";
            }

        }
        return "Unknown";
    }

    public String checkExistence(InstanceMatch i) {
        boolean checked = false;
        if (i.getE().isRefinedSuccessful()) {
            for (Record r : i.getEventReduction()) {
                try {
                    checked = true;
                    if (Double.parseDouble(r.getEntry(i.getE().getA())) > 0) {
                        if (c.getNodeB().getLabel().equals("false")) {
                            return "False";
                        } else {
                            return "True";
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        if (checked) {
            if (c.getNodeB().getLabel().equals("false")) {
                return "True";
            } else {
                return "False";
            }
        }
        return "Unknown";
    }

    public String checkValue(InstanceMatch i) {
        boolean checked = false;
        if (i.getE().isRefinedSuccessful()) {
            for (Record r : i.getEventReduction()) {
                checked = true;
                if (c.getNodeB().hasOperator()) {
                    if (c.getNodeB().getOperator().equals("not")) {
                        try {
                            if (Double.parseDouble(r.getEntry(i.getE().getA())) != Double.parseDouble(c.getNodeB().getLabel())) {
                                return "True";
                            }
                        } catch (Exception e) {
                            if (!r.getEntry(i.getE().getA()).equals(c.getNodeB().getLabel())) {
                                return "True";
                            }
                        }
                    }
                    if (c.getNodeB().getOperator().equals(">")) {
                        try {
                            if (Double.parseDouble(r.getEntry(i.getE().getA())) > Double.parseDouble(c.getNodeB().getLabel())) {
                                return "True";
                            }
                        } catch (Exception e) {
                            if (r.getEntry(i.getE().getA()).compareTo(c.getNodeB().getLabel()) > 0) {
                                return "True";
                            }
                        }
                    }
                    if (c.getNodeB().getOperator().equals("<")) {
                        try {
                            if (Double.parseDouble(r.getEntry(i.getE().getA())) < Double.parseDouble(c.getNodeB().getLabel())) {
                                return "True";
                            }
                        } catch (Exception e) {
                            if (r.getEntry(i.getE().getA()).compareTo(c.getNodeB().getLabel()) < 0) {
                                return "True";
                            }
                        }
                    }
                } else {
                    try {
                        if (Double.parseDouble(r.getEntry(i.getE().getA())) == Double.parseDouble(c.getNodeB().getLabel())) {
                            return "True";
                        }
                    } catch (Exception e) {
                        if (r.getEntry(i.getE().getA()) != null) {
                            if (r.getEntry(i.getE().getA()).equals(c.getNodeB().getLabel())) {
                                return "True";
                            }
                        }
                    }
                }
            }
        }
        if (checked) {
            return "False";
        }
        return "Unknown";
    }

    public HashMap<EventMatch,FactualBinding> getResults() {
        return results;
    }

}
