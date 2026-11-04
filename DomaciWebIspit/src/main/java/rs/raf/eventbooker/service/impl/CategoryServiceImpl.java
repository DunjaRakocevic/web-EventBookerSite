package rs.raf.eventbooker.service.impl;

import rs.raf.eventbooker.dto.PagedResponse;
import rs.raf.eventbooker.model.Category;
import rs.raf.eventbooker.repository.CategoryRepository;
import rs.raf.eventbooker.service.CategoryService;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Category> list(int limit, int offset) {
        int l = Math.max(1, limit);
        int page = (offset <= 0) ? 1 : (offset / l) + 1; // mapiramo offset u pages
        return repo.list(page, l);
    }

    @Override
    public PagedResponse<Category> listPaged(String q, int page, int limit) {
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        String term = (q == null) ? "" : q.trim();

        List<Category> items;
        long total;
        if (term.isEmpty()) {
            items = repo.list(p, l);
            total = repo.count();
        } else {
            items = repo.listFiltered(term, p, l);
            total = repo.countFiltered(term);
        }
        return new PagedResponse<>(items, p, l, total);
    }

    @Override
    public Category create(String name, String description) {
        validateNameAndDesc(name, description);
        if (repo.findByName(name.trim()).isPresent()) {
            throw new IllegalStateException("category_name_taken");
        }
        return repo.create(name.trim(), description.trim());
    }

    @Override
    public Category update(Long id, String name, String description) {
        if (id == null) throw new IllegalArgumentException("id_required");
        validateNameAndDesc(name, description);

        repo.findById(id).orElseThrow(() -> new IllegalArgumentException("not_found"));

        var byName = repo.findByName(name.trim());
        if (byName.isPresent() && !byName.get().getId().equals(id)) {
            throw new IllegalStateException("category_name_taken");
        }

        return repo.update(id, name.trim(), description.trim());
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id_required");
        if (repo.hasEvents(id)) throw new IllegalStateException("category_has_events");
        return repo.delete(id);
    }

    private void validateNameAndDesc(String name, String description) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name_required");
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("description_required");

        String n = name.trim();
        if (n.length() < 2)
            throw new IllegalArgumentException("name_too_short");
        if (n.length() > 200)
            throw new IllegalArgumentException("name_too_long");

        if (description.trim().length() > 1000)
            throw new IllegalArgumentException("description_too_long");
    }
}
