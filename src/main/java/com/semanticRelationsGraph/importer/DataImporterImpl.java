package com.semanticRelationsGraph.importer;

import com.semanticRelationsGraph.data.SemanticData;
import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oliver on 4/11/2017.
 */
public class DataImporterImpl implements DataImporter {

    private GraphDatabaseService graphDb;

    private GraphSearcher graphSearcher;

    private final String NODE_LABEL = "semanticObject";

    private final String NODE_PROPERTY_KEY = "name";

    public DataImporterImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        graphSearcher = new GraphSearcherImpl(graphDb);
    }

    public void importData(List<SemanticData> semanticDataList) throws IOException {
        System.out.println("Starting database ...");
//        FileUtils.deleteRecursively(DB_PATH);
        createIndex();
        try (Transaction tx = graphDb.beginTx()) {
            for (SemanticData semanticData : semanticDataList) {
                createNodesAndRelationship(semanticData);
            }
            tx.success();
        }

//        try (Transaction tx = graphDb.beginTx()) {
//            Node myNode = findNode();
//
//            Iterable<Relationship> relationships = myNode.getRelationships();
//            Label label2 = Label.label(NODE_LABEL);
//            Node animalNode = graphDb.createNode(label2);
//            animalNode.setProperty(NODE_PROPERTY_KEY, "animal");
//            myNode.createRelationshipTo(animalNode, RelationshipType.withName("is"));
//
//            for (Relationship relationship : relationships) {
//                System.out.println(relationship.getStartNode().getProperty(NODE_PROPERTY_KEY));
//                System.out.println(relationship.getType());
//                System.out.println(relationship.getEndNode().getProperty(NODE_PROPERTY_KEY));
//            }
//        }
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
//        try (Transaction tx = graphDb.beginTx()) {
            Label label2 = Label.label(NODE_LABEL);
        String atomicSubject = semanticData.getAtomicSubject();
        Node object1 = null;
        Node atomicSubjectNode = graphSearcher.findNode(atomicSubject);
        if (atomicSubjectNode == null) {
            object1 = graphDb.createNode(label2);
            object1.setProperty(NODE_PROPERTY_KEY, atomicSubject);
        } else {
            object1 = atomicSubjectNode;
        }

        Node object2 = null;
        String atomicNounPredicate = semanticData.getAtomicNounPredicate();
        Node atomicNounPredicateNode = graphSearcher.findNode(atomicNounPredicate);
        if (atomicNounPredicateNode == null) {
            object2 = graphDb.createNode(label2);
            object2.setProperty(NODE_PROPERTY_KEY, atomicNounPredicate);
        } else {
            object2 = atomicNounPredicateNode;
        }

        object1.createRelationshipTo(object2, RelationshipType.withName(semanticData.getAtomicVerbPredicate()));
//            relationship.setProperty("verb", "eat");
        System.out.println("Nodes with relationship created: " + atomicSubject + " [ " + semanticData.getAtomicVerbPredicate() + " ] -> "
                + atomicNounPredicate);

//            tx.success();
//        }
    }



}