package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.GroupManagementService;
import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Group;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupManagementServiceImpl implements GroupManagementService {

    private final GroupRepository groupRepository;

    public GroupManagementServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }
    @Override
    public Group createGroup(Group group) {

        if (groupRepository.findAll().stream()
                .anyMatch(existingGroup -> existingGroup.getName().equals(group.getName()))) {
            throw new IllegalArgumentException("Group with name '" + group.getName() + "' already exists");
        }

        return groupRepository.create(group);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Group> getGroup(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Group updateGroup(Group group) {
        return groupRepository.update(group);
    }

    @Override
    public void deleteGroup(Long id) {
        groupRepository.delete(id);
    }
}
