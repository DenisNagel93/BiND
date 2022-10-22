package system;

import datasetComponents.DataSet;
import datasetComponents.Record;
import matches.*;
import narrativeComponents.EventProperty;
import narrativeComponents.FactualRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InstanceMatching {

    ArrayList<InstanceMatch> matching;

    public InstanceMatching(EventRefinement em) {
        this.matching = new ArrayList<>();
        for (EventMatch m : em.getEventMatching()) {
            if (m.isRefinedSuccessful()) {
                InstanceMatch im = new InstanceMatch(m);
                HashMap<FactMatch,HashSet<Record>> factReduction = new HashMap<>();
                HashMap<PropertyMatch,HashSet<Record>> propertyReduction = new HashMap<>();
                //if (m.getPm().size() == 0 && m.getFm().size() == 0) {
                //    im.setReduced(true);
                //}
                for (PropertyMatch p : m.getPm().values()) {
                    HashSet<Record> red = im.findRecords(p);
                    propertyReduction.put(p,red);
                    /*if (red.size() > 0 || p.isTitleMatch()) {
                        im.setReduced(true);
                    }*/
                }
                for (FactMatch f : m.getFm().values()) {
                    HashSet<Record> red = im.findRecords(f);
                    factReduction.put(f,red);
                    /*if (red.size() > 0) {
                        im.setReduced(true);
                    }*/
                }
                im.setPropertyReduction(propertyReduction);
                im.setFactReduction(factReduction);
                if (im.isReduced()) {
                    this.matching.add(im);
                }
            }
        }
        filterFullMatches();
    }

    public void filterFullMatches() {
        ArrayList<InstanceMatch> filteredList = new ArrayList<>();
        for (InstanceMatch im : this.matching) {
            if ((im.getE().isFullMatch()) && !im.isEmptyMatch()) {
                filteredList.add(im);
            }
        }
        if (!filteredList.isEmpty()) {
            this.matching = filteredList;
        }
    }

    public ArrayList<InstanceMatch> getMatching() {
        return matching;
    }

}
