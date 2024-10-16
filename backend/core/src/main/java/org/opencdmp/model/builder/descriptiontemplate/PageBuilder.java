package org.opencdmp.model.builder.descriptiontemplate;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.PageEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.Page;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PageBuilder extends BaseBuilder<Page, PageEntity> {

    private final BuilderFactory builderFactory;
    
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public PageBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PageBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public PageBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Page> build(FieldSet fields, List<PageEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Page> models = new ArrayList<>();
        FieldSet sectionsFields = fields.extractPrefixed(this.asPrefix(Page._sections));
        for (PageEntity d : data) {
            Page m = new Page();
            if (fields.hasField(this.asIndexer(Page._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Page._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Page._title))) m.setTitle(d.getTitle());
            if (!sectionsFields.isEmpty() && d.getSections() != null) m.setSections(this.builderFactory.builder(SectionBuilder.class).authorize(this.authorize).build(sectionsFields, d.getSections()));
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
