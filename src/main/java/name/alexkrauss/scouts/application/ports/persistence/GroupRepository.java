package name.alexkrauss.scouts.application.ports.persistence;

import name.alexkrauss.scouts.domain.model.Group;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing scout groups in the persistence layer.
 */
public interface GroupRepository {

    /**
     * Creates a new group record.
     *
     * @param group the group to create
     * @return the created group with id set
     */
    Group create(Group group);

    /**
     * Updates an existing group record.
     *
     * @param group the group to update
     * @return the updated group
     * @throws OptimisticLockingFailureException if the version number has changed
     */
    Group update(Group group);

    /**
     * Deletes a group by its id.
     *
     * @param id the id of the group to delete
     */
    void delete(long id);

    /**
     * Finds a group by its id.
     *
     * @param id the id of the group to find
     * @return the group if found, empty optional otherwise
     */
    Optional<Group> findById(long id);

    /**
     * Returns all groups.
     *
     * @return list of all groups
     */
    List<Group> findAll();
}
