package com.semanticRelationsGraph.main;

import com.semanticRelationsGraph.data.SemanticData;
import com.semanticRelationsGraph.graph.SemanticRelationsGraph;
import com.semanticRelationsGraph.graph.SemanticRelationsGraphImpl;
import com.semanticRelationsGraph.importer.DataImporter;
import com.semanticRelationsGraph.importer.DataImporterImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver on 4/11/2017.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        SemanticRelationsGraph semanticRelationsGraph = new SemanticRelationsGraphImpl();
        semanticRelationsGraph.processGraphData();

        List<SemanticData> semanticDataList = new ArrayList<>();
        SemanticData semanticData = new SemanticData();
        semanticData.setAtomicSubject("cow");
        semanticData.setAtomicVerbPredicate("eats");
        semanticData.setAtomicNounPredicate("grass");
        semanticDataList.add(semanticData);

        DataImporter dataImporter = new DataImporterImpl();
        dataImporter.importData(semanticDataList);
    }
}
