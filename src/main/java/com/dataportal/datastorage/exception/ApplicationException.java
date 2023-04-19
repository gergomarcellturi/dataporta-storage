package com.dataportal.datastorage.exception;


import com.dataportal.datastorage.model.common.Response;

public class ApplicationException extends RuntimeException {
    private final Response<Void> response;

    public ApplicationException() {
        this.response = Response.error();
    }

    public ApplicationException(Response<Void> response) {
        this.response = response;
    }

    public ApplicationException(final String errorMessage) {
        this.response = Response.error(errorMessage);
    }

    public ApplicationException(final Integer errorCode, final String errorMessage) {
        this.response = Response.error(errorCode, errorMessage);
    }
}
