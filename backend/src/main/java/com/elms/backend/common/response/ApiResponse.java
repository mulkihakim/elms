package com.elms.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ApiError(code, message))
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> fields) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ApiError(code, message, fields, fields))
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiError {
        private String code;
        private String message;
        private Map<String, String> details;
        private Map<String, String> fields;

        public ApiError(String code, String message) {
            this(code, message, null, null);
        }

        public ApiError(String code, String message, Map<String, String> details) {
            this(code, message, details, details);
        }
    }
}

