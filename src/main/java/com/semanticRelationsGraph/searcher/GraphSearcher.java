package com.semanticRelationsGraph.searcher;

import org.neo4j.graphdb.Node;

import java.util.Optional;

/**
 * Created by Oliver on 4/12/2017.
 */
public interface GraphSearcher {

    Optional<Node> findNode(String nodeName, boolean isShortName);

    Optional<Node> findNode(String shortName, String longName);

}