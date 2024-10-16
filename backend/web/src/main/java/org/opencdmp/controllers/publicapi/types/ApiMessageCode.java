package org.opencdmp.controllers.publicapi.types;

public enum ApiMessageCode {
    NO_MESSAGE(0), SUCCESS_MESSAGE(200), WARN_MESSAGE(300), ERROR_MESSAGE(400),
    DEFAULT_ERROR_MESSAGE(444), VALIDATION_MESSAGE(445),NULL_EMAIL(480), UNSUCCESS_DELETE(674);

    private Integer value;

    private ApiMessageCode(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static ApiMessageCode fromInteger(Integer value) {
        switch (value) {
            case 0:
                return NO_MESSAGE;
            case 200:
                return SUCCESS_MESSAGE;
            case 300:
                return WARN_MESSAGE;
            case 400:
                return ERROR_MESSAGE;
            case 444:
                return DEFAULT_ERROR_MESSAGE;
            case 480:
                return NULL_EMAIL;
            case 674:
                return UNSUCCESS_DELETE;
            default:
                throw new RuntimeException("Unsupported Api Message Code");
        }
    }
}
