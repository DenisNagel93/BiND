package datasetComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//A single relational data set
public class DataSet {

    //List of attributes
    public ArrayList<Attribute> attributeList;
    //List of records
    public ArrayList<Record> recordList;
    //Set of Attributes
    public HashSet<Attribute> attributes;
    //Set of Records
    public HashSet<Record> records;
    //SourceFile
    File src;
    public String title;

    //Constructor (no attributes)
    public DataSet(File src) {
        this.records = new HashSet<>();
        this.src = src;
        this.title = readTitle();
        this.recordList = new ArrayList<>();
    }

    //Constructor (with attributes)
    public DataSet(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
        this.records = new HashSet<>();
        this.recordList = new ArrayList<>();
    }

    public String readTitle() {
        String[] sub = this.getSrc().getName().split("\\.");
        StringBuilder temp = new StringBuilder(sub[0]);
        for (int i = 1; i < sub.length - 1; i++) {
            temp.append(".").append(sub[i]);
        }
        return temp.toString();
    }

    //Add a record to the data set
    public void addRecord(Record r) {
        this.records.add(r);
        this.recordList.add(r);
    }

    //Returns the attribute at the specified position
    public Attribute getAttribute(int index) {
        return this.attributeList.get(index);
    }

    //Returns the attribute with the specified title
    public Attribute getAttribute(String label) {
        Attribute out = null;
        for (Attribute dr : this.attributes) {
            String truncA = dr.getTitle().replace("\"", "");
            String truncB = label.replace("\"", "");
            if (truncA.equals(truncB)) {
                out = dr;
                break;
            }
        }
        return out;
    }

    //Computes candidates for join attributes for a given data set
    public HashMap<Attribute,Attribute> getJoinCandidates(DataSet d) {
        HashMap<Attribute,Attribute> joinCandidates = new HashMap<>();
        for (Attribute a : this.attributes) {
            if (d.getAttribute(a.getTitle()) != null) {
                joinCandidates.put(a,d.getAttribute(a.getTitle()));
            }
        }
        return joinCandidates;
    }

    //----------Getter/Setter----------

    public HashSet<Attribute> getAttributes() {
        return attributes;
    }

    public ArrayList<Attribute> getAttributeList() {
        return attributeList;
    }

    public HashSet<Record> getRecords() {
        return records;
    }

    public File getSrc() {
        return src;
    }

    public String getTitle() {
        return title;
    }

    public void setAttributeList(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public void setAttributes(HashSet<Attribute> attributes) {
        this.attributes = attributes;
    }

    //--------------------

}
