package com.semanticRelationsGraph.importer;

import com.semanticRelationsGraph.data.SemanticData;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.impl.batchimport.input.csv.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oliver on 4/11/2017.
 */
public class DataImporterImpl implements DataImporter {

//    private static final File DB_PATH = new File("target/semantic-relations-graph");//"C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph";

    private static final File DB_PATH = new File("C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph");

    private GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);

    private final String NODE_LABEL = "semanticObject";

    private final String NODE_PROPERTY_KEY = "name";


    public void importData(List<SemanticData> semanticDataList) throws IOException {
        System.out.println("Starting database ...");
//        FileUtils.deleteRecursively(DB_PATH);
        createIndex();
        for (SemanticData semanticData : semanticDataList) {
            createNodesAndRelationship(semanticData);
        }

        try (Transaction tx = graphDb.beginTx()) {
            Node myNode = findNode();

            Iterable<Relationship> relationships = myNode.getRelationships();
            for (Relationship relationship : relationships) {
                System.out.println(relationship.getStartNode().getProperty("name"));
                System.out.println(relationship.getType());
                System.out.println(relationship.getEndNode().getProperty("name"));
            }
        }
    }

    private void createIndex() {
        // START SNIPPET: createIndex
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            indexDefinition = schema.indexFor(Label.label(NODE_LABEL))
                    .on(NODE_PROPERTY_KEY)
                    .create();
            tx.success();
        }
        // END SNIPPET: createIndex
        // START SNIPPET: wait
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
        // END SNIPPET: wait
        // START SNIPPET: progress
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            System.out.println(String.format("Percent complete: %1.0f%%",
                    schema.getIndexPopulationProgress(indexDefinition).getCompletedPercentage()));
        }
        // END SNIPPET: progress
    }

    private void createNodesAndRelationship(SemanticData semanticData) {
        try (Transaction tx = graphDb.beginTx()) {
            Label label2 = Label.label(NODE_LABEL);
            Node object1 = graphDb.createNode(label2);
            object1.setProperty(NODE_PROPERTY_KEY, semanticData.getAtomicSubject());
            Node object2 = graphDb.createNode(label2);
            object2.setProperty(NODE_PROPERTY_KEY, semanticData.getAtomicNounPredicate());
            Relationship relationship = object1.createRelationshipTo(object2, RelationshipType.withName(semanticData.getAtomicVerbPredicate()));
//            relationship.setProperty("verb", "eat");
            System.out.println("Nodes with relationship created");
            tx.success();
        }
    }

    private Node findNode() {
        // START SNIPPET: findUsers
        ArrayList<Node> userNodes = new ArrayList<>();

        Label label = Label.label("semanticObject");
        String nameToFind = "cow";
        try (Transaction tx = graphDb.beginTx()) {
            try (ResourceIterator<Node> users =
                         graphDb.findNodes(label, "name", nameToFind)) {
                while (users.hasNext()) {
                    userNodes.add(users.next());
                }

//                for (Node node : userNodes) {
//                    System.out.println(
//                            "The username of user " + idToFind + " is " + node.getProperty("username"));
//                }
            }
        }
        return userNodes.get(0);
        // END SNIPPET: findUsers
    }

}