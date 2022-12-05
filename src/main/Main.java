package main;

import ui.applicationSelect;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    static String path;

    /*public static void main(String[] args) throws IOException {
        CausalityAssessment.computeFullALift();
        CausalityAssessment.printOut();
    }*/

    public static void main(String[] args) throws IOException, URISyntaxException {
        path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String[] sub = path.split("/");
        path = "";
        for (int i = 0; i <= sub.length-2; i++) {
            path = path + "/" + sub[i];
        }
        applicationSelect.selectOption(path);
    }

}
