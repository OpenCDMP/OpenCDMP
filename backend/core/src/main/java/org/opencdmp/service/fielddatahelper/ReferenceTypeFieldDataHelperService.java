package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.ReferenceTypeDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.ReferenceTypeDataImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ReferenceTypeEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.ReferenceTypeDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.ReferenceTypeDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.ReferenceTypeData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.ReferenceTypeDataPersist;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.query.ReferenceTypeQuery;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class ReferenceTypeFieldDataHelperService extends BaseFieldDataHelperService<ReferenceTypeData, ReferenceTypeDataPersist, ReferenceTypeDataEntity, ReferenceTypeDataImportExport, ReferenceTypeDataModel> {

	private final BuilderFactory builderFactory;
	private final QueryFactory queryFactory;
	private final ConventionService conventionService;
	private final MessageSource messageSource;
	private final ErrorThesaurusProperties errors;

	private final ValidatorFactory validatorFactory;
	public ReferenceTypeFieldDataHelperService(BuilderFactory builderFactory, QueryFactory queryFactory, ConventionService conventionService, MessageSource messageSource, ErrorThesaurusProperties errors, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.queryFactory = queryFactory;
		this.conventionService = conventionService;
		this.messageSource = messageSource;
        this.errors = errors;
        this.validatorFactory = validatorFactory;
	}

	@Override
	public ReferenceTypeDataEntity newDataInstanceInternal() {
		return new ReferenceTypeDataEntity();
	}

	@Override
	public ReferenceTypeData newModelInstanceInternal() {
		return new ReferenceTypeData();
	}

	@Override
	public ReferenceTypeDataPersist newPersistModelInstanceInternal() {
		return new ReferenceTypeDataPersist();
	}

	@Override
	protected ReferenceTypeDataImportExport newImportExportInstanceInternal() {
		return new ReferenceTypeDataImportExport();
	}

	@Override
	protected ReferenceTypeDataModel newCommonModelInstanceInternal() {
		return new ReferenceTypeDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return ReferenceTypeDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return ReferenceTypeData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return ReferenceTypeDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return ReferenceTypeDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return ReferenceTypeDataModel.class;
	}
	
	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(ReferenceTypeDataPersist.ReferenceTypeDataPersistValidator.class);
	}

	@Override
	public List<ReferenceTypeData> buildInternal(FieldSet fieldSet, List<ReferenceTypeDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(ReferenceTypeDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<ReferenceTypeDataModel> buildCommonModelInternal(List<ReferenceTypeDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(ReferenceTypeDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	protected ReferenceTypeDataEntity applyPersistInternal(ReferenceTypeDataPersist persist, ReferenceTypeDataEntity data) {
		data.setMultipleSelect(persist.getMultipleSelect());
		data.setReferenceTypeId(persist.getReferenceTypeId());
		return data;
	}

	@Override
	protected ReferenceTypeDataPersist importExportMapDataToPersistInternal(ReferenceTypeDataImportExport data, ReferenceTypeDataPersist persist){
		ReferenceTypeEntity referenceTypeEntity = data.getReferenceTypeId() != null ? this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(data.getReferenceTypeId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id)) : null;
		if (referenceTypeEntity == null){
			if (!this.conventionService.isNullOrEmpty(data.getReferenceTypeCode())) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().codes(data.getReferenceTypeCode()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id));
			if (referenceTypeEntity == null) throw new MyValidationException(this.errors.getReferenceTypeImportNotFound().getCode(), data.getReferenceTypeCode());
		}
		persist.setMultipleSelect(data.getMultipleSelect());
		persist.setReferenceTypeId(referenceTypeEntity.getId());
		return persist;
	}

	@Override
	protected ReferenceTypeDataPersist commonModelMapDataToPersistInternal(ReferenceTypeDataModel data, ReferenceTypeDataPersist persist){
		ReferenceTypeEntity referenceTypeEntity = data.getReferenceType() != null && data.getReferenceType().getId() != null  ? this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(data.getReferenceType().getId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id)) : null;
		if (referenceTypeEntity == null){
			if (!this.conventionService.isNullOrEmpty(data.getReferenceType().getCode())) referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().codes(data.getReferenceType().getCode()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._id));
			if (referenceTypeEntity == null)  throw new MyValidationException(this.errors.getReferenceTypeImportNotFound().getCode(), data.getReferenceType().getCode());
		}
		persist.setMultipleSelect(data.getMultipleSelect());
		persist.setReferenceTypeId(referenceTypeEntity.getId());
		return persist;
	}

	@Override
	protected ReferenceTypeDataImportExport dataToImportExportXmlInternal(ReferenceTypeDataEntity data, ReferenceTypeDataImportExport xml) {
		ReferenceTypeEntity referenceTypeEntity = this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(data.getReferenceTypeId()).disableTracking().firstAs(new BaseFieldSet().ensure(ReferenceType._code).ensure(ReferenceType._name));
		if (referenceTypeEntity == null) throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{data.getReferenceTypeId(), ReferenceType.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		xml.setMultipleSelect(data.getMultipleSelect());
		xml.setReferenceTypeCode(referenceTypeEntity.getCode());
		xml.setReferenceTypeName(referenceTypeEntity.getName());
		xml.setReferenceTypeId(data.getReferenceTypeId());
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(ReferenceTypeDataEntity data) {
		return data.getMultipleSelect();
	}
}
