package com.semanticRelationsGraph.searcher;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Oliver on 4/12/2017.
 */
public class GraphSearcherImpl implements GraphSearcher {

    private final String NODE_LABEL = "semanticObject";

    private final String NODE_PROPERTY_KEY = "name";

    private GraphDatabaseService graphDb;

    public GraphSearcherImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public Optional<Node> findNode(String nodeName, boolean isShortName) {
        List<Node> nodeList = new ArrayList<>();
        String nodeNameNew = "'" + nodeName + "'";
        String query = "";
        if (isShortName) {
            query = "MATCH (node) WHERE node.shortName = " + nodeNameNew + " RETURN node";
        } else {
            query = "MATCH (node) WHERE node.longName = " + nodeNameNew + " RETURN node";
        }
        try (Transaction tx = graphDb.beginTx();
             Result result = graphDb.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    System.out.println(column.getKey() + ": " + column.getValue());
                    nodeList.add((Node) column.getValue());
                }
            }
            tx.success();
            tx.close();
        }

        if (nodeList.size() > 0) {
            return Optional.of(nodeList.get(0));
        } else {
            return Optional.empty();
        }
    }


    @Override
    public Optional<Node> findNode(String shortName, String longName) {
        List<Node> nodeList = new ArrayList<>();
        String shortNameNew = "'" + shortName + "'";
        String longNameNew = "'" + longName + "'";
        String query = "MATCH (node) WHERE node.shortName = " + shortNameNew + " AND node.longName = " + longNameNew + " RETURN node";
        try (Transaction ignored = graphDb.beginTx();
             Result result = graphDb.execute(query)) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
//                    System.out.println(column.getKey() + ": " + column.getValue());
                    nodeList.add((Node) column.getValue());
                }
            }
        }
        if (nodeList.size() > 0) {
            return Optional.of(nodeList.get(0));
        } else {
            return Optional.empty();
        }
    }

}
