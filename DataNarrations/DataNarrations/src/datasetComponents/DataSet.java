package datasetComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataSet {

    public ArrayList<Attribute> attributeList;
    public ArrayList<Record> recordList;
    //Set of Attributes
    public HashSet<Attribute> attributes;
    //Set of Records
    public HashSet<Record> records;
    File src;

    //Constructor (no annotations)
    public DataSet(File src) {
        this.records = new HashSet<>();
        this.src = src;
        this.recordList = new ArrayList<>();
    }

    public DataSet(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
        this.records = new HashSet<>();
        this.recordList = new ArrayList<>();
    }

    public void addRecord(Record r) {
        this.records.add(r);
        this.recordList.add(r);
    }

    //Returns the column with the specified header
    public Record getRecord(int index) {
        Record out = this.recordList.get(index);
        return out;
    }

    //Returns the column with the specified header
    public Attribute getAttribute(int index) {
        Attribute out = this.attributeList.get(index);
        return out;
    }

    //Returns the column with the specified header
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

    public HashMap<Attribute,Attribute> getJoinCandidates(DataSet d) {
        HashMap<Attribute,Attribute> joinCandidates = new HashMap<Attribute,Attribute>();
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

    public ArrayList<Record> getRecordList() {
        return recordList;
    }

    public HashSet<Record> getRecords() {
        return records;
    }

    public File getSrc() {
        return src;
    }

    public void setAttributeList(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public void setAttributes(HashSet<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setRecords(HashSet<Record> records) {
        this.records = records;
    }

    //--------------------

}
