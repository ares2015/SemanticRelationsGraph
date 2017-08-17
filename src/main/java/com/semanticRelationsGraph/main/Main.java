package com.semanticRelationsGraph.main;

import com.semanticRelationsGraph.graph.SemanticRelationsGraph;
import com.semanticRelationsGraph.graph.SemanticRelationsGraphImpl;
import com.semanticRelationsGraph.importer.DataImporter;
import com.semanticRelationsGraph.importer.DataImporterImpl;
import com.semanticRelationsGraph.searcher.GraphSearcher;
import com.semanticRelationsGraph.searcher.GraphSearcherImpl;
import org.neo4j.graphdb.*;
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
        Node tusk = null;

        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
        SemanticRelationsGraph semanticRelationsGraph = new SemanticRelationsGraphImpl();

//        semanticRelationsGraph.processGraphData();
        DataImporter dataImporter = new DataImporterImpl(graphDb);
        dataImporter.importData();

        GraphSearcher graphSearcher = new GraphSearcherImpl(graphDb);


        try (Transaction tx = graphDb.beginTx()) {
            Node myNode = graphSearcher.findNode("orbiter");
            Iterable<Relationship> relationships = myNode.getRelationships();
            for (Relationship relationship : relationships) {
                System.out.println(relationship.getStartNode().getProperty(NODE_PROPERTY_KEY));
                System.out.println(relationship.getProperty("verbPredicate"));
                System.out.println(relationship.getEndNode().getProperty(NODE_PROPERTY_KEY));
            }
        }
//
//        try (Transaction tx = graphDb.beginTx()) {
//
//            tusk = graphSearcher.findNode("Tusk");
//            Node merkel = graphSearcher.findNode("Merkel");
//            // START SNIPPET: shortestPathUsage
//            PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(REL, Direction.BOTH), 5);
//            Path foundPath = finder.findSinglePath(tusk, merkel);
//            Iterable<Relationship> relationships = foundPath.relationships();
//            Iterator<Relationship> iterator = relationships.iterator();
//            while (iterator.hasNext()) {
//                Relationship relationship = iterator.next();
//                System.out.println(relationship.getStartNode().getProperty("name") + " ->" + relationship.getProperty("verbPredicate") + "->" +
//                        relationship.getEndNode().getProperty("name"));
//            }
//            System.out.println("Path from Tusk to Merkel: " + Paths.simplePathToString(foundPath, NODE_PROPERTY_KEY));
//            // END SNIPPET: shortestPathUsage
//        }
//
//        try (Transaction tx = graphDb.beginTx()) {
//            String output = "";
//            // START SNIPPET: knowslikestraverser
//            for (Path position : graphDb.traversalDescription()
//                    .depthFirst()
//                    .relationships(REL)
//                    .evaluator(Evaluators.toDepth(3))
//                    .traverse(tusk)) {
////                output += position.startNode().getProperty("name") + " ---" + position.lastRelationship().getProperty("verbPredicate") + "---" + position.endNode().getProperty("name") + "\n";
////                output += position.startNode().getProperty("name") + " ---" + iterateRels(position.relationships()) + "---" + position.endNode().getProperty("name") + " ";
//                output += iterateRels(position.relationships()) + "********";
//            }
//            System.out.println("Traversal output" + output);
//            // END SNIPPET: knowslikestraverser
//        }
//
//
//
////        String rows = "";
////        try ( Transaction ignored = graphDb.beginTx();
////              Result result = graphDb.execute( "MATCH (n {name: 'my node'}) RETURN n, n.name" ) )
////        {
////            while ( result.hasNext() )
////            {
////                Map<String,Object> row = result.next();
////                for ( Map.Entry<String,Object> column : row.entrySet() )
////                {
////                    rows += column.getKey() + ": " + column.getValue() + "; ";
////                }
////                rows += "\n";
////            }
////        }
//    }
//
//    private static String iterateRels(Iterable<Relationship> iterateRels) {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Relationship rel : iterateRels) {
//            stringBuilder.append(rel.getStartNode().getProperty("name"));
//            stringBuilder.append("--->");
//            stringBuilder.append(rel.getProperty("verbPredicate"));
//            stringBuilder.append("--->");
//            stringBuilder.append(rel.getEndNode().getProperty("name"));
//            stringBuilder.append(" ");
//        }
//        return stringBuilder.toString();
    }
}
