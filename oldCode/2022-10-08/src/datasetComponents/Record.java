package datasetComponents;

import java.util.HashMap;
import java.util.HashSet;

//An individual record of a data set
public class Record {

    //individual values for the respective entries
    public HashMap<Attribute, String> entries;
    //associated entities (Entity Linking)
    public HashSet<HashSet<String>> concepts;

    //Constructor
    public Record(HashMap<Attribute,String> entries) {
        this.entries = entries;
        this.concepts = new HashSet<>();
    }

    //--------Getter/Setter--------

    public String getEntry(Attribute a) {
        return this.entries.get(a);
    }

    public HashMap<Attribute, String> getEntries()  {
        return this.entries;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return concepts;
    }

}
