package com.ecommerce.search_service.model.response;

import lombok.*;

import java.util.Map;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private StatusEnum status;
    private T payload;
    private Map<String, String> error;
    private Map<String, Object> metadata;



    public void ok() {
        this.status = StatusEnum.SUCCESS;
    }

    public void ok(T data) {
        this.status = StatusEnum.SUCCESS;
        this.payload = data;
    }

    public void ok(T data, Map<String, Object> metadata) {
        this.status = StatusEnum.SUCCESS;
        this.payload = data;
        this.metadata = metadata;
    }

    public void error(Map<String, String> error) {
        this.status = StatusEnum.ERROR;
        this.error = error;
    }
    public void error(Map<String, String> error, Map<String, Object> metadata) {
        this.status = StatusEnum.ERROR;
        this.error = error;
        this.metadata=metadata;
    }

}
