package com.semanticRelationsGraph.main;

import com.semanticRelationsGraph.graph.SemanticRelationsGraph;
import com.semanticRelationsGraph.graph.SemanticRelationsGraphImpl;
import com.semanticRelationsGraph.importer.DataImporter;
import com.semanticRelationsGraph.importer.DataImporterImpl;
import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by Oliver on 4/11/2017.
 */
public class Main {

    private static final File DB_PATH = new File("C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph");

    public static void main(String[] args) throws IOException {

        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        SemanticRelationsGraph semanticRelationsGraph = new SemanticRelationsGraphImpl();
        GraphSearcher graphSearcher = new GraphSearcherImpl(graphDb);
        DataImporter dataImporter = new DataImporterImpl(graphDb);
        dataImporter.importData();
        try (Transaction tx2 = graphDb.beginTx()) {
            Optional<Node> nodeOptional = graphSearcher.findNode("Putin", true);
            Iterable<Relationship> relationships = nodeOptional.get().getRelationships();
            for (Relationship relationship : relationships) {
                System.out.println(relationship.getStartNode().getProperty("shortName"));
                System.out.println(relationship.getProperty("verbPredicate"));
                System.out.println(relationship.getEndNode().getProperty("shortName"));
            }
        }

    }
}
