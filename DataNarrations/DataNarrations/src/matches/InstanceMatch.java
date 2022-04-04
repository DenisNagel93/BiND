package matches;

import datasetComponents.Attribute;
import datasetComponents.Record;
import narrativeComponents.Edge;
import narrativeComponents.Event;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InstanceMatch {

    SchemaMatch s;
    EventMatch e;
    HashMap<FactMatch,HashSet<Record>> factReduction;
    HashMap<PropertyMatch,HashSet<Record>> propertyReduction;
    HashMap<EventMatch,HashSet<Record>> eventReduction;

    HashSet<Attribute> noMatch;
    boolean isReduced;

    public InstanceMatch(SchemaMatch s) {
        this.s = s;
        this.e = null;
        this.factReduction = new HashMap<>();
        this.propertyReduction = new HashMap<>();
        this.eventReduction = new HashMap<>();
        this.noMatch = new HashSet<>();
    }

    public InstanceMatch(EventMatch e) {
        this.isReduced = false;
        this.s = null;
        this.e = e;
        this.factReduction = new HashMap<>();
        this.propertyReduction = new HashMap<>();
        this.eventReduction = new HashMap<>();
        this.noMatch = new HashSet<>();
    }

    public HashSet<Record> getReductionByEvent(Event e) {
        for (EventMatch em : eventReduction.keySet()) {
            if (em.getE() == e) {
                return eventReduction.get(em);
            }
        }
        return null;
    }

    public void addNoMatch(Attribute a) {
        this.noMatch.add(a);
    }

    public boolean isReduced() {
        return isReduced;
    }

    public SchemaMatch getS() {
        return s;
    }

    public EventMatch getE() {
        return e;
    }

    public HashMap<EventMatch, HashSet<Record>> getEventReduction() {
        return eventReduction;
    }

    public HashMap<FactMatch, HashSet<Record>> getFactReduction() {
        return factReduction;
    }

    public HashMap<PropertyMatch, HashSet<Record>> getPropertyReduction() {
        return propertyReduction;
    }

    public HashSet<Attribute> getNoMatch() {
        return noMatch;
    }

    public void setEventReduction(HashMap<EventMatch, HashSet<Record>> eventReduction) {
        this.eventReduction = eventReduction;
    }

    public void setFactReduction(HashMap<FactMatch, HashSet<Record>> factReduction) {
        this.factReduction = factReduction;
    }

    public void setPropertyReduction(HashMap<PropertyMatch, HashSet<Record>> propertyReduction) {
        this.propertyReduction = propertyReduction;
    }

    public void setReduced(boolean reduced) {
        isReduced = reduced;
    }
}
