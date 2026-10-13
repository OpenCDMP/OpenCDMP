package org.opencdmp.service.semantic;

import org.opencdmp.query.lookup.SemanticsLookup;

import java.io.IOException;
import java.util.List;

public interface SemanticsService {

    List<String> getSemantics(SemanticsLookup lookup) throws IOException;

    List<Semantic> getSemantics() throws IOException;

    /** What the tenant's plugins read: the manifests of its file transformers, deposits and evaluators that publish one. */
    List<PluginSemantics> getPluginSemantics();
}
