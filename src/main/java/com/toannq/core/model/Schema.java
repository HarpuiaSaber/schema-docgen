package com.toannq.core.model;

import java.util.List;

public record Schema(String db, String name, List<Table> tables) {
}
