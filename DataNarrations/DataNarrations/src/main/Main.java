package main;

import debug.AblationStudy;
import debug.EvaluationTest;

import java.io.IOException;

public class Main {

    static String path = "/home/nagel/DataNarrationsEval/";
    //static String path = "/home/nagel/EvaluationTest/";

    public static void main(String[] args) throws IOException {
        //EvaluationTest.testEvalPipeline(path);
        //EvaluationTest.eventMatchEvaluation(path,0.65);
        //EvaluationTest.propertyMatchEvaluation(path,0.65);
        //EvaluationTest.instanceMatchEvaluation(path,0.65);
        EvaluationTest.relationAssessmentEvaluation(path,0.65);
        //AblationStudy.noEventMatch(path);
        //AblationStudy.propertyMatchEvaluation(path, -1.0);
        //AblationStudy.instanceMatchEvaluation(path, -1.0);
        //AblationStudy.relationAssessmentEvaluation(path, -1.0);
    }

}
