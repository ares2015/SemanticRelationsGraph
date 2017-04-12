package com.semanticRelationsGraph.reader;

import com.semanticRelationsGraph.data.SemanticData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 4/11/2017.
 */
public class SemanticDataReaderImpl implements SemanticDataReader {

    private String inputFilePath = "C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\SemanticExtractedData.txt";

    public List<SemanticData> read() throws IOException {
        List<SemanticData> semanticDataList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
        String extractedDataRow = br.readLine();
        while (extractedDataRow != null) {
            String atomicSubject = "";
            String atomicVerbPredicate = "";
            String atomicNounPredicate = "";
            String[] split = extractedDataRow.split("#");
            if (split.length >= 5) {
                if (split[0] != null && !split[0].equals("")) {
                    atomicSubject = split[0];
                }
                if (split[2] != null && !split[2].equals("")) {
                    atomicVerbPredicate = split[2];
                }
                if (split[4] != null && !split[4].equals("")) {
                    atomicNounPredicate = split[4];
                }
                if (atomicSubject != "" && atomicVerbPredicate != "" && atomicNounPredicate != "") {
                    SemanticData semanticData = new SemanticData();
                    semanticData.setAtomicSubject(atomicSubject);
                    semanticData.setAtomicVerbPredicate(atomicVerbPredicate);
                    semanticData.setAtomicNounPredicate(atomicNounPredicate);
                    semanticDataList.add(semanticData);
                }
            }
            extractedDataRow = br.readLine();
        }
        return semanticDataList;
    }

}
