package org.opencdmp.commons.types.plan.importexport;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.types.description.importexport.DescriptionImportExport;
import org.opencdmp.commons.types.planblueprint.importexport.BlueprintImportExport;
import org.opencdmp.commons.xmladapter.InstantXmlAdapter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@XmlRootElement(name = "plan")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanImportExport {

    @XmlElement(name = "id")
    private UUID id;

    @XmlElement(name = "description")
    private String  description;

    @XmlElement(name = "title")
    private String  title;

    @XmlElement(name = "language")
    private String  language;

    @XmlElement(name = "access")
    private PlanAccessType access;

    @XmlElement(name = "status")
    private PlanStatusImportExport status;

    @XmlElement(name = "version")
    private Short version;

    @XmlElementWrapper(name = "contacts")
    @XmlElement(name = "contact")
    private List<PlanContactImportExport> contacts;

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<PlanUserImportExport> users;

    @XmlElement(name = "finalizedAt")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant finalizedAt;

    @XmlElement(name = "publicAfter")
    @XmlJavaTypeAdapter(InstantXmlAdapter.class)
    private Instant publicAfter;

    @XmlElement(name = "blueprint")
    private BlueprintImportExport blueprint;

    @XmlElementWrapper(name = "blueprintValues")
    @XmlElement(name = "blueprintValue")
    private List<PlanBlueprintValueImportExport> blueprintValues;

    @XmlElementWrapper(name = "descriptionTemplates")
    @XmlElement(name = "descriptionTemplate")
    private List<PlanDescriptionTemplateImportExport> descriptionTemplates;

    @XmlElementWrapper(name = "references")
    @XmlElement(name = "reference")
    private List<PlanReferenceImportExport> references;

    @XmlElementWrapper(name = "descriptions")
    @XmlElement(name = "description")
    private List<DescriptionImportExport> descriptions;
    
    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    public PlanAccessType getAccess() {
        return this.access;
    }

    public void setAccess(PlanAccessType access) {
        this.access = access;
    }

    public PlanStatusImportExport getStatus() {
        return status;
    }

    public void setStatus(PlanStatusImportExport status) {
        this.status = status;
    }

    public List<PlanContactImportExport> getContacts() {
        return this.contacts;
    }

    public void setContacts(List<PlanContactImportExport> contacts) {
        this.contacts = contacts;
    }

    public List<PlanUserImportExport> getUsers() {
        return this.users;
    }

    public void setUsers(List<PlanUserImportExport> users) {
        this.users = users;
    }

    public Instant getFinalizedAt() {
        return this.finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Instant getPublicAfter() {
        return this.publicAfter;
    }

    public void setPublicAfter(Instant publicAfter) {
        this.publicAfter = publicAfter;
    }

    public BlueprintImportExport getBlueprint() {
        return this.blueprint;
    }

    public void setBlueprint(BlueprintImportExport blueprint) {
        this.blueprint = blueprint;
    }

    public List<PlanBlueprintValueImportExport> getBlueprintValues() {
        return this.blueprintValues;
    }

    public void setBlueprintValues(List<PlanBlueprintValueImportExport> blueprintValues) {
        this.blueprintValues = blueprintValues;
    }

    public List<PlanDescriptionTemplateImportExport> getDescriptionTemplates() {
        return this.descriptionTemplates;
    }

    public void setDescriptionTemplates(List<PlanDescriptionTemplateImportExport> descriptionTemplates) {
        this.descriptionTemplates = descriptionTemplates;
    }

    public List<PlanReferenceImportExport> getReferences() {
        return this.references;
    }

    public void setReferences(List<PlanReferenceImportExport> references) {
        this.references = references;
    }

    public List<DescriptionImportExport> getDescriptions() {
        return this.descriptions;
    }

    public void setDescriptions(List<DescriptionImportExport> descriptions) {
        this.descriptions = descriptions;
    }

    public Short getVersion() {
        return this.version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }
}

