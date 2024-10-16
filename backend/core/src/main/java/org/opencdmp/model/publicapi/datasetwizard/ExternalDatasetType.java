package org.opencdmp.model.publicapi.datasetwizard;

public enum ExternalDatasetType{

    SOURCE(0), OUTPUT(1) ;
    private Integer value;

    private ExternalDatasetType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static ExternalDatasetType fromInteger(Integer value) {
        switch (value) {
            case 0:
                return SOURCE;
            case 1:
                return OUTPUT;
            default:
                throw new RuntimeException("Unsupported Api Message Code");
        }
    }
}