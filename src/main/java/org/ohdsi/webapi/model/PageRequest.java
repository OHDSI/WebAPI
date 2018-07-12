package org.ohdsi.webapi.model;

import java.util.List;

public class PageRequest {
  private int draw;
  private int length;
  private int start;
  private Order[] order;
  private Search search;
  private Column[] columns;
  private List<Filter> filters;

  public int getDraw() {
    return draw;
  }

  public void setDraw(int draw) {
    this.draw = draw;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public Order[] getOrder() {
    return order;
  }

  public void setOrder(Order[] order) {
    this.order = order;
  }

  public Search getSearch() {
    return search;
  }

  public void setSearch(Search search) {
    this.search = search;
  }

  public Column[] getColumns() {
    return columns;
  }

  public void setColumns(Column[] columns) {
    this.columns = columns;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public void setFilters(List<Filter> filters) {
    this.filters = filters;
  }

  public static class Order {
    private int column;
    private String dir;

    public int getColumn() {
      return column;
    }

    public void setColumn(int column) {
      this.column = column;
    }

    public String getDir() {
      return dir;
    }

    public void setDir(String dir) {
      this.dir = dir;
    }
  }

  public static class Search {
    private boolean regex;
    private String value;

    public boolean isRegex() {
      return regex;
    }

    public void setRegex(boolean regex) {
      this.regex = regex;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }

  public static class Column {
    private String data;
    private String name;
    private boolean orderable;
    private boolean searchable;
    private Search search;

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public boolean isOrderable() {
      return orderable;
    }

    public void setOrderable(boolean orderable) {
      this.orderable = orderable;
    }

    public boolean isSearchable() {
      return searchable;
    }

    public void setSearchable(boolean searchable) {
      this.searchable = searchable;
    }

    public Search getSearch() {
      return search;
    }

    public void setSearch(Search search) {
      this.search = search;
    }
  }

  public static class Filter {
    private String columnName;
    private boolean computed;

    public String getColumnName() {
      return columnName;
    }

    public void setColumnName(String columnName) {
      this.columnName = columnName;
    }

    public boolean isComputed() {
      return computed;
    }

    public void setComputed(boolean computed) {
      this.computed = computed;
    }

    public List<String> getValues() {
      return values;
    }

    public void setValues(List<String> values) {
      this.values = values;
    }

    private List<String> values;
  }
}
