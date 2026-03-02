package com.toannq.core.db;

import com.toannq.core.model.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractDbMetadataProvider implements DbMetadataProvider {
  private final String dbCode;
  private final DbConnection dbConnection;

  public AbstractDbMetadataProvider(String dbCode, DbConnection dbConnection) {
    this.dbCode = dbCode;
    this.dbConnection = dbConnection;
  }

  public DbConnection getDbConnection() {
    return dbConnection;
  }

  public String getDbCode() {
    return dbCode;
  }

  @Override
  public List<Table> collectMetadata(Connection connection, String schemaName) throws SQLException {
    throw new UnsupportedOperationException();
  }
}
