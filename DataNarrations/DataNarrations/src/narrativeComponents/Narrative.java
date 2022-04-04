package narrativeComponents;

import java.io.File;
import java.util.HashSet;

public class Narrative {

    //Set of Relations
    HashSet<Edge> relations;
    //Set of Factual Relations
    HashSet<FactualRelation> factualRelations;
    //Set of Narrative Relations
    HashSet<NarrativeRelation> narrativeRelations;
    //Set of Event Properties
    HashSet<EventProperty> eventProperties;
    //Set of Nodes
    HashSet<Node> nodes;
    //Set of Events
    HashSet<Event> events;
    //Set of Entities
    HashSet<Entity> entities;
    File src;

    //Constructor (empty narrative)
    public Narrative(File src) {
        this.relations = new HashSet<>();
        this.factualRelations = new HashSet<>();
        this.narrativeRelations = new HashSet<>();
        this.eventProperties = new HashSet<>();
        this.nodes = new HashSet<>();
        this.events = new HashSet<>();
        this.entities = new HashSet<>();
        this.src = src;
    }

    public Event assignEvent(String label, String caption) {
        Event e;
        if (this.inSet(label)) {
            e = this.getEvent(label);
        } else {
            e = new Event(label,caption);
        }
        return e;
    }

    public Entity assignEntity(String label) {
        Entity e;
        if (this.inSet(label)) {
            e = this.getEntity(label);
        } else {
            e = new Entity(label);
        }
        return e;
    }

    //Adds a new relation to the narrative
    public void addFactualRelation(FactualRelation fr) {
        this.relations.add(fr);
        this.factualRelations.add(fr);
        fr.getNodeA().addProperty(fr);
        if (!inSet(fr.getNodeA())) {
            this.nodes.add(fr.getNodeA());
            this.entities.add(fr.getNodeA());
        }
        if (!inSet(fr.getNodeB())) {
            this.nodes.add(fr.getNodeB());
            this.entities.add(fr.getNodeB());
        }
    }

    public void addEvent(Event e) {
        if (!inSet(e)) {
            this.nodes.add(e);
            this.events.add(e);
        }
    }

    //Adds a new relation to the narrative
    public void addNarrativeRelation(NarrativeRelation nr) {
        this.relations.add(nr);
        this.narrativeRelations.add(nr);
        nr.getNodeA().addRelation(nr);
        nr.getNodeB().addIngoing(nr);
        if (!inSet(nr.getNodeA())) {
            this.nodes.add(nr.getNodeA());
            this.events.add(nr.getNodeA());
        }
        if (!inSet(nr.getNodeB())) {
            this.nodes.add(nr.getNodeB());
            this.events.add(nr.getNodeB());
        }
    }

    //Adds a new relation to the narrative
    public void addEventProperty(EventProperty ep) {
        this.relations.add(ep);
        this.eventProperties.add(ep);
        ep.getNodeA().addProperty(ep);
        ep.getNodeB().setProp(true);
        ep.getNodeB().addEvent(ep.getNodeA());
        if (!inSet(ep.getNodeA())) {
            this.nodes.add(ep.getNodeA());
            this.events.add(ep.getNodeA());
        }
        if (!inSet(ep.getNodeB())) {
            this.nodes.add(ep.getNodeB());
            this.entities.add(ep.getNodeB());
        }
    }

    //Returns true if the narrative contains the given node
    public boolean inSet(Node n) {
        for (Node comp : this.nodes) {
            if (comp.getLabel().equals(n.getLabel())) {
                return true;
            }
        }
        return false;
    }

    //Returns true if the narrative contains the given node
    public boolean inSet(String n) {
        for (Node comp : this.nodes) {
            if (comp.getLabel().equals(n)) {
                return true;
            }
        }
        return false;
    }

    //Returns the node with the respective node label
    public Node getNode(String label) {
        Node out = null;
        for (Node n : this.nodes) {
            if (n.getLabel().equals(label)) {
                out = n;
                break;
            }
        }
        return out;
    }

    //Returns the node with the respective node label
    public Event getEvent(String label) {
        Event out = null;
        for (Event e : this.events) {
            if (e.getLabel().equals(label)) {
                out = e;
                break;
            }
        }
        return out;
    }

    //Returns the node with the respective node label
    public Entity getEntity(String label) {
        Entity out = null;
        for (Entity e : this.entities) {
            if (e.getLabel().equals(label)) {
                out = e;
                break;
            }
        }
        return out;
    }

    //----------Getter/Setter----------

    public HashSet<Node> getNodes() {
        return nodes;
    }

    public HashSet<Event> getEvents() {
        return events;
    }

    public HashSet<Entity> getEntities() {
        return entities;
    }

    public HashSet<Edge> getRelations() {
        return relations;
    }

    public HashSet<FactualRelation> getFactualRelations() {
        return factualRelations;
    }

    public HashSet<NarrativeRelation> getNarrativeRelations() {
        return narrativeRelations;
    }

    public HashSet<EventProperty> getEventProperties() {
        return eventProperties;
    }

    public File getSrc() {
        return src;
    }

    //--------------------

}
