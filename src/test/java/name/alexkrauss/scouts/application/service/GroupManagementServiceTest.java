package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.GroupManagementService;
import name.alexkrauss.scouts.domain.model.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("db-mock")
@ContextConfiguration(classes = MockedDbTestConfiguration.class)
@TestExecutionListeners(
        listeners = MockedDbTestConfiguration.DbMockResetTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class GroupManagementServiceTest {

    @Autowired
    private GroupManagementService service;


    @Test
    void createAndRetrieveGroup() {
        var group = Group.builder().name("Foo").build();
        var id = service.createGroup(group).getId();
        assertThat(id).isNotNull();

        var retrievedGroup = service.getGroup(id).orElseThrow();
        assertThat(retrievedGroup.getName()).isEqualTo("Foo");
    }

    @Test
    void createAndDeleteGroup() {
        var group = Group.builder().name("Foo").build();

        var id = service.createGroup(group).getId();
        assertThat(id).isNotNull();
        service.deleteGroup(id);
        assertThat(service.getGroup(id)).isEmpty();
    }

    @Test
    void createDuplicateGroup() {
        var group = Group.builder().name("Foo").build();
        var id = service.createGroup(group).getId();
        assertThat(id).isNotNull();
        assertThatThrownBy(() -> service.createGroup(group))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Group with name 'Foo' already exists");
    }

}
