package com.toannq.providers.oracle;

import com.toannq.core.db.AbstractDbMetadataProvider;
import com.toannq.core.db.DbConnection;
import com.toannq.core.model.Column;
import com.toannq.core.model.Constraint;
import com.toannq.core.model.Index;
import com.toannq.core.model.Table;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static com.toannq.providers.oracle.OracleHelper.*;

public class OracleProvider extends AbstractDbMetadataProvider {
  private static final String TABLE_COMMENTS_QUERY = """
      select t.table_name, tc.comments
      from all_tables t
               left join all_tab_comments tc on t.owner = tc.owner and t.table_name = tc.table_name
      where t.owner = ?
      """;
  private static final String COLUMNS_QUERY = """
      select c.table_name,
             c.column_name,
             c.data_type,
             c.data_length,
             c.data_precision,
             c.data_scale,
             c.char_length,
             c.char_used,
             c.nullable,
             c.data_default,
             c.virtual_column,
             c.identity_column,
             cc.comments,
             case when pk.column_name is not null then 1 else 0 end as primary_key
      from all_tab_cols c
               left join all_col_comments cc
                         on c.owner = cc.owner and c.table_name = cc.table_name and c.column_name = cc.column_name
               left join (select acc.owner, acc.table_name, acc.column_name
                          from all_constraints ac
                                   join all_cons_columns acc
                                        on ac.owner = acc.owner
                                            and ac.constraint_name = acc.constraint_name
                          where ac.constraint_type = 'P'
                            and ac.owner = ?) pk
                         on c.owner = pk.owner and c.table_name = pk.table_name and c.column_name = pk.column_name
      where c.owner = ?
        and hidden_column = 'NO'
      order by c.column_id
      """;
  private static final String CONSTRAINTS_QUERY = """
      select ac.table_name,
             ac.constraint_name,
             ac.constraint_type,
             acc.column_list AS column_list,
             ac.status,
             ac.search_condition_vc
      from all_constraints ac
               join (select owner,
                            constraint_name,
                            table_name,
                            listagg(column_name, ',') within group (order by position) as column_list
                     from all_cons_columns
                     where owner = ?
                     group by owner, constraint_name, table_name) acc
                    on ac.owner = acc.owner and ac.constraint_name = acc.constraint_name
      where ac.owner = ?
        and acc.table_name NOT LIKE 'BIN$%'
        and ac.generated = 'USER NAME'
      """;
  private static final String INDEXES_QUERY = """
      select i.table_name,
             i.index_name,
             i.uniqueness,
             ic.column_list as column_list,
             case
                 when i.partitioned = 'NO' then 'GLOBAL (Non-Partitioned)'
                 when p.locality = 'LOCAL' then 'LOCAL'
                 when p.locality = 'GLOBAL' then 'GLOBAL (Partitioned)'
                 else 'UNDEFINED'
                 end        as index_type
      from all_indexes i
               join (select index_owner,
                            index_name,
                            LISTAGG(column_name, ', ') within group (order by column_position) as column_list
                     from all_ind_columns
                     where index_owner = ?
                     group by index_owner, index_name) ic on i.owner = ic.index_owner and i.index_name = ic.index_name
               left join all_part_indexes p
                         on i.owner = p.owner and i.index_name = p.index_name
               left join all_constraints ac
                         on i.owner = ac.owner
                             and i.index_name = ac.constraint_name
                             and ac.constraint_type in ('P', 'U')
      where i.table_owner = ?
        and i.table_name not like 'BIN$%'
        and i.generated = 'N'
        and ac.constraint_name is null
      """;

  public OracleProvider(DbConnection dbConnection) {
    super("ORACLE", dbConnection);
  }

  @Override
  public List<Table> collectMetadata(Connection connection, String schemaName) throws SQLException {
    var tableMetadata = new ArrayList<Table>();
    var tableComments = collectTable(connection, schemaName);
    var tableColumns = collectColumns(connection, schemaName);
    var tableConstraints = collectConstraints(connection, schemaName);
    var tableIndexes = collectIndexes(connection, schemaName);
    tableComments.forEach((tableName, comments) -> {
      var constraints = tableConstraints.get(tableName).stream().sorted(Comparator.comparing(Constraint::name)).toList();
      var indexes = tableIndexes.get(tableName).stream().sorted(Comparator.comparing(Index::name)).toList();
      var columns = tableColumns.get(tableName).stream().map(column -> {
        return column;
      }).toList();
      tableMetadata.add(new Table(tableName, columns, constraints, indexes, comments));
    });
    return tableMetadata.stream().sorted(Comparator.comparing(Table::name)).toList();
  }

  private Map<String, String> collectTable(Connection connection, String schemaName) throws SQLException {
    try (var ps = connection.prepareStatement(TABLE_COMMENTS_QUERY)) {
      ps.setString(1, schemaName);
      try (var rs = ps.executeQuery()) {
        var tableComments = new HashMap<String, String>();
        while (rs.next()) {
          var tableName = rs.getString("table_name");
          var comments = rs.getString("comments");
          tableComments.put(tableName, comments);
        }
        return tableComments;
      }
    }
  }

