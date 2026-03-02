package com.toannq.core.db;

import com.toannq.core.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AbstractDbConnection implements DbConnection {
  @Override
  public Connection getConnection(DbConfig dbConfig) throws SQLException {
    return DriverManager.getConnection(dbConfig.url(), dbConfig.username(), dbConfig.password());
  }
}
