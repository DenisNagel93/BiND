package narrativeComponents;

import java.util.HashSet;

public class Entity extends Node {

    HashSet<FactualRelation> properties;
    boolean isProp;
    HashSet<Event> participation;

    public Entity(String label) {
        super(label);
        this.properties = new HashSet<>();
        this.isProp = false;
        this.participation = new HashSet<>();
    }

    public boolean isProperty() {
        return isProp;
    }

    public void addEvent(Event e) {
        this.participation.add(e);
    }

    public void addProperty(FactualRelation e) {
        this.properties.add(e);
    }

    public HashSet<FactualRelation> getProperties() {
        return properties;
    }

    public void setProp(boolean prop) {
        isProp = prop;
    }
}
