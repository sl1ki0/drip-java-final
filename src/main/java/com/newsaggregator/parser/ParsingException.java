package com.newsaggregator.parser;

public class ParsingException extends RuntimeException {
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
