package ui;

import evaluation.EvaluationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

public class applicationSelect {

    static String configFile;

    static String dataPath;
    static String narrativePath;
    static String outputPath;
    static String embeddingPath;
    static String groundTruthPath;

    static String delim;

    public static void selectOption(String path) throws IOException {
        configFile = path + "/config.txt";
        initBiND();
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Application Mode:");
        System.out.println("--0: Run BiND (complete)");
        System.out.println("--1: Run BiND (partially)");
        System.out.println("--2: Run Ablation Study");
        System.out.println("-------------------------");
        System.out.println("--3: Configure BiND");
        System.out.println("--4: Prepare Embeddings");
        String input = sc.nextLine();
        switch (input) {
            case("0"):
                selectEvaluationMode("ra",delim);
                break;
            case("1"):
                selectPartialExecution();
                break;
            case("2"):;
                break;
            case("3"):;
                break;
            case("4"):
                EvaluationTest.prepareEmbedding(delim);
                break;
            default:
                System.out.println("Please Select a Mode");
                selectOption(path);
        }
        sc.close();
    }

    public static void selectEvaluationMode(String opt,String delim) throws IOException {
        long loadStarttime = System.nanoTime();
        EvaluationTest.loadInput(new File(dataPath), new File(narrativePath),delim);
        EvaluationTest.loadEmbeddings(",");
        long loadRuntime = System.nanoTime() - loadStarttime;
        System.out.println("Load Time: " + (double)loadRuntime / 1000000000 + " seconds");
        double tEM,tPM,tRA;
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Evaluation Mode:");
        System.out.println("--0: Full Evaluation");
        System.out.println("--1: Single Threshold");
        String input = sc.nextLine();
        switch (input) {
            case ("0"):
                BigDecimal t = new BigDecimal("1.0");
                BigDecimal delta = new BigDecimal("0.05");
                switch (opt) {
                    case ("em"):
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.eventMatchEvaluation(t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("pm"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.propertyMatchEvaluation(tEM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("im"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.instanceMatchEvaluation(tEM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("ra"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.relationAssessmentEvaluation(tEM,tPM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    default:;
                }
                break;
            case ("1"):
                switch (opt) {
                    case ("em"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.eventMatchEvaluation(tEM,true);
                        break;
                    case ("pm"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.propertyMatchEvaluation(tEM,tPM,true);
                        break;
                    case ("im"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.instanceMatchEvaluation(tEM,tPM,true);
                        break;
                    case ("ra"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set RelationAssessment threshold");
                        tRA = Double.parseDouble(sc.nextLine());
                        EvaluationTest.relationAssessmentEvaluation(tEM,tPM,tRA,true);
                        break;
                    default:;
                }
                break;
            default:
                System.out.println("Please Select a Mode");
                selectEvaluationMode(opt,delim);
        }
        sc.close();
    }

    public static void selectPartialExecution() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Steps to run:");
        System.out.println("--0: Only Event Matching");
        System.out.println("--1: Event- and Property Matching");
        System.out.println("--2: Event-, Property- and Instance Matching");
        String input = sc.nextLine();
        switch (input) {
            case("0"):
                selectEvaluationMode("em",delim);
                break;
            case("1"):
                selectEvaluationMode("pm",delim);
                break;
            case("2"):
                selectEvaluationMode("im",delim);
                break;
            default:
                System.out.println("Please Select an Option");
        }
        sc.close();
    }

    public static void initBiND() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(configFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] sub = line.split("=");
            switch (sub[0]) {
                case "narratives_path": narrativePath = sub[1];
                    break;
                case "data_path": dataPath = sub[1];
                    break;
                case "groundtruth_path": groundTruthPath = sub[1];
                    break;
                case "output_path": outputPath = sub[1];
                    break;
                case "embeddings_path": embeddingPath = sub[1];
                    break;
                case "csv_delimiter": delim = sub[1];
                    break;
                default:;
            }
        }
        br.close();
    }

    public static String getDataPath () {
        return dataPath;
    }

    public static String getNarrativePath() {
        return narrativePath;
    }

    public static String getOutputPath() {
        return outputPath;
    }

    public static String getEmbeddingPath() {
        return embeddingPath;
    }

    public static String getGroundTruthPath() {
        return groundTruthPath;
    }
}


//EvaluationTest.testEvalPipeline(path);
//AblationStudy.noEventMatch(path);
//AblationStudy.propertyMatchEvaluation(path, -1.0);
//AblationStudy.instanceMatchEvaluation(path, -1.0);
//AblationStudy.relationAssessmentEvaluation(path, -1.0);