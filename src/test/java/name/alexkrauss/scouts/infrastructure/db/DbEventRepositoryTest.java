package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static name.alexkrauss.scouts.domain.model.EventsTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for EventRepository implementation.
 * This test suite verifies that the repository correctly handles CRUD operations
 * and more complex queries such as filtering events by group.
 */
@SpringBootTest
class DbEventRepositoryTest {

    @Autowired
    private EventRepository repository;

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
     * - The version is set to 0
     * - The retrieved event matches the saved event
     */
    @Test
    void createAndRetrieveEvent() {
        Event event = SUMMER_CAMP;
        Event savedEvent = repository.create(event);

        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getVersion()).isEqualTo(0);

        Event retrievedEvent = repository.findById(savedEvent.getId()).orElseThrow();
        assertThat(retrievedEvent).usingRecursiveComparison().isEqualTo(savedEvent);
    }

    /**
     * Tests that an existing event can be updated.
     * Verifies that:
     * - The updated fields are properly persisted
     * - The version is incremented
     * - The retrieved event after update matches the updated event
     */
    @Test
    void updateEvent() {
        Event event = repository.create(SUMMER_CAMP);
        Event updatedEvent = repository.update(event.toBuilder()
                .location("New Lake Forest Campground")
                .build());

        assertThat(updatedEvent.getLocation()).isEqualTo("New Lake Forest Campground");
        assertThat(updatedEvent.getVersion()).isEqualTo(1);

        Event retrievedEvent = repository.findById(event.getId()).orElseThrow();
        assertThat(retrievedEvent).usingRecursiveComparison().isEqualTo(updatedEvent);
    }

    /**
     * Tests that optimistic locking works correctly when updating events.
     * Verifies that:
     * - Trying to update an event with a stale version throws an OptimisticLockingFailureException
     */
    @Test
    void optimisticLockingOnUpdate() {
        Event event = repository.create(SUMMER_CAMP);
        Event concurrentEvent = event.toBuilder().build();

        repository.update(event.toBuilder()
                .location("Updated Location")
                .build());

        assertThatThrownBy(() -> repository.update(concurrentEvent.toBuilder()
                .location("Concurrent Location")
                .build()))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    /**
     * Tests that findAll returns all events.
     * Verifies that:
     * - The returned list contains all created events
     * - The size of the list increases when new events are added
     */
    @Test
    void findAllEvents() {
        int initialCount = repository.findAll().size();

        repository.create(SUMMER_CAMP);
        repository.create(HIKING_TRIP);

        var events = repository.findAll();
        assertThat(events).hasSize(initialCount + 2);
        assertThat(events).extracting("name")
                .contains("Summer Camp 2025", "Mountain Hiking Trip");
    }

    /**
     * Tests that an event can be deleted.
     * Verifies that:
     * - After deletion, the event can no longer be found by its ID
     */
    @Test
    void deleteEvent() {
        Event event = repository.create(HIKING_TRIP);
        repository.delete(event.getId());
        assertThat(repository.findById(event.getId())).isEmpty();
    }

    /**
     * Tests that events can be found by group ID.
     * Verifies that:
     * - Events explicitly assigned to a group are returned when querying by that group ID
     * - Events not assigned to a group are not returned
     */
    @Test
    void findEventsByGroupId() {
        // Create events with specific group assignments
        Event event1 = repository.create(SUMMER_CAMP.toBuilder()
                .participatingGroups(Set.of(testGroup1))
                .build());

        Event event2 = repository.create(HIKING_TRIP.toBuilder()
                .participatingGroups(Set.of(testGroup1, testGroup2))
                .build());

        Event event3 = repository.create(CRAFT_WORKSHOP.toBuilder()
                .participatingGroups(Set.of(testGroup2))
                .build());

        // Create an event with a different group assignment
        Event event4 = repository.create(Event.builder()
                .name("Special Event")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .meetingPoint("City Center")
                .location("Scout Hall")
                .cost("Free")
                .additionalInfo("")
                .participatingGroups(Set.of(testGroup2))
                .build());

        // Test finding events for group 1
        List<Event> group1Events = repository.findEventsByGroupId(testGroup1.getId());
        assertThat(group1Events).extracting("id")
                .containsExactlyInAnyOrder(event1.getId(), event2.getId())
                .doesNotContain(event3.getId(), event4.getId());

        // Test finding events for group 2
        List<Event> group2Events = repository.findEventsByGroupId(testGroup2.getId());
        assertThat(group2Events).extracting("id")
                .containsExactlyInAnyOrder(event2.getId(), event3.getId(), event4.getId())
                .doesNotContain(event1.getId());
    }

    /**
     * Tests that groups can be added to an event.
     * Verifies that:
     * - A newly created event has no participating groups
     * - After adding a group, the event has the group in its participatingGroups set
     * - The association is correctly persisted and can be retrieved
     */
    @Test
    void addGroupToEvent() {
        Event event = repository.create(SUMMER_CAMP);
        
        // Initially no participating groups
        assertThat(event.getParticipatingGroups()).isEmpty();
        
        // Add a group
        Event updatedEvent = repository.update(event.toBuilder()
                .participatingGroups(Set.of(testGroup1))
                .build());
        
        Event retrievedEvent = repository.findById(updatedEvent.getId()).orElseThrow();
        assertThat(retrievedEvent.getParticipatingGroups())
                .extracting("id")
                .containsExactly(testGroup1.getId());
    }
}