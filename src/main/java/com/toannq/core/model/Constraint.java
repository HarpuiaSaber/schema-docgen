package com.toannq.core.model;

import java.util.List;

public record Constraint(String name, ConstraintType type, List<String> columns, String expression, boolean active) {
}
