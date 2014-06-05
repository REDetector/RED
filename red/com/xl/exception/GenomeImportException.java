package com.xl.exception;

public class GenomeImportException extends RuntimeException {

    public GenomeImportException(String message) {
        super(message);
    }

    public GenomeImportException(String message, Throwable e) {
        super(message, e);
    }
}