  private Map<String, List<Column>> collectColumns(Connection connection, String schemaName) throws SQLException {
    try (var ps = connection.prepareStatement(COLUMNS_QUERY)) {
      ps.setString(1, schemaName);
      ps.setString(2, schemaName);
      try (var rs = ps.executeQuery()) {
        var tableColumns = new HashMap<String, List<Column>>();
        while (rs.next()) {
          var tableName = rs.getString("table_name");
          var columnName = rs.getString("column_name");
          var dataType = rs.getString("data_type");
          var dataLength = rs.getObject("data_length", Integer.class);
          var dataPrecision = rs.getObject("data_precision", Integer.class);
          var dataScale = rs.getObject("data_scale", Integer.class);
          var charLength = rs.getObject("char_length", Integer.class);
          var charUsed = rs.getString("char_used");
          var nullable = IS_NULLABLE_VALUE.equals(rs.getString("nullable"));
          var dataDefault = rs.getString("data_default");
          var virtualColumn = IS_VIRTUAL_COLUMN_VALUE.equals(rs.getString("virtual_column"));
          var identityColumn = IS_IDENTITY_COLUMN_VALUE.equals(rs.getString("identity_column"));
          var comments = rs.getString("comments");
          var primaryKey = rs.getBoolean("primary_key");
          var map = new HashMap<String, String>();
          if (primaryKey) {
            map.put(PRIMARY_KEY, "true");
          }
          if (virtualColumn) {
            map.put(VIRTUAL, "true");
          }
          if (identityColumn) {
            dataDefault = null;
            map.put(IDENTITY, "true");
          }
          if (dataPrecision != null) {
            var att = DATA_PRECISION;
            int value = dataPrecision;
            if (dataType.contains("NUMBER")) {
              att = "integer";
              value = dataPrecision - dataScale;
            }
            map.put(att, Integer.toString(value));
          }
          if (dataScale != null) {
            var att = DATA_SCALE;
            String value = null;
            if (dataType.contains("NUMBER")) {
              att = "fraction";
              value = dataScale.toString();
            } else if (dataType.contains("TIMESTAMP")) {
              att = "time precision";
              value = toTimePrecision(dataScale);
            } else {
              value = dataScale.toString();
            }
            map.put(att, value);
          }
          var attributes = Map.copyOf(map);
          var column = isCharType(dataType)
              ? new Column(columnName, dataType, charLength, toLengthUnit(charLength, charUsed), nullable, dataDefault, comments, attributes)
              : new Column(columnName, dataType, dataLength, null, nullable, dataDefault, comments, attributes);
          tableColumns.compute(tableName, (k, columns) -> {
            if (columns == null) columns = new ArrayList<>();
            columns.add(column);
            return columns;
          });
        }
        return tableColumns;
      }
    }
  }

  private Map<String, List<Constraint>> collectConstraints(Connection connection, String schemaName) throws SQLException {
    try (var ps = connection.prepareStatement(CONSTRAINTS_QUERY)) {
      ps.setString(1, schemaName);
      ps.setString(2, schemaName);
      try (var rs = ps.executeQuery()) {
        var tableConstraints = new HashMap<String, List<Constraint>>();
        while (rs.next()) {
          var tableName = rs.getString("table_name");
          var constraintName = rs.getString("constraint_name");
          var constrainType = toConstraintType(rs.getString("constraint_type"));
          var columnList = rs.getString("column_list");
          var active = isActive(rs.getString("status"));
          var searchCondition = rs.getString("search_condition_vc");
          tableConstraints.compute(tableName, (k, constraints) -> {
            if (constraints == null) {
              constraints = new ArrayList<>();
            }
            var columns = Arrays.stream(columnList.split(",")).toList();
            var constraint = new Constraint(constraintName, constrainType, columns, searchCondition, active);
            constraints.add(constraint);
            return constraints;
          });
        }

        return tableConstraints;
      }
    }
  }

  private Map<String, List<Index>> collectIndexes(Connection connection, String schemaName) throws SQLException {
    try (var ps = connection.prepareStatement(INDEXES_QUERY)) {
      ps.setString(1, schemaName);
      ps.setString(2, schemaName);
      try (var rs = ps.executeQuery()) {
        var tableIndexes = new HashMap<String, List<Index>>();
        while (rs.next()) {
          var tableName = rs.getString("table_name");
          var indexName = rs.getString("index_name");
          var unique = isUniqueIndex(rs.getString("uniqueness"));
          var columnList = rs.getString("column_list");
          var indexType = rs.getString("index_type");
          tableIndexes.compute(tableName, (k, indexes) -> {
            if (indexes == null) {
              indexes = new ArrayList<>();
            }
            var columns = Arrays.stream(columnList.split(",")).toList();
            var index = new Index(indexName, columns, unique, indexType);
            indexes.add(index);
            return indexes;
          });
        }
        return tableIndexes;
      }
    }
  }

}
