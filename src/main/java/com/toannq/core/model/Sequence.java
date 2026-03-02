package com.toannq.core.model;

public record Sequence(String name, long startWith, long incrementBy, Long minValue, Long maxValue, boolean cycle) {
}
