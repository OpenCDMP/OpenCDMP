package org.opencdmp.service.elastic;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.model.*;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.lookup.DescriptionLookup;
import org.opencdmp.query.lookup.PlanLookup;
import gr.cite.tools.fieldset.FieldSet;

import java.util.EnumSet;


public interface ElasticQueryHelperService {
    QueryResult<Plan> collect(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet);
    QueryResult<PublicPlan> collectPublic(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet);
    long count(PlanLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags);
    QueryResult<Description> collect(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet);
    QueryResult<PublicDescription> collectPublic(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags, FieldSet fieldSet);
    long count(DescriptionLookup lookup, EnumSet<AuthorizationFlags> authorizationFlags);
}