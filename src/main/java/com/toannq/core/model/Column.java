package com.toannq.core.model;

import java.util.Map;

public record Column(String name,
                     String dataType,
                     Integer length,
                     String lengthUnit,
                     boolean nullable,
                     String defaultValue,
                     String comments,
                     Map<String, String> attributes) {
}