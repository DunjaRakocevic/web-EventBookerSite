package rs.raf.eventbooker.service;

import rs.raf.eventbooker.dto.PagedResponse;
import rs.raf.eventbooker.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> list(int limit, int offset);
    Category create(String name, String description);
    Category update(Long id, String name, String description);
    boolean delete(Long id);


    PagedResponse<Category> listPaged(String q, int page, int limit);
}
