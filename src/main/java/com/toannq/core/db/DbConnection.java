package com.toannq.core.db;

import com.toannq.core.config.DbConfig;

import java.sql.Connection;
import java.sql.SQLException;

public interface DbConnection {
  Connection getConnection(DbConfig dbConfig) throws SQLException;
}
