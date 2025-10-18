package at.mediaRatingsPlatform.dao;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for DAOs providing generic CRUD operations.
 *
 * @param <T> the type of entity
 */
public abstract class AbstractDao<T> {
    // Storage for entities using their ID as the key
    protected final Map<Integer, T> entities = new HashMap<>();
    // Atomic sequence to generate unique IDs
    protected final AtomicInteger seq = new AtomicInteger(1);

    // Abstract because each DAO must define how the entity ID is assigned and stored.
    public abstract T create(T entity);

    //TODO: use UUID v7
    public T getById(int id) {
        return entities.get(id);
    }

    public List<T> getAll() {
        return new ArrayList<>(entities.values());
    }

    public void update(int id, T entity) {
        entities.put(id, entity);
    }

    public void delete(int id) {
        entities.remove(id);
    }
}
