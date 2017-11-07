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

    private String inputFilePath = "C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\jos_nlp_semantic_data.csv";

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
                    if (split[1] != null && !split[1].isEmpty()) {
                        atomicSubject = removeDoubleQuotesEmptyString(split[1]);
                    }
                    if (split[2] != null && !split[2].isEmpty()) {
                        extendedSubject = removeDoubleQuotesEmptyString(split[2]);
                    }
                    if (split[3] != null && !split[3].isEmpty()) {
                        atomicVerbPredicate = removeDoubleQuotesEmptyString(split[3]);
                    }
                    if (split[4] != null && !split[4].isEmpty()) {
                        extendedVerbPredicate = removeDoubleQuotesEmptyString(split[4]);
                    }
                    if (split[5] != null && !split[5].isEmpty()) {
                        atomicNounPredicate = removeDoubleQuotesEmptyString(split[5]);
                    }
                    if (split[6] != null && !split[6].isEmpty()) {
                        extendedNounPredicate = removeDoubleQuotesEmptyString(split[6]);
                    }
                    if (split[7] != null && !split[7].isEmpty()) {
                        sentence = removeDoubleQuotesEmptyString(split[7]);
                    }
                    if (split[8] != null && !split[8].isEmpty()) {
                        wikiTopic = removeDoubleQuotesEmptyString(split[8]);
                    }
                    SemanticData semanticData = new SemanticData(atomicSubject, extendedSubject, atomicVerbPredicate, extendedVerbPredicate,
                            atomicNounPredicate, extendedNounPredicate, sentence, wikiTopic);
                    createNodesAndRelationship(semanticData);
                }
                numberOfRelationships++;
                if (numberOfRelationships == 200000) {
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
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();

            indexDefinition = schema.indexFor(Label.label(NODE_LABEL))
                    .on(NODE_PROPERTY_KEY)
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

            Node atomicSubjectNode = null;
            Node extendedSubjectNode = null;
            String atomicSubject = semanticData.getAtomicSubject();
            String extendedSubject = semanticData.getExtendedSubject();
            if (!atomicSubject.isEmpty() && !extendedSubject.isEmpty()) {
                if (atomicSubject.equals(extendedSubject)) {
                    atomicSubjectNode = createNode(atomicSubject);
                } else {
                    atomicSubjectNode = createNode(atomicSubject);
                    extendedSubjectNode = createNode(extendedSubject);
                }
            } else if (!atomicSubject.isEmpty() && extendedSubject.isEmpty()) {
                atomicSubjectNode = createNode(atomicSubject);
            } else if (atomicSubject.isEmpty() && !extendedSubject.isEmpty()) {
                extendedSubjectNode = createNode(extendedSubject);
            }

            Node atomicNounPredicateNode = null;
            Node extendedNounPredicateNode = null;
            String atomicNounPredicate = semanticData.getAtomicNounPredicate();
            String extendedNounPredicate = semanticData.getExtendedNounPredicate();
            if (!atomicNounPredicate.isEmpty() && !extendedNounPredicate.isEmpty()) {
                if (atomicNounPredicate.equals(extendedNounPredicate)) {
                    atomicNounPredicateNode = createNode(atomicNounPredicate);
                } else {
                    atomicNounPredicateNode = createNode(atomicNounPredicate);
                    extendedNounPredicateNode = createNode(extendedNounPredicate);
                }
            } else if (!atomicNounPredicate.isEmpty() && extendedNounPredicate.isEmpty()) {
                atomicNounPredicateNode = createNode(atomicNounPredicate);
            } else if (atomicNounPredicate.isEmpty() && !extendedNounPredicate.isEmpty()) {
                extendedNounPredicateNode = createNode(extendedNounPredicate);
            }

            //ATOMIC - ATOMIC - EXTENDED - EXTENDED
            if (atomicSubjectNode != null && extendedSubjectNode != null && atomicNounPredicateNode != null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //EXTENDED - ATOMIC - EXTENDED
            if (atomicSubjectNode == null && extendedSubjectNode != null && atomicNounPredicateNode != null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //ATOMIC - ATOMIC - EXTENDED
            if (atomicSubjectNode != null && extendedSubjectNode == null && atomicNounPredicateNode != null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //ATOMIC - EXTENDED - EXTENDED
            if (atomicSubjectNode != null && extendedSubjectNode != null && atomicNounPredicateNode == null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //ATOMIC - EXTENDED - ATOMIC
            if (atomicSubjectNode != null && extendedSubjectNode != null && atomicNounPredicateNode != null && extendedNounPredicateNode == null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //ATOMIC - ATOMIC
            if (atomicSubjectNode != null && extendedSubjectNode == null && atomicNounPredicateNode != null && extendedNounPredicateNode == null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //ATOMIC - EXTENDED
            if (atomicSubjectNode != null && extendedSubjectNode == null && atomicNounPredicateNode == null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(atomicSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //EXTENDED - ATOMIC
            if (atomicSubjectNode == null && extendedSubjectNode != null && atomicNounPredicateNode != null && extendedNounPredicateNode == null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, atomicNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            //EXTENDED - EXTENDED
            if (atomicSubjectNode == null && extendedSubjectNode != null && atomicNounPredicateNode == null && extendedNounPredicateNode != null) {
                String atomicVerbPredicate = semanticData.getAtomicVerbPredicate();
                String extendedVerbPredicate = semanticData.getExtendedVerbPredicate();
                if (!atomicVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, atomicVerbPredicate, semanticData);
                }
                if (!extendedVerbPredicate.isEmpty()) {
                    createRelationship(extendedSubjectNode, extendedNounPredicateNode, extendedVerbPredicate, semanticData);
                }
            }

            tx.success();
            tx.close();
        }
    }

    private void createRelationship(Node node1, Node node2, String verbPredicate, SemanticData semanticData) {
        if (!existsRelationship(node1, node2, verbPredicate)) {
            Relationship relationship = node1.createRelationshipTo(node2, REL);
            relationship.setProperty("verbPredicate", verbPredicate);
            relationship.setProperty("sentence", semanticData.getSentence());
            relationship.setProperty("wikiTopic", semanticData.getWikiTopic());

            System.out.println("Nodes with relationship created: " + node1.getProperty("name") + " [ " + verbPredicate + " ] -> "
                    + node2.getProperty("name"));
        }
    }


    private Node createNode(String nodeName) {
//        if (!containsAlphaNumericalChars(nodeName)) {
//            return null;
//        }
        Label label = Label.label(NODE_LABEL);
        Node node = null;
        node = graphSearcher.findNode(nodeName);
        if (node == null) {
            node = graphDb.createNode(label);
            node.setProperty(NODE_PROPERTY_KEY, nodeName);
        }
        return node;
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

    private boolean existsRelationship(Node n1, Node n2, String property) { // RelationshipType type, Direction direction
        if (n1.getRelationships() == null) {
            return false;
        }
        for (Relationship rel : n1.getRelationships()) { // n1.getRelationships(type,direction)
            if (rel.getOtherNode(n1).equals(n2)) {
                return property.equals(rel.getProperty("verbPredicate"));
            }
        }
        return false;
    }

    private boolean containsAlphaNumericalChars(String s) {
        return s.matches("[A-Za-z0-9]+");
    }

}