package com.semanticRelationsGraph.searcher;

import org.neo4j.graphdb.*;

import java.util.ArrayList;

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
    public Node findNode(String nodeName) {
        ArrayList<Node> userNodes = new ArrayList<>();
        Label label = Label.label(NODE_LABEL);
        try (Transaction tx = graphDb.beginTx()) {
            try (ResourceIterator<Node> users =
                         graphDb.findNodes(label, NODE_PROPERTY_KEY, nodeName)) {
                while (users.hasNext()) {
                    userNodes.add(users.next());
                }

//                for (Node node : userNodes) {
//                    System.out.println(
//                            "The username of user " + idToFind + " is " + node.getProperty("username"));
//                }
            }
            tx.success();
        }
        if (userNodes.size() > 0) {
            return userNodes.get(0);
        } else {
            return null;
        }

    }

}
