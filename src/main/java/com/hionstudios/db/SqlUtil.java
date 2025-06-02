package com.hionstudios.db;

import static com.hionstudios.StringUtil.coalesce;

import java.util.ArrayList;

import com.hionstudios.StringUtil;
import com.hionstudios.datagrid.DataGridParams;
import com.hionstudios.time.TimeUtil;

public class SqlUtil {
    public static SqlCriteria constructCriteria(
            DataGridParams params,
            SqlCriteria customCriteria) {
        return constructCriteria(params, customCriteria, false);
    }

    public static SqlCriteria constructCriteria(
            DataGridParams params,
            SqlCriteria customCriteria,
            boolean order) {
        return constructCriteria(params, customCriteria, order, "Id Desc", null);
    }

    public static SqlCriteria constructCriteria(
            DataGridParams params,
            SqlCriteria customCriteria,
            boolean order,
            String orderColumn,
            String group) {
        return constructCriteria(
                params.getFilterColumn(),
                params.getFilterOperator(),
                params.getFilterValue(),
                params.getLinkOperator(),
                params.getSortColumn(),
                params.getSortDirection(),
                params.getStart(),
                params.getLength(),
                customCriteria,
                order,
                orderColumn,
                group);
    }

    public static SqlCriteria constructCriteria(DataGridParams params) {
        return constructCriteria(params, null, false);
    }

    public static SqlCriteria constructCriteria(
            String[] filterColumn,
            String[] filterOperator,
            String[] filterValue,
            String linkOperator,
            String sortColumn,
            String sortDirection,
            Integer start,
            Integer length,
            SqlCriteria customCriteria,
            boolean order,
            String orderColumn) {
        return constructCriteria(
                filterColumn,
                filterOperator,
                filterValue,
                linkOperator,
                sortColumn,
                sortDirection,
                start,
                length,
                customCriteria,
                order,
                orderColumn,
                null);
    }

    public static SqlCriteria constructCriteria(
            String filterColumn,
            String filterOperator,
            String filterValue,
            String sortColumn,
            String sortDirection,
            Integer start,
            Integer length,
            SqlCriteria customCriteria,
            boolean order,
            String orderColumn) {
        return constructCriteria(
                filterColumn,
                filterOperator,
                filterValue,
                sortColumn,
                sortDirection,
                start,
                length,
                customCriteria,
                order,
                orderColumn,
                null);
    }

