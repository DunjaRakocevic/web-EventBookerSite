package rs.raf.eventbooker.dto;

import java.util.List;

public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int limit;
    private long total;
    private int totalPages;

    public PagedResponse() {}

    public PagedResponse(List<T> items, int page, int limit, long total) {
        this.items = items;
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = (int) Math.max(1, Math.ceil((double) total / Math.max(1, limit)));
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }

    public void setItems(List<T> items) { this.items = items; }
    public void setPage(int page) { this.page = page; }
    public void setLimit(int limit) { this.limit = limit; }
    public void setTotal(long total) { this.total = total; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
