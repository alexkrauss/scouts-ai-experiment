package name.alexkrauss.scouts.application.ports.persistence;

import name.alexkrauss.scouts.domain.model.Scout;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Scout entities in the persistence layer.
 * Follows CRUD conventions with optimistic locking using the version field.
 */
public interface ScoutRepository {
    /**
     * Creates a new scout record.
     *
     * @param scout The scout to create
     * @return The created scout with assigned ID
     */
    Scout create(Scout scout);

    /**
     * Updates an existing scout record.
     *
     * @param scout The scout to update
     * @return The updated scout
     * @throws OptimisticLockingFailureException if version conflict occurs
     */
    Scout update(Scout scout) throws OptimisticLockingFailureException;

    /**
     * Deletes a scout by its ID.
     *
     * @param id The ID of the scout to delete
     */
    void delete(Long id);

    /**
     * Finds a scout by its ID.
     *
     * @param id The ID of the scout
     * @return Optional containing the scout if found
     */
    Optional<Scout> findById(Long id);

    /**
     * Retrieves all scouts.
     *
     * @return List of all scouts
     */
    List<Scout> findAll();

    /**
     * Finds scouts by name.
     *
     * @param name The name to search for
     * @return List of scouts with matching name
     */
    List<Scout> findByName(String name);
}
