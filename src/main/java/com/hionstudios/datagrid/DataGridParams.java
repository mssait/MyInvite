package com.hionstudios.datagrid;

public class DataGridParams {
    public String[] filterColumn = {};
    public String[] filterOperator = {};
    public String[] filterValue = {};
    public String linkOperator = "And";
    public String sortColumn;
    public String sortDirection;
    public String search;
    public int start = 0;
    private int length = 10;

    public String[] getFilterColumn() {
        return filterColumn;
    }

    public String[] getFilterOperator() {
        return filterOperator;
    }

    public String[] getFilterValue() {
        return filterValue;
    }

    public int getLength() {
        return length;
    }

    public String getLinkOperator() {
        return linkOperator;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public String getSearch() {
        return search;
    }

    public int getStart() {
        return start;
    }

    public void setFilterColumn(String[] filterColumn) {
        this.filterColumn = filterColumn;
    }

    public void setFilterOperator(String[] filterOperator) {
        this.filterOperator = filterOperator;
    }

    public void setFilterValue(String[] filterValue) {
        this.filterValue = filterValue;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setLinkOperator(String linkOperator) {
        this.linkOperator = linkOperator;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
