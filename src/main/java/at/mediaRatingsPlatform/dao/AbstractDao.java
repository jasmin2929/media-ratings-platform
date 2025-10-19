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
    protected final Map<UUID, T> entities = new HashMap<>();

    // Abstract because each DAO must define how the entity ID is assigned and stored.
    public abstract T create(T entity);

    public T getById(UUID id) {
        return entities.get(id);
    }

    public List<T> getAll() {
        return new ArrayList<>(entities.values());
    }

    public void update(UUID id, T entity) {
        entities.put(id, entity);
    }

    public void delete(UUID id) {
        entities.remove(id);
    }
}
