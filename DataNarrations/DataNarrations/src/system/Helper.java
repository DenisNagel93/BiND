package system;

import datasetComponents.Attribute;
import datasetComponents.Record;
import matches.EventMatch;
import matches.InstanceMatchPair;

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
        double avg = sum / (double)(records.size() - errors);
        return avg;
    }

    public static List<List<EventMatch>> calculateCartesianProduct(List<List<EventMatch>> inputLists) {

        List<List<EventMatch>> cartesianProducts = new ArrayList<>();
        if (inputLists != null && inputLists.size() > 0) {
            // separating the list at 0th index
            List<EventMatch> initialList = inputLists.get(0);
            // recursive call
            List<List<EventMatch>> remainingLists = calculateCartesianProduct(inputLists.subList(1, inputLists.size()));
            // calculating the cartesian product
            initialList.forEach(element -> {
                remainingLists.forEach(remainingList -> {
                    ArrayList<EventMatch> cartesianProduct = new ArrayList<>();
                    cartesianProduct.add(element);
                    cartesianProduct.addAll(remainingList);
                    cartesianProducts.add(cartesianProduct);
                });
            });
        } else {
            // Base Condition for Recursion (returning empty List as only element)
            cartesianProducts.add(new ArrayList<>());
        }
        return cartesianProducts;
    }

    public static double prepareForDouble(String s) {
        s = s.replaceAll("\"","");
        String[] sub = s.split("\\[");
        return Double.parseDouble(sub[0]);
    }

    //Computes the Jaccard index between two sets of annotations
    public static double jaccard(HashSet<HashSet<String>> setA, HashSet<HashSet<String>> setB) {
        return (double)intersection(setA,setB) / (double)union(setA,setB);
    }

    //Computes the size of the union between to sets of annotations
    public static int union(HashSet<HashSet<String>> setA, HashSet<HashSet<String>> setB) {
        if (setA == null || setB == null) {
            return 0;
        }
        return setA.size() + (setB.size() - intersection(setA,setB));
    }

    //Computes the size of the intersection between to sets of annotations
    public static int intersection(HashSet<HashSet<String>> setA, HashSet<HashSet<String>> setB) {
        int count = 0;
        if (setA == null || setB == null) {
            return 0;
        }
        for (HashSet<String> s1 : setA) {
            for (HashSet<String> s2 : setB) {
                if (vocabMatch(s1,s2)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    //Checks whether two sets of strings have an intersection
    public static boolean vocabMatch(HashSet<String> setA, HashSet<String> setB) {
        for (String s1 : setA) {
            if (setB.contains(s1)) {
                return true;
            }
        }
        return false;
    }

    public static LinkedHashMap<InstanceMatchPair, Double> sortHashMapByValues(HashMap<InstanceMatchPair, Double> passedMap) {
        List<InstanceMatchPair> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);

        LinkedHashMap<InstanceMatchPair, Double> sortedMap =
                new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<InstanceMatchPair> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                InstanceMatchPair key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
