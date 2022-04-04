package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    //File to parse
    public File file;
    //Loaded input
    public ArrayList<ArrayList<String>> fileContent;

    //Constructor (No extraction of columns)
    public Parser(File file) {
        this.file = file;
        this.fileContent = new ArrayList<>();
        String type = file.getName().split("\\.")[1];
        if (type.equals("json")) {
            try {
                readJsonFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                readFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void readJsonFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean inDataSet = false;
        boolean inRecord = false;
        ArrayList<String> currentRecord = null;
        while ((line = br.readLine()) != null) {
            if (line.trim().equals("[")) {
                if (!inDataSet) {
                    inDataSet = true;
                } else {
                    if (!inRecord) {
                        inRecord = true;
                        currentRecord = new ArrayList<>();
                    }
                }
            } else {
                if (line.trim().equals("]") || line.trim().equals("],")) {
                    if (inRecord) {
                        inRecord = false;
                        fileContent.add(currentRecord);
                    } else {
                        if (inDataSet) {
                            inDataSet = false;
                        }
                    }
                } else {
                    currentRecord.add(line.trim().split(",")[0]);
                    //System.out.println(line.trim().split(",")[0]);
                }
            }
        }
        br.close();
    }

    //Parses the file and creates the respective table
    public void readFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            ArrayList<String> lineElements = new ArrayList<>();
            String[] subString = line.split(",");
            String entry = "";
            boolean append = false;
            for (String s : subString) {
                if (s.startsWith("\"") && !s.endsWith("\"")) {
                    entry = s;
                    append = true;
                } else {
                    if (!s.startsWith("\"") && s.endsWith("\"")) {
                        entry = entry.concat(s);
                        lineElements.add(entry);
                        append = false;
                    } else {
                        if (!append) {
                            lineElements.add(s);
                        } else {
                            entry = entry.concat(s);
                        }
                    }
                }
            }
            fileContent.add(lineElements);
        }
        br.close();
    }

    //----------Getter/Setter----------

    public ArrayList<ArrayList<String>> getContent () {
        return this.fileContent;
    }

    //--------------------

}
