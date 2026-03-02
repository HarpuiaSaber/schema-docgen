package com.toannq.core.model;

import java.util.List;

public record Table(String name, List<Column> columns, List<Constraint> constraints, List<Index> indexes, String comment) {
}
