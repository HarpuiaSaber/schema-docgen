package com.toannq.core.db;

import com.toannq.core.model.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DbMetadataProvider {
  List<Table> collectMetadata(Connection connection, String schemaName) throws SQLException;
}
