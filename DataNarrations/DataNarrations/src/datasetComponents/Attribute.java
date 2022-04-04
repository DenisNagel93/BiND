package datasetComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Attribute {

    //header
    public String title;
    //associated meta-information
    public ArrayList<String> metadata;
    //associated entities
    public HashSet<HashSet<String>> concepts;
    public DataSet ds;
    public boolean isRelative;

    //Constructor (empty column)
    public Attribute(String title,DataSet ds) {
        this.title = title;
        this.isRelative = checkAttributeType();
        this.metadata = new ArrayList<>();
        this.concepts = new HashSet<>();
        this.ds = ds;
    }

    public boolean checkAttributeType() {
        if (title.contains("%") || title.contains(" rate ") || title.contains("Proportion") || title.contains(" per ")
                || title.contains("Percentage") || title.contains("Rate ") || title.contains("(per ")) {
            return true;
        }
        return false;
    }

    //Adding related concepts
    public void addConcepts(HashSet<String> terms) {
        this.concepts.add(terms);
    }

    //----------Getter/Setter----------

    public String getTitle() {
        return this.title;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public ArrayList<String> getMetaData() {
        return this.metadata;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return concepts;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMetaData(ArrayList<String> metaData) {
        this.metadata = metaData;
    }

    public DataSet getDs() {
        return ds;
    }

    //--------------------

}
