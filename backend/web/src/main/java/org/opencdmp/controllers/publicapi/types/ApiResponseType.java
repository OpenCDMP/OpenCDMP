package org.opencdmp.controllers.publicapi.types;

/**
 * Created by ikalyvas on 3/5/2018.
 */
public enum ApiResponseType {
    JSON_RESPONSE(0), FILE_RESPONSE(1);

    private Integer value;

    private ApiResponseType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static ApiResponseType fromInteger(Integer value) {
        switch (value) {
            case 0:
                return JSON_RESPONSE;
            case 200:
                return FILE_RESPONSE;
            default:
                throw new RuntimeException("Unsupported Api Response Type Code");
        }
    }
}
