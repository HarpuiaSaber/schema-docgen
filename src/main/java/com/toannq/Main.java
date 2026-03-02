package com.toannq;

import com.toannq.core.config.DbConfig;
import com.toannq.core.model.Schema;
import com.toannq.core.render.HtmlRender;
import com.toannq.providers.oracle.OracleDbConnection;
import com.toannq.providers.oracle.OracleProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;

public class Main {
  public static void main(String[] args) {
    var schemaName = "DBTEST";
    var dbConfig = new DbConfig("jdbc:oracle:thin:@//localhost:1521/FREEPDB1", "dbtest", "dbtest");
    var metadataProvider = new OracleProvider(new OracleDbConnection());
    var start = System.currentTimeMillis();
    try (var connection = metadataProvider.getDbConnection().getConnection(dbConfig)) {
      var tables = metadataProvider.collectMetadata(connection, schemaName);
      var schema = new Schema(metadataProvider.getDbCode(), schemaName, tables);
      var generated = HtmlRender.generate(schema);
      try (var fileChannel = FileChannel.open(Path.of("/home/toannq/Downloads/" + schemaName + "_docs.html"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
        var buffer = ByteBuffer.wrap(generated.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {
          fileChannel.write(buffer);
        }
        fileChannel.force(true);
      }
      var end = System.currentTimeMillis();
      System.out.println((end - start));
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
