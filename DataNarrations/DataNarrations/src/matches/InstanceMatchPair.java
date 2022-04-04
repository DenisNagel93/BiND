package matches;

import narrativeComponents.Event;

public class InstanceMatchPair {

    InstanceMatch eA,eB;

    public InstanceMatchPair(InstanceMatch eA, InstanceMatch eB) {
        this.eA = eA;
        this.eB = eB;
    }

    public InstanceMatch getEA() {
        return eA;
    }

    public InstanceMatch getEB() {
        return eB;
    }
}
