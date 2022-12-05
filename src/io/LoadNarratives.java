package io;

import narrativeComponents.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class LoadNarratives {

    //Loads narratives from folder
    public static HashSet<Narrative> loadNarrativesFromFolder(File inputFile, String delim) {
        HashSet<Narrative> output = new HashSet<>();
        for (File entry : inputFile.listFiles()) {
            System.out.println("Load N: " + entry);
            output.add(loadNarrativeFromFile(entry,delim));
        }
        return output;
    }

    public static String checkForOperator(String in) {
        ArrayList<String> ops = new ArrayList<>();
        ops.add("<");
        ops.add(">");
        ops.add("not ");
        for (String op : ops) {
            if (in.startsWith(op)) {
                return op.trim();
            }
        }
        if (in.contains(" or ")) {
            return "or";
        }
        return "";
    }

    //Loads narrative from file
    public static Narrative loadNarrativeFromFile(File inputFile, String delim) {
        Narrative n = new Narrative(inputFile);
        Parser ln = new Parser(inputFile,delim);
        for (ArrayList<String> line : ln.fileContent) {
            if (line.get(0).equals("ev")) {
                Event v1 = n.assignEvent(line.get(1),line.get(2));
                n.addEvent(v1);
            } else {
                if (line.get(0).equals("fr")) {
                    Entity v1 = n.assignEntity(line.get(1));
                    Entity v2 = n.assignEntity(line.get(3));
                    FactualRelation fr = new FactualRelation(line.get(2), v1, v2);
                    v1.addEdge(fr);
                    n.addFactualRelation(fr);
                } else {
                    Event v1 = n.getEvent(line.get(1));
                    if (line.get(0).equals("nr")) {
                        Event v2 = n.getEvent(line.get(3));
                        NarrativeRelation nr = new NarrativeRelation(line.get(2), v1, v2);
                        v1.addEdge(nr);
                        n.addNarrativeRelation(nr);
                    } else {
                        String op = checkForOperator(line.get(3));
                        String value;
                        if (op.equals("<") || op.equals(">") || op.equals("not")) {
                            value = line.get(3).replaceFirst(op,"");
                        } else {
                            if (op.equals("or")) {
                                value = line.get(3).replaceFirst(" " + op + " ",":");
                            } else {
                                value = line.get(3);
                            }
                        }
                        Entity v2 = n.assignEntity(value);
                        if (!op.equals("")) {
                            v2.setOperator(op);
                        }
                        if (line.get(0).equals("cl")) {
                            Claim cl = new Claim(line.get(2), v1, v2);
                            v1.addEdge(cl);
                            n.addClaim(cl);
                        } else {
                            if (line.get(0).equals("cl2")) {
                                Event v3 = n.getEvent(line.get(3));
                                Claim cl = new Claim(line.get(2), v1, v3);
                                cl.setBinary(true);
                                v1.addEdge(cl);
                                n.addClaim(cl);
                            } else {
                                EventProperty ep = new EventProperty(line.get(2), v1, v2);
                                v1.addEdge(ep);
                                n.addEventProperty(ep);
                            }
                        }
                    }
                }
            }
        }
        for (Event e : n.getEvents()) {
            e.checkRelations();
        }
        return n;
    }

}
