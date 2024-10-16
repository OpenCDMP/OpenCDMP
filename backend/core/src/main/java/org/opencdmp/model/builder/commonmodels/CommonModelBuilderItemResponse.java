package org.opencdmp.model.builder.commonmodels;

public class CommonModelBuilderItemResponse<M, D>{
    private final M model;
    private final D data;

    public CommonModelBuilderItemResponse(M model, D data) {
        this.model = model;
        this.data = data;
    }

    public D getData() {
        return data;
    }

    public M getModel() {
        return model;
    }
}
