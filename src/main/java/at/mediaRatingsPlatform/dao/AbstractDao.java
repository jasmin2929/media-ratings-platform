/**
 * =============================================================
 * AbstractDao.java
 * =============================================================
 *
 * PURPOSE:
 * Generic base class for all DAO implementations. Provides common CRUD functionality using JPA's EntityManager.
 *
 * This file is part of the persistence (DAO) layer.
 *
 * The DAO layer is responsible for:
 *  - Talking directly to the database
 *  - Executing SQL queries
 *  - Returning entities to the service layer
 *
 * IMPORTANT:
 *  - DAOs contain NO business logic
 *  - They only fetch, store, update, or delete data
 *  - All decision-making happens in services
 *
 * =============================================================
 */

package at.mediaRatingsPlatform.dao;

import java.util.List;
import java.util.UUID;

public abstract class AbstractDao<T> {
    public abstract T create(T entity);
    public abstract T getById(UUID id);
    public abstract List<T> getAll();
    public abstract void update(UUID id, T entity);
    public abstract void delete(UUID id);
}
