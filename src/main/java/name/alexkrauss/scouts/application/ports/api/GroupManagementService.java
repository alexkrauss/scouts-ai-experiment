package name.alexkrauss.scouts.application.ports.api;

import name.alexkrauss.scouts.domain.model.Group;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing scout groups.
 * Provides CRUD operations for groups in the scout organization.
 */
public interface GroupManagementService {
    /**
     * Creates a new scout group.
     * @param group the group to create
     * @return the created group with assigned id
     */
    Group createGroup(Group group);

    /**
     * Retrieves a group by its id.
     * @param id the group id
     * @return the group if found, empty otherwise
     */
    Optional<Group> getGroup(Long id);

    /**
     * Retrieves all scout groups.
     * @return list of all groups
     */
    List<Group> getAllGroups();

    /**
     * Updates an existing group.
     * @param group the group with updated information
     * @return the updated group
     */
    Group updateGroup(Group group);

    /**
     * Deletes a group by its id.
     * @param id the id of the group to delete
     */
    void deleteGroup(Long id);
}
