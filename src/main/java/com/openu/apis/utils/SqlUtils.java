package com.openu.apis.utils;

import java.util.List;

public class SqlUtils {
    @SafeVarargs
    public static boolean isWhereClause(List<String>... filters) {
        for (List<String> filter : filters) {
            if (filter != null && !filter.isEmpty())
                return true;
        }
        return false;
    }
}
