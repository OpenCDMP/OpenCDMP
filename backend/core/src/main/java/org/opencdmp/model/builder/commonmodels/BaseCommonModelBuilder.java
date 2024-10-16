package org.opencdmp.model.builder.commonmodels;

import org.opencdmp.convention.ConventionService;
import gr.cite.tools.data.builder.Builder;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseCommonModelBuilder<M, D> implements Builder {
    protected final LoggerService logger;
    protected final ConventionService conventionService;

    public BaseCommonModelBuilder(
            ConventionService conventionService,
            LoggerService logger
    ) {
        this.conventionService = conventionService;
        this.logger = logger;
    }

    public M build(D data) throws MyApplicationException {
        if (data == null) {
            //this.logger.Debug(new MapLogEntry("requested build for null item requesting fields").And("fields", directives));
//			return default(M);
            M model = null;
            return null; //TODO
        }
        List<CommonModelBuilderItemResponse<M, D>> models = this.buildInternal(List.of(data));
        return models.stream().map(CommonModelBuilderItemResponse::getModel).findFirst().orElse(null); //TODO
    }

    public List<M> build(List<D> data) throws MyApplicationException{
        List<CommonModelBuilderItemResponse<M, D>> models = this.buildInternal(data);
        return models == null ? null : models.stream().map(CommonModelBuilderItemResponse::getModel).collect(Collectors.toList());
    }

    protected abstract List<CommonModelBuilderItemResponse<M, D>> buildInternal(List<D> data) throws MyApplicationException;

    public <K> Map<K, M> asForeignKey(QueryBase<D> query, Function<D, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building references from query");
        List<D> data = query.collect();
        this.logger.debug("collected {} items to build", Optional.ofNullable(data).map(List::size).orElse(0));
        return this.asForeignKey(data, keySelector);
    }

    public <K> Map<K, M> asForeignKey(List<D> data, Function<D, K> keySelector) throws MyApplicationException {
        this.logger.trace("building references");
        List<CommonModelBuilderItemResponse<M, D>> models = this.buildInternal(data);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(List::size).orElse(0), Optional.ofNullable(data).map(List::size).orElse(0));
	    return models == null ? new HashMap<>() : models.stream().collect(Collectors.toMap(x-> keySelector.apply(x.getData()), CommonModelBuilderItemResponse::getModel));
    }
    public <K> Map<K, List<M>> asMasterKey(QueryBase<D> query,Function<D, K> keySelector) throws MyApplicationException {
        this.logger.trace("Building details from query");
        List<D> data = query.collect();
        this.logger.debug("collected {} items to build", Optional.ofNullable(data).map(List::size).orElse(0));
        return this.asMasterKey(data, keySelector);
    }

    public <K> Map<K, List<M>> asMasterKey(List<D> data, Function<D, K> keySelector) throws MyApplicationException {
        this.logger.trace("building details");
        List<CommonModelBuilderItemResponse<M, D>> models = this.buildInternal(data);
        this.logger.debug("mapping {} build items from {} requested", Optional.ofNullable(models).map(List::size).orElse(0), Optional.ofNullable(data).map(List::size).orElse(0));
        Map<K, List<M>> map = new HashMap<>();
        if (models == null) return map;
        for (CommonModelBuilderItemResponse<M, D> model : models) {
            K key = keySelector.apply(model.getData());
            if (!map.containsKey(key)) map.put(key, new ArrayList<M>());
            map.get(key).add(model.getModel());
        }
        return map;
    }
}

