package com.epam.demo;

public record TraceRecord(
    String id,
    long timestamp,
    String endpoint,
    String userText,
    Long promptTokens,
    Long completionTokens,
    Long totalTokens
) {}
