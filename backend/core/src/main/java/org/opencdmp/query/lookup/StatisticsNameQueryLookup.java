package org.opencdmp.query.lookup;

import gr.cite.tools.fieldset.BaseFieldSet;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public class StatisticsNameQueryLookup {
    private List<UUID> ids;
    @Schema(
            description = "<li>\n     This is an object controlling the data projection of the results.\n     It contains a list of strings called <i>fields</i> with the names of the properties to project.\n     <br/>You can also include properties that are deeper in the object tree by prefixing them with dots.\n<li/>\n"
    )
    private BaseFieldSet project;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

    public BaseFieldSet getProject() {
        return this.project;
    }

    public void setProject(BaseFieldSet project) {
        this.project = project;
    }
}
