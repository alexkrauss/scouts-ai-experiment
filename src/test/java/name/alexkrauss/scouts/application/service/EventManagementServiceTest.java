package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.EventManagementService;
import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

import java.util.Set;

import static name.alexkrauss.scouts.domain.model.EventsTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the EventManagementService implementation.
 * These tests verify that the service correctly manages events and their associations with groups.
 */
@SpringBootTest
@ActiveProfiles("db-mock")
@ContextConfiguration(classes = MockedDbTestConfiguration.class)
@TestExecutionListeners(
        listeners = MockedDbTestConfiguration.DbMockResetTestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class EventManagementServiceTest {

    @Autowired
    private EventManagementService service;

    @Autowired
    private GroupRepository groupRepository;

    private Group testGroup1;
    private Group testGroup2;

    @BeforeEach
    void setUp() {
        testGroup1 = groupRepository.create(Group.builder().name("Test Group 1").build());
        testGroup2 = groupRepository.create(Group.builder().name("Test Group 2").build());
    }

    /**
     * Tests that an event can be created and then retrieved by its ID.
     * Verifies that:
     * - The created event receives an ID
     * - The retrieved event matches the input event
     */
    @Test
    void createAndRetrieveEvent() {
        Event event = SUMMER_CAMP;
        Event savedEvent = service.createEvent(event);
        
        assertThat(savedEvent.getId()).isNotNull();
        
        Event retrievedEvent = service.getEvent(savedEvent.getId()).orElseThrow();
        assertThat(retrievedEvent.getName()).isEqualTo(event.getName());
        assertThat(retrievedEvent.getStartDate()).isEqualTo(event.getStartDate());
        assertThat(retrievedEvent.getEndDate()).isEqualTo(event.getEndDate());
        assertThat(retrievedEvent.getLocation()).isEqualTo(event.getLocation());
    }

    /**
     * Tests that an event can be updated.
     * Verifies that:
     * - The updated event properties are correctly persisted
     * - The version is incremented
     */
    @Test
    void updateEvent() {
        Event event = service.createEvent(SUMMER_CAMP);
        
        Event updatedEvent = service.updateEvent(event.toBuilder()
                .name("Updated Summer Camp")
                .location("New Lake Forest Campground")
                .build());
        
        assertThat(updatedEvent.getName()).isEqualTo("Updated Summer Camp");
        assertThat(updatedEvent.getLocation()).isEqualTo("New Lake Forest Campground");
        assertThat(updatedEvent.getVersion()).isEqualTo(1);
        
        Event retrievedEvent = service.getEvent(event.getId()).orElseThrow();
        assertThat(retrievedEvent).usingRecursiveComparison().isEqualTo(updatedEvent);
    }

    /**
     * Tests that an event can be deleted.
     * Verifies that:
     * - After deletion, the event can no longer be found by its ID
     */
    @Test
    void deleteEvent() {
        Event event = service.createEvent(HIKING_TRIP);
        
        service.deleteEvent(event.getId());
        
        assertThat(service.getEvent(event.getId())).isEmpty();
    }

    /**
     * Tests retrieving all events.
     * Verifies that:
     * - All created events are returned
     */
    @Test
    void getAllEvents() {
        // Clear any existing events
        service.getAllEvents().forEach(event -> service.deleteEvent(event.getId()));
        
        Event event1 = service.createEvent(SUMMER_CAMP);
        Event event2 = service.createEvent(HIKING_TRIP);
        Event event3 = service.createEvent(CRAFT_WORKSHOP);
        
        var events = service.getAllEvents();
        
        assertThat(events).hasSize(3);
        assertThat(events).extracting("id")
                .containsExactlyInAnyOrder(event1.getId(), event2.getId(), event3.getId());
    }

    /**
     * Tests assigning a group to an event.
     * Verifies that:
     * - A group can be successfully assigned to an event
     * - The group is included in the event's participating groups
     */
    @Test
    void assignGroupToEvent() {
        Event event = service.createEvent(SUMMER_CAMP);
        
        // Initially no participating groups
        assertThat(event.getParticipatingGroups()).isEmpty();
        
        // Assign group to event
        Event updatedEvent = service.assignGroupToEvent(event.getId(), testGroup1.getId());
        
        // Verify the group was added
        assertThat(updatedEvent.getParticipatingGroups())
                .extracting("id")
                .containsExactly(testGroup1.getId());
        
        // Retrieve event and verify again
        Event retrievedEvent = service.getEvent(event.getId()).orElseThrow();
        assertThat(retrievedEvent.getParticipatingGroups())
                .extracting("id")
                .containsExactly(testGroup1.getId());
    }

    /**
     * Tests removing a group from an event.
     * Verifies that:
     * - A group can be successfully removed from an event
     * - The group is no longer included in the event's participating groups
     */
    @Test
    void removeGroupFromEvent() {
        // Create an event with a group already assigned
        Event event = service.createEvent(
                SUMMER_CAMP.toBuilder()
                        .participatingGroups(Set.of(testGroup1))
                        .build()
        );
        
        // Verify the group is initially present
        assertThat(event.getParticipatingGroups())
                .extracting("id")
                .containsExactly(testGroup1.getId());
        
        // Remove the group
        Event updatedEvent = service.removeGroupFromEvent(event.getId(), testGroup1.getId());
        
        // Verify the group was removed
        assertThat(updatedEvent.getParticipatingGroups()).isEmpty();
        
        // Retrieve event and verify again
        Event retrievedEvent = service.getEvent(event.getId()).orElseThrow();
        assertThat(retrievedEvent.getParticipatingGroups()).isEmpty();
    }

    /**
     * Tests retrieving events for a specific group.
     * Verifies that:
     * - Only events that have the group assigned are returned
     * - Events not assigned to the group are not included
     */
    @Test
    void getEventsForGroup() {
        // Create events with different group assignments
        Event event1 = service.createEvent(
                SUMMER_CAMP.toBuilder()
                        .participatingGroups(Set.of(testGroup1))
                        .build()
        );
        
        Event event2 = service.createEvent(
                HIKING_TRIP.toBuilder()
                        .participatingGroups(Set.of(testGroup1, testGroup2))
                        .build()
        );
        
        Event event3 = service.createEvent(
                CRAFT_WORKSHOP.toBuilder()
                        .participatingGroups(Set.of(testGroup2))
                        .build()
        );
        
        // Test events for group 1
        var group1Events = service.getEventsForGroup(testGroup1.getId());
        assertThat(group1Events).hasSize(2);
        assertThat(group1Events).extracting("id")
                .containsExactlyInAnyOrder(event1.getId(), event2.getId());
        
        // Test events for group 2
        var group2Events = service.getEventsForGroup(testGroup2.getId());
        assertThat(group2Events).hasSize(2);
        assertThat(group2Events).extracting("id")
                .containsExactlyInAnyOrder(event2.getId(), event3.getId());
    }

    /**
     * Tests that assigning a non-existent group to an event throws an exception.
     * Verifies that:
     * - An IllegalArgumentException is thrown when attempting to assign a non-existent group
     */
    @Test
    void assignNonExistentGroupToEvent() {
        Event event = service.createEvent(SUMMER_CAMP);
        
        assertThatThrownBy(() -> service.assignGroupToEvent(event.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }

    /**
     * Tests that assigning a group to a non-existent event throws an exception.
     * Verifies that:
     * - An IllegalArgumentException is thrown when attempting to assign to a non-existent event
     */
    @Test
    void assignGroupToNonExistentEvent() {
        assertThatThrownBy(() -> service.assignGroupToEvent(999L, testGroup1.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not exist");
    }
}