package narrativeComponents;

import java.util.HashSet;

//An entity of a narrative
public class Entity extends Node {

    //Properties with the entity as Origin-Node
    HashSet<FactualRelation> properties;
    //Flag setting whether the entity is the value of an event property
    boolean isProp;
    boolean isClaim;
    //Events the entity is connected to
    HashSet<Event> participation;
    String operator;
    String value;

    //Constructor
    public Entity(String label) {
        super(label);
        this.properties = new HashSet<>();
        this.isProp = false;
        this.isClaim = false;
        this.participation = new HashSet<>();
    }

    //Add an event that the entity is connected to
    public void addEvent(Event e) {
        this.participation.add(e);
    }

    //Add a factual relation where the entity participates as Origin-Node
    public void addProperty(FactualRelation e) {
        this.properties.add(e);
    }

    public boolean hasOperator() {
        if (this.operator == null) {
            return false;
        }
        return true;
    }

    //--------Getter/Setter--------

    public HashSet<FactualRelation> getProperties() {
        return properties;
    }

    public String getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public void setProp(boolean prop) {
        isProp = prop;
    }

    public void setClaim(boolean claim) {
        isClaim = claim;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
