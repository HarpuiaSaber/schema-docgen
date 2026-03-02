package com.toannq.providers.oracle;

import com.toannq.core.model.ConstraintType;

final class OracleHelper {
  public static String PRIMARY_KEY = "primary key";
  public static String VIRTUAL = "virtual";
  public static String IDENTITY = "identity";
  public static String DATA_PRECISION = "precision";
  public static String DATA_SCALE = "scale";
  public static String IS_NULLABLE_VALUE = "Y";
  public static String IS_VIRTUAL_COLUMN_VALUE = "YES";
  public static String IS_IDENTITY_COLUMN_VALUE = "YES";
  private static final String[] CHAR_TYPES = new String[]{"CHAR", "VARCHAR2", "NCHAR", "NVARCHAR2"};

  public static boolean isCharType(String dataType) {
    for (var charType : CHAR_TYPES) {
      if (charType.equals(dataType)) {
        return true;
      }
    }
    return false;
  }

  public static String toLengthUnit(int charLength, String charUsed) {
    if (charLength < 0) return "unknown";
    if (charLength == 1) {
      if ("B".equals(charUsed)) return "byte";
      if ("C".equals(charUsed)) return "character";
    } else {
      if ("B".equals(charUsed)) return "bytes";
      if ("C".equals(charUsed)) return "characters";
    }
    return "unknown";
  }

  public static String toTimePrecision(Integer dataScale) {
    if (dataScale == null) return "unknown";
    if (dataScale == 9) return "nanosecond";
    if (dataScale == 6) return "millisecond";
    if (dataScale == 3) return "microsecond";
    return "unknown";
  }

  public static ConstraintType toConstraintType(String constrainType) {
    return switch (constrainType) {
      case "P" -> ConstraintType.PRIMARY_KEY;
      case "U" -> ConstraintType.UNIQUE;
      case "C" -> ConstraintType.CHECK;
      default -> null;
    };
  }

  public static boolean isActive(String status) {
    return "ENABLED".equals(status);
  }

  public static boolean isUniqueIndex(String uniqueness) {
    return "UNIQUE".equals(uniqueness);
  }

  private OracleHelper() {
    throw new UnsupportedOperationException();
  }
}
