package name.alexkrauss.scouts.infrastructure.db;

import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.RegistrationRepository;
import name.alexkrauss.scouts.application.ports.persistence.ScoutRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Registration;
import name.alexkrauss.scouts.domain.model.RegistrationStatus;
import name.alexkrauss.scouts.domain.model.Scout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;

import static name.alexkrauss.scouts.domain.model.EventsTestData.*;
import static name.alexkrauss.scouts.domain.model.RegistrationsTestData.*;
import static name.alexkrauss.scouts.domain.model.ScoutsTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for RegistrationRepository implementation.
 * This test suite verifies that the repository correctly handles CRUD operations
 * for registrations, as well as the relationships between registrations, scouts, and events.
 */
@SpringBootTest
class DbRegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository repository;

    @Autowired
    private ScoutRepository scoutRepository;

    @Autowired
    private EventRepository eventRepository;

    private Scout savedJohn;
    private Scout savedEmma;
    private Event savedSummerCamp;
    private Event savedHikingTrip;

    @BeforeEach
    void setUp() {
        savedJohn = scoutRepository.create(JOHN_DOE);
        savedEmma = scoutRepository.create(EMMA_SMITH);
        savedSummerCamp = eventRepository.create(SUMMER_CAMP);
        savedHikingTrip = eventRepository.create(HIKING_TRIP);
    }

    /**
     * Tests that a registration can be created and then retrieved by its ID.
     * Verifies that:
     * - The created registration receives an ID
     * - The version is set to 0
     * - The retrieved registration matches the saved registration
     */
    @Test
    void createAndRetrieveRegistration() {
        Registration registration = Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build();

        Registration savedRegistration = repository.create(registration);

        assertThat(savedRegistration.getId()).isNotNull();
        assertThat(savedRegistration.getVersion()).isEqualTo(0);

        Registration retrievedRegistration = repository.findById(savedRegistration.getId()).orElseThrow();
        assertThat(retrievedRegistration).usingRecursiveComparison().isEqualTo(savedRegistration);
    }

    /**
     * Tests that an existing registration can be updated.
     * Verifies that:
     * - The updated fields (status and note) are properly persisted
     * - The version is incremented
     * - The retrieved registration after update matches the updated registration
     */
    @Test
    void updateRegistration() {
        Registration registration = Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Original note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build();

        Registration savedRegistration = repository.create(registration);
        Registration updatedRegistration = repository.update(savedRegistration.toBuilder()
                .status(RegistrationStatus.CONFIRMED)
                .note("Updated note")
                .build());

        assertThat(updatedRegistration.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(updatedRegistration.getNote()).isEqualTo("Updated note");
        assertThat(updatedRegistration.getVersion()).isEqualTo(1);

        Registration retrievedRegistration = repository.findById(savedRegistration.getId()).orElseThrow();
        assertThat(retrievedRegistration).usingRecursiveComparison().isEqualTo(updatedRegistration);
    }

    /**
     * Tests that optimistic locking works correctly when updating registrations.
     * Verifies that:
     * - Trying to update a registration with a stale version throws an OptimisticLockingFailureException
     */
    @Test
    void optimisticLockingOnUpdate() {
        Registration registration = Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build();

        Registration savedRegistration = repository.create(registration);
        Registration concurrentRegistration = savedRegistration.toBuilder().build();

        repository.update(savedRegistration.toBuilder()
                .status(RegistrationStatus.CONFIRMED)
                .build());

        assertThatThrownBy(() -> repository.update(concurrentRegistration.toBuilder()
                .status(RegistrationStatus.CANCELLED)
                .build()))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    /**
     * Tests that registrations can be found by event ID.
     * Verifies that:
     * - The correct number of registrations is returned for each event
     * - The returned registrations are the ones associated with the specified event
     */
    @Test
    void findByEventId() {
        Registration reg1 = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Note 1")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("account1")
                .build());

        Registration reg2 = repository.create(Registration.builder()
                .scout(savedEmma)
                .event(savedSummerCamp)
                .note("Note 2")
                .status(RegistrationStatus.CONFIRMED)
                .registrationDate(LocalDateTime.now())
                .accountId("account2")
                .build());

        Registration reg3 = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedHikingTrip)
                .note("Note 3")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("account1")
                .build());

        List<Registration> summerCampRegistrations = repository.findByEventId(savedSummerCamp.getId());
        assertThat(summerCampRegistrations).hasSize(2);
        assertThat(summerCampRegistrations).extracting("id")
                .containsExactlyInAnyOrder(reg1.getId(), reg2.getId());

        List<Registration> hikingTripRegistrations = repository.findByEventId(savedHikingTrip.getId());
        assertThat(hikingTripRegistrations).hasSize(1);
        assertThat(hikingTripRegistrations).extracting("id")
                .containsExactly(reg3.getId());
    }

    /**
     * Tests that registrations can be found by scout ID.
     * Verifies that:
     * - The correct number of registrations is returned for each scout
     * - The returned registrations are the ones associated with the specified scout
     */
    @Test
    void findByScoutId() {
        Registration reg1 = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Note 1")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("account1")
                .build());

        Registration reg2 = repository.create(Registration.builder()
                .scout(savedEmma)
                .event(savedSummerCamp)
                .note("Note 2")
                .status(RegistrationStatus.CONFIRMED)
                .registrationDate(LocalDateTime.now())
                .accountId("account2")
                .build());

        Registration reg3 = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedHikingTrip)
                .note("Note 3")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("account1")
                .build());

        List<Registration> johnRegistrations = repository.findByScoutId(savedJohn.getId());
        assertThat(johnRegistrations).hasSize(2);
        assertThat(johnRegistrations).extracting("id")
                .containsExactlyInAnyOrder(reg1.getId(), reg3.getId());

        List<Registration> emmaRegistrations = repository.findByScoutId(savedEmma.getId());
        assertThat(emmaRegistrations).hasSize(1);
        assertThat(emmaRegistrations).extracting("id")
                .containsExactly(reg2.getId());
    }

    /**
     * Tests that a registration can be deleted.
     * Verifies that:
     * - After deletion, the registration can no longer be found by its ID
     */
    @Test
    void deleteRegistration() {
        Registration registration = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build());

        repository.delete(registration.getId());
        assertThat(repository.findById(registration.getId())).isEmpty();
    }

    /**
     * Tests that deleting an event cascades to delete all associated registrations.
     * Verifies that:
     * - When an event is deleted, all registrations for that event are automatically deleted
     */
    @Test
    void deleteEventCascadesToRegistrations() {
        Registration registration = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build());

        // Verify registration exists
        assertThat(repository.findById(registration.getId())).isPresent();

        // Delete event
        eventRepository.delete(savedSummerCamp.getId());

        // Verify registration is gone
        assertThat(repository.findById(registration.getId())).isEmpty();
    }

    /**
     * Tests that deleting a scout cascades to delete all associated registrations.
     * Verifies that:
     * - When a scout is deleted, all registrations for that scout are automatically deleted
     */
    @Test
    void deleteScoutCascadesToRegistrations() {
        Registration registration = repository.create(Registration.builder()
                .scout(savedJohn)
                .event(savedSummerCamp)
                .note("Test note")
                .status(RegistrationStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .accountId("test-account")
                .build());

        // Verify registration exists
        assertThat(repository.findById(registration.getId())).isPresent();

        // Delete scout
        scoutRepository.delete(savedJohn.getId());

        // Verify registration is gone
        assertThat(repository.findById(registration.getId())).isEmpty();
    }
}