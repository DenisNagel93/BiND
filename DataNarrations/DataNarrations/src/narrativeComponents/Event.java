package narrativeComponents;

import java.util.HashSet;

public class Event extends Node {

    String caption;
    HashSet<EventProperty> properties;
    HashSet<NarrativeRelation> relations;
    HashSet<NarrativeRelation> ingoingRelation;

    public Event(String label,String caption) {
        super(label);
        this.caption = caption;
        this.properties = new HashSet<>();
        this.relations = new HashSet<>();
        this.ingoingRelation = new HashSet<>();
    }

    public boolean isProperty(Entity p) {
        for (Edge e : this.properties) {
            if (e.getNodeB().getLabel().equals(p.getLabel())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void addProperty(EventProperty e) {
        this.properties.add(e);
    }

    public void addRelation(NarrativeRelation e) {
        this.relations.add(e);
    }

    public void addIngoing(NarrativeRelation e) {
        this.ingoingRelation.add(e);
    }

    public String getCaption() {
        return caption;
    }

    public HashSet<EventProperty> getProperties() {
        return properties;
    }

    public HashSet<NarrativeRelation> getRelations() {
        return relations;
    }

    public HashSet<NarrativeRelation> getIngoing() {
        return ingoingRelation;
    }
}