    public static SqlCriteria constructCriteria(
            String[] filterColumn,
            String[] filterOperator,
            String[] filterValue,
            String linkOperator,
            String sortColumn,
            String sortDirection,
            Integer start,
            Integer length,
            SqlCriteria customCriteria,
            boolean order,
            String orderColumn,
            String group) {
        ArrayList<Object> criteriaParams = new ArrayList<>();
        StringBuilder criteria = new StringBuilder(" ");
        if (filterColumn != null &&
                filterColumn.length > 0 &&
                filterColumn[0] != null &&
                !"".equals(filterColumn[0]) &&
                filterValue.length > 0 &&
                !"".equals(filterValue[0])) {
            for (int i = 0; i < filterColumn.length; i++) {
                if (filterValue[i].equals("") || filterValue[i].equals("undefined")) {
                    continue;
                } else if (i == 0) {
                    criteria.append(" Where ");
                }
                if (i != 0) {
                    criteria.append(" ");
                    criteria.append(coalesce(linkOperator, "And"));
                    criteria.append(" ");
                }
                criteria.append("(");
                criteria.append(filterColumn[i]).append(" ");
                switch (filterOperator[i]) {
                    case "equals":
                        criteria.append(" = ?");
                        criteriaParams.add(filterValue[i]);
                        break;
                    case "startsWith":
                        criteria.append(" iLike ?");
                        criteriaParams.add(filterValue[i] + "%");
                        break;
                    case "endsWith":
                        criteria.append(" iLike ?");
                        criteriaParams.add("%" + filterValue[i]);
                        break;
                    case "contains":
                        criteria.append(" iLike ?");
                        criteriaParams.add("%" + filterValue[i] + "%");
                        break;
                    case "isEmpty":
                        criteria.append(" Is Null");
                        break;
                    case "isNotEmpty":
                        criteria.append(" Is Not Null");
                        break;
                    case "is":
                        criteria.append(" = ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "not":
                        criteria.append(" != ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "after":
                        criteria.append(" > ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "onOrAfter":
                        criteria.append(" >= ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "before":
                        criteria.append(" < ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "onOrBefore":
                        criteria.append(" <= ?");
                        criteriaParams.add(parseFilterValue(filterValue[i]));
                        break;
                    case "isAnyOf":
                    case "In":
                        String firstValue = filterValue[i].split(",")[0];
                        if (!StringUtil.isNumber(firstValue)) {
                            filterValue[i] = "'" + filterValue[i].replace(",", "','") + "'";
                        }
                        criteria.append(" In (").append(filterValue[i]).append(")");
                        break;
                    case "=":
                    case "!=":
                    case ">":
                    case ">=":
                    case "<":
                    case "<=":
                        criteria.append(filterOperator[i]).append(" ?");
                        criteriaParams.add(Double.parseDouble(filterValue[i]));
                        break;
                    default:
                        criteria.append(filterOperator[i]).append(" ?");
                        criteriaParams.add(filterValue);
                }
                criteria.append(") ");
            }

            if (customCriteria != null) {
                criteria.append(" And ");
                criteria.append(customCriteria.getCriteria());
                criteriaParams.addAll(customCriteria.getParams());
                criteria.append(" ");
            }
        } else if (customCriteria != null) {
            criteria.append(" Where ");
            criteria.append(customCriteria.getCriteria());
            criteriaParams.addAll(customCriteria.getParams());
            criteria.append(" ");
        }
        if (group != null) {
            criteria.append(" ")
                    .append(group)
                    .append(" ");
        }
        if (order) {
            if (sortColumn != null
                    && !"".equals(sortColumn)
                    && !"undefined".equals(sortColumn)
                    && !"null".equals(sortColumn)
                    && !sortColumn.equals("__row_group_by_columns_group__")) {
                criteria.append(" Order By ");
                boolean hasSpace = sortColumn.contains(" ");
                if (hasSpace) {
                    criteria.append("\"");
                }
                criteria.append(sortColumn);
                if (hasSpace) {
                    criteria.append("\"");
                }
                criteria.append(" ").append(sortDirection).append(" ");
            } else {
                criteria.append(" Order By ").append(orderColumn);
            }
            if (length != null) {
                criteria.append(" Limit ").append(length);
            }
            if (start != null) {
                criteria.append(" Offset ").append(start);
            }
        }
        return new SqlCriteria(criteria.toString(), criteriaParams);
    }

    public static SqlCriteria constructCriteria(
            String filterColumn,
            String filterOperator,
            String filterValue,
            String sortColumn,
            String sortDirection,
            Integer start,
            Integer length,
            SqlCriteria customCriteria,
            boolean order,
            String orderColumn,
            String group) {
        return constructCriteria(
                new String[] { filterColumn },
                new String[] { filterOperator },
                new String[] { filterValue },
                null,
                sortColumn, sortDirection, start, length, customCriteria, order, orderColumn, group);
    }

    private static Object parseFilterValue(String filterValue) {
        if (StringUtil.isBoolean(filterValue)) {
            return Boolean.parseBoolean(filterValue);
        } else if (StringUtil.isNumber(filterValue)) {
            return Integer.parseInt(filterValue);
        } else if (StringUtil.isTime(filterValue)) {
            return TimeUtil.parse(filterValue.replace("T", " "), "yyyy-MM-dd HH:mm");
        } else if (StringUtil.isDate(filterValue)) {
            return TimeUtil.parse(filterValue, "yyyy-MM-dd");
        }
        return filterValue;
    }
}
