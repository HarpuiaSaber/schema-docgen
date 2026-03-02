package com.toannq.core.render;

import com.toannq.core.model.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HtmlRender {
  public static String generate(Schema schema) {
    return """
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Segoe UI, Arial; line-height: 1.6; color: #333; max-width: 1200px; margin: auto; padding: 20px; }
                table { border-collapse: collapse; width: 100%%; margin-bottom: 30px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                th, td { border: 1px solid #ccc; padding: 10px; text-align: left; }
                th { background-color: #f8f9fa; font-weight: bold; color: #444; }
                tr:nth-child(even) { background-color: #fdfdfd; }
                h1 { color: #0056b3; border-bottom: 2px solid #0056b3; }
                h2 { color: #28a745; margin-top: 40px; }
                .comment { color: #666; font-style: italic; margin-bottom: 15px; }
                .badge { padding: 2px 6px; border-radius: 4px; font-size: 11px; background: #eee; }
            </style>
        </head>
        <body>
            <h1>Schema: %s (DB: %s)</h1>
            %s
        </body>
        </html>
        """.formatted(
        schema.name(),
        schema.db(),
        schema.tables().stream().map(HtmlRender::renderTable).collect(Collectors.joining())
    );
  }

  private static String renderTable(Table table) {
    return """
        <div id="%s">
            <h2>Table: %s</h2>
            <p class="comment">%s</p>
        
            <h3>Columns</h3>
            <table>
                <tr><th>Column</th><th>Type</th><th>Length</th><th>Nullable</th><th>Default</th><th>Comments</th><th>Notes</th></tr>
                %s
            </table>
        
            %s
            %s
        </div>
        """.formatted(
        table.name(),
        table.name(),
        Objects.toString(table.comment(), "No comment"),
        table.columns().stream().map(HtmlRender::renderColumn).collect(Collectors.joining()),
        renderConstraints(table.constraints()),
        renderIndexes(table.indexes())
    );
  }

  private static String renderColumn(Column col) {
    var lengthStr = col.length() != null ? col.length() + (col.lengthUnit() != null ? " " + col.lengthUnit() : "") : "";
    return """
        <tr>
            <td><strong>%s</strong></td>
            <td>%s</td>
            <td>%s</td>
            <td>%s</td>
            <td>%s</td>
            <td>%s</td>
            <td>%s</td>
        </tr>
        """.formatted(
        col.name(),
        col.dataType(),
        lengthStr,
        col.nullable() ? "Y" : "N",
        Objects.toString(col.defaultValue(), ""),
        Objects.toString(col.comments(), ""),
        col.attributes() == null ? "" : col.attributes().entrySet()
            .stream()
            .map(entry -> entry.getKey() + " = " + entry.getValue())
            .collect(Collectors.joining("<br>"))
    );
  }

  private static String renderConstraints(List<Constraint> constraints) {
    if (constraints == null || constraints.isEmpty()) return "";
    return """
        <h3>Constraints</h3>
        <table>
            <tr><th>Name</th><th>Type</th><th>Columns</th><th>Condition</th><th>Status</th></tr>
            %s
        </table>
        """.formatted(constraints.stream()
        .map(c -> {
          var columnsDisplay = c.columns().stream()
              .filter(Objects::nonNull)
              .collect(Collectors.joining(", "));
          var statusHtml = c.active()
              ? "<span style='color: green;'>Enabled</span>"
              : "<span style='color: red;'>Disabled</span>";
          return String.format(
              """
                  <tr>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                  </tr>
                  """,
              c.name(),
              c.type(),
              columnsDisplay,
              Objects.toString(c.expression(), ""),
              statusHtml
          );
        })
        .collect(Collectors.joining())
    );
  }

  private static String renderIndexes(List<Index> indexes) {
    if (indexes == null || indexes.isEmpty()) return "";
    return """
        <h3>Indexes</h3>
        <table>
            <tr><th>Name</th><th>Columns</th><th>Unique</th><th>Type</th></tr>
            %s
        </table>
        """.formatted(indexes.stream()
        .map(c -> {
          var columnsDisplay = c.columns().stream()
              .filter(Objects::nonNull)
              .collect(Collectors.joining(", "));
          return String.format(
              """
                  <tr>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%s</td>
                  </tr>
                  """,
              c.name(),
              columnsDisplay,
              c.unique() ? "Y" : "N",
              c.type()
          );
        })
        .collect(Collectors.joining())
    );
  }
}
