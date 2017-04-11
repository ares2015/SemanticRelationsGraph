package com.semanticRelationsGraph.importer;

import com.semanticRelationsGraph.data.SemanticData;

import java.io.IOException;
import java.util.List;

/**
 * Created by Oliver on 4/11/2017.
 */
public interface DataImporter {

    void importData(List<SemanticData> semanticDataList) throws IOException;

}
