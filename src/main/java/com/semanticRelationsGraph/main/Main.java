package com.semanticRelationsGraph.main;

import com.semanticRelationsGraph.data.SemanticData;
import com.semanticRelationsGraph.graph.SemanticRelationsGraph;
import com.semanticRelationsGraph.graph.SemanticRelationsGraphImpl;
import com.semanticRelationsGraph.importer.DataImporter;
import com.semanticRelationsGraph.importer.DataImporterImpl;
import com.semanticRelationsGraph.reader.SemanticDataReader;
import com.semanticRelationsGraph.reader.SemanticDataReaderImpl;
import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Oliver on 4/11/2017.
 */
public class Main {

    private static final File DB_PATH = new File("C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph");

    private static final String NODE_LABEL = "semanticObject";

    private static final String NODE_PROPERTY_KEY = "name";

    public static void main(String[] args) throws IOException {


        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        SemanticRelationsGraph semanticRelationsGraph = new SemanticRelationsGraphImpl();
        SemanticDataReader semanticDataReader = new SemanticDataReaderImpl();
        List<SemanticData> semanticDataList = semanticDataReader.read();

//        semanticRelationsGraph.processGraphData();
        DataImporter dataImporter = new DataImporterImpl(graphDb);
        dataImporter.importData(semanticDataList);

        GraphSearcher graphSearcher = new GraphSearcherImpl(graphDb);


        try (Transaction tx = graphDb.beginTx()) {
            Node myNode = graphSearcher.findNode("women");
            Iterable<Relationship> relationships = myNode.getRelationships();
            for (Relationship relationship : relationships) {
                System.out.println(relationship.getStartNode().getProperty(NODE_PROPERTY_KEY));
                System.out.println(relationship.getType());
                System.out.println(relationship.getEndNode().getProperty(NODE_PROPERTY_KEY));
            }
        }
    }
}
