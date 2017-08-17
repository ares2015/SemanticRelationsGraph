package com.semanticRelationsGraph.importer;

import com.semanticRelationsGraph.data.SemanticData;
import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oliver on 4/11/2017.
 */
public class DataImporterImpl implements DataImporter {

    private String inputFilePath = "C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\jos_nlp_semantic_data_reverb.csv";

    private GraphDatabaseService graphDb;

    private GraphSearcher graphSearcher;

    private final String NODE_LABEL = "semanticObject";

    private final String NODE_PROPERTY_KEY = "name";

    private static RelationshipType REL = RelationshipType.withName("REL");

    private int numberOfRelationships = 0;

    public DataImporterImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        graphSearcher = new GraphSearcherImpl(graphDb);
    }

    public void importData() throws IOException {
        System.out.println("Starting database ...");
//        FileUtils.deleteRecursively(DB_PATH);
//        createIndex();
        try (Transaction tx = graphDb.beginTx()) {
            BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
            String extractedDataRow = br.readLine();
            while (extractedDataRow != null) {
                String atomicSubject = "";
                String extendedSubject = "";
                String atomicVerbPredicate = "";
                String extendedVerbPredicate = "";
                String atomicNounPredicate = "";
                String extendedNounPredicate = "";
                String sentence = "";
                String wikiTopic = "";
                String[] split = extractedDataRow.split(",");
                if (split.length >= 5) {
                    if (split[1] != null && !split[1].equals("")) {
                        atomicSubject = split[1];
                    }
                    if (split[2] != null && !split[2].equals("")) {
                        extendedSubject = split[2];
                    }
                    if (split[3] != null && !split[3].equals("")) {
                        atomicVerbPredicate = split[3];
                    }
                    if (split[4] != null && !split[4].equals("")) {
                        extendedVerbPredicate = split[4];
                    }
                    if (split[5] != null && !split[5].equals("")) {
                        atomicNounPredicate = split[5];
                    }
                    if (split[6] != null && !split[6].equals("")) {
                        atomicNounPredicate = split[5];
                    }
                    if (split[7] != null && !split[7].equals("")) {
                        sentence = split[7];
                    }
                    if (split[8] != null && !split[8].equals("")) {
                        wikiTopic = split[8];
                    }
                    SemanticData semanticData = new SemanticData(atomicSubject, extendedSubject, atomicVerbPredicate, extendedVerbPredicate,
                            atomicNounPredicate, extendedNounPredicate, sentence, wikiTopic);
                    createNodesAndRelationship(semanticData);
                }
                numberOfRelationships++;
                if (numberOfRelationships == 20) {
                    break;
                }
                System.out.println("Number of created relationships: " + numberOfRelationships);
                extractedDataRow = br.readLine();
            }
            tx.success();
            tx.close();
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
            Label label = Label.label(NODE_LABEL);
            String subject = "";
            String verbPredicate = "";
            String nounPredicate = "";
            if (semanticData.getExtendedSubject() != "") {
                subject = semanticData.getExtendedSubject();
            } else {
                subject = semanticData.getAtomicSubject();
            }
            if (semanticData.getExtendedVerbPredicate() != "") {
                verbPredicate = semanticData.getExtendedVerbPredicate();
            } else {
                verbPredicate = semanticData.getAtomicVerbPredicate();
            }
            if (semanticData.getExtendedNounPredicate() != "") {
                nounPredicate = semanticData.getExtendedNounPredicate();
            } else {
                nounPredicate = semanticData.getAtomicNounPredicate();
            }
            Node object1 = null;
            Node subjectNode = graphSearcher.findNode(subject);
            if (subjectNode == null) {
                object1 = graphDb.createNode(label);
                object1.setProperty(NODE_PROPERTY_KEY, subject);
            } else {
                object1 = subjectNode;
            }

            Node object2 = null;
            Node nounPredicateNode = graphSearcher.findNode(nounPredicate);
            if (nounPredicateNode == null) {
                object2 = graphDb.createNode(label);
                object2.setProperty(NODE_PROPERTY_KEY, nounPredicate);
            } else {
                object2 = nounPredicateNode;
            }

            Relationship relationship = object1.createRelationshipTo(object2, REL);
            relationship.setProperty("verbPredicate", verbPredicate);
            relationship.setProperty("sentence", semanticData.getSentence());
            relationship.setProperty("wikiTopic", semanticData.getWikiTopic());

            System.out.println("Nodes with relationship created: " + subject + " [ " + verbPredicate + " ] -> "
                    + nounPredicate);

            tx.success();
            tx.close();
        }
    }

    Relationship getRelationshipBetween(Node n1, Node n2) { // RelationshipType type, Direction direction
        for (Relationship rel : n1.getRelationships()) { // n1.getRelationships(type,direction)
            if (rel.getOtherNode(n1).equals(n2)) return rel;
        }
        return null;
    }

}