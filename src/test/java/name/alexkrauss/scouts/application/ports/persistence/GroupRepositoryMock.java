package name.alexkrauss.scouts.application.ports.persistence;

import name.alexkrauss.scouts.domain.model.Group;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class GroupRepositoryMock implements GroupRepository {

    private final HashMap<Long, Group> groups = new HashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Group create(Group group) {
        Long id = idSequence.getAndIncrement();
        Group groupWithId = group.toBuilder().id(id).version(0).build();
        groups.put(id, groupWithId);
        return groupWithId;
    }

    @Override
    public Group update(Group group) {
        if (!groups.containsKey(group.getId())) {
            throw new IllegalArgumentException("Group not found with id: " + group.getId());
        }

        Group existingGroup = groups.get(group.getId());
        if (existingGroup.getVersion() != group.getVersion()) {
            throw new OptimisticLockingFailureException("Concurrent modification detected");
        }

        Group updatedGroup = group.toBuilder()
                .version(group.getVersion() + 1)
                .build();
        groups.put(group.getId(), updatedGroup);
        return updatedGroup;
    }

    @Override
    public void delete(long id) {
        groups.remove(id);
    }

    @Override
    public Optional<Group> findById(long id) {
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public List<Group> findAll() {
        return new ArrayList<>(groups.values());
    }
}