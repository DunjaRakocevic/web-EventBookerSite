package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category create(String name, String description);
    Optional<Category> findById(long id);
    Optional<Category> findByName(String name);
    Category update(long id, String name, String description);
    boolean delete(long id);                // true = deleted
    boolean hasEvents(long categoryId);     // zabrana brisanja ako je true

    long count();
    List<Category> list(int page, int limit); // 1-based page

    // NOVO: filter po nazivu (LIKE) + total za EMS pretragu
    List<Category> listFiltered(String q, int page, int limit);
    long countFiltered(String q);
}
