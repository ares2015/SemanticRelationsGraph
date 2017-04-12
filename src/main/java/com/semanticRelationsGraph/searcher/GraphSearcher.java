package com.semanticRelationsGraph.searcher;

import org.neo4j.graphdb.Node;

/**
 * Created by Oliver on 4/12/2017.
 */
public interface GraphSearcher {

    Node findNode(String nodeName);

}
