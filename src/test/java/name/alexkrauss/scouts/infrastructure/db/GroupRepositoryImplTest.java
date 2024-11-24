package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class GroupRepositoryImplTest {

    @Autowired
    private GroupRepository repository;

    @Test
    void createAndRetrieveGroup() {
        Group group = Group.builder()
                .name("Test Group")
                .build();

        Group savedGroup = repository.create(group);

        assertThat(savedGroup.getId()).isNotNull();
        assertThat(savedGroup.getName()).isEqualTo("Test Group");
        assertThat(savedGroup.getVersion()).isEqualTo(0);

        Group retrievedGroup = repository.findById(savedGroup.getId()).orElseThrow();
        assertThat(retrievedGroup).usingRecursiveComparison().isEqualTo(savedGroup);
    }

    @Test
    void updateGroup() {
        Group group = repository.create(Group.builder()
                .name("Original Name")
                .build());

        Group updatedGroup = repository.update(group.toBuilder()
                .name("Updated Name")
                .build());

        assertThat(updatedGroup.getName()).isEqualTo("Updated Name");
        assertThat(updatedGroup.getVersion()).isEqualTo(1);

        Group retrievedGroup = repository.findById(group.getId()).orElseThrow();
        assertThat(retrievedGroup).usingRecursiveComparison().isEqualTo(updatedGroup);
    }

    @Test
    void optimisticLockingOnUpdate() {
        Group group = repository.create(Group.builder()
                .name("Test Group")
                .build());

        Group concurrentGroup = group.toBuilder().build();

        repository.update(group.toBuilder()
                .name("First Update")
                .build());

        assertThatThrownBy(() -> repository.update(concurrentGroup.toBuilder()
                .name("Concurrent Update")
                .build()))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void deleteGroup() {
        Group group = repository.create(Group.builder()
                .name("To Delete")
                .build());

        repository.delete(group.getId());

        assertThat(repository.findById(group.getId())).isEmpty();
    }

    @Test
    void findAllGroups() {
    int initialCount = repository.findAll().size();

        repository.create(Group.builder().name("Group 1").build());
        repository.create(Group.builder().name("Group 2").build());

        var groups = repository.findAll();

        assertThat(groups).hasSize(initialCount + 2);
        assertThat(groups).extracting("name")
                .contains("Group 1", "Group 2");
    }

    @Test
    void findByIdNonExistent() {
        assertThat(repository.findById(999L)).isEmpty();
    }
}