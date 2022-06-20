package com.boosteel.nativedb.core.support;

import java.util.List;
import java.util.Map;

public class Pager {

    protected int page = 1;
    protected int size = 10;
    protected String[] orders;
    protected int totalPages;
    protected long totalElements;
    protected List<Map<String, Object>> contents;


    public Pager() {
    }

    public Pager(Map<String, Object> map) {
        setInfo(map);
    }

    public Pager setInfo(Map<String, Object> map) {
        String v = (String)map.get("page");
        if (v != null && !v.isEmpty())
            page = Integer.parseInt(v);
        if ((v = (String)map.get("size")) != null && !v.isEmpty()) {
            size = Integer.parseInt(v);
        }
        if ((v = (String)map.get("order")) != null && !v.isEmpty()) {
            orders = v.split(",");
        }
        return this;
    }

    public Pager(int page, int size) {
        this(page, size, null);
    }

    public Pager(int page, int size, String[] orders) {
        this.page = page;
        this.size = size;
        this.orders = orders;
    }

    public Pager setPage(int page) {
        this.page = page;
        return this;
    }

    public Pager setSize(int size) {
        this.size = size;
        return this;
    }

    public String[] getOrders() {
        return orders;
    }

    public Pager setOrders(String[] orders) {
        this.orders = orders;
        return this;
    }
    public void setOrder(String order) {
        this.orders = new String[]{order};
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List getContents() {
        return contents;
    }

    public long getTotalElements() {
        return totalElements;
    }


    @Override
    public String toString() {
        return "[page : " + page + " / size : " + size + "]";
    }

    /* ************************ 프레임워크에서 사용할 메서드 ************************** */
    public String orderBy() {
        int len = orders == null ? 0 : orders.length;

        if (len > 0) {
            String[] values = new String[len];
            String val;
            for (int i = 0; i < len; i++) {
                val = orders[i].replaceAll("^<|>", "");
                values[i] = val + (orders[i].charAt(0) == '<' ? " DESC" : " ASC");
            }
            return " ORDER BY " + String.join(", ", values);
        }

        return "";
    }

    public String limit() {
        return " LIMIT " + ((page - 1) * size) + ", " + size;
    }

    public Pager setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        totalPages = (int) totalElements / size;
        if (totalElements % size != 0) totalPages++;

        return this;
    }

    public Pager setContents(List contents) {
        this.contents = contents;
        return this;
    }

}
