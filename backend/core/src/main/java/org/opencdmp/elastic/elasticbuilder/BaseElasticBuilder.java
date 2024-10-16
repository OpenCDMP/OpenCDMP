package org.opencdmp.elastic.elasticbuilder;

import org.opencdmp.convention.ConventionService;
import gr.cite.tools.data.builder.Builder;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseElasticBuilder<M, D> implements Builder {
    protected final LoggerService logger;
    protected final ConventionService conventionService;

    public BaseElasticBuilder(
            ConventionService conventionService,
            LoggerService logger
    ) {
        this.conventionService = conventionService;
        this.logger = logger;
    }

    public M build(D data) throws MyApplicationException {
        if (data == null) {
            M model = null;
            return null; //TODO
        }
        List<M> models = this.build(Arrays.asList(data));
        return models.stream().findFirst().orElse(null); //TODO
    }

    public abstract List<M> build(List<D> datas) throws MyApplicationException;

    public <K> Map<K, M> asForeignKey(QueryBase<D> query, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building references from query");
        List<D> datas = query.collect();
        this.logger.debug("collected {} items to build", Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
        return this.asForeignKey(datas, keySelector);
    }

    public <K> Map<K, M> asForeignKey(List<D> datas, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("building references");
        List<M> models = this.build(datas);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(e -> e.size()).orElse(0), Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
        Map<K, M> map = models.stream().collect(Collectors.toMap(o -> keySelector.apply(o), o -> o));
        return map;
    }

    public <K> Map<K, List<M>> asMasterKey(QueryBase<D> query, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building details from query");
        List<D> datas = query.collect();
        this.logger.debug("collected {} items to build", Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
        return this.asMasterKey(datas, keySelector);
    }

    public <K> Map<K, List<M>> asMasterKey(List<D> datas, Function<M, K> keySelector) throws MyApplicationException {
        this.logger.trace("building details");
        List<M> models = this.build(datas);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(e -> e.size()).orElse(0), Optional.ofNullable(datas).map(e -> e.size()).orElse(0));
        Map<K, List<M>> map = new HashMap<>();
        for (M model : models) {
            K key = keySelector.apply(model);
            if (!map.containsKey(key)) map.put(key, new ArrayList<M>());
            map.get(key).add(model);
        }
        return map;
    }

    public <FK, FM> Map<FK, FM> asEmpty(List<FK> keys, Function<FK, FM> mapper, Function<FM, FK> keySelector) {
        this.logger.trace("building static references");
        List<FM> models = keys.stream().map(x -> mapper.apply(x)).collect(Collectors.toList());
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(x -> x.size()).orElse(0), Optional.ofNullable(keys).map(x -> x.size()));
        Map<FK, FM> map = models.stream().collect(Collectors.toMap(o -> keySelector.apply(o), o -> o));
        return map;
    }

    protected String hashValue(Instant value) throws MyApplicationException {
        return this.conventionService.hashValue(value);
    }

    protected String asPrefix(String name) {
        return this.conventionService.asPrefix(name);
    }

    protected String asIndexer(String... names) {
        return this.conventionService.asIndexer(names);
    }

}

