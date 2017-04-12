package com.semanticRelationsGraph.reader;

import com.semanticRelationsGraph.data.SemanticData;

import java.io.IOException;
import java.util.List;

/**
 * Created by Oliver on 4/11/2017.
 */
public interface SemanticDataReader {

    List<SemanticData> read() throws IOException;

}