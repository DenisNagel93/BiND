package narrativeComponents;

import java.util.HashSet;

//An event of a narrative
public class Event extends Node {

    //Event caption
    String caption;
    String value;
    //Ou
    HashSet<EventProperty> properties;
    HashSet<Claim> claims;
    //Outgoing Narrative Relations
    HashSet<NarrativeRelation> relations;
    //Ingoing Narrative Relations
    HashSet<NarrativeRelation> ingoingRelation;
    boolean hasValue;
    boolean needsRelativeValues;
    boolean needsNumericalValues;

    public Event(String label,String caption) {
        super(label);
        this.caption = caption;
        this.properties = new HashSet<>();
        this.relations = new HashSet<>();
        this.claims = new HashSet<>();
        this.ingoingRelation = new HashSet<>();
        this.hasValue = false;
    }

    public void checkRelations() {
        this.needsNumericalValues = false;
        this.needsRelativeValues = false;
        HashSet<NarrativeRelation> toCheck = new HashSet<>();
        toCheck.addAll(this.relations);
        toCheck.addAll(this.ingoingRelation);
        if (toCheck.size() > 0) {
            this.needsNumericalValues = true;
        }
        for (NarrativeRelation nr : toCheck) {
            if (nr.getLabel().equals("higher probability") || nr.getLabel().equals("lower probability")) {
                this.needsRelativeValues = true;
                break;
            }
        }
    }

    public boolean hasValue() {
        return hasValue;
    }

    public boolean needsRelativeValues() {
        return this.needsRelativeValues;
    }

    public boolean needsNumericalValues() {
        return needsNumericalValues;
    }

    public void addProperty(EventProperty e) {
        this.properties.add(e);
    }

    public void addRelation(NarrativeRelation e) {
        this.relations.add(e);
    }

    public void addClaim(Claim c) {
        this.claims.add(c);
    }

    public void addIngoing(NarrativeRelation e) {
        this.ingoingRelation.add(e);
    }

    //--------Getter/Setter--------

    public String getCaption() {
        return caption;
    }

    public HashSet<EventProperty> getProperties() {
        return properties;
    }

    public HashSet<Claim> getClaims() {
        return claims;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.hasValue = true;
        this.value = value;
    }
}
