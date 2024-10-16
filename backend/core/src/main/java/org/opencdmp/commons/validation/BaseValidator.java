package org.opencdmp.commons.validation;

import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.validation.AbstractValidator;
import gr.cite.tools.validation.ValidationResult;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.*;

public abstract class BaseValidator<T> extends AbstractValidator<T> {
	protected final ConventionService conventionService;
	protected final ErrorThesaurusProperties errors;

	protected BaseValidator(ConventionService conventionService, ErrorThesaurusProperties errors) {
		this.conventionService = conventionService;
		this.errors = errors;
	}
	
	@Override
	public void validateForce(Object target) {
		this.validate(target);
		ValidationResult result = result();
		if (!result.isValid()) {
			List<Map.Entry<String, List<String>>> errorsMap = this.flattenValidationResult();
			throw new MyValidationException(this.errors.getModelValidation().getCode(), errorsMap);
		}
	}

	protected Boolean isValidGuid(UUID guid) {
		return this.conventionService.isValidGuid(guid);
	}
	protected Boolean isValidEmail(String value) {
		return EmailValidator.getInstance().isValid(value);
	}

	protected Boolean isValidHash(String hash) {
		return this.conventionService.isValidHash(hash);
	}

	protected Boolean isEmpty(String value) {
		return this.conventionService.isNullOrEmpty(value);
	}
	protected Boolean isListNullOrEmpty(List<?> value) {
		return this.conventionService.isListNullOrEmpty(value);
	}
	protected Boolean isNull(Object value) {
		return value == null;
	}

	protected boolean isBoolean(String value) {
		return value != null && Arrays.stream(new String[]{"true", "false"})
				.anyMatch(b -> b.equalsIgnoreCase(value));
	}

	protected boolean isUUID(String value) {
		try {
			UUID.fromString(value);
			return true;
		} catch (Exception e){
			return false;
		}
	}
	
	protected Boolean isNull(Collection<?> value) {
		return value == null;
	}

	protected Boolean lessEqualLength(String value, int size) {
		return value.length() <= size;
	}

	protected Boolean lessEqual(Integer value, int target) {
		return value <= target;
	}
}

