package org.opencdmp.model.persist.validation;

public interface StatusAware<T extends Enum<?>> {

    StatusAware<T> setStatus(T status);

}
