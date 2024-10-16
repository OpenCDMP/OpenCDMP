package org.opencdmp.controllers.publicapi.query;

import org.opencdmp.controllers.publicapi.QueryableList;
import org.opencdmp.controllers.publicapi.query.definition.TableQuery;
import org.opencdmp.controllers.publicapi.query.definition.helpers.ColumnOrderings;
import org.opencdmp.controllers.publicapi.query.definition.helpers.Ordering;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ikalyvas on 3/21/2018.
 */
public class PaginationService {
    public static <T> QueryableList<T> applyPaging(QueryableList<T> items, TableQuery tableRequest) {
        if (tableRequest.getOrderings() != null) applyOrder(items, tableRequest);
        if (tableRequest.getLength() != null) items.take(tableRequest.getLength());
        if (tableRequest.getOffset() != null) items.skip(tableRequest.getOffset());
        if (tableRequest.getSelection() != null && tableRequest.getSelection().getFields() != null && tableRequest.getSelection().getFields().length > 0)
            items.withFields(Arrays.asList(tableRequest.getSelection().getFields()));
        return items;
    }
    public static <T> void applyOrder(QueryableList<T> items, TableQuery tableRequest)  {
        applyOrder(items, tableRequest.getOrderings());
    }


    public static <T> void applyOrder(QueryableList<T> items, ColumnOrderings columnOrderings)  {
        for (Ordering ordering : columnOrderings.getFieldOrderings()) {
            if (ordering.getOrderByType() == Ordering.OrderByType.ASC)
                applyAscOrder(items, ordering);
            if (ordering.getOrderByType() == Ordering.OrderByType.DESC) {
                applyDescOrder(items, ordering);
            }
        }
        return;
    }

    private static <T> void applyAscOrder(QueryableList<T> items, Ordering ordering) {
        if (ordering.getColumnType() == Ordering.ColumnType.COUNT) {
            items.orderBy((builder, root) -> builder.asc(builder.size(root.<Collection>get(ordering.getFieldName()))));
        } else if (ordering.getColumnType() == Ordering.ColumnType.JOIN_COLUMN) {
            String[] fields = ordering.getFieldName().split(":");
            items.orderBy((builder, root) -> builder.asc(root.get(fields[0]).get(fields[1])));
        } else {
            items.orderBy((builder, root) -> builder.asc(root.get(ordering.getFieldName())));
        }
    }

    private static <T> void applyDescOrder(QueryableList<T> items, Ordering ordering) {
        if (ordering.getColumnType() == Ordering.ColumnType.COUNT) {
            items.orderBy((builder, root) -> builder.desc(builder.size(root.<Collection>get(ordering.getFieldName()))));
        } else if (ordering.getColumnType() == Ordering.ColumnType.JOIN_COLUMN) {
            String[] fields = ordering.getFieldName().split(":");
            items.orderBy((builder, root) -> builder.desc(root.get(fields[0]).get(fields[1])));
        } else {
            items.orderBy((builder, root) -> builder.desc(root.get(ordering.getFieldName())));
        }
    }
}
