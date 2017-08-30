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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oliver on 4/11/2017.
 */
public class DataImporterImpl implements DataImporter {

//    private String inputFilePath = "C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\jos_nlp_semantic_data.csv";

    private String inputFilePath = "C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\SemanticExtractedData.txt";

    private GraphDatabaseService graphDb;

    private GraphSearcher graphSearcher;

    private final String NODE_LABEL = "semanticObject";

    private final String SHORT_NAME = "shortName";

    private final String LONG_NAME = "longName";

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
//                String[] split = extractedDataRow.split(",");
                String[] split = extractedDataRow.split("#");
                if (split.length >= 5) {
                    if (split[1] != null && !split[1].equals("")) {
                        atomicSubject = removeDoubleQuotesEmptyString(split[1]);
                    }
                    if (split[2] != null && !split[2].equals("")) {
                        extendedSubject = removeDoubleQuotesEmptyString(split[2]);
                    }
                    if (split[3] != null && !split[3].equals("")) {
                        atomicVerbPredicate = removeDoubleQuotesEmptyString(split[3]);
                    }
                    if (split[4] != null && !split[4].equals("")) {
                        extendedVerbPredicate = removeDoubleQuotesEmptyString(split[4]);
                    }
                    if (split[5] != null && !split[5].equals("")) {
                        atomicNounPredicate = removeDoubleQuotesEmptyString(split[5]);
                    }
                    if (split[6] != null && !split[6].equals("")) {
                        extendedNounPredicate = removeDoubleQuotesEmptyString(split[6]);
                    }
//                    if (split[7] != null && !split[7].equals("")) {
//                        sentence = removeDoubleQuotesEmptyString(split[7]);
//                    }
//                    if (split[8] != null && !split[8].equals("")) {
//                        wikiTopic = removeDoubleQuotesEmptyString(split[8]);
//                    }
                }

                SemanticData semanticData = new SemanticData(atomicSubject, extendedSubject, atomicVerbPredicate, extendedVerbPredicate,
                        atomicNounPredicate, extendedNounPredicate, sentence, wikiTopic);
                createNodesAndRelationship(semanticData);

                numberOfRelationships++;
//                if (numberOfRelationships == 6000) {
//                    break;
//                }
                System.out.println("Number of created relationships: " + numberOfRelationships);
                extractedDataRow = br.readLine();
            }

//            tx.success();
//            tx.close();

        }

    }


    private void createIndex() {
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();

            indexDefinition = schema.indexFor(Label.label(NODE_LABEL))
                    .on(SHORT_NAME)
                    .create();
            tx.success();
        }
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            System.out.println(String.format("Percent complete: %1.0f%%",
                    schema.getIndexPopulationProgress(indexDefinition).getCompletedPercentage()));
        }
    }

    private void createNodesAndRelationship(SemanticData semanticData) {
        try (Transaction tx = graphDb.beginTx()) {
            Node subjectNode = createNode(semanticData.getAtomicSubject(), semanticData.getExtendedSubject());
            Node nounPredicateNode = createNode(semanticData.getAtomicNounPredicate(), semanticData.getExtendedNounPredicate());
            String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
            String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();

            String verbPredicate = "";
            if (!"".equals(extendedVerbPredicate)) {
                verbPredicate = extendedVerbPredicate;
            } else {
                verbPredicate = atomicVerbPredicate;
            }
            if (!existsRelationship(subjectNode, nounPredicateNode, verbPredicate)) {
                Relationship relationship = subjectNode.createRelationshipTo(nounPredicateNode, REL);
                relationship.setProperty("verbPredicate", verbPredicate);
                relationship.setProperty("sentence", semanticData.getSentence());
                relationship.setProperty("wikiTopic", semanticData.getWikiTopic());
                System.out.println("Nodes with relationship created: " + subjectNode.getProperty("shortName") + " [ " + verbPredicate + " ] -> "
                        + nounPredicateNode.getProperty("shortName"));
            }
            tx.success();
            tx.close();
        }
    }

    private Node createNode(String atomicProperty, String extendedProperty) {
        Label label = Label.label(NODE_LABEL);
        Node node = null;
        Optional<Node> nodeOptional = null;
        if ("".equals(atomicProperty) && !"".equals(extendedProperty)) {
            nodeOptional = graphSearcher.findNode(extendedProperty, false);
        } else if (!"".equals(atomicProperty) && "".equals(extendedProperty)) {
            nodeOptional = graphSearcher.findNode(atomicProperty, true);
        } else if (!"".equals(atomicProperty) && !"".equals(extendedProperty)) {
            nodeOptional = graphSearcher.findNode(atomicProperty, extendedProperty);
        }
        if (!nodeOptional.isPresent()) {
            node = graphDb.createNode(label);
            if (!"".equals(atomicProperty)) {
                node.setProperty(SHORT_NAME, atomicProperty);
            }
            if (!"".equals(extendedProperty)) {
                node.setProperty(LONG_NAME, extendedProperty);
            }
            return node;
        } else {
            return nodeOptional.get();
        }
    }

    private String removeDoubleQuotesEmptyString(String word) {
        if (word.startsWith("\"")) {
            word = word.substring(1, word.length() - 1);
        }
        if (word.endsWith("\"")) {
            word = word.substring(0, word.length() - 1);
        }
        if (word.endsWith(" ")) {
            word = word.substring(0, word.length() - 1);
        }
        return word;
    }

    private boolean existsRelationship(Node n1, Node n2, String verbPredicate) {
        if (n1.getRelationships() == null) {
            return false;
        }
        for (Relationship rel : n1.getRelationships()) {
            if (rel.getOtherNode(n1).equals(n2)) {
                return verbPredicate.equals(rel.getProperty("verbPredicate"));
            }
        }
        return false;
    }

}