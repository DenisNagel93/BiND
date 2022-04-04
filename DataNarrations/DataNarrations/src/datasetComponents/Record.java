package datasetComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Record {

    //individual values for the respective entries
    public HashMap<Attribute, String> entries;
    //associated entities
    public HashSet<HashSet<String>> concepts;

    //Constructor
    public Record(HashMap<Attribute,String> entries) {
        this.entries = entries;
        this.concepts = new HashSet<>();
    }

    //Adding related concepts
    public void addConcepts(HashSet<String> terms) {
        this.concepts.add(terms);
    }

    public String getEntry(Attribute a) {
        return this.entries.get(a);
    }

    public HashMap<Attribute, String> getEntries()  {
        return this.entries;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return concepts;
    }

    public void setEntries(HashMap<Attribute, String> entries) {
        this.entries = entries;
    }


}
