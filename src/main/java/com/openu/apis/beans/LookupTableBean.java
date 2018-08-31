package com.openu.apis.beans;

public class LookupTableBean {
    private final String schemaName;
    private final String tableName;
    private final String keyColumn;
    private final String valueColumn;
    private final int maxCacheSize;

    public LookupTableBean(String schemaName, String tableName, String keyColumn, String valueColumn, int maxCacheSize){
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.maxCacheSize = maxCacheSize;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getKeyColumn() {
        return keyColumn;
    }

    public String getValueColumn() {
        return valueColumn;
    }

    public int getMaxCacheSize() {
        return maxCacheSize;
    }

}
