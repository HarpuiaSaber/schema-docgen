package com.toannq.core.model;

import java.util.List;

public record Index(String name, List<String> columns, boolean unique, String type) {
}
