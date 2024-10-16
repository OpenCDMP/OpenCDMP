package org.opencdmp.controllers.publicapi.types;


public class ResponseItem<T> {
    private Integer statusCode;
    private Integer responseType = ApiResponseType.JSON_RESPONSE.getValue();
    private String message;
    private T payload;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public Integer getResponseType() {
        return responseType;
    }

    public void setResponseType(Integer responseType) {
        this.responseType = responseType;
    }

    public ResponseItem<T> status(ApiMessageCode statusCode) {
        this.statusCode = statusCode.getValue();
        return this;
    }

    public ResponseItem<T> message(String message) {
        this.message = message;
        return this;
    }

    public ResponseItem<T> payload(T payload) {
        this.payload = payload;
        return this;
    }

    public ResponseItem<T> responseType(ApiResponseType apiResponseType) {
        this.responseType = apiResponseType.getValue();
        return this;
    }

}
