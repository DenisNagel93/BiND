package datasetComponents;

import java.util.ArrayList;

//Single Attribute of a Data Set
public class Attribute {

    //attribute title
    public String title;
    //associated meta-information
    public ArrayList<String> metadata;
    //data set
    public DataSet ds;
    //Whether attribute values are percentage values
    public boolean isRelative;
    //Whether attribute is artificial (does not exist in its data set - required for EventMatching)
    public boolean isPlaceholder;

    //Constructor (empty column)
    public Attribute(String title,DataSet ds) {
        this.title = title;
        this.isRelative = checkAttributeType();
        this.metadata = new ArrayList<>();
        this.ds = ds;
        this.isPlaceholder = false;
    }

    //Constructor (placeholder)
    public Attribute(String title,DataSet ds,boolean placeholder) {
        this.title = title;
        this.isRelative = checkAttributeType();
        this.metadata = new ArrayList<>();
        this.ds = ds;
        this.isPlaceholder = placeholder;
    }

    //Searches for keywords in the title that indicate percentage values
    public boolean checkAttributeType() {
        return title.contains("%") || title.contains(" rate ") || title.contains("Proportion") || title.contains(" per ")
                || title.contains("Percentage") || title.contains("Rate ") || title.contains("(per ");
    }

    public double computeAvg() {
        double sum = 0.0;
        int count = 0;
        for (Record r : this.ds.getRecords()) {
            try {
                sum = sum + Double.parseDouble(r.getEntry(this));
                count++;
            } catch (Exception e) {
                return 0.0;
            }
        }
        return sum / (double)count;
    }

    //----------Getter/Setter----------

    public String getTitle() {
        return this.title;
    }

    public boolean isRelative() {
        return isRelative;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public DataSet getDs() {
        return ds;
    }

    //--------------------

}
