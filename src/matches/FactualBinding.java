package matches;

import narrativeComponents.Claim;

public class FactualBinding {

    InstanceMatch im;
    Claim cl;
    String isValid;

    public FactualBinding(InstanceMatch im, Claim cl, String isValid) {
        this.im = im;
        this.cl = cl;
        this.isValid = isValid;
    }

    public String isValid() {
        return isValid;
    }

    public Claim getCl() {
        return cl;
    }

    public InstanceMatch getIm() {
        return im;
    }

    public void setValid(String valid) {
        isValid = valid;
    }
}
