package com.semanticRelationsGraph;

import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.Optional;


/**
 * Unit test for simple App.
 */
public class GraphSearchTest {

    private static final File DB_PATH = new File("C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph");

    private static GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    private static final String SHORT_NAME = "shortName";

    private static final String LONG_NAME = "longName";

    private static GraphSearcher graphSearcher = new GraphSearcherImpl(graphDb);

    public static void main(String[] args) {
        testGetNodeRelationships();
    }

    private static void testGetNodeRelationships() {
        try (Transaction tx = graphDb.beginTx()) {
            Optional<Node> nodeOptional = graphSearcher.findNode("Putin", true);
            Iterable<Relationship> relationships = nodeOptional.get().getRelationships();
            for (Relationship relationship : relationships) {
                System.out.println(relationship.getStartNode().getProperty(SHORT_NAME));
                System.out.println(relationship.getProperty("verbPredicate"));
                System.out.println(relationship.getEndNode().getProperty(SHORT_NAME));
            }
        }
    }


}
