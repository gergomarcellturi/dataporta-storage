package com.dataportal.datastorage.model.common;

import lombok.Data;

import java.time.Instant;

@Data
public class Response<T> {
    private T data;
    private Instant timestamp;
    private Integer httpCode;
    private ResponseStatus status;
    private String message;
    private String error;

    public Response(final T data, final String message) {
        this.data = data;
        this.timestamp = Instant.now();
        this.status = ResponseStatus.OK;
        this.httpCode = 200;
        this.message = message;
        this.error = null;
    }

    public Response(final T data) {
        this.data = data;
        this.timestamp = Instant.now();
        this.status = ResponseStatus.OK;
        this.httpCode = 200;
        this.error = null;
    }

    public Response() {
    }

    public Response(final String message) {
        this.data = null;
        this.timestamp = Instant.now();
        this.status = ResponseStatus.OK;
        this.httpCode = 200;
        this.message = message;
        this.error = null;
    }

    private static Response<Void> getBlankResponse() {
        return new Response<>();
    }

    public static Response<String> ok() {
        Response<String> response = new Response<>();
        response.data = null;
        response.message = "Success";
        response.httpCode = 200;
        response.status = ResponseStatus.OK;
        return response;
    }

    public static Response<Void> error() {
        Response<Void> response = Response.getBlankResponse();
        response.timestamp = Instant.now();
        response.status = ResponseStatus.ERROR;
        response.httpCode = 400;
        response.message = null;
        response.error = "Application Error";
        return response;
    }

    public static Response<Void> error(final String errorMessage) {
        Response<Void> response = Response.getBlankResponse();
        response.timestamp = Instant.now();
        response.status = ResponseStatus.ERROR;
        response.httpCode = 400;
        response.message = null;
        response.error = errorMessage;
        return response;
    }

    public static Response<Void> error(final Integer errorCode, final String errorMessage) {
        Response<Void> response = Response.getBlankResponse();
        response.timestamp = Instant.now();
        response.status = ResponseStatus.ERROR;
        response.httpCode = errorCode;
        response.message = null;
        response.error = errorMessage;
        return response;
    }
}
