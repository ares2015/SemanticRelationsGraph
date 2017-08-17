package com.semanticRelationsGraph.main;

import com.semanticRelationsGraph.graph.SemanticRelationsGraph;
import com.semanticRelationsGraph.graph.SemanticRelationsGraphImpl;
import com.semanticRelationsGraph.importer.DataImporter;
import com.semanticRelationsGraph.importer.DataImporterImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Oliver on 4/11/2017.
 */
public class Main {

    private static final File DB_PATH = new File("C:\\Users\\Oliver\\Documents\\NlpTrainingData\\SemanticExtraction\\semantic-relations-graph");

    private static final String NODE_LABEL = "semanticObject";

    private static final String NODE_PROPERTY_KEY = "name";

    private static RelationshipType REL = RelationshipType.withName("REL");

    public static void main(String[] args) throws IOException {

        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        SemanticRelationsGraph semanticRelationsGraph = new SemanticRelationsGraphImpl();
        DataImporter dataImporter = new DataImporterImpl(graphDb);
        dataImporter.importData();

    }
}
