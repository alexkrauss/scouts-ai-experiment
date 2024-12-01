package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Group;
import name.alexkrauss.scouts.domain.model.Scout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Set;

import static name.alexkrauss.scouts.domain.model.ScoutsTestData.EMMA_SMITH;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.JOHN_DOE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DbScoutRepositoryTest {

    @Autowired
    private ScoutRepository repository;

    @Autowired
    private GroupRepository groupRepository;

    private Group testGroup1;

    @BeforeEach
    void setUp() {
        testGroup1 = groupRepository.create(Group.builder().name("Test Group 1").build());
    }

    @Test
    void createAndRetrieveScout() {
        Scout scout = JOHN_DOE.toBuilder().groups(Set.of(testGroup1)).build();
        Scout savedScout = repository.create(scout);

        assertThat(savedScout.getId()).isNotNull();
        assertThat(savedScout.getVersion()).isEqualTo(0);

        Scout retrievedScout = repository.findById(savedScout.getId()).orElseThrow();
        assertThat(retrievedScout).usingRecursiveComparison().isEqualTo(savedScout);
    }

    @Test
    void updateScout() {
        Scout scout = repository.create(JOHN_DOE);
        Scout updatedScout = repository.update(scout.toBuilder()
                .phoneNumber("555-9999")
                .build());

        assertThat(updatedScout.getPhoneNumber()).isEqualTo("555-9999");
        assertThat(updatedScout.getVersion()).isEqualTo(1);

        Scout retrievedScout = repository.findById(scout.getId()).orElseThrow();
        assertThat(retrievedScout).usingRecursiveComparison().isEqualTo(updatedScout);
    }

    @Test
    void optimisticLockingOnUpdate() {
        Scout scout = repository.create(JOHN_DOE);
        Scout concurrentScout = scout.toBuilder().build();

        repository.update(scout.toBuilder()
                .phoneNumber("555-1111")
                .build());

        assertThatThrownBy(() -> repository.update(concurrentScout.toBuilder()
                .phoneNumber("555-2222")
                .build()))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void findByName() {
        repository.create(JOHN_DOE);
        repository.create(EMMA_SMITH);

        var results = repository.findByName("John Doe");
        assertThat(results).extracting("name")
                .contains("John Doe");
    }

    @Test
    void findAllScouts() {
        int initialCount = repository.findAll().size();

        repository.create(JOHN_DOE);
        repository.create(EMMA_SMITH);

        var scouts = repository.findAll();
        assertThat(scouts).hasSize(initialCount + 2);
        assertThat(scouts).extracting("name")
                .contains("John Doe", "Emma Smith");
    }

    @Test
    void deleteScout() {
        Scout scout = repository.create(JOHN_DOE);
        repository.delete(scout.getId());
        assertThat(repository.findById(scout.getId())).isEmpty();
    }

    @Test
    void groupDeletionCascadesToScoutAssociation() {
        Scout scout = repository.create(JOHN_DOE.toBuilder().groups(Set.of(testGroup1)).build());

        // Verify association exists
        Scout retrievedScout = repository.findById(scout.getId()).orElseThrow();
        assertThat(retrievedScout.getGroups()).extracting("id").contains(testGroup1.getId());

        // Delete group
        groupRepository.delete(testGroup1.getId());

        // Verify association is gone but scout remains
        Scout scoutAfterGroupDeletion = repository.findById(scout.getId()).orElseThrow();
        assertThat(scoutAfterGroupDeletion.getGroups()).isEmpty();
    }

}